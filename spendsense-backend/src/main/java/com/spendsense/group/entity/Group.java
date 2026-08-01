package com.spendsense.group.entity;

import com.spendsense.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID groupId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType groupType;

    @Column(length = 7)
    private String color;

    @Column(length = 30)
    private String icon;

    @Column(nullable = false)
    private boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToOne(
            mappedBy = "group",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private GroupSettings settings;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Group() {
    }

    public Group(boolean archived, String color, LocalDateTime createdAt, User createdBy, String description, UUID groupId, GroupType groupType, String icon, String name, GroupSettings settings, LocalDateTime updatedAt) {
        this.archived = archived;
        this.color = color;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.description = description;
        this.groupId = groupId;
        this.groupType = groupType;
        this.icon = icon;
        this.name = name;
        this.settings = settings;
        this.updatedAt = updatedAt;
    }

    public void setSettings(GroupSettings settings) {
        this.settings = settings;

        if (settings != null) {
            settings.setGroup(this);
        }
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public User getOwner() {
        return createdBy;
    }

    public void setOwner(User createdBy) {
        this.createdBy = createdBy;
    }

    public GroupSettings getSettings() {
        return settings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
