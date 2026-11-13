package cl.gamego.cliente.controller;

import cl.gamego.cliente.dto.ContactoRequest;
import cl.gamego.cliente.dto.LoginRequest;
import cl.gamego.cliente.dto.RegistroRequest;
import cl.gamego.cliente.model.Cliente;
import cl.gamego.cliente.model.MensajeContacto;
import cl.gamego.cliente.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;

    @GetMapping({"", "/"})
    public CollectionModel<EntityModel<Cliente>> listar() {
        List<EntityModel<Cliente>> recursos = service.listar().stream().map(this::toModel).toList();
        return CollectionModel.of(recursos, linkTo(methodOn(ClienteController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<Cliente> buscar(@PathVariable Long id) {
        return toModel(service.buscarPorId(id));
    }

    @PostMapping("/registro")
    public ResponseEntity<EntityModel<Cliente>> registrar(@Valid @RequestBody RegistroRequest request) {
        Cliente creado = service.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(creado));
    }

    @PostMapping("/login")
    public EntityModel<Cliente> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return toModel(service.iniciarSesion(request));
    }

    @PostMapping("/contacto")
    public ResponseEntity<MensajeContacto> contacto(@Valid @RequestBody ContactoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarContacto(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Cliente> toModel(Cliente entidad) {
        return EntityModel.of(entidad,
                linkTo(methodOn(ClienteController.class).buscar(entidad.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).listar()).withRel("todos"));
    }
}
