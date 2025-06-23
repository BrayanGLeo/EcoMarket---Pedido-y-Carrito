package com.EcoMarket.Pedido.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
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
    private ProductoClient productoClient;

    @Autowired
    private ClienteClient clienteClient;

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

    // Guarda un nuevo pedido, valida que tenga productos y calcula el total.
    public Pedido guardarPedido(Pedido pedido) {
        if (pedido.getProductos() == null || pedido.getProductos().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto.");
        }

        double totalCalculado = 0.0;
        for (ItemPedido item : pedido.getProductos()) {
            if (item.getPrecioUnitario() == null || item.getPrecioUnitario() < 0) {
                throw new IllegalArgumentException(
                        "Cada item del pedido debe tener un precio unitario debe ser mayor 0.");
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
        ClienteDTO clienteDTO = clienteClient.getClienteById(clienteId);

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
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<Long> productoIds = items.stream()
                .map(ItemPedido::getProductoId)
                .collect(Collectors.toList());

        List<ProductoDTO> productosDesdeApi = productoClient.findProductosByIds(productoIds);

        Map<Long, ProductoDTO> mapaProductos = productosDesdeApi.stream()
                .collect(Collectors.toMap(ProductoDTO::getId, producto -> producto));

        return items.stream().map(item -> {
            ProductoDTO productoDTO = mapaProductos.get(item.getProductoId());

            ProductoDTO productoParaItem = new ProductoDTO();
            if (productoDTO != null) {
                productoParaItem.setId(productoDTO.getId());
                productoParaItem.setNombre(productoDTO.getNombre());
            }
            productoParaItem.setPrecio(item.getPrecioUnitario());

            ItemPedidoDTO itemDTO = new ItemPedidoDTO();
            itemDTO.setProducto(productoParaItem);
            itemDTO.setCantidad(item.getCantidad());
            itemDTO.setTotalItem(item.getPrecioUnitario() * item.getCantidad());
            return itemDTO;
        }).collect(Collectors.toList());
    }

    // Construye la respuesta final con todos los detalles del pedido
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
