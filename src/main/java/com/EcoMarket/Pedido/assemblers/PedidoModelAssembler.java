package com.EcoMarket.Pedido.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.EcoMarket.Pedido.controller.PedidoController;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PedidoModelAssembler
        implements RepresentationModelAssembler<PedidoRespuestaDTO, EntityModel<PedidoRespuestaDTO>> {

    @Override
    public EntityModel<PedidoRespuestaDTO> toModel(PedidoRespuestaDTO pedidoDTO) {
        return EntityModel.of(pedidoDTO,
                linkTo(methodOn(PedidoController.class).pedidoPorId(pedidoDTO.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).obtenerTodosLosPedidos()).withRel("pedidos"));
    }
}