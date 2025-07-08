package com.EcoMarket.Pedido.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.EcoMarket.Pedido.controller.CarritoController;
import com.EcoMarket.Pedido.dto.CarritoRespuestaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CarritoModelAssembler implements RepresentationModelAssembler<CarritoRespuestaDTO, EntityModel<CarritoRespuestaDTO>> {

    @Override
    public EntityModel<CarritoRespuestaDTO> toModel(CarritoRespuestaDTO carritoDTO) {
        return EntityModel.of(carritoDTO,
                linkTo(methodOn(CarritoController.class).mostrarCarrito(carritoDTO.getClienteId())).withSelfRel());
    }
}