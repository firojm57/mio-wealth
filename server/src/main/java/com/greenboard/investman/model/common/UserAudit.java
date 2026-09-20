package com.greenboard.investman.model.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class UserAudit implements Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on", nullable = false, updatable = false)
    @CreatedDate
    protected Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_on", nullable = false)
    @LastModifiedDate
    protected Date updatedOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login", nullable = false)
    @LastModifiedDate
    protected Date lastLogin;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        if (this.createdOn == null) {
            this.createdOn = now;
        }
        if (this.updatedOn == null) {
            this.updatedOn = now;
        }
        if (this.lastLogin == null) {
            this.lastLogin = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedOn = new Date();
    }
}
