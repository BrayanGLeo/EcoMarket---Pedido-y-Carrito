package com.EcoMarket.Pedido.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoClient productoClient;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoMock;

    @BeforeEach
    void setUp() {
        pedidoMock = new Pedido();
        pedidoMock.setId(1L);
        pedidoMock.setClienteId(10L);
        pedidoMock.setFecha(LocalDateTime.now());
        pedidoMock.setTotal(150.0);
        pedidoMock.setEstado("PENDIENTE");

        ItemPedido item = new ItemPedido();
        item.setProductoId(101L);
        item.setCantidad(3);
        item.setPrecioUnitario(50.0);
        pedidoMock.setProductos(List.of(item));
    }

    @Test
    void testObtenerPedidoConDetalles_Exitoso() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));

        ClienteDTO clienteMock = new ClienteDTO();
        clienteMock.setId(10L);
        clienteMock.setNombre("Cliente de Prueba");
        when(clienteClient.getClienteById(10L)).thenReturn(clienteMock);

        when(pedidoRepository.findByClienteId(10L)).thenReturn(Collections.emptyList());

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setId(101L);
        productoMock.setNombre("Producto Test");
        productoMock.setPrecio(55.0);
        List<Long> idsDeProductos = List.of(101L);
        when(productoClient.findProductosByIds(idsDeProductos)).thenReturn(List.of(productoMock));

        PedidoRespuestaDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Cliente de Prueba", resultado.getCliente().getNombre());
        assertEquals(1, resultado.getProductos().size());
        assertEquals("Producto Test", resultado.getProductos().get(0).getProducto().getNombre());
        assertEquals(50.0, resultado.getProductos().get(0).getProducto().getPrecio());
        assertEquals(3, resultado.getProductos().get(0).getCantidad());

        verify(pedidoRepository, times(1)).findById(1L);
        verify(clienteClient, times(1)).getClienteById(10L);
        verify(productoClient, times(1)).findProductosByIds(idsDeProductos);
    }

    @Test
    void testObtenerPedidoConDetalles_PedidoNoEncontrado() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoConDetalles(99L);
        });

        verifyNoInteractions(clienteClient);
        verifyNoInteractions(productoClient);
    }
}