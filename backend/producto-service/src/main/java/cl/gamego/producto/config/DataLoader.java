package cl.gamego.producto.config;

import cl.gamego.producto.model.Producto;
import cl.gamego.producto.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Carga el catálogo inicial de GameGo (el mismo que antes estaba fijo en el frontend).
 * H2 es en memoria: cada vez que el servicio arranca se vuelve a cargar.
 */
@Configuration
public class DataLoader {
    private static final int STOCK_INICIAL = 25;

    @Bean
    CommandLineRunner cargarDatos(ProductoRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.saveAll(List.of(
                    juego("bioshock", "Bioshock", 12000, "destacados", "bioshock.jpg", "https://www.youtube.com/watch?v=CoYorK3E4aM"),
                    juego("rayman-3", "Rayman 3", 15000, "destacados", "rayman3.png", "https://www.youtube.com/watch?v=2KNXCYYuHmc"),
                    juego("devil-may-cry-3", "Devil May Cry 3", 20000, "destacados", "dmc3.jpg", "https://www.youtube.com/watch?v=KmeMu8qb_t4"),
                    juego("sonic-adventure-2", "Sonic Adventure 2", 12000, "otros", "sonic-adventure-2.jpg", "https://www.youtube.com/watch?v=xmbnZa9H6go"),
                    juego("god-of-war", "God of War", 30000, "otros", "god-of-war.webp", "https://www.youtube.com/watch?v=jZkX3LJDeYQ"),
                    juego("resident-evil-2", "Resident Evil 2", 14000, "otros", "resident-evil-2.png", "https://www.youtube.com/watch?v=kUjLG8ZxuHQ"),
                    juego("tony-hawk-s-pro-skater", "Tony Hawk's Pro Skater", 21000, "otros", "tony-hawks-pro-skater.jpg", "https://www.youtube.com/watch?v=w3okqIHJaxA"),
                    juego("mario-luigi-superstar-saga", "Mario & Luigi Superstar Saga", 17000, "otros", "mario-luigi-superstar-saga.jpg", "https://www.youtube.com/watch?v=d-jkNir4-wg"),
                    juego("donkey-kong-country", "Donkey Kong Country", 19000, "otros", "donkey-kong-country.jpg", "https://www.youtube.com/watch?v=g8PJ7oiskR0")
            ));
        };
    }

    private Producto juego(String codigo, String nombre, int precio, String seccion, String archivoImagen, String trailer) {
        return Producto.builder()
                .codigo(codigo)
                .nombre(nombre)
                .precio(precio)
                .seccion(seccion)
                .imagen("imagenes/" + archivoImagen)
                .trailer(trailer)
                .stock(STOCK_INICIAL)
                .build();
    }
}
