package com.EcoMarket.Pedido.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CarritoController.class)
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAgregarProducto_Exitoso() throws Exception {
        Long clienteId = 1L;
        AgregarItemRespuestaDTO itemRequest = new AgregarItemRespuestaDTO();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(2);

        CarritoRespuestaDTO carritoActualizado = new CarritoRespuestaDTO();
        carritoActualizado.setClienteId(clienteId);

        when(carritoService.agregarItemAlCarrito(eq(clienteId), any(AgregarItemRespuestaDTO.class)))
                .thenReturn(carritoActualizado);

        mockMvc.perform(post("/api/carrito/{clienteId}/productos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId));
    }

    @Test
    void testAgregarProducto_FalloPorProductoNoExistente() throws Exception {
        Long clienteId = 1L;
        AgregarItemRespuestaDTO itemRequest = new AgregarItemRespuestaDTO();

        when(carritoService.agregarItemAlCarrito(eq(clienteId), any(AgregarItemRespuestaDTO.class)))
                .thenThrow(new RuntimeException("Producto no existe"));

        mockMvc.perform(post("/api/carrito/{clienteId}/productos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEliminarProductoDelCarrito_Exitoso() throws Exception {
        Long clienteId = 1L;
        Long productoId = 202L;
        int cantidad = 1;

        CarritoRespuestaDTO carritoActualizado = new CarritoRespuestaDTO();
        carritoActualizado.setClienteId(clienteId);

        when(carritoService.eliminarItemDelCarrito(eq(clienteId), eq(productoId), eq(cantidad)))
                .thenReturn(carritoActualizado);

        mockMvc.perform(delete("/api/carrito/{clienteId}/productos/{productoId}", clienteId, productoId)
                .param("cantidad", String.valueOf(cantidad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId));
    }

    @Test
    void testEliminarProductoDelCarrito_Fallo() throws Exception {
        Long clienteId = 1L;
        Long productoId = 202L;

        when(carritoService.eliminarItemDelCarrito(eq(clienteId), eq(productoId), anyInt()))
                .thenThrow(new RuntimeException("Error al eliminar item"));

        mockMvc.perform(delete("/api/carrito/{clienteId}/productos/{productoId}", clienteId, productoId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMostarCarrito_RetornaCarritoExistente() throws Exception {
        Long clienteId = 1L;
        CarritoRespuestaDTO carritoDTO = new CarritoRespuestaDTO();
        carritoDTO.setClienteId(clienteId);

        when(carritoService.obtenerCarritoPorCliente(clienteId)).thenReturn(carritoDTO);

        mockMvc.perform(get("/api/carrito/{clienteId}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId));
    }
}
