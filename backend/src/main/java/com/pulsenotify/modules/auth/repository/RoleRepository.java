package com.pulsenotify.modules.auth.repository;

import com.pulsenotify.modules.auth.entity.ERole;
import com.pulsenotify.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
