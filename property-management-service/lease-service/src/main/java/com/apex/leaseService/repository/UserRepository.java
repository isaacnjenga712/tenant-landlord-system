package com.apex.leaseService.repository;

import com.apex.leaseService.entity.User;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // No extra methods – JpaRepository provides findAllById etc.
}