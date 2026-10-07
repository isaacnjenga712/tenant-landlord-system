package com.apex.auth.service;

import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.exception.ResourceNotFoundException;
import com.apex.auth.mapper.UserAdminMapper;
import com.apex.auth.repository.UserRepository;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.AdminUserDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminService {

    private final UserRepository userRepository;
    private final UserAdminMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserDto> list(int page, int size,
                                           String role, String status, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<User> spec = buildSpec(role, status, search);
        Page<User> result = userRepository.findAll(spec, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(mapper::toDto).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserDto get(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + publicId));
        return mapper.toDto(user);
    }

    @Transactional
    public void updateRole(UUID publicId, String newRole) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + publicId));

        Role role;
        try {
            role = Role.valueOf(newRole);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        if (user.getRole() == role) {
            log.debug("Role already {} for user {}", role, publicId);
            return;
        }

        user.setRole(role);
        userRepository.save(user);
        log.info("Role changed: user={} {} → {}", publicId, user.getRole(), role);
    }

    @Transactional
    public void updateStatus(UUID publicId, boolean active) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + publicId));

        if (user.isEnabled() == active) {
            log.debug("Enabled already {} for user {}", active, publicId);
            return;
        }

        user.setEnabled(active);
        userRepository.save(user);
        log.info("Status changed: user={} enabled={}", publicId, active);
    }

    @Transactional(readOnly = true)
    public long count(String role) {
        if (role == null || role.isBlank()) {
            return userRepository.count();
        }
        try {
            return userRepository.countByRole(Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role in count query: {}", role);
            return 0L;
        }
    }

    @Transactional
    public void triggerPasswordReset(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + publicId));

        // TODO: publish to Kafka topic `auth.user.events` with eventType=password.reset.requested
        // notification-engine will pick it up and send the email.
        log.info("Password reset requested for user {}", publicId);
    }

    private Specification<User> buildSpec(String role, String status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null && !role.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("role"), Role.valueOf(role)));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role filter: {}", role);
                }
            }

            if (status != null && !status.isBlank()) {
                boolean enabled = !"inactive".equalsIgnoreCase(status);
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
