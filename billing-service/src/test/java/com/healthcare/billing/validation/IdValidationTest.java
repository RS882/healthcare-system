package com.healthcare.billing.validation;

import com.healthcare.billing.exception.RestException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class IdValidationTest {

    private static final String ID_NAME = "test ID";

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            1L,
            271L,
            Long.MAX_VALUE
    })
    void isValid_should_accept_positive_id(Long id) {

        assertTrue(IdValidation.isValid(id));
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -52L,
            Long.MIN_VALUE
    })
    void isValid_should_reject_null_or_non_positive_id(Long id) {

        assertFalse(IdValidation.isValid(id));
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            1L,
            271L,
            Long.MAX_VALUE
    })
    void validateId_should_accept_positive_id(Long id) {

        assertDoesNotThrow(() -> IdValidation.validateId(
                id,
                ID_NAME,
                TestException::new
        ));
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @ValueSource(longs = {
            1L,
            271L,
            Long.MAX_VALUE
    })
    void validateId_should_accept_valid_id_with_null_factory(Long id) {

        assertDoesNotThrow(() -> IdValidation.validateId(
                id,
                ID_NAME,
                null
        ));
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -52L,
            Long.MIN_VALUE
    })
    void validateId_should_reject_invalid_id(Long id) {

        assertThrows(TestException.class,
                () -> IdValidation.validateId(id,
                        ID_NAME,
                        TestException::new));
    }

    @ParameterizedTest(name = "Test {index}: id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -52L,
            Long.MIN_VALUE
    })
    void validateId_should_reject_invalid_id_with_null_factory(Long id) {

        assertThrows(NullPointerException.class,
                () -> IdValidation.validateId(id,
                        ID_NAME,
                        null));
    }

    @Test
    void validateId_should_not_invoke_factory_for_valid_id() {

        AtomicBoolean factoryCalled = new AtomicBoolean(false);

        assertDoesNotThrow(() -> IdValidation.validateId(
                97L,
                ID_NAME,
                message -> {
                    factoryCalled.set(true);
                    return new TestException(message);
                }
        ));

        assertFalse(factoryCalled.get());
    }

    private static class TestException extends RestException {

        private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

        public TestException(String message) {
            super(STATUS, message);
        }
    }
}