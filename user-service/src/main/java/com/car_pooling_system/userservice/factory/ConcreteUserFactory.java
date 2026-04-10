package com.car_pooling_system.userservice.factory;

import com.car_pooling_system.userservice.model.HostDriver;
import com.car_pooling_system.userservice.model.Passenger;
import com.car_pooling_system.userservice.model.User;

/**
 * Singleton Factory Pattern (from class diagram):
 * - Private constructor prevents external instantiation
 * - Static getInstance() provides single global access point
 * - createDriver() and createPassenger() are the factory methods
 */
public class ConcreteUserFactory implements RoleBasedUserFactory {

    // Singleton: static instance
    private static ConcreteUserFactory instance;

    // Singleton: private constructor
    private ConcreteUserFactory() {
    }

    // Singleton: thread-safe getInstance
    public static synchronized ConcreteUserFactory getInstance() {
        if (instance == null) {
            instance = new ConcreteUserFactory();
        }
        return instance;
    }

    @Override
    public HostDriver createDriver(User user) {
        HostDriver driver = new HostDriver();
        driver.setUser(user);
        driver.setLicenseNumber("NOT_SET");
        return driver;
    }

    @Override
    public Passenger createPassenger(User user) {
        Passenger passenger = new Passenger();
        passenger.setUser(user);
        passenger.setPreferredPaymentMethod("CARD");
        return passenger;
    }
}
