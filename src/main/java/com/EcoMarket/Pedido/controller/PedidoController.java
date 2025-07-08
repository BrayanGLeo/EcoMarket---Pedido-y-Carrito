package com.EcoMarket.Pedido.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.service.PedidoService;
import com.EcoMarket.Pedido.assemblers.PedidoModelAssembler;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;

import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoModelAssembler pedidoModelAssembler;

    // Crea un nuevo pedido
    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> crearPedido(@RequestBody Pedido pedido) {
        Pedido nuevo = pedidoService.guardarPedido(pedido);
        if (nuevo == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(EntityModel.of(nuevo,
                linkTo(methodOn(PedidoController.class).pedidoPorId(nuevo.getId())).withSelfRel()), HttpStatus.CREATED);
    }

    // Obtiene un pedido por ID con detalles
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PedidoRespuestaDTO>> pedidoPorId(@PathVariable Long id) {
        try {
            PedidoRespuestaDTO pedidoDTO = pedidoService.obtenerPedidoConDetalles(id);
            return ResponseEntity.ok(pedidoModelAssembler.toModel(pedidoDTO));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtiene todos los pedidos
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> obtenerTodosLosPedidos() {
        List<Pedido> pedidos = pedidoService.listarTodosPedidos();
        if (pedidos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<EntityModel<Pedido>> pedidoModels = pedidos.stream()
                .map(pedido -> EntityModel.of(pedido,
                        linkTo(methodOn(PedidoController.class).pedidoPorId(pedido.getId())).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Pedido>> collectionModel = CollectionModel.of(pedidoModels,
                linkTo(methodOn(PedidoController.class).obtenerTodosLosPedidos()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    // Elimina un pedido por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        if (!pedidoService.eliminarPedido(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
