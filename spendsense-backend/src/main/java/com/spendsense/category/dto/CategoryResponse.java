package com.spendsense.category.dto;

import java.util.UUID;

public class CategoryResponse {

    private UUID categoryId;

    private String name;

    private String icon;

    private String color;

    private boolean isDefault;


    public CategoryResponse() {
    }


    public CategoryResponse(
            UUID categoryId,
            String name,
            String icon,
            String color,
            boolean isDefault
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }


    public UUID getCategoryId() {
        return categoryId;
    }


    public String getName() {
        return name;
    }


    public String getIcon() {
        return icon;
    }


    public String getColor() {
        return color;
    }


    public boolean isDefault() {
        return isDefault;
    }

}
