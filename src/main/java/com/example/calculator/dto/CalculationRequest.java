package com.example.calculator.dto;

public class CalculationRequest {
    private String expression;
    private String operation;
    private Double operand1;
    private Double operand2;

    public CalculationRequest() {
    }

    public CalculationRequest(String expression) {
        this.expression = expression;
    }

    public CalculationRequest(String operation, Double operand1, Double operand2) {
        this.operation = operation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Double getOperand1() {
        return operand1;
    }

    public void setOperand1(Double operand1) {
        this.operand1 = operand1;
    }

    public Double getOperand2() {
        return operand2;
    }

    public void setOperand2(Double operand2) {
        this.operand2 = operand2;
    }
}
