package com.EcoMarket.Pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.EcoMarket.Pedido.dto.AgregarItemRespuestaDTO;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import com.EcoMarket.Pedido.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // Agrega un item al carrito del cliente
    @PostMapping("/{clienteId}/productos")
    public ResponseEntity<CarritoRespuestaDTO> agregarProductos(@PathVariable Long clienteId,
            @RequestBody AgregarItemRespuestaDTO itemRequest) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.agregarItemAlCarrito(clienteId, itemRequest);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Elimina un producto del carrito del cliente
    @DeleteMapping("/{clienteId}/productos/{productoId}")
    public ResponseEntity<CarritoRespuestaDTO> eliminarProductoDelCarrito(
            @PathVariable Long clienteId,
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        try {
            CarritoRespuestaDTO carritoActualizado = carritoService.eliminarItemDelCarrito(clienteId, productoId,
                    cantidad);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Obtiene el carrito del cliente por su ID
    @GetMapping("{clienteId}")
    public ResponseEntity<CarritoRespuestaDTO> mostrarCarrito(@PathVariable Long clienteId) {
        CarritoRespuestaDTO carrito = carritoService.obtenerCarritoPorCliente(clienteId);
        return ResponseEntity.ok(carrito);
    }

}
