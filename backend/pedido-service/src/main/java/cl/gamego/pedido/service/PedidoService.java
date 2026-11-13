package cl.gamego.pedido.service;

import cl.gamego.pedido.dto.*;
import cl.gamego.pedido.exception.ConflictException;
import cl.gamego.pedido.exception.ResourceNotFoundException;
import cl.gamego.pedido.model.ItemPedido;
import cl.gamego.pedido.model.Pedido;
import cl.gamego.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.cliente.url:http://CLIENTE-SERVICE}")
    private String clienteServiceUrl;

    @Value("${services.producto.url:http://PRODUCTO-SERVICE}")
    private String productoServiceUrl;

    public List<Pedido> listar() { return repository.findAll(); }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe pedido con ID " + id));
    }

    public Pedido crear(PedidoRequest request) {
        WebClient cliente = webClientBuilder.build();

        ClienteResponse comprador = cliente.get()
                .uri(clienteServiceUrl + "/api/clientes/{id}", request.getClienteId())
                .retrieve().bodyToMono(ClienteResponse.class).block();

        // Si el mismo juego viene en dos líneas, se suman las cantidades
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemPedidoRequest item : request.getItems()) {
            cantidades.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        // 1) Se consultan todos los productos y se valida el stock ANTES de descontar nada
        List<ItemPedido> items = new ArrayList<>();
        int total = 0;
        for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
            ProductoResponse producto = cliente.get()
                    .uri(productoServiceUrl + "/api/productos/{id}", linea.getKey())
                    .retrieve().bodyToMono(ProductoResponse.class).block();
            if (producto.stock() < linea.getValue()) {
                throw new ConflictException("Stock insuficiente de " + producto.nombre()
                        + ": quedan " + producto.stock() + " y se pidieron " + linea.getValue());
            }
            items.add(ItemPedido.builder()
                    .productoId(producto.id())
                    .nombreProducto(producto.nombre())
                    .cantidad(linea.getValue())
                    .precioUnitario(producto.precio())
                    .build());
            total += producto.precio() * linea.getValue();
        }

        // 2) Recién ahora se descuenta el stock de cada producto
        for (ItemPedido item : items) {
            cliente.post()
                    .uri(productoServiceUrl + "/api/productos/{id}/descontar-stock", item.getProductoId())
                    .bodyValue(Map.of("cantidad", item.getCantidad()))
                    .retrieve().toBodilessEntity().block();
        }

        Pedido pedido = Pedido.builder()
                .clienteId(comprador.id())
                .total(total)
                .estado("CREADO")
                .fechaCreacion(LocalDateTime.now())
                .items(items)
                .build();
        log.info("Pedido creado para {} con {} producto(s), total {}", comprador.nombre(), items.size(), total);
        return repository.save(pedido);
    }

    public Pedido cambiarEstado(Long id, String estado) {
        Pedido pedido = buscarPorId(id);
        pedido.setEstado(estado.trim().toUpperCase());
        log.info("Pedido {} pasa a estado {}", id, pedido.getEstado());
        return repository.save(pedido);
    }

    public void eliminar(Long id) { repository.delete(buscarPorId(id)); }
}
