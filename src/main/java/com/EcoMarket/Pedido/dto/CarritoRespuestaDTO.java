package com.EcoMarket.Pedido.dto;

import java.util.List;

import lombok.Data;

@Data
public class CarritoRespuestaDTO {
    private Long id;
    private Long clienteId;
    private List<ItemCarritoDTO> productos;
    private double subTotal;
}
