package com.EcoMarket.Pedido.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.ClienteDTO;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.model.ProductoPedido;
import com.EcoMarket.Pedido.repository.PedidoRepository;

@SpringBootTest
public class PedidoServiceTest {

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private ProductoClient productoClient;

    @MockBean
    private ClienteClient clienteClient;

    @Autowired
    private PedidoService pedidoService;

    private Pedido pedido;
    private ClienteDTO clienteDTO;
    private ProductoDTO productoDTO1;
    private ProductoDTO productoDTO2;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setClienteId(1L);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("Pendiente");

        ProductoPedido producto1 = new ProductoPedido(01L, 2, 15.0);
        ProductoPedido producto2 = new ProductoPedido(02L, 1, 25.0);
        pedido.setProductos(new ArrayList<>(List.of(producto1, producto2)));
        pedido.setTotal(55.0);

        clienteDTO = new ClienteDTO();
        clienteDTO.setId(1L);
        clienteDTO.setNombre("Wacoldo");
        clienteDTO.setApellido("Soto");

        productoDTO1 = new ProductoDTO();
        productoDTO1.setId(01L);
        productoDTO1.setNombre("Sal fina");
        productoDTO1.setPrecio(15.0);

        productoDTO2 = new ProductoDTO();
        productoDTO2.setId(02L);
        productoDTO2.setNombre("Azucar Flor");
        productoDTO2.setPrecio(25.0);
    }

    @Test
    void testListarTodosPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

        List<Pedido> resultado = pedidoService.listarTodosPedidos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void testEliminarPedido_Exitoso() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);
        boolean resultado = pedidoService.eliminarPedido(1L);
        assertTrue(resultado);
        verify(pedidoRepository).deleteById(1L);
    }

    @Test
    void testEliminarPedido_NoEncontrado() {
        when(pedidoRepository.existsById(99L)).thenReturn(false);
        boolean resultado = pedidoService.eliminarPedido(99L);
        assertFalse(resultado);
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGuardarPedido_Exitoso() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        Pedido pedidoGuardado = pedidoService.guardarPedido(pedido);

        assertNotNull(pedidoGuardado);
        assertEquals(55.0, pedidoGuardado.getTotal());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testGuardarPedido_ListaDeProductosNula() {
        pedido.setProductos(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.guardarPedido(pedido);
        });
        assertEquals("El pedido debe tener al menos un producto.", exception.getMessage());
    }

    @Test
    void testGuardarPedido_ListaDeProductosVacia() {
        pedido.setProductos(Collections.emptyList());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.guardarPedido(pedido);
        });
        assertEquals("El pedido debe tener al menos un producto.", exception.getMessage());
    }

    @Test
    void testGuardarPedido_PrecioEsInvalido() {
        pedido.getProductos().get(0).setPrecioUnitario(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.guardarPedido(pedido);
        });
        assertEquals("Cada item del pedido debe tener un precio unitario debe ser mayor 0.", exception.getMessage());
    }

    @Test
    void testBuscarPedidoxId_Encontrado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.buscarPedidoxId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testBuscarPedidoxId_NoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        Pedido resultado = pedidoService.buscarPedidoxId(99L);
        assertNull(resultado);
    }

    @Test
    void testObtenerPedidoConDetalles_Exitoso() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteClient.getClienteById(1L)).thenReturn(clienteDTO);
        when(productoClient.findProductosByIds(List.of(01L, 02L))).thenReturn(List.of(productoDTO1, productoDTO2));

        PedidoRespuestaDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pendiente", resultado.getEstado());

        assertNotNull(resultado.getCliente());
        assertEquals("Wacoldo", resultado.getCliente().getNombre());

        assertNotNull(resultado.getProductos());
        assertEquals(2, resultado.getProductos().size());
        assertEquals("Sal fina", resultado.getProductos().get(0).getProducto().getNombre());
        assertEquals(30.0, resultado.getProductos().get(0).getTotalItem());
        assertEquals("Azucar Flor", resultado.getProductos().get(1).getProducto().getNombre());
        assertEquals(25.0, resultado.getProductos().get(1).getTotalItem());

        verify(pedidoRepository).findById(1L);
        verify(clienteClient).getClienteById(1L);
        verify(productoClient).findProductosByIds(anyList());
    }

    @Test
    void testObtenerPedidoConDetalles_PedidoNoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoConDetalles(99L);
        });
    }

    @Test
    void testObtenerDetallesDeProductos_ConListaVacia() {
        pedido.setProductos(Collections.emptyList());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteClient.getClienteById(1L)).thenReturn(clienteDTO);

        PedidoRespuestaDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertTrue(resultado.getProductos().isEmpty());
        verify(productoClient, never()).findProductosByIds(anyList());
    }

}