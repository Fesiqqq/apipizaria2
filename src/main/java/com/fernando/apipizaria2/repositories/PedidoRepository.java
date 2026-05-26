package com.fernando.apipizaria2.repositories;

import com.fernando.apipizaria2.domain.entities.Pedido;
import com.fernando.apipizaria2.domain.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);
}
