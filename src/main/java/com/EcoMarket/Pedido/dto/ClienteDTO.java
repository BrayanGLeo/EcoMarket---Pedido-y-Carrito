package com.EcoMarket.Pedido.dto;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long id;
    private String run;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
}
