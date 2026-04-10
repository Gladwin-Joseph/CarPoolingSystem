package com.car_pooling_system.userservice.repository;

import com.car_pooling_system.userservice.model.HostDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostDriverRepository extends JpaRepository<HostDriver, Long> {
    Optional<HostDriver> findByUserId(Long userId);
}
