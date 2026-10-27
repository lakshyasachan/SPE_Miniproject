package com.spe.calculator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain JUnit 5 unit tests for the arithmetic logic (no Spring context needed).
 */
class CalculatorServiceTest {

    private final CalculatorService calculator = new CalculatorService();

    @Test
    void addReturnsSum() {
        assertEquals(5, calculator.add(2, 3));
        assertEquals(-1, calculator.add(2, -3));
        assertEquals(0.3, calculator.add(0.1, 0.2), 1e-9);
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(6, calculator.subtract(10, 4));
        assertEquals(-6, calculator.subtract(4, 10));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(42, calculator.multiply(6, 7));
        assertEquals(-42, calculator.multiply(-6, 7));
        assertEquals(0, calculator.multiply(5, 0));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.5, calculator.divide(10, 4));
        assertEquals(-3, calculator.divide(9, -3));
    }

    @Test
    void divideByZeroThrowsArithmeticException() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> calculator.divide(1, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }
}
