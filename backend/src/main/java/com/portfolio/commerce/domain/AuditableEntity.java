package com.portfolio.commerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

/**
 * Base class for entities that need createdAt/updatedAt auditing.
 *
 * Strategy: JPA lifecycle callbacks ({@code @PrePersist}/{@code @PreUpdate})
 * instead of Spring Data JPA auditing. Rationale: zero extra configuration,
 * works with plain JPA, and we do not need auditor metadata (createdBy/updatedBy)
 * yet. If that changes, migrating to Spring Data auditing is straightforward.
 */
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
