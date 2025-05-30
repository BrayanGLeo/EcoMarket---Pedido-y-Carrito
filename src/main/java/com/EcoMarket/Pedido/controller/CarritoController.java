package com.EcoMarket.Pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;

@RestController 
@RequestMapping("/api/carritos")
public class CarritoController {
    @Autowired
    private CarritoService carritoService;

    // Agrega un item al carrito del cliente
    @PostMapping("/{clienteId}/items")
    public ResponseEntity<CarritoRespuestaDTO> addItem(@PathVariable Long clienteId,
            @RequestBody AgregarItemRespuestaDTO itemRequest) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.agregarItemAlCarrito(clienteId, itemRequest);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Obtiene el carrito del cliente por su ID
    @GetMapping("/{clienteId}")
    public ResponseEntity<CarritoRespuestaDTO> getCarrito(@PathVariable Long clienteId) {
        CarritoRespuestaDTO carrito = carritoService.obtenerCarritoPorCliente(clienteId);
        return new ResponseEntity<>(carrito, HttpStatus.OK);
    }
}
