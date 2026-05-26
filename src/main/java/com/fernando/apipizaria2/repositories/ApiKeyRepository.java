package com.fernando.apipizaria2.repositories;

import com.fernando.apipizaria2.domain.entities.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByChaveAndAtivoTrue(String chave);
}
