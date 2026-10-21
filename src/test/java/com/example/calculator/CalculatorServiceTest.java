package com.example.calculator;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import com.example.calculator.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
        service.clearHistory();
    }

    @Test
    @DisplayName("Evaluate simple arithmetic: addition and subtraction")
    void testBasicAdditionSubtraction() {
        CalculationResponse resp1 = service.calculate(new CalculationRequest("15 + 27"));
        assertTrue(resp1.isSuccess());
        assertEquals(42.0, resp1.getResult());
        assertEquals("42", resp1.getFormattedResult());

        CalculationResponse resp2 = service.calculate(new CalculationRequest("100 - 37.5"));
        assertTrue(resp2.isSuccess());
        assertEquals(62.5, resp2.getResult());
    }

    @Test
    @DisplayName("Evaluate operator precedence and parentheses")
    void testPrecedenceAndParentheses() {
        CalculationResponse resp1 = service.calculate(new CalculationRequest("2 + 3 * 4"));
        assertTrue(resp1.isSuccess());
        assertEquals(14.0, resp1.getResult());

        CalculationResponse resp2 = service.calculate(new CalculationRequest("(2 + 3) * 4"));
        assertTrue(resp2.isSuccess());
        assertEquals(20.0, resp2.getResult());
    }

    @Test
    @DisplayName("Evaluate power and factorial")
    void testPowerAndFactorial() {
        CalculationResponse resp1 = service.calculate(new CalculationRequest("2 ^ 8"));
        assertTrue(resp1.isSuccess());
        assertEquals(256.0, resp1.getResult());

        CalculationResponse resp2 = service.calculate(new CalculationRequest("5!"));
        assertTrue(resp2.isSuccess());
        assertEquals(120.0, resp2.getResult());
    }

    @Test
    @DisplayName("Division by zero returns meaningful error")
    void testDivisionByZero() {
        CalculationResponse resp = service.calculate(new CalculationRequest("50 / 0"));
        assertFalse(resp.isSuccess());
        assertEquals("Cannot divide by zero", resp.getErrorMessage());
    }

    @Test
    @DisplayName("Square root operations")
    void testSquareRoot() {
        CalculationResponse resp1 = service.calculate(new CalculationRequest("sqrt(144)"));
        assertTrue(resp1.isSuccess());
        assertEquals(12.0, resp1.getResult());

        CalculationResponse resp2 = service.calculate(new CalculationRequest("sqrt(-9)"));
        assertFalse(resp2.isSuccess());
        assertTrue(resp2.getErrorMessage().contains("negative number"));
    }

    @Test
    @DisplayName("Structured operation request: multiply and divide")
    void testStructuredOperations() {
        CalculationResponse resp = service.calculate(new CalculationRequest("multiply", 7.0, 8.0));
        assertTrue(resp.isSuccess());
        assertEquals(56.0, resp.getResult());

        CalculationResponse respDivZero = service.calculate(new CalculationRequest("divide", 10.0, 0.0));
        assertFalse(respDivZero.isSuccess());
        assertEquals("Cannot divide by zero", respDivZero.getErrorMessage());
    }

    @Test
    @DisplayName("History management: records and clears")
    void testHistoryManagement() {
        service.calculate(new CalculationRequest("1 + 1"));
        service.calculate(new CalculationRequest("2 + 2"));
        assertEquals(2, service.getHistory().size());

        service.clearHistory();
        assertEquals(0, service.getHistory().size());
    }
}
