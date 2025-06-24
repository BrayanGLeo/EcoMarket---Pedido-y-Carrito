package com.EcoMarket.Pedido.dto;

import lombok.Data;

@Data
public class ProductoPedidoDTO {
    private ProductoDTO producto;
    private int cantidad;
    private double totalItem;
}
