package com.EcoMarket.Pedido.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.EcoMarket.Pedido.dto.ClienteDTO;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.ItemPedido;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoService, "clienteServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(pedidoService, "productoServiceUrl", "http://localhost:8081");

        pedidoMock = new Pedido();
        pedidoMock.setId(1L);
        pedidoMock.setClienteId(10L);
        pedidoMock.setFecha(new Date());
        pedidoMock.setTotal(150.0);
        pedidoMock.setEstado("PENDIENTE");
        
        ItemPedido item = new ItemPedido();
        item.setProductoId(101L);
        item.setCantidad(3);
        pedidoMock.setProductos(List.of(item));
    }

    @Test
    void testObtenerPedidoConDetalles_Exitoso() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));

        ClienteDTO clienteMock = new ClienteDTO();
        clienteMock.setId(10L);
        clienteMock.setNombre("Cliente de Prueba");
        String clienteUrl = "http://localhost:8082/api/clientes/10";
        when(restTemplate.getForObject(clienteUrl, ClienteDTO.class)).thenReturn(clienteMock);
        
        when(pedidoRepository.findByClienteId(10L)).thenReturn(Collections.emptyList());

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setId(101L);
        productoMock.setNombre("Producto Test");
        String productoUrl = "http://localhost:8081/api/productos/101";
        when(restTemplate.getForObject(productoUrl, ProductoDTO.class)).thenReturn(productoMock);
        
        PedidoRespuestaDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Cliente de Prueba", resultado.getCliente().getNombre());
        assertEquals(1, resultado.getProductos().size());
        assertEquals("Producto Test", resultado.getProductos().get(0).getProducto().getNombre());
        assertEquals(3, resultado.getProductos().get(0).getCantidad());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).getForObject(clienteUrl, ClienteDTO.class);
        verify(restTemplate, times(1)).getForObject(productoUrl, ProductoDTO.class);
    }
    
    @Test
    void testObtenerPedidoConDetalles_PedidoNoEncontrado() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoConDetalles(99L);
        });
    }
}