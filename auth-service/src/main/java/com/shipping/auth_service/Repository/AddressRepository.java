package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.Address;
import com.shipping.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> findByUser_UserId(Long userId);
}
