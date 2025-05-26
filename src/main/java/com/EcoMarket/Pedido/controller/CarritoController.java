package com.EcoMarket.Pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;

public class CarritoController {
    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{clienteId}")
    // Obtiene el carrito del cliente por su ID, si no existe, crea un nuevo carrito
    // vacío para el cliente.
    public ResponseEntity<CarritoRespuestaDTO> getCarrito(@PathVariable Long clienteId) {
        CarritoRespuestaDTO carrito = carritoService.obtenerCarritoPorCliente(clienteId);
        return new ResponseEntity<>(carrito, HttpStatus.OK);
    }

    @PostMapping("/{clienteId}/items")
    // Agrega un item al carrito del cliente. Si el producto ya existe, actualiza la
    // cantidad.
    public ResponseEntity<CarritoRespuestaDTO> addItem(@PathVariable Long clienteId,
            @RequestBody AgregarItemRespuestaDTO itemRequest) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.agregarItemAlCarrito(clienteId, itemRequest);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (Exception e) {
            // Manejar caso en que el producto no exista.
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
