package com.EcoMarket.Pedido.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ItemCarritoDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.Carrito;
import com.EcoMarket.Pedido.model.ItemCarrito;
import com.EcoMarket.Pedido.repository.CarritoRepository;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoClient productoClient;

    // Obtiene el carrito del cliente por su ID, o crea uno nuevo si no existe.
    @Transactional
    public CarritoRespuestaDTO obtenerCarritoPorCliente(Long clienteId) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setClienteId(clienteId);
            return carritoRepository.save(nuevoCarrito);
        });

        return construirCarrito(carrito);
    }

    // Agrega un item al carrito del cliente, o actualiza la cantidad si ya existe.
    @Transactional
    public CarritoRespuestaDTO agregarItemAlCarrito(Long clienteId, AgregarItemRespuestaDTO itemRequest) {
        Optional<Carrito> carritoOpt = carritoRepository.findByClienteId(clienteId);
        Carrito carrito = carritoOpt.orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setClienteId(clienteId);
            return nuevoCarrito;
        });

        Optional<ItemCarrito> itemExistente = carrito.getProductos().stream()
                .filter(item -> item.getProductoId().equals(itemRequest.getProductoId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().setCantidad(itemExistente.get().getCantidad() + itemRequest.getCantidad());
        } else {
            carrito.getProductos().add(new ItemCarrito(itemRequest.getProductoId(), itemRequest.getCantidad()));
        }

        carritoRepository.save(carrito);
        return construirCarrito(carrito);
    }

    // Elimina un item del carrito del cliente, reduciendo la cantidad.
    @Transactional
    public CarritoRespuestaDTO eliminarItemDelCarrito(Long clienteId, Long productoId, int cantidadAEliminar) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado para el cliente con ID: " + clienteId));

        Optional<ItemCarrito> itemExistente = carrito.getProductos().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() - cantidadAEliminar;

            if (nuevaCantidad > 0) {
                item.setCantidad(nuevaCantidad);
            } else {
                carrito.getProductos().remove(item);
            }
            carritoRepository.save(carrito);
        } else {
            throw new RuntimeException("Producto no encontrado en el carrito");
        }
        return construirCarrito(carrito);
    }

    // Calcula el total por item y el subtotal general.
    private CarritoRespuestaDTO construirCarrito(Carrito carrito) {
        if (carrito.getProductos().isEmpty()) {
            CarritoRespuestaDTO response = new CarritoRespuestaDTO();
            response.setId(carrito.getId());
            response.setClienteId(carrito.getClienteId());
            response.setProductos(List.of());
            response.setSubTotal(0.0);
            return response;
        }

        List<Long> productoIds = carrito.getProductos().stream()
                .map(ItemCarrito::getProductoId)
                .collect(Collectors.toList());

        List<ProductoDTO> productosDesdeApi = productoClient.findProductosByIds(productoIds);

        Map<Long, ProductoDTO> mapaProductos = productosDesdeApi.stream()
                .collect(Collectors.toMap(ProductoDTO::getId, producto -> producto));

        List<ItemCarritoDTO> itemsDTO = carrito.getProductos().stream().map(item -> {
            ProductoDTO productoDTO = mapaProductos.get(item.getProductoId());
            double totalDelItem = (productoDTO != null) ? productoDTO.getPrecio() * item.getCantidad() : 0.0;

            ItemCarritoDTO itemDTO = new ItemCarritoDTO();
            itemDTO.setProducto(productoDTO);
            itemDTO.setCantidad(item.getCantidad());
            itemDTO.setTotalItem(totalDelItem);
            return itemDTO;
        }).collect(Collectors.toList());

        double subTotalGeneral = itemsDTO.stream()
                .mapToDouble(ItemCarritoDTO::getTotalItem)
                .sum();

        CarritoRespuestaDTO response = new CarritoRespuestaDTO();
        response.setId(carrito.getId());
        response.setClienteId(carrito.getClienteId());
        response.setProductos(itemsDTO);
        response.setSubTotal(subTotalGeneral);

        return response;
    }
}
