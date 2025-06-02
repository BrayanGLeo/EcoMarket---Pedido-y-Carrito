package com.EcoMarket.Pedido.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.EcoMarket.Pedido.dto.ClienteDTO;
import com.EcoMarket.Pedido.dto.ItemPedidoDTO;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.dto.PedidoResumidoDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.ItemPedido;
import com.EcoMarket.Pedido.model.Pedido;
import com.EcoMarket.Pedido.repository.PedidoRepository;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.clientes.url}")
    private String clienteServiceUrl;

    @Value("${service.productos.url}")
    private String productoServiceUrl;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public boolean eliminar(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true; 
        }
        return false; 
    }

    public Pedido guardar(Pedido pedido) {
        if (pedido.getProductos() == null || pedido.getProductos().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto.");
        }

        double totalCalculado = 0.0;
        for (ItemPedido item : pedido.getProductos()) {
            if (item.getPrecioUnitario() == null || item.getPrecioUnitario() < 0) {
                throw new IllegalArgumentException("Cada item del pedido debe tener un precio unitario debe ser mayor 0.");
            }
            totalCalculado += item.getCantidad() * item.getPrecioUnitario();
        }
        pedido.setTotal(totalCalculado);
        return pedidoRepository.save(pedido);
    }

    public Pedido pedidoxId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public PedidoRespuestaDTO obtenerPedidoConDetalles(Long id) {
        Pedido pedido = this.pedidoxId(id);
        if (pedido == null) {
            throw new RuntimeException("Pedido no encontrado con id: " + id);
        }

        ClienteDTO clienteCompleto = obtenerDatosCompletosCliente(pedido.getClienteId());
        List<ItemPedidoDTO> productosCompletos = obtenerDetallesDeProductos(pedido.getProductos());

        return construirRespuestaFinal(pedido, clienteCompleto, productosCompletos);
    }

    // Obtiene los datos completos del cliente, incluyendo su historial de pedidos.
    private ClienteDTO obtenerDatosCompletosCliente(Long clienteId) {
        String urlCliente = clienteServiceUrl + "/" + clienteId;
        ClienteDTO clienteDTO = restTemplate.getForObject(urlCliente, ClienteDTO.class);

        if (clienteDTO != null) {
            List<Pedido> historial = pedidoRepository.findByClienteId(clienteId);
            List<PedidoResumidoDTO> historialDTO = historial.stream().map(p -> {
                PedidoResumidoDTO resumido = new PedidoResumidoDTO();
                resumido.setId(p.getId());
                resumido.setFecha(p.getFecha());
                resumido.setTotal(p.getTotal());
                resumido.setEstado(p.getEstado());
                return resumido;
            }).collect(Collectors.toList());
            clienteDTO.setHistorialPedidos(historialDTO);
        }
        return clienteDTO;
    }

    // Obtiene los detalles de cada producto en el pedido
    private List<ItemPedidoDTO> obtenerDetallesDeProductos(List<ItemPedido> items) {
        List<ItemPedidoDTO> itemsDTO = new ArrayList<>();
        if (items != null) {
            for (ItemPedido item : items) {
                String urlProducto = productoServiceUrl + "/api/productos/" + item.getProductoId();
                ProductoDTO productoDTO = restTemplate.getForObject(urlProducto, ProductoDTO.class);
                
                ItemPedidoDTO itemDTO = new ItemPedidoDTO();
                if (productoDTO != null) { 
                    itemDTO.setProducto(productoDTO);
                }
                itemDTO.setCantidad(item.getCantidad());
                itemDTO.setTotalItem(item.getPrecioUnitario() * item.getCantidad()); 
                itemsDTO.add(itemDTO);
            }
        }
        return itemsDTO;
    }

    // Construye la respuesta final con todos los detalles del pedido, cliente y
    // productos.
    private PedidoRespuestaDTO construirRespuestaFinal(Pedido pedido, ClienteDTO cliente,
            List<ItemPedidoDTO> productos) {
        PedidoRespuestaDTO response = new PedidoRespuestaDTO();
        response.setId(pedido.getId());
        response.setCliente(cliente);
        response.setProductos(productos);
        response.setFecha(pedido.getFecha());
        response.setTotal(pedido.getTotal());
        response.setEstado(pedido.getEstado());
        return response;
    }

}
