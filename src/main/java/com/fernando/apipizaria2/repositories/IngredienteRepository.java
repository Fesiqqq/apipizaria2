package com.fernando.apipizaria2.repositories;

import com.fernando.apipizaria2.domain.entities.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    Page<Ingrediente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
