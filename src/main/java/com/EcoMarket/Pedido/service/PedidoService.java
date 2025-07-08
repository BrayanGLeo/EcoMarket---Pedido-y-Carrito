package com.EcoMarket.Pedido.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EcoMarket.Pedido.client.ClienteClient;
import com.EcoMarket.Pedido.client.ProductoClient;
import com.EcoMarket.Pedido.dto.ClienteDTO;
import com.EcoMarket.Pedido.dto.ProductoPedidoDTO;
import com.EcoMarket.Pedido.dto.PedidoRespuestaDTO;
import com.EcoMarket.Pedido.dto.ProductoDTO;
import com.EcoMarket.Pedido.model.ProductoPedido;
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

    public List<Pedido> listarTodosPedidos() {
        return pedidoRepository.findAll();
    }

    public boolean eliminarPedido(Long id) {
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
        for (ProductoPedido item : pedido.getProductos()) {
            if (item.getPrecioUnitario() == null || item.getPrecioUnitario() < 0) {
                throw new IllegalArgumentException(
                        "Cada item del pedido debe tener un precio unitario debe ser mayor 0.");
            }
            totalCalculado += item.getCantidad() * item.getPrecioUnitario();
        }
        pedido.setTotal(totalCalculado);
        return pedidoRepository.save(pedido);
    }

    // Busca un pedido por ID, devuelve null si no existe.
    public Pedido buscarPedidoxId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    // Obtiene un pedido por ID y construye una respuesta con detalles del cliente y
    // productos.
    public PedidoRespuestaDTO obtenerPedidoConDetalles(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        ClienteDTO clienteInfo = clienteClient.getClienteById(pedido.getClienteId());
        List<ProductoPedidoDTO> productosConDetalles = obtenerDetallesDeProductos(pedido.getProductos());

        return construirRespuestaFinal(pedido, clienteInfo, productosConDetalles);
    }

    // Obtiene los detalles de cada producto en el pedido
    private List<ProductoPedidoDTO> obtenerDetallesDeProductos(List<ProductoPedido> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<Long> productoIds = items.stream().map(ProductoPedido::getProductoId).collect(Collectors.toList());
        List<ProductoDTO> productosDesdeApi = productoClient.findProductosByIds(productoIds);
        Map<Long, ProductoDTO> mapaProductos = productosDesdeApi.stream()
                .collect(Collectors.toMap(ProductoDTO::getId, producto -> producto));

        return items.stream().map(item -> {
            ProductoDTO infoProducto = mapaProductos.get(item.getProductoId());
            ProductoPedidoDTO itemDTO = new ProductoPedidoDTO();
            itemDTO.setProducto(infoProducto);
            itemDTO.setCantidad(item.getCantidad());
            itemDTO.setTotalItem(item.getPrecioUnitario() * item.getCantidad());
            return itemDTO;
        }).collect(Collectors.toList());
    }

    // Construye la respuesta final con todos los detalles del pedido
    private PedidoRespuestaDTO construirRespuestaFinal(Pedido pedido, ClienteDTO cliente,
            List<ProductoPedidoDTO> productos) {
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
