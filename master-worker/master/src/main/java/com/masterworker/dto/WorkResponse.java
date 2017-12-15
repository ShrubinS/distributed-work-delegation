package com.masterworker.dto;

import java.io.Serializable;

public class WorkResponse implements Serializable {

    private Double complexity;

    public Double getComplexity() {
        return complexity;
    }

    public void setComplexity(Double complexity) {
        this.complexity = complexity;
    }
}
