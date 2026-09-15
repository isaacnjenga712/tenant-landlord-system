package com.apex.leaseService.entity;

import java.util.UUID;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  
	@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "UUID") 
    private UUID userId;

    private String name;
    private String email;
}
