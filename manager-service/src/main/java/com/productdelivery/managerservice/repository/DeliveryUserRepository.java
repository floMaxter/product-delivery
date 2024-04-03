package com.productdelivery.managerservice.repository;

import com.productdelivery.managerservice.model.DeliveryUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface DeliveryUserRepository extends CrudRepository<DeliveryUser, Integer> {

    Optional<DeliveryUser> findByUsername(String username);
}
