package com.car_pooling_system.userservice.factory;

import com.car_pooling_system.userservice.model.HostDriver;
import com.car_pooling_system.userservice.model.Passenger;
import com.car_pooling_system.userservice.model.User;

public interface RoleBasedUserFactory {
    HostDriver createDriver(User user);
    Passenger createPassenger(User user);
}
