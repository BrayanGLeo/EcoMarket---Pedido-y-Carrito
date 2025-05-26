package com.EcoMarket.Pedido.dto;

import lombok.Data;

@Data
public class ItemCarritoDTO {
    private ProductoDTO producto;
    private int cantidad;
    private double totalItem;
}
