package com.EcoMarket.Pedido.client;

import com.EcoMarket.Pedido.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "catalogodb", url = "jdbc:mysql://localhost:3306/catalogodb?serverTimezone=UTC")
public interface ProductoClient {

    @GetMapping("/api/productos/id")
    List<ProductoDTO> findProductosByIds(@RequestParam("id") List<Long> Producto);

    Object getForObject(String anyString, Class<ProductoDTO> eq);
}