package com.example.calculator.service;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.dto.CalculationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CalculatorService {

    private final List<CalculationResponse> history = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_HISTORY = 50;

    public CalculationResponse calculate(CalculationRequest request) {
        if (request == null) {
            return CalculationResponse.error("", "Empty request");
        }

        // 1. Structured operation
        if (request.getOperation() != null && !request.getOperation().isBlank()) {
            return calculateStructured(request);
        }

        // 2. Free-form expression
        String expr = request.getExpression();
        if (expr == null || expr.trim().isEmpty()) {
            return CalculationResponse.error("", "Expression cannot be empty");
        }

        try {
            double result = evaluateExpression(expr);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return CalculationResponse.error(expr, "Math error (undefined or overflow)");
            }
            CalculationResponse response = CalculationResponse.ok(expr, result);
            recordHistory(response);
            return response;
        } catch (ArithmeticException | IllegalArgumentException e) {
            return CalculationResponse.error(expr, e.getMessage());
        } catch (Exception e) {
            return CalculationResponse.error(expr, "Invalid mathematical expression");
        }
    }

    private CalculationResponse calculateStructured(CalculationRequest req) {
        String op = req.getOperation().trim().toLowerCase();
        Double a = req.getOperand1();
        Double b = req.getOperand2();

        if (a == null) a = 0.0;
        if (b == null) b = 0.0;

        String expr;
        double result;

        try {
            switch (op) {
                case "add", "+" -> {
                    result = a + b;
                    expr = a + " + " + b;
                }
                case "subtract", "-" -> {
                    result = a - b;
                    expr = a + " - " + b;
                }
                case "multiply", "*", "x" -> {
                    result = a * b;
                    expr = a + " × " + b;
                }
                case "divide", "/" -> {
                    if (b == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    result = a / b;
                    expr = a + " ÷ " + b;
                }
                case "power", "^", "pow" -> {
                    result = Math.pow(a, b);
                    expr = a + " ^ " + b;
                }
                case "modulo", "%", "mod" -> {
                    if (b == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    result = a % b;
                    expr = a + " % " + b;
                }
                case "sqrt", "√" -> {
                    if (a < 0) {
                        throw new IllegalArgumentException("Cannot calculate square root of a negative number");
                    }
                    result = Math.sqrt(a);
                    expr = "√(" + a + ")";
                }
                case "reciprocal", "1/x" -> {
                    if (a == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    result = 1.0 / a;
                    expr = "1 / (" + a + ")";
                }
                case "square", "sqr" -> {
                    result = a * a;
                    expr = "(" + a + ")²";
                }
                default -> throw new IllegalArgumentException("Unsupported operation: " + req.getOperation());
            }

            CalculationResponse response = CalculationResponse.ok(expr, result);
            recordHistory(response);
            return response;
        } catch (Exception e) {
            return CalculationResponse.error(req.getOperation(), e.getMessage());
        }
    }

    public List<CalculationResponse> getHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public void clearHistory() {
        history.clear();
    }

    private void recordHistory(CalculationResponse item) {
        synchronized (history) {
            history.add(0, item);
            if (history.size() > MAX_HISTORY) {
                history.remove(history.size() - 1);
            }
        }
    }

    /**
     * Evaluates a mathematical expression string using a recursive descent parser.
     * Supports: +, -, *, /, %, ^, parentheses, functions (sqrt, abs, sin, cos, tan, log, ln, fact).
     */
    public double evaluateExpression(String expression) {
        String sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt ")
                .trim();

        return new ExpressionParser(sanitized).parse();
    }

    private static class ExpressionParser {
        private final String str;
        private int pos = -1;
        private int ch;

        ExpressionParser(String str) {
            this.str = str;
        }

        private void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ' || ch == '\t') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            while (ch == ' ' || ch == '\t') nextChar();
            if (pos < str.length()) {
                throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            }
            return x;
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor | term `%` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor | factor `!`

        private double parseExpression() {
            double x = parseTerm();
            while (true) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            while (true) {
                if (eat('*')) {
                    x *= parseFactor();
                } else if (eat('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    x /= divisor;
                } else if (eat('%')) {
                    double divisor = parseFactor();
                    if (divisor == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    x %= divisor;
                } else {
                    return x;
                }
            }
        }

        private double parseFactor() {
            if (eat('+')) return +parseFactor(); // unary plus
            if (eat('-')) return -parseFactor(); // unary minus

            double x;
            int startPos = this.pos;

            if (eat('(')) { // parentheses
                x = parseExpression();
                if (!eat(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis ')'");
                }
            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                try {
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format: " + str.substring(startPos, this.pos));
                }
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') { // functions
                while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') nextChar();
                String func = str.substring(startPos, this.pos).toLowerCase();
                double arg = parseFactor();
                x = switch (func) {
                    case "sqrt" -> {
                        if (arg < 0) throw new IllegalArgumentException("Cannot calculate square root of a negative number");
                        yield Math.sqrt(arg);
                    }
                    case "sin" -> Math.sin(Math.toRadians(arg));
                    case "cos" -> Math.cos(Math.toRadians(arg));
                    case "tan" -> {
                        if (Math.abs(arg % 180) == 90) throw new ArithmeticException("Tangent is undefined at 90°");
                        yield Math.tan(Math.toRadians(arg));
                    }
                    case "log" -> {
                        if (arg <= 0) throw new IllegalArgumentException("Logarithm argument must be > 0");
                        yield Math.log10(arg);
                    }
                    case "ln" -> {
                        if (arg <= 0) throw new IllegalArgumentException("Natural logarithm argument must be > 0");
                        yield Math.log(arg);
                    }
                    case "abs" -> Math.abs(arg);
                    case "fact" -> factorial(arg);
                    default -> throw new IllegalArgumentException("Unknown function: " + func);
                };
            } else {
                throw new IllegalArgumentException("Unexpected token at position " + pos + (ch != -1 ? ": '" + (char) ch + "'" : " (end of input)"));
            }

            // Right-associative power operator: ^
            if (eat('^')) {
                x = Math.pow(x, parseFactor());
            }

            // Postfix factorial: !
            if (eat('!')) {
                x = factorial(x);
            }

            return x;
        }

        private double factorial(double n) {
            if (n < 0 || n != Math.floor(n)) {
                throw new IllegalArgumentException("Factorial is only defined for non-negative integers");
            }
            if (n > 20) {
                throw new ArithmeticException("Factorial overflow (max supported is 20!)");
            }
            long result = 1;
            for (int i = 2; i <= (int) n; i++) {
                result *= i;
            }
            return (double) result;
        }
    }
}
