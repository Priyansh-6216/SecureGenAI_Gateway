package com.securegenai.gateway.repository;
import com.securegenai.gateway.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface ResponseRepository extends JpaRepository<Response, UUID> {
}
