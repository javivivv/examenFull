package cl.gamego.producto.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Identificador legible usado por el frontend (ej: "god-of-war"). */
    @Column(unique = true)
    private String codigo;
    private String nombre;
    /** "destacados" u "otros": sección de la tienda donde se muestra. */
    private String seccion;
    /** Ruta relativa de la imagen dentro del frontend (ej: "imagenes/bioshock.jpg"). */
    private String imagen;
    private String trailer;
    private Integer stock;
    private Integer precio;
}
