
package org.example;

import java.util.concurrent.*;

public class ExecutorServiceAndThreads {

    public static void main(String[] args)
            throws ExecutionException, InterruptedException, TimeoutException {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch resourceAcquired = new CountDownLatch(1);
        CountDownLatch releaseResource = new CountDownLatch(1);

        try {
            Future<?> futureA = executorService.submit(() -> {
                try {
                    System.out.println("A: Resource acquired");

                    resourceAcquired.countDown();

                    System.out.println("A: Waiting for permission");
                    releaseResource.await();

                    System.out.println("A: Resource released");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Task A interrupted", e);
                }
            });

            Future<?> futureB = executorService.submit(() -> {
                try {
                    boolean acquired =
                            resourceAcquired.await(5, TimeUnit.SECONDS);

                    if (!acquired) {
                        throw new IllegalStateException(
                                "Task A did not acquire resource"
                        );
                    }

                    if (futureA.isDone()) {
                        throw new IllegalStateException(
                                "Task A finished too early"
                        );
                    }

                    System.out.println("B: Task A is still waiting");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Task B interrupted", e);
                }
            });

            // Main ожидает завершения проверки в B
            futureB.get(5, TimeUnit.SECONDS);

            System.out.println("MAIN: Verification completed");

            // Пункт 5: только Main разрешает A завершиться
            releaseResource.countDown();

            // Main ожидает завершения A
            futureA.get(5, TimeUnit.SECONDS);

            System.out.println("MAIN: All tasks completed");

        } finally {
            // Освобождаем A даже при ошибке в B
            releaseResource.countDown();

            executorService.shutdown();

            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }
}
