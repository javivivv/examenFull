package cl.gamego.pago.service;

import cl.gamego.pago.dto.*;
import cl.gamego.pago.exception.ConflictException;
import cl.gamego.pago.exception.ResourceNotFoundException;
import cl.gamego.pago.model.Pago;
import cl.gamego.pago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.pedido.url:http://PEDIDO-SERVICE}")
    private String pedidoServiceUrl;

    public List<Pago> listar() { return repository.findAll(); }
    public Pago buscarPorId(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe pago con ID " + id)); }

    public Pago crear(PagoRequest request) {
        WebClient cliente = webClientBuilder.build();
        PedidoResponse pedido = cliente.get()
                .uri(pedidoServiceUrl + "/api/pedidos/{id}", request.getPedidoId())
                .retrieve().bodyToMono(PedidoResponse.class).block();
        if ("PAGADO".equals(pedido.estado())) {
            throw new ConflictException("El pedido " + pedido.id() + " ya fue pagado");
        }
        // El monto lo define el pedido, no el cliente: así nadie puede pagar menos de lo que debe
        Pago pago = Pago.builder()
                .pedidoId(pedido.id())
                .metodo(request.getMetodo())
                .monto(pedido.total())
                .estado("APROBADO")
                .fechaPago(LocalDateTime.now())
                .build();
        Pago guardado = repository.save(pago);
        log.info("Pago de {} aprobado para pedido {}", guardado.getMonto(), pedido.id());

        // El pedido pasa a PAGADO. Si pedido-service no responde, el pago igual queda registrado.
        try {
            cliente.patch()
                    .uri(pedidoServiceUrl + "/api/pedidos/{id}/estado", pedido.id())
                    .bodyValue(Map.of("estado", "PAGADO"))
                    .retrieve().toBodilessEntity().block();
        } catch (Exception e) {
            log.warn("No se pudo marcar el pedido {} como PAGADO: {}", pedido.id(), e.getMessage());
        }
        return guardado;
    }

    public void eliminar(Long id) { repository.delete(buscarPorId(id)); }
}
