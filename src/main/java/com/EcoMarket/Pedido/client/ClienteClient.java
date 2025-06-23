package com.EcoMarket.Pedido.client;

import com.EcoMarket.Pedido.dto.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cliente-service", url = "${service.clientes.url}")
public interface ClienteClient {

    @GetMapping("/{id}")
    ClienteDTO getClienteById(@PathVariable("id") Long id);
}