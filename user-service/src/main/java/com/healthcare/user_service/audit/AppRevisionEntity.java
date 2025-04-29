//package com.healthcare.user_service.audit;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.envers.DefaultRevisionEntity;
//import org.hibernate.envers.RevisionEntity;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "audit_user")
//@RevisionEntity(AppRevisionEntityListener.class)
//@Setter
//@Getter
//public class AppRevisionEntity extends DefaultRevisionEntity {
//
//    private String modifiedBy;
//    private LocalDateTime modifiedDate;
//}
