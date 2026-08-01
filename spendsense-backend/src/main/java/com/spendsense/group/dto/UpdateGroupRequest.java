package com.spendsense.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 50, message = "Group name cannot exceed 50 characters")
    String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description;

    @Size(max = 7, message = "Color must be a valid hex code")
    String color;

    @Size(max = 30, message = "Icon name cannot exceed 30 characters")
    String icon;

    public UpdateGroupRequest(String name, String description, String color, String icon) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
