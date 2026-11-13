package cl.gamego.pedido.controller;

import cl.gamego.pedido.dto.EstadoRequest;
import cl.gamego.pedido.dto.PedidoRequest;
import cl.gamego.pedido.model.Pedido;
import cl.gamego.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @GetMapping({"", "/"})
    public CollectionModel<EntityModel<Pedido>> listar() {
        List<EntityModel<Pedido>> recursos = service.listar().stream().map(this::toModel).toList();
        return CollectionModel.of(recursos, linkTo(methodOn(PedidoController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<Pedido> buscar(@PathVariable Long id) {
        return toModel(service.buscarPorId(id));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<EntityModel<Pedido>> crear(@Valid @RequestBody PedidoRequest request) {
        Pedido creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(creado));
    }

    @PatchMapping("/{id}/estado")
    public EntityModel<Pedido> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return toModel(service.cambiarEstado(id, request.getEstado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Pedido> toModel(Pedido entidad) {
        return EntityModel.of(entidad,
                linkTo(methodOn(PedidoController.class).buscar(entidad.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listar()).withRel("todos"));
    }
}
