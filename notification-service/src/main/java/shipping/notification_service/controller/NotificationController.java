package shipping.notification_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shipping.notification_service.dto.response.NotificationResponse;
import shipping.notification_service.dto.response.PagedResponse;
import shipping.notification_service.security.UserPrinciple;
import shipping.notification_service.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/my")
    public ResponseEntity<PagedResponse<NotificationResponse>> mine(
            @AuthenticationPrincipal UserPrinciple p,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listByUser(p.userId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@AuthenticationPrincipal UserPrinciple p,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id, p.userId()));
    }
}