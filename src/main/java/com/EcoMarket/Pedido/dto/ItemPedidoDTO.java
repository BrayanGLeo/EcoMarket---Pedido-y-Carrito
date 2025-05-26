package com.EcoMarket.Pedido.dto;

import lombok.Data;

@Data
public class ItemPedidoDTO {
    private ProductoDTO producto;
    private int cantidad;
}
