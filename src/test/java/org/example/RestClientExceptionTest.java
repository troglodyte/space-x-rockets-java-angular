package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class RestClientExceptionTest {

    @Test
    void isThrowable() {
        Throwable t = new RestClientException();
        assertInstanceOf(Throwable.class, t);
    }
}
