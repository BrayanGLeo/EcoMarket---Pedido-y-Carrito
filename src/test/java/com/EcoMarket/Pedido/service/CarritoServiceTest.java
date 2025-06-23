package com.EcoMarket.Pedido.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.Carrito;
import com.EcoMarket.Pedido.model.ItemCarrito;
import com.EcoMarket.Pedido.repository.CarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carritoService, "productoServiceUrl", "http://localhost:8081");
    }

    @Test
    void testAgregarItemAlCarrito_ItemNuevo() {
        Long clienteId = 1L;
        Long productoId = 10L;
        Carrito carritoExistente = new Carrito();
        carritoExistente.setClienteId(clienteId);
        carritoExistente.setId(100L);
        carritoExistente.setProductos(new ArrayList<>());

        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carritoExistente));

        AgregarItemRespuestaDTO itemRequest = new AgregarItemRespuestaDTO();
        itemRequest.setProductoId(productoId);
        itemRequest.setCantidad(2);

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setId(productoId);
        productoMock.setNombre("Producto de Prueba");
        productoMock.setPrecio(50.0);
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(productoMock);

        carritoService.agregarItemAlCarrito(clienteId, itemRequest);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(productoId, carritoGuardado.getProductos().get(0).getProductoId());
        assertEquals(2, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testAgregarItemAlCarrito_ActualizarCantidadItemExistente() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 1);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.setId(100L);
        carrito.getProductos().add(itemExistente);

        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));

        AgregarItemRespuestaDTO itemRequest = new AgregarItemRespuestaDTO();
        itemRequest.setProductoId(productoId);
        itemRequest.setCantidad(2);

        ProductoDTO productoMock = new ProductoDTO();
        productoMock.setPrecio(10.0);
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(productoMock);

        carritoService.agregarItemAlCarrito(clienteId, itemRequest);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(3, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testEliminarItemDelCarrito_ReduceCantidadCorrectamente() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 5);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.getProductos().add(itemExistente);

        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(new ProductoDTO());

        // Act
        carritoService.eliminarItemDelCarrito(clienteId, productoId, 2);

        // Assert
        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertEquals(1, carritoGuardado.getProductos().size());
        assertEquals(3, carritoGuardado.getProductos().get(0).getCantidad());
    }

    @Test
    void testEliminarItemDelCarrito_EliminaItemPorCompleto() {
        Long clienteId = 1L;
        Long productoId = 10L;
        ItemCarrito itemExistente = new ItemCarrito(productoId, 2);

        Carrito carrito = new Carrito();
        carrito.setClienteId(clienteId);
        carrito.getProductos().add(itemExistente);
        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.of(carrito));

        carritoService.eliminarItemDelCarrito(clienteId, productoId, 2);

        ArgumentCaptor<Carrito> carritoCaptor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(carritoCaptor.capture());

        Carrito carritoGuardado = carritoCaptor.getValue();
        assertTrue(carritoGuardado.getProductos().isEmpty());
    }

    @Test
    void testEliminarItemDelCarrito_CarritoNoEncontrado() {
        Long clienteId = 1L;
        Long productoId = 10L;
        when(carritoRepository.findByClienteId(clienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            carritoService.eliminarItemDelCarrito(clienteId, productoId, 1);
        });

        assertEquals("Carrito no encontrado para el cliente con ID: " + clienteId, exception.getMessage());
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void testObtenerCarritoPorCliente_CuandoNoExisteCreaUnoNuevo() {
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
    void testConstruirCarrito_CalculaSubTotalCorrectamente() {
        Carrito carrito = new Carrito();
        carrito.setClienteId(1L);
        carrito.getProductos().add(new ItemCarrito(101L, 2));
        carrito.getProductos().add(new ItemCarrito(102L, 1));

        when(carritoRepository.findByClienteId(1L)).thenReturn(Optional.of(carrito));

        ProductoDTO producto1 = new ProductoDTO();
        producto1.setId(101L);
        producto1.setPrecio(10.0);
        String urlProducto1 = "http://localhost:8081/api/productos/101";
        when(restTemplate.getForObject(urlProducto1, ProductoDTO.class)).thenReturn(producto1);

        ProductoDTO producto2 = new ProductoDTO();
        producto2.setId(102L);
        producto2.setPrecio(25.0);
        String urlProducto2 = "http://localhost:8081/api/productos/102";
        when(restTemplate.getForObject(urlProducto2, ProductoDTO.class)).thenReturn(producto2);

        CarritoRespuestaDTO resultado = carritoService.obtenerCarritoPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.getProductos().size());
        assertEquals(45.0, resultado.getSubTotal());
    }

}