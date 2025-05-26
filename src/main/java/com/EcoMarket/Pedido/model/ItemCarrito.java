package com.EcoMarket.Pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrito {

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private int cantidad;
}
