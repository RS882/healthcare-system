//package com.healthcare.user_service.audit;
//
//
//import org.hibernate.envers.RevisionListener;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.time.LocalDateTime;
//
//public class AppRevisionEntityListener implements RevisionListener {
//
//    @Override
//    public void newRevision(Object revisionEntity) {
//        AppRevisionEntity customRevisionEntity = (AppRevisionEntity) revisionEntity;
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        customRevisionEntity.setModifiedBy(getModifiedBy(auth));
//        customRevisionEntity.setModifiedDate(LocalDateTime.now());
//    }
//
//    private String getModifiedBy(Authentication authentication) {
//        if (authentication != null && authentication.getPrincipal() instanceof User) {
//            User authenticatedUser = (User) authentication.getPrincipal();
//            return authenticatedUser.getEmail();
//        }
//        return "anonymousUser";
//    }
//}
