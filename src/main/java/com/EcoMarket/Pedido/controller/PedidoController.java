package com.EcoMarket.Pedido.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Crea un nuevo pedido
    @PostMapping
    public ResponseEntity<Pedido> postPedido(@RequestBody Pedido pedido) {
        Pedido nuevo = pedidoService.guardar(pedido);
        if (nuevo == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // Obtiene un pedido por ID con detalles
    @GetMapping("/{id}")
    public ResponseEntity<PedidoRespuestaDTO> getPedidoById(@PathVariable Long id) {
        try {
            PedidoRespuestaDTO pedidoDTO = pedidoService.obtenerPedidoConDetalles(id);
            if (pedidoDTO == null) { 
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(pedidoDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.err.println("Error al obtener pedido con detalles: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtiene todos los pedidos
    @GetMapping
    public ResponseEntity<List<Pedido>> getPedidos() {
        List<Pedido> pedidos = pedidoService.listarTodos();
        if (pedidos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    // Elimina un pedido por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        boolean eliminado = pedidoService.eliminar(id); 
        if (!eliminado) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); 
    }
}

