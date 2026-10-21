package com.example.calculator.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CalculationResponse {
    private String expression;
    private Double result;
    private String formattedResult;
    private boolean success;
    private String errorMessage;
    private String timestamp;

    public CalculationResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static CalculationResponse ok(String expression, double result) {
        CalculationResponse resp = new CalculationResponse();
        resp.setExpression(expression);
        resp.setResult(result);
        resp.setSuccess(true);
        // Format result: if whole number, show as integer, otherwise format nicely
        if (result == (long) result) {
            resp.setFormattedResult(String.format("%d", (long) result));
        } else {
            // Trim unnecessary trailing zeros
            String formatted = String.format("%.8f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
            resp.setFormattedResult(formatted);
        }
        return resp;
    }

    public static CalculationResponse error(String expression, String errorMessage) {
        CalculationResponse resp = new CalculationResponse();
        resp.setExpression(expression);
        resp.setSuccess(false);
        resp.setErrorMessage(errorMessage);
        resp.setFormattedResult("Error");
        return resp;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Double getResult() {
        return result;
    }

    public void setResult(Double result) {
        this.result = result;
    }

    public String getFormattedResult() {
        return formattedResult;
    }

    public void setFormattedResult(String formattedResult) {
        this.formattedResult = formattedResult;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
