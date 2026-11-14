package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.Role;
import com.shipping.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);
    Page<User> findByRoleIn(Collection<Role> roles, Pageable pageable);
    List<User> findByRoleAndAssignedPortId(Role role, Long portId);
}