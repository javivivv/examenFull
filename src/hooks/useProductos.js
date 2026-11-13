import { useCallback, useEffect, useState } from "react";
import { despertarServicios, listarProductos } from "../api/cliente.js";
import { productos as productosLocales } from "../data/productos.js";

// Carga el catálogo desde producto-service.
// Empieza mostrando el catálogo local (la página nunca queda vacía) y lo reemplaza
// cuando el servidor responde.
//   estado: "cargando" | "conectado" | "sinConexion"
export function useProductos() {
    const [productos, setProductos] = useState(productosLocales);
    const [estado, setEstado] = useState("cargando");

    const recargar = useCallback(async () => {
        try {
            const desdeApi = await listarProductos();
            if (desdeApi.length === 0) throw new Error("catálogo vacío");
            setProductos(desdeApi);
            setEstado("conectado");
        } catch {
            setEstado("sinConexion");
        }
    }, []);

    useEffect(() => {
        despertarServicios();
        recargar();
    }, [recargar]);

    return { productos, estado, recargar };
}
