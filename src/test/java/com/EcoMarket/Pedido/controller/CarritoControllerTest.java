package com.EcoMarket.Pedido.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.AgregarProductoRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class CarritoControllerTest {

        private MockMvc mockMvc;

        @Mock
        private CarritoService carritoService;

        @Mock
        private ProductoClient productoClient;

        @InjectMocks
        private CarritoController carritoController;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        }

        @Test
        void testAgregarProducto_Exitoso() throws Exception {
                Long clienteId = 1L;
                AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
                itemRequest.setProductoId(101L);
                itemRequest.setCantidad(2);

                CarritoRespuestaDTO carritoActualizado = new CarritoRespuestaDTO();
                carritoActualizado.setClienteId(clienteId);

                when(carritoService.agregarProductoAlCarrito(eq(clienteId), any(AgregarProductoRespuestaDTO.class)))
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
                AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();

                when(carritoService.agregarProductoAlCarrito(eq(clienteId), any(AgregarProductoRespuestaDTO.class)))
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

                when(carritoService.eliminarProductoDelCarrito(eq(clienteId), eq(productoId), eq(cantidad)))
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

                when(carritoService.eliminarProductoDelCarrito(eq(clienteId), eq(productoId), anyInt()))
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