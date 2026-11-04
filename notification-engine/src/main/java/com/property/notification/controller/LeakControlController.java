package com.property.notification.controller;

import com.property.notification.domain.Notification;
import com.property.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * INTERNAL ONLY — negative-control endpoint for the load-test leak detector.
 *
 * <p>Deliberately violates tenant isolation so the k6 cross-tenant test can
 * be proven to detect a real leak. Loaded ONLY under the "leaktest" Spring
 * profile. Never packaged in production jars (profile-gated), and the
 * endpoint is disabled unless the profile is active.
 *
 * <p>See LOAD_TEST.md § "Proof the leak detector can fail".
 */
@Profile("leaktest")
@RestController
@RequestMapping("/api/v1/leaktest")
public class LeakControlController {

    private static final Logger log = LoggerFactory.getLogger(LeakControlController.class);

    private final NotificationRepository repo;

    public LeakControlController(NotificationRepository repo) {
        this.repo = repo;
    }

    /**
     * Ignores X-Org-Id entirely and returns notifications for ANY recipient.
     * The k6 test sends org-B's X-Org-Id header with org-A's recipient, and
     * this endpoint returns org-A's data — which the classifier treats as
     * a LEAK. The point is to validate the detector.
     */
    @GetMapping("/inbox")
    public Map<String, Object> leakyInbox(
            @RequestHeader(value = "X-User-PublicId", required = false) String recipientPublicId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        log.warn("LEAKTEST ENDPOINT CALLED — this bypasses tenant isolation by design");

        if (recipientPublicId == null || recipientPublicId.isBlank()) {
            return Map.of("items", List.of(), "total", 0);
        }

        UUID pid;
        try {
            pid = UUID.fromString(recipientPublicId);
        } catch (IllegalArgumentException e) {
            return Map.of("items", List.of(), "total", 0);
        }

        // DELIBERATELY unscoped — no organizationId filter.
        List<Notification> all = repo.findAllByRecipientPublicId(pid);
        long total = repo.countByRecipientPublicId(pid);

        List<Map<String, Object>> items = all.stream()
                .skip((long) page * size)
                .limit(size)
                .map(n -> Map.<String, Object>of(
                        "id",              n.getId(),
                        "recipientPublicId", n.getRecipientPublicId() != null
                                ? n.getRecipientPublicId().toString() : "",
                        "eventType",       n.getEventType() != null ? n.getEventType() : "",
                        "subject",         n.getSubject() != null ? n.getSubject() : "",
                        "body",            n.getBody() != null ? n.getBody() : "",
                        "read",            n.isRead(),
                        "createdAt",       n.getCreatedAt() != null
                                ? n.getCreatedAt().toString() : ""
                ))
                .toList();

        return Map.of(
                "items", items,
                "total", total,
                "page",  page,
                "size",  size,
                "hasMore", (long) (page + 1) * size < total,
                "_leaktest", true
        );
    }

    /**
     * Mirrors the PATCH endpoint without org scoping — for the write-path
     * negative control. Returns 204 on success.
     */
    @PatchMapping("/inbox/{id}/read")
    public void leakyMarkRead(
            @RequestHeader(value = "X-User-PublicId", required = false) String recipientPublicId,
            @PathVariable String id) {

        log.warn("LEAKTEST PATCH CALLED — this bypasses tenant isolation by design");

        if (recipientPublicId == null || recipientPublicId.isBlank()) {
            return;
        }

        UUID pid;
        try {
            pid = UUID.fromString(recipientPublicId);
        } catch (IllegalArgumentException e) {
            return;
        }

        // DELIBERATELY unscoped — marks ANY notification with this ID as read.
        repo.findAllByRecipientPublicId(pid).stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .ifPresent(n -> {
                    n.setRead(true);
                    n.setReadAt(java.time.LocalDateTime.now());
                    repo.save(n);
                });
    }
}