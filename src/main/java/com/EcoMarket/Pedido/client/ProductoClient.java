package com.EcoMarket.Pedido.client;

import com.EcoMarket.Pedido.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "producto-service", url = "${service.productos.url}")
public interface ProductoClient {

    @GetMapping("/api/productos/by-ids")
    List<ProductoDTO> findProductosByIds(@RequestParam("ids") List<Long> ids);
}