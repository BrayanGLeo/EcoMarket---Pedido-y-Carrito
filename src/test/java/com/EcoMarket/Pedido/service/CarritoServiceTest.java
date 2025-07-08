package com.EcoMarket.Pedido.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.AgregarProductoRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.Carrito;
import com.EcoMarket.Pedido.model.ItemCarrito;
import com.EcoMarket.Pedido.repository.CarritoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class CarritoServiceTest {

    @MockBean
    private CarritoRepository carritoRepository;

    @MockBean
    private ProductoClient productoClient;

    @Autowired
    private CarritoService carritoService;

    @Test
    void testAgregarProductoAlCarrito_ProductoNuevo() {
        Long clienteId = 1L;
        Long productoId = 10L;
        Carrito carritoExistente = new Carrito(100L, clienteId, new ArrayList<>());
        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carritoExistente));

        AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
        itemRequest.setProductoId(productoId);
        itemRequest.setCantidad(2);

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setId(productoId);
        productoMock.setNombre("Producto de Prueba");
        productoMock.setPrecio(50.0);
        when(productoClient.findProductosByIds(anyList())).thenReturn(List.of(productoMock));

        carritoService.agregarProductoAlCarrito(clienteId, itemRequest);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(productoId, carritoGuardado.getProductos().get(0).getProductoId());
        assertEquals(2, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testAgregarProductoAlCarrito_AgregarCantidadProductoExistente() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 1);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.setId(100L);
        carrito.getProductos().add(itemExistente);

        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));

        AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
        itemRequest.setProductoId(productoId);
        itemRequest.setCantidad(2);

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setPrecio(10.0);

        carritoService.agregarProductoAlCarrito(clienteId, itemRequest);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(3, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testEliminarProductoDelCarrito_ReduceCantidadProducto() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 5);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.getProductos().add(itemExistente);

        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));

        carritoService.eliminarProductoDelCarrito(clienteId, productoId, 2);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(3, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testEliminarProductoDelCarrito() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 2);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.getProductos().add(itemExistente);
        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));

        carritoService.eliminarProductoDelCarrito(clienteId, productoId, 2);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertTrue(carritoGuardado.getProductos().isEmpty());
    }

    @Test
    void testObtenerCarritoPorCliente() {
        Long clienteId = 1L;
        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.empty());

        Carrito nuevoCarrito = new Carrito();
        nuevoCarrito.setClienteId(clienteId);
        nuevoCarrito.setId(100L);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(nuevoCarrito);

        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(clienteId);

        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getClienteId());
        assertTrue(resultado.getProductos().isEmpty());
        verify(carritoRepository, times(1)).findByClienteId(clienteId);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    void testConstruirCarrito() {
        Carrito carrito = new Carrito();
        carrito.setClienteId(1L);
        carrito.getProductos().add(new ItemCarrito(101L, 2));
        carrito.getProductos().add(new ItemCarrito(102L, 1));

        List<Long> idsDeProductos = List.of(101L, 102L);

        ProductoDTO producto1 = new ProductoDTO();
        producto1.setId(101L);
        producto1.setPrecio(10.0);

        ProductoDTO producto2 = new ProductoDTO();
        producto2.setId(102L);
        producto2.setPrecio(25.0);

        when(productoClient.findProductosByIds(idsDeProductos)).thenReturn(List.of(producto1, producto2));

        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));
        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.getProductos().size());
        assertEquals(45.0, resultado.getSubTotal());
        verify(productoClient, times(1)).findProductosByIds(idsDeProductos);
    }

}