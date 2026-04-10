package com.car_pooling_system.userservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private HostDriver driver;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "car_model", nullable = false)
    private String carModel;

    @Column(nullable = false)
    private int capacity;
}
