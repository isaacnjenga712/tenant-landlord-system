package com.property.notification.config;

import com.property.notification.domain.UserPreference;
import com.property.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class PreferenceBootstrap implements CommandLineRunner {

    private final UserPreferenceRepository repo;

    @Override
    public void run(String... args) {
        seed(UUID.fromString("24e46781-9e51-464c-b3cc-67637c803b74"),
                "Landlord (old)", "landlord@test.com", "+254757380426");
        seed(UUID.fromString("dc1f52cb-9694-4492-b798-2799dbab8a00"),
                "Tenant (old)", "tenant@test.com", "+254757380426");
        seed(UUID.fromString("07767213-612b-4f0b-987c-75e43e5ca152"),
                "Alice Landlord", "alice@test.com", "+254757380426");
        seed(UUID.fromString("c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a"),
                "Bob Tenant", "bob@test.com", "+254757380426");
    }

    private void seed(UUID publicId, String name, String email, String phone) {
        if (repo.findByPublicId(publicId).isPresent()) return;
        repo.save(UserPreference.builder()
                .publicId(publicId)
                .fullName(name)
                .email(email)
                .phone(phone)
                .whatsappNumber(phone)
                .emailOptIn(true)
                .smsOptIn(true)
                .inAppOptIn(true)
                .whatsappOptIn(true)
                .build());
        log.info("Seeded preference for {} ({})", name, email);
    }
}
