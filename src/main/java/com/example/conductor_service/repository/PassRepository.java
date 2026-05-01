package com.example.conductor_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.conductor_service.model.Pass;

public interface PassRepository extends JpaRepository<Pass, String> {

    Optional<Pass> findFirstByUserIdAndPassType_PassType(Long userId, String passType);
}
