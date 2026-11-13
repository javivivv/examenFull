package cl.gamego.producto.service;

import cl.gamego.producto.dto.ProductoRequest;
import cl.gamego.producto.exception.ConflictException;
import cl.gamego.producto.exception.ResourceNotFoundException;
import cl.gamego.producto.model.Producto;
import cl.gamego.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository repository;

    public List<Producto> listar() {
        log.info("Listando productos");
        return repository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe producto con ID " + id));
    }

    public Producto crear(ProductoRequest request) {
        if (repository.existsByCodigo(request.getCodigo())) {
            throw new ConflictException("Ya existe un producto con código " + request.getCodigo());
        }
        Producto entidad = new Producto();
        copiar(request, entidad);
        log.info("Creando producto: {}", request.getNombre());
        return repository.save(entidad);
    }

    public Producto actualizar(Long id, ProductoRequest request) {
        Producto entidad = buscarPorId(id);
        copiar(request, entidad);
        log.info("Actualizando producto ID {}", id);
        return repository.save(entidad);
    }

    public void eliminar(Long id) {
        Producto entidad = buscarPorId(id);
        repository.delete(entidad);
        log.info("Eliminado producto ID {}", id);
    }

    /** Resta unidades al stock. Lo usa pedido-service al confirmar una compra. */
    @Transactional
    public Producto descontarStock(Long id, int cantidad) {
        Producto entidad = buscarPorId(id);
        if (entidad.getStock() < cantidad) {
            throw new ConflictException("Stock insuficiente de " + entidad.getNombre()
                    + ": quedan " + entidad.getStock() + " y se pidieron " + cantidad);
        }
        entidad.setStock(entidad.getStock() - cantidad);
        log.info("Stock de {} descontado en {}. Quedan {}", entidad.getNombre(), cantidad, entidad.getStock());
        return repository.save(entidad);
    }

    private void copiar(ProductoRequest origen, Producto destino) {
        destino.setCodigo(origen.getCodigo());
        destino.setNombre(origen.getNombre());
        destino.setSeccion(origen.getSeccion());
        destino.setImagen(origen.getImagen());
        destino.setTrailer(origen.getTrailer());
        destino.setStock(origen.getStock());
        destino.setPrecio(origen.getPrecio());
    }
}
