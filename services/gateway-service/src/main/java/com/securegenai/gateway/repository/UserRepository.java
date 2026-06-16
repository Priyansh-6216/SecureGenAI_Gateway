package com.securegenai.gateway.repository;
import com.securegenai.gateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
