package com.spendsense.group.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_settings")
public class GroupSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID settingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private Group group;

    @Column(nullable = false)
    private boolean budgetEnabled;

    @Column(nullable = false)
    private boolean splitEnabled;

    @Column(nullable = false)
    private boolean notificationEnabled;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public GroupSettings() {
    }

    public GroupSettings(boolean budgetEnabled, LocalDateTime createdAt, Group group, boolean notificationEnabled, UUID settingsId, boolean splitEnabled, LocalDateTime updatedAt) {
        this.budgetEnabled = budgetEnabled;
        this.createdAt = createdAt;
        this.group = group;
        this.notificationEnabled = notificationEnabled;
        this.settingsId = settingsId;
        this.splitEnabled = splitEnabled;
        this.updatedAt = updatedAt;
    }

    public UUID getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(UUID settingsId) {
        this.settingsId = settingsId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isBudgetEnabled() {
        return budgetEnabled;
    }

    public void setBudgetEnabled(boolean budgetEnabled) {
        this.budgetEnabled = budgetEnabled;
    }

    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    public void setSplitEnabled(boolean splitEnabled) {
        this.splitEnabled = splitEnabled;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
