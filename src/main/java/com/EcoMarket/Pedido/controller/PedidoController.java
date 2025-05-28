package com.EcoMarket.Pedido.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<PedidoRespuestaDTO> getPedidoById(@PathVariable Long id) {
        try {
            PedidoRespuestaDTO pedidoDTO = pedidoService.obtenerPedidoConDetalles(id);
            return new ResponseEntity<>(pedidoDTO, HttpStatus.OK);
        } catch (Exception e) {
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
}
