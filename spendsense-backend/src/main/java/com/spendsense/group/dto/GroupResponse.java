package com.spendsense.group.dto;

import com.spendsense.group.entity.GroupType;

import java.time.LocalDateTime;
import java.util.UUID;

public class GroupResponse {

    UUID groupId;

    String name;

    String description;

    GroupType groupType;

    String color;

    String icon;

    boolean archived;

    UUID createdById;

    String createdByName;

    boolean budgetEnabled;

    boolean splitEnabled;

    boolean notificationEnabled;

    LocalDateTime createdAt;

    public GroupResponse(UUID groupId, String name, String description, GroupType groupType, String color, String icon, boolean archived, UUID createdById, String createdByName, boolean budgetEnabled, boolean splitEnabled, boolean notificationEnabled, LocalDateTime createdAt) {
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.groupType = groupType;
        this.color = color;
        this.icon = icon;
        this.archived = archived;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.budgetEnabled = budgetEnabled;
        this.splitEnabled = splitEnabled;
        this.notificationEnabled = notificationEnabled;
        this.createdAt = createdAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
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

    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(boolean splitEnabled) {
        this.splitEnabled = splitEnabled;
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
}
