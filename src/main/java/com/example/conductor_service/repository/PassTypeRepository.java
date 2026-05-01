package com.example.conductor_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.conductor_service.model.PassType;

public interface PassTypeRepository extends JpaRepository<PassType, String> {
}
