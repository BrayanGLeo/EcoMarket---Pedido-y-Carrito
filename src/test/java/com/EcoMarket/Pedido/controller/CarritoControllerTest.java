package com.EcoMarket.Pedido.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.EcoMarket.Pedido.assemblers.CarritoModelAssembler;
import com.EcoMarket.Pedido.dto.AgregarProductoRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;
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

@WebMvcTest(CarritoController.class)
@Import(CarritoModelAssembler.class)
public class CarritoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CarritoService carritoService;

        @Autowired
        private ObjectMapper objectMapper;

        private CarritoRespuestaDTO carritoRespuestaDTO;

        @BeforeEach
        void setup() {
                carritoRespuestaDTO = new CarritoRespuestaDTO();
                carritoRespuestaDTO.setId(1L);
                carritoRespuestaDTO.setClienteId(1L);
                carritoRespuestaDTO.setSubTotal(150.0);
        }

        @Test
        void testAgregarProducto_Exitoso() throws Exception {
                Long clienteId = 1L;
                AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
                itemRequest.setProductoId(101L);
                itemRequest.setCantidad(2);

                when(carritoService.agregarProductoAlCarrito(eq(clienteId), any(AgregarProductoRespuestaDTO.class)))
                                .thenReturn(carritoRespuestaDTO);

                mockMvc.perform(post("/api/carrito/{clienteId}/productos", clienteId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequest)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                                .andExpect(jsonPath("$.clienteId").value(clienteId))
                                .andExpect(jsonPath("$._links.self.href").exists());
        }

        @Test
        void testAgregarProducto_FalloProductoNoExistente() throws Exception {
                Long clienteId = 1L;

                when(carritoService.agregarProductoAlCarrito(eq(clienteId), any(AgregarProductoRespuestaDTO.class)))
                                .thenThrow(new RuntimeException("Producto no existe"));

                mockMvc.perform(post("/api/carrito/{clienteId}/productos", clienteId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new AgregarProductoRespuestaDTO())))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testEliminarProductoDelCarrito_Exitoso() throws Exception {
                Long clienteId = 1L;
                Long productoId = 101L;

                when(carritoService.eliminarProductoDelCarrito(eq(clienteId), eq(productoId), anyInt()))
                                .thenReturn(carritoRespuestaDTO);

                mockMvc.perform(delete("/api/carrito/{clienteId}/productos/{productoId}", clienteId, productoId)
                                .param("cantidad", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                                .andExpect(jsonPath("$.clienteId").value(clienteId))
                                .andExpect(jsonPath("$._links.self.href").exists());
        }

        @Test
        void testMostarCarrito_RetornaCarritoExistente() throws Exception {
                Long clienteId = 1L;
                when(carritoService.obtenerCarritoPorCliente(clienteId)).thenReturn(carritoRespuestaDTO);

                mockMvc.perform(get("/api/carrito/{clienteId}", clienteId))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                                .andExpect(jsonPath("$.clienteId").value(clienteId))
                                .andExpect(jsonPath("$._links.self.href").exists());
        }
}