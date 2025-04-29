package com.healthcare.user_service.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@MappedSuperclass
@Getter
@Audited
@EntityListeners(AuditingEntityListener.class)
public class EntityAudit {

    @RevisionTimestamp
    @LastModifiedDate
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "last_modified")
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @CreatedBy
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
