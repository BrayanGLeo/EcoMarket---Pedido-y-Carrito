package com.EcoMarket.Pedido.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PedidoRespuestaDTO {
    private Long id;
    private ClienteDTO cliente;
    private List<ItemPedidoDTO> productos;
    private LocalDateTime fecha;
    private Double total;
    private String estado;
}
