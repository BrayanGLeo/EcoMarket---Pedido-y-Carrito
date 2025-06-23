package com.EcoMarket.Pedido.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PedidoService pedidoService;

    @Mock
    private ProductoClient productoClient;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private PedidoController pedidoController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(pedidoController).build();
    }

    @Test
    void testPostCrearPedido_CreadoExitosamente() throws Exception {
        Pedido pedidoACrear = new Pedido();
        pedidoACrear.setClienteId(1L);

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(100L);
        pedidoGuardado.setClienteId(1L);

        when(pedidoService.guardarPedido(any(Pedido.class))).thenReturn(pedidoGuardado);

        mockMvc.perform(post("/api/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoACrear)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testPostCrearPedido_Conflicto() throws Exception {
        Pedido pedidoACrear = new Pedido();
        when(pedidoService.guardarPedido(any(Pedido.class))).thenReturn(null);

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoACrear)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetPedidoPorId_Encontrado() throws Exception {
        Long pedidoId = 1L;
        PedidoRespuestaDTO dto = new PedidoRespuestaDTO();
        dto.setId(pedidoId);
        when(pedidoService.obtenerPedidoConDetalles(pedidoId)).thenReturn(dto);

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId));
    }

    @Test
    void testGetPedidoPorId_NoEncontrado() throws Exception {
        Long pedidoId = 99L;
        when(pedidoService.obtenerPedidoConDetalles(pedidoId)).thenThrow(new RuntimeException("Pedido no encontrado"));

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetObtenerTodosLosPedidos_CuandoHayPedidos() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(List.of(new Pedido()));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void testGetObtenerTodosLosPedidos_CuandoNoHayPedidos() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarPedido_Eliminado() throws Exception {
        Long pedidoId = 5L;
        when(pedidoService.eliminar(pedidoId)).thenReturn(true);

        mockMvc.perform(delete("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarPedido_NoEncontrado() throws Exception {
        Long pedidoId = 999L;
        when(pedidoService.eliminar(pedidoId)).thenReturn(false);

        mockMvc.perform(delete("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isNotFound());
    }
}