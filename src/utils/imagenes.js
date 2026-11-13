// Las imágenes viven en public/imagenes/ y se publican tal cual en la raíz del sitio.
// BASE_URL permite que funcionen aunque el sitio no esté en la raíz del dominio.
export function rutaImagen(archivo) {
    return `${import.meta.env.BASE_URL}${archivo}`;
}
