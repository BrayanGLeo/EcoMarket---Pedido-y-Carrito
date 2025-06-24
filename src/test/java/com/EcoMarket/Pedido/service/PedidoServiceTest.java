package com.EcoMarket.Pedido.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.ClienteDTO;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.model.ProductoPedido;
import com.EcoMarket.Pedido.repository.PedidoRepository;

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
    void testEliminarPedido() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pedidoRepository).deleteById(1L);

        boolean resultado = pedidoService.eliminarPedido(1L);

        assertTrue(resultado);
        verify(pedidoRepository, times(1)).existsById(1L);
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGuardarPedido() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        Pedido pedidoGuardado = pedidoService.guardarPedido(pedido);

        assertNotNull(pedidoGuardado);
        assertEquals(55.0, pedidoGuardado.getTotal());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testBuscarPedidoxId() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.buscarPedidoxId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerPedidoConDetalles() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteClient.getClienteById(1L)).thenReturn(clienteDTO);
        when(pedidoRepository.findByClienteId(1L)).thenReturn(List.of(pedido));
        when(productoClient.findProductosByIds(anyList())).thenReturn(List.of(productoDTO1, productoDTO2));

        PedidoRespuestaDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertEquals(pedido.getId(), resultado.getId());
        assertEquals(pedido.getTotal(), resultado.getTotal());

        assertNotNull(resultado.getCliente());
        assertEquals(clienteDTO.getNombre(), resultado.getCliente().getNombre());
        assertNotNull(resultado.getCliente().getHistorialPedidos());
        assertEquals(1, resultado.getCliente().getHistorialPedidos().size());

        assertNotNull(resultado.getProductos());
        assertEquals(2, resultado.getProductos().size());
        assertEquals("Sal fina", resultado.getProductos().get(0).getProducto().getNombre());
        assertEquals(30.0, resultado.getProductos().get(0).getTotalItem());
        assertEquals("Azucar Flor", resultado.getProductos().get(1).getProducto().getNombre());
        assertEquals(25.0, resultado.getProductos().get(1).getTotalItem());

        verify(pedidoRepository, times(1)).findById(1L);
        verify(clienteClient, times(1)).getClienteById(1L);
        verify(pedidoRepository, times(1)).findByClienteId(1L);
        verify(productoClient, times(1)).findProductosByIds(anyList());
    }

}