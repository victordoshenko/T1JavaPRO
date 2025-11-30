package com.example.limit.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfirmOperationRequest {
    
    @NotBlank(message = "Operation ID is required")
    private String operationId;
    
    public ConfirmOperationRequest() {
    }
    
    public ConfirmOperationRequest(String operationId) {
        this.operationId = operationId;
    }
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}


