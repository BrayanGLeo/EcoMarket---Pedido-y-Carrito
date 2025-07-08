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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
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

    private Carrito carrito;
    private ProductoDTO productoMock;

    @BeforeEach
    void setUp() {
        carrito = new Carrito(1L, 1L, new ArrayList<>(List.of(new ItemCarrito(101L, 5))));

        productoMock = new ProductoDTO();
        productoMock.setId(101L);
        productoMock.setNombre("Coca Cola 3L");
        productoMock.setPrecio(2490.0);
    }

    @Test
    void testObtenerCarritoPorCliente_Existente() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));
        when(productoClient.findProductosByIds(anyList())).thenReturn(List.of(productoMock));

        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getClienteId());
        assertEquals(1, resultado.getProductos().size());
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void testObtenerCarritoPorCliente_CreaUnoNuevoSiNoExiste() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenReturn(new Carrito(2L, 1L, new ArrayList<>()));

        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getClienteId());
        assertTrue(resultado.getProductos().isEmpty());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void testConstruirCarrito_ConProductosVacios() {
        carrito.setProductos(Collections.emptyList());
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));

        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(1L);

        assertNotNull(resultado);
        assertTrue(resultado.getProductos().isEmpty());
        assertEquals(0.0, resultado.getSubTotal());
        verify(productoClient, never()).findProductosByIds(anyList());
    }

    @Test
    void testAgregarProductoAlCarrito_CuandoCarritoNoExiste() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.empty());

        AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(2);

        carritoService.agregarProductoAlCarrito(1L, itemRequest);

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getClienteId());
        assertEquals(1, captor.getValue().getProductos().size());
    }

    @Test
    void testAgregarProductoAlCarrito_AumentaCantidadDeExistente() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));

        AgregarProductoRespuestaDTO itemRequest = new AgregarProductoRespuestaDTO();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(2);

        carritoService.agregarProductoAlCarrito(1L, itemRequest);

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getProductos().get(0).getCantidad());
    }

    @Test
    void testEliminarProductoDelCarrito_FallaSiCarritoNoExiste() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            carritoService.eliminarProductoDelCarrito(1L, 101L, 1);
        });
    }

    @Test
    void testEliminarProductoDelCarrito_FallaSiProductoNoEstaEnCarrito() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));
        assertThrows(RuntimeException.class, () -> {
            carritoService.eliminarProductoDelCarrito(1L, 999L, 1);
        });
    }

    @Test
    void testEliminarProductoDelCarrito_ReduceLaCantidad() {
        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));

        carritoService.eliminarProductoDelCarrito(1L, 101L, 3);

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getProductos().get(0).getCantidad());
    }
}