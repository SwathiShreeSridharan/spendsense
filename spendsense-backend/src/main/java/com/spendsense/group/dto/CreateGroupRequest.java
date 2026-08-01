package com.spendsense.group.dto;

import com.spendsense.group.entity.GroupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 50, message = "Group name cannot exceed 50 characters")
    String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description;

    @NotNull(message = "Group type is required")
    GroupType groupType;

    @Size(max = 7, message = "Color must be a valid hex code")
    String color;

    @Size(max = 30, message = "Icon name cannot exceed 30 characters")
    String icon;

    boolean budgetEnabled;

    boolean splitEnabled;

    boolean notificationEnabled;

    public CreateGroupRequest(String name, String description, GroupType groupType, String color, String icon, boolean budgetEnabled, boolean splitEnabled, boolean notificationEnabled) {
        this.name = name;
        this.description = description;
        this.groupType = groupType;
        this.color = color;
        this.icon = icon;
        this.budgetEnabled = budgetEnabled;
        this.splitEnabled = splitEnabled;
        this.notificationEnabled = notificationEnabled;
    }

    public boolean isBudgetEnabled() {
        return budgetEnabled;
    }

    public void setBudgetEnabled(boolean budgetEnabled) {
        this.budgetEnabled = budgetEnabled;
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

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
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

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(boolean splitEnabled) {
        this.splitEnabled = splitEnabled;
    }
}
