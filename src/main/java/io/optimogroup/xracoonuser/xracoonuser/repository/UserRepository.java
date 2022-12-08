package io.optimogroup.xracoonuser.xracoonuser.repository;

import io.optimogroup.xracoonuser.xracoonuser.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserUuid(String uuId);

    User findByUserUuid(String uuId);
}
