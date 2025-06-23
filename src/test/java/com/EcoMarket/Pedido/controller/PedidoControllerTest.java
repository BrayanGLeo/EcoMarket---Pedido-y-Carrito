package com.EcoMarket.Pedido.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetPedidos_CuandoHayPedidos() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(List.of(new Pedido()));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }


    @Test
    void testGetPedidos_CuandoNoHayPedidos() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPedidoById_Encontrado() throws Exception {
        Long pedidoId = 1L;
        PedidoRespuestaDTO dto = new PedidoRespuestaDTO();
        dto.setId(pedidoId);
        when(pedidoService.obtenerPedidoConDetalles(pedidoId)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId));
    }

    @Test
    void testGetPedidoById_NoEncontrado() throws Exception {
        Long pedidoId = 99L;
        when(pedidoService.obtenerPedidoConDetalles(pedidoId)).thenThrow(new RuntimeException("Pedido no encontrado"));

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostPedido_CreadoExitosamente() throws Exception {
        Pedido pedidoACrear = new Pedido();
        pedidoACrear.setClienteId(1L);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(100L);
        pedidoGuardado.setClienteId(1L);

        when(pedidoService.guardar(any(Pedido.class))).thenReturn(pedidoGuardado);
        
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoACrear)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void testPostPedido_Conflicto() throws Exception {
        Pedido pedidoACrear = new Pedido();
        when(pedidoService.guardar(any(Pedido.class))).thenReturn(null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoACrear)))
                .andExpect(status().isConflict());
    }
}