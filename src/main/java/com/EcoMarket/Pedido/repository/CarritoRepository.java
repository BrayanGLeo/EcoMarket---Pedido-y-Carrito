package com.EcoMarket.Pedido.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.EcoMarket.Pedido.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByClienteId(Long clienteId);
}
