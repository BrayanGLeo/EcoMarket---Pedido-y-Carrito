package com.EcoMarket.Pedido.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.EcoMarket.Pedido.assemblers.PedidoModelAssembler;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@WebMvcTest(PedidoController.class)
@Import(PedidoModelAssembler.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Pedido pedido;
    private PedidoRespuestaDTO pedidoRespuestaDTO;

    @BeforeEach
    void setup() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setClienteId(1L);
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(100.0);
        pedido.setEstado("CREADO");

        pedidoRespuestaDTO = new PedidoRespuestaDTO();
        pedidoRespuestaDTO.setId(1L);
        pedidoRespuestaDTO.setTotal(100.0);
        pedidoRespuestaDTO.setEstado("CREADO");
    }

    @Test
    void testCrearPedido_Exitosamente() throws Exception {
        when(pedidoService.guardarPedido(any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Pedido())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void testCrearPedido_Conflicto() throws Exception {
        when(pedidoService.guardarPedido(any(Pedido.class))).thenReturn(null);

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Pedido())))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetPedidoPorId_Encontrado() throws Exception {
        when(pedidoService.obtenerPedidoConDetalles(1L)).thenReturn(pedidoRespuestaDTO);

        mockMvc.perform(get("/api/pedidos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    void testGetPedidoPorId_NoEncontrado() throws Exception {
        when(pedidoService.obtenerPedidoConDetalles(99L)).thenThrow(new RuntimeException("Pedido no encontrado"));

        mockMvc.perform(get("/api/pedidos/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerTodosLosPedidos_CuandoHayPedidos() throws Exception {
        when(pedidoService.listarTodosPedidos()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.pedidoList[0].id").value(1L))
                .andExpect(jsonPath("$._embedded.pedidoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void testObtenerTodosLosPedidos_CuandoNoHayPedidos() throws Exception {
        when(pedidoService.listarTodosPedidos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarPedido_Eliminado() throws Exception {
        when(pedidoService.eliminarPedido(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/pedidos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarPedido_NoEncontrado() throws Exception {
        when(pedidoService.eliminarPedido(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/pedidos/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}