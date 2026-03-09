package com.ecommerce.commerce.notification.controller;

import com.ecommerce.commerce.notification.entity.Notification;
import com.ecommerce.commerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("Commerce Service is running!"); }

    @GetMapping("/user/{userId:.+}")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}
