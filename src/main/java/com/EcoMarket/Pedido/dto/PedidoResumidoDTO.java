package com.EcoMarket.Pedido.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PedidoResumidoDTO {
    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private String estado;
}
