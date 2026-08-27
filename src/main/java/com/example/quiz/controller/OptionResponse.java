package com.example.quiz.controller;

public class OptionResponse {

    private Long id;
    private String optionText;
    private Integer displayOrder;

    public OptionResponse(
            Long id,
            String optionText,
            Integer displayOrder) {

        this.id = id;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}