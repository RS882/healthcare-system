package com.healthcare.user_service.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



    @RestController
    public class TestController {

        @PostConstruct
        public void init() {
            System.out.println("✅ TestController loaded");
        }

        @GetMapping("/test")
        public ResponseEntity<String> test() {
            return ResponseEntity.ok("OK");
        }
    }


