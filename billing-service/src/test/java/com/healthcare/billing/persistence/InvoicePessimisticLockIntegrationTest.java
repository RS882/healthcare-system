package com.healthcare.billing.persistence;

import com.healthcare.billing.dto.invoice.CreateInvoiceItemRequest;
import com.healthcare.billing.dto.invoice.CreateInvoiceRequest;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.persistence.repository.InvoiceJpaRepository;
import com.healthcare.billing.service.InvoiceCreator;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "stub"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class InvoicePessimisticLockIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private InvoiceStore invoiceStore;

    @Autowired
    private InvoiceCreator invoiceCreator;

    @Autowired
    private InvoiceJpaRepository invoiceJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);
        invoiceJpaRepository.deleteAll();
    }

    @Test
    void should_throw_when_find_by_id_with_lock_is_called_without_transaction() {

        Long invoiceId = 1L;

        assertThrows(IllegalTransactionStateException.class,
                () -> invoiceStore.findByIdWithBlock(invoiceId));

    }

    @Test
    void should_invoice_when_find_by_id_with_lock_is_called_with_transaction() {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Invoice invoice = transactionTemplate.execute(
                status -> invoiceStore.findByIdWithBlock(saved.getId())
        );

        assertNotNull(invoice);
        assertEquals(saved.getId(), invoice.getId());
        assertEquals(saved.getStatus(), invoice.getStatus());

    }

    @Test
    void two_transactions_will_lock()
            throws InterruptedException, ExecutionException, TimeoutException {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Long invoiceId = saved.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CountDownLatch attemptStarted = new CountDownLatch(1);

        AtomicReference<Integer> transactionBPid = new AtomicReference<>();
        AtomicReference<Integer> transactionAPid = new AtomicReference<>();

        try {
            Future<Invoice> futureA = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                Invoice invoice = invoiceStore.findByIdWithBlock(invoiceId);

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionAPid.set(backendPid);

                                lockAcquired.countDown();

                                try {

                                    releaseLock.await();

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task A interrupted", e);
                                }

                                return invoice;
                            }
                    )
            );

            Future<Invoice> futureB = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                try {
                                    boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);

                                    if (!acquired) {
                                        throw new IllegalStateException(
                                                "Task A did not acquire resource"
                                        );
                                    }
                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task B interrupted", e);
                                }

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionBPid.set(backendPid);

                                attemptStarted.countDown();


                                return invoiceStore.findByIdWithBlock(invoiceId);
                            }
                    )
            );

            boolean started = attemptStarted.await(5, TimeUnit.SECONDS);

            assertTrue(started, "Transaction B did not start lock attempt");

            if (futureA.isDone()) {
                throw new IllegalStateException(
                        "Task A finished too early"
                );
            }

            assertFalse(futureB.isDone(), "Future B did not finish lock attempt");

            Integer pidA = transactionAPid.get();

            assertNotNull(pidA, "Transaction A PostgreSQL PID is null");
            assertTrue(pidA > 0, "Invalid PostgreSQL backend PID");

            Integer pidB = transactionBPid.get();

            assertNotNull(pidB, "Transaction B PostgreSQL PID is null");
            assertTrue(pidB > 0, "Invalid PostgreSQL backend PID");

            assertNotEquals(pidA, pidB);

            List<Integer> pidList = new ArrayList<>();

            long deadline = System.nanoTime()
                    + TimeUnit.SECONDS.toNanos(1);

            while (!pidList.contains(pidA)
                    && System.nanoTime() < deadline) {

                Integer[] pids = jdbcTemplate.queryForObject(
                        "SELECT pg_blocking_pids(?)",
                        (rs, rowNum) -> {
                            Array sqlArray = rs.getArray(1);

                            return (Integer[]) sqlArray.getArray();
                        },
                        pidB
                );

                assertNotNull(pids);
                pidList = Arrays.asList(pids);

                if (!pidList.contains(pidA)) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            }

            assertFalse(pidList.isEmpty(), "Transaction B is not blocked");

            assertTrue(
                    pidList.contains(pidA),
                    "Transaction B is not blocked by Transaction A"
            );

            releaseLock.countDown();

            Invoice invoiceA = futureA.get(5, TimeUnit.SECONDS);

            Invoice invoiceB = futureB.get(5, TimeUnit.SECONDS);


            assertNotNull(invoiceA);
            assertEquals(invoiceId, invoiceA.getId());
            assertNotNull(invoiceB);
            assertEquals(invoiceId, invoiceB.getId());

        } finally {
            releaseLock.countDown();
            executor.shutdown();

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    void different_invoices_do_not_block_each_other()
            throws InterruptedException, ExecutionException, TimeoutException {

        Invoice draftA = invoiceCreator.createDraft(create_request());
        Invoice draftB = invoiceCreator.createDraft(create_request());

        Invoice savedA = invoiceStore.save(draftA);
        Invoice savedB = invoiceStore.save(draftB);

        Long invoiceAId = savedA.getId();
        Long invoiceBId = savedB.getId();

        assertNotEquals(invoiceAId, invoiceBId);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        try {
            Future<Invoice> futureA = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                Invoice invoice = invoiceStore.findByIdWithBlock(invoiceAId);

                                lockAcquired.countDown();

                                try {

                                    releaseLock.await();

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task A interrupted", e);
                                }

                                return invoice;
                            }
                    )
            );

            boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);
            assertTrue(acquired, "Transaction A did not acquire the lock");

            Future<Invoice> futureB = executor.submit(() ->
                    transactionTemplate.execute(status ->
                            invoiceStore.findByIdWithBlock(invoiceBId)
                    )
            );

            Invoice invoiceB = futureB.get(5, TimeUnit.SECONDS);

            assertFalse(
                    futureA.isDone(),
                    "Transaction A finished before releasing its lock"
            );

            releaseLock.countDown();

            Invoice invoiceA = futureA.get(5, TimeUnit.SECONDS);

            assertNotNull(invoiceA);
            assertEquals(invoiceAId, invoiceA.getId());
            assertNotNull(invoiceB);
            assertEquals(invoiceBId, invoiceB.getId());

        } finally {
            releaseLock.countDown();
            executor.shutdown();

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    void rollback_releases_lock()
            throws InterruptedException, ExecutionException, TimeoutException {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Long invoiceId = saved.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CountDownLatch attemptStarted = new CountDownLatch(1);

        AtomicReference<Integer> transactionBPid = new AtomicReference<>();
        AtomicReference<Integer> transactionAPid = new AtomicReference<>();

        try {
            Future<Invoice> futureA = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                Invoice invoice = invoiceStore.findByIdWithBlock(invoiceId);

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionAPid.set(backendPid);

                                lockAcquired.countDown();

                                try {

                                    releaseLock.await();

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task A interrupted", e);
                                }

                                status.setRollbackOnly();

                                return invoice;
                            }
                    )
            );

            Future<Invoice> futureB = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                try {
                                    boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);

                                    if (!acquired) {
                                        throw new IllegalStateException(
                                                "Task A did not acquire resource"
                                        );
                                    }
                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task B interrupted", e);
                                }

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionBPid.set(backendPid);

                                attemptStarted.countDown();


                                return invoiceStore.findByIdWithBlock(invoiceId);
                            }
                    )
            );

            boolean started = attemptStarted.await(5, TimeUnit.SECONDS);

            assertTrue(started, "Transaction B did not start lock attempt");

            if (futureA.isDone()) {
                throw new IllegalStateException(
                        "Task A finished too early"
                );
            }

            assertFalse(futureB.isDone(), "Future B did not finish lock attempt");

            Integer pidA = transactionAPid.get();

            assertNotNull(pidA, "Transaction A PostgreSQL PID is null");
            assertTrue(pidA > 0, "Invalid PostgreSQL backend PID");

            Integer pidB = transactionBPid.get();

            assertNotNull(pidB, "Transaction B PostgreSQL PID is null");
            assertTrue(pidB > 0, "Invalid PostgreSQL backend PID");

            assertNotEquals(pidA, pidB);

            List<Integer> pidList = new ArrayList<>();

            long deadline = System.nanoTime()
                    + TimeUnit.SECONDS.toNanos(1);

            while (!pidList.contains(pidA)
                    && System.nanoTime() < deadline) {

                Integer[] pids = jdbcTemplate.queryForObject(
                        "SELECT pg_blocking_pids(?)",
                        (rs, rowNum) -> {
                            Array sqlArray = rs.getArray(1);

                            return (Integer[]) sqlArray.getArray();
                        },
                        pidB
                );

                assertNotNull(pids);
                pidList = Arrays.asList(pids);

                if (!pidList.contains(pidA)) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            }

            assertFalse(pidList.isEmpty(), "Transaction B is not blocked");

            assertTrue(
                    pidList.contains(pidA),
                    "Transaction B is not blocked by Transaction A"
            );

            releaseLock.countDown();

            Invoice invoiceA = futureA.get(5, TimeUnit.SECONDS);

            Invoice invoiceB = futureB.get(5, TimeUnit.SECONDS);

            Invoice invoiceADb = invoiceStore.findById(invoiceId);

            assertNotNull(invoiceA);
            assertEquals(invoiceId, invoiceA.getId());

            assertNotNull(invoiceADb);
            assertEquals(invoiceId, invoiceADb.getId());

            assertNotNull(invoiceB);
            assertEquals(invoiceId, invoiceB.getId());

        } finally {
            releaseLock.countDown();
            executor.shutdown();

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    void pessimistic_lock_timeout()
            throws InterruptedException, ExecutionException, TimeoutException {

        Invoice draft = invoiceCreator.createDraft(create_request());

        Invoice saved = invoiceStore.save(draft);

        Long invoiceId = saved.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CountDownLatch attemptStarted = new CountDownLatch(1);

        AtomicReference<Integer> transactionBPid = new AtomicReference<>();
        AtomicReference<Integer> transactionAPid = new AtomicReference<>();

        try {
            Future<Invoice> futureA = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                Invoice invoice = invoiceStore.findByIdWithBlock(invoiceId);

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionAPid.set(backendPid);

                                lockAcquired.countDown();

                                try {

                                    releaseLock.await();

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task A interrupted", e);
                                }

                                return invoice;
                            }
                    )
            );

            Future<Invoice> futureB = executor.submit(() -> transactionTemplate.execute(
                            status -> {

                                try {
                                    boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);

                                    if (!acquired) {
                                        throw new IllegalStateException(
                                                "Task A did not acquire resource"
                                        );
                                    }
                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("Task B interrupted", e);
                                }

                                Integer backendPid = jdbcTemplate.queryForObject(
                                        "SELECT pg_backend_pid()",
                                        Integer.class
                                );

                                transactionBPid.set(backendPid);

                                attemptStarted.countDown();

                                return invoiceStore.findByIdWithBlock(invoiceId);
                            }
                    )
            );

            boolean started = attemptStarted.await(5, TimeUnit.SECONDS);

            assertTrue(started, "Transaction B did not start lock attempt");

            if (futureA.isDone()) {
                throw new IllegalStateException(
                        "Task A finished too early"
                );
            }

            assertFalse(futureB.isDone(), "Future B did not finish lock attempt");

            Integer pidA = transactionAPid.get();

            assertNotNull(pidA, "Transaction A PostgreSQL PID is null");
            assertTrue(pidA > 0, "Invalid PostgreSQL backend PID");

            Integer pidB = transactionBPid.get();

            assertNotNull(pidB, "Transaction B PostgreSQL PID is null");
            assertTrue(pidB > 0, "Invalid PostgreSQL backend PID");

            assertNotEquals(pidA, pidB);

            List<Integer> pidList = new ArrayList<>();

            long deadline = System.nanoTime()
                    + TimeUnit.SECONDS.toNanos(1);

            while (!pidList.contains(pidA)
                    && System.nanoTime() < deadline) {

                Integer[] pids = jdbcTemplate.queryForObject(
                        "SELECT pg_blocking_pids(?)",
                        (rs, rowNum) -> {
                            Array sqlArray = rs.getArray(1);

                            return (Integer[]) sqlArray.getArray();
                        },
                        pidB
                );

                assertNotNull(pids);
                pidList = Arrays.asList(pids);

                if (!pidList.contains(pidA)) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            }

            assertFalse(pidList.isEmpty(), "Transaction B is not blocked");

            assertTrue(
                    pidList.contains(pidA),
                    "Transaction B is not blocked by Transaction A"
            );

            ExecutionException exception = assertThrows(
                    ExecutionException.class,
                    () -> futureB.get(4, TimeUnit.SECONDS)
            );

            assertInstanceOf(
                    PessimisticLockingFailureException.class,
                    exception.getCause()
            );

            assertTrue(hasSqlStateUsingCommons(exception, "55P03"));

        } finally {
            releaseLock.countDown();
            executor.shutdown();

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

    }

    private static boolean hasSqlStateUsingCommons(Throwable throwable, String expectedSqlState) {
        return ExceptionUtils.getThrowableList(throwable).stream()
                .filter(SQLException.class::isInstance)
                .map(SQLException.class::cast)
                .map(SQLException::getSQLState)
                .anyMatch(expectedSqlState::equals);
    }

    private CreateInvoiceRequest create_request() {
        return CreateInvoiceRequest.builder()
                .patientId(100L)
                .medicalFacilityId(200L)
                .items(
                        List.of(
                                CreateInvoiceItemRequest.builder()
                                        .serviceId(1L)
                                        .quantity(
                                                new BigDecimal("1.0000")
                                        )
                                        .discountRate(
                                                new BigDecimal("0.000000")
                                        )
                                        .build(),

                                CreateInvoiceItemRequest.builder()
                                        .serviceId(3L)
                                        .quantity(
                                                new BigDecimal("2.0000")
                                        )
                                        .discountRate(
                                                new BigDecimal("0.100000")
                                        )
                                        .build()
                        )
                )
                .build();
    }
}
