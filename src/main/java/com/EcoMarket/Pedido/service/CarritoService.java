package com.EcoMarket.Pedido.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private RestTemplate restTemplate;

    @Value("${service.productos.url}")
    private String productoServiceUrl;

    @Transactional
    public CarritoRespuestaDTO obtenerCarritoPorCliente(Long clienteId) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId).orElseGet(() -> {
            // Si no existe un carrito para el cliente, se crea uno nuevo
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setClienteId(clienteId);
            return carritoRepository.save(nuevoCarrito);
        });

        return construirCarrito(carrito);
    }

    @Transactional
    public CarritoRespuestaDTO agregarItemAlCarrito(Long clienteId, AgregarItemRespuestaDTO itemRequest) {
        // Obtiene o crear el carrito
        Optional<Carrito> carritoOpt = carritoRepository.findByClienteId(clienteId);
        Carrito carrito = carritoOpt.orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setClienteId(clienteId);
            return nuevoCarrito;
        });

        // Buscar si el item ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getProductos().stream()
                .filter(item -> item.getProductoId().equals(itemRequest.getProductoId()))
                .findFirst();

        // Actualiza la cantidad o añade un nuevo item
        if (itemExistente.isPresent()) {
            itemExistente.get().setCantidad(itemExistente.get().getCantidad() + itemRequest.getCantidad());
        } else {
            carrito.getProductos().add(new ItemCarrito(itemRequest.getProductoId(), itemRequest.getCantidad()));
        }

        // Guarda los cambios y devuelve la respuesta
        carritoRepository.save(carrito);
        return construirCarrito(carrito);
    }

    // Este método calcula el total por item y el subtotal general.
    private CarritoRespuestaDTO construirCarrito(Carrito carrito) {
        List<ItemCarritoDTO> itemsDTO = new ArrayList<>();
        double subTotalGeneral = 0; // El total de todo el carrito

        for (ItemCarrito item : carrito.getProductos()) {
            String urlProducto = productoServiceUrl + "/api/productos/" + item.getProductoId();
            ProductoDTO productoDTO = restTemplate.getForObject(urlProducto, ProductoDTO.class);

            if (productoDTO != null) {
                double totalDelItem = productoDTO.getPrecio() * item.getCantidad();

                ItemCarritoDTO itemDTO = new ItemCarritoDTO();
                itemDTO.setProducto(productoDTO);
                itemDTO.setCantidad(item.getCantidad());
                itemDTO.setTotalItem(totalDelItem); // Se asigna el total de la línea

                itemsDTO.add(itemDTO);

                subTotalGeneral += totalDelItem; // Se suma al total general
            }
        }

        CarritoRespuestaDTO response = new CarritoRespuestaDTO();
        response.setId(carrito.getId());
        response.setClienteId(carrito.getClienteId());
        response.setProductos(itemsDTO);
        response.setSubTotal(subTotalGeneral); // Se asigna el total de todo el carrito

        return response;
    }
}
