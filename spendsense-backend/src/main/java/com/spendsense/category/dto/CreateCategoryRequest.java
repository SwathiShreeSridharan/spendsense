package com.spendsense.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must not exceed 50 characters")
    private String name;

    private String icon;

    private String color;


    public CreateCategoryRequest() {
    }


    public CreateCategoryRequest(
            String name,
            String icon,
            String color
    ) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getIcon() {
        return icon;
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getColor() {
        return color;
    }


    public void setColor(String color) {
        this.color = color;
    }
}
