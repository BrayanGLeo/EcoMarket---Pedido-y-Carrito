package com.EcoMarket.Pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.EcoMarket.Pedido.dto.AgregarProductoRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;
import com.EcoMarket.Pedido.assemblers.CarritoModelAssembler;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoModelAssembler carritoModelAssembler;

    // Agrega un item al carrito del cliente
    @PostMapping("/{clienteId}/productos")
    public ResponseEntity<EntityModel<CarritoRespuestaDTO>> agregarProductos(@PathVariable Long clienteId,
            @RequestBody AgregarProductoRespuestaDTO itemRequest) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.agregarProductoAlCarrito(clienteId, itemRequest);
            return ResponseEntity.ok(carritoModelAssembler.toModel(carritoActualizado));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Elimina un producto del carrito del cliente
    @DeleteMapping("/{clienteId}/productos/{productoId}")
    public ResponseEntity<EntityModel<CarritoRespuestaDTO>> eliminarProductoDelCarrito(@PathVariable Long clienteId,
            @PathVariable Long productoId, @RequestParam(defaultValue = "1") int cantidad) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.eliminarProductoDelCarrito(clienteId, productoId,
                    cantidad);
            return ResponseEntity.ok(carritoModelAssembler.toModel(carritoActualizado));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Obtiene el carrito del cliente por su ID
    @GetMapping("{clienteId}")
    public ResponseEntity<EntityModel<CarritoRespuestaDTO>> mostrarCarrito(@PathVariable Long clienteId) {
        CarritoRespuestaDTO carrito = carritoService.obtenerCarritoPorCliente(clienteId);
        return ResponseEntity.ok(carritoModelAssembler.toModel(carrito));
    }

}
