package com.EcoMarket.Pedido.dto;

import java.util.List;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long id;
    private String run;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private List<PedidoResumidoDTO> historialPedidos;
}
