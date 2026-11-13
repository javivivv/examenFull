import { useEffect, useMemo, useState } from "react";
import { productos as productosLocales } from "../data/productos.js";

export const MAX_UNIDADES = 10;
const CLAVE_CARRITO = "gamegoCarrito";

// Los ids del carrito son los códigos de producto, que son iguales en el catálogo local y en la API.
const idsConocidos = new Set(productosLocales.map((p) => p.id));

// Máximo de unidades que se puede pedir de un producto (nunca más que el stock, si se conoce)
const limiteDe = (producto) => Math.min(MAX_UNIDADES, producto.stock ?? MAX_UNIDADES);

function cargarCarrito() {
    try {
        const guardado = JSON.parse(localStorage.getItem(CLAVE_CARRITO) || "{}");
        const limpio = {};
        for (const [id, cantidad] of Object.entries(guardado)) {
            const n = Math.floor(Number(cantidad));
            // Se descartan productos que ya no existen o cantidades inválidas
            if (idsConocidos.has(id) && n > 0) limpio[id] = Math.min(n, MAX_UNIDADES);
        }
        return limpio;
    } catch {
        return {};
    }
}

function guardarCarrito(carrito) {
    try {
        localStorage.setItem(CLAVE_CARRITO, JSON.stringify(carrito));
    } catch {
        // sin almacenamiento disponible: el carrito sigue funcionando, pero no se guarda
    }
}

// Estado del carrito: { idProducto: cantidad }, guardado en localStorage.
// Recibe el catálogo vigente (el de la API, o el local si el servidor no responde).
export function useCarrito(productos) {
    const [carrito, setCarrito] = useState(cargarCarrito);
    const catalogo = useMemo(() => new Map(productos.map((p) => [p.id, p])), [productos]);

    // Cada cambio se guarda
    useEffect(() => {
        guardarCarrito(carrito);
    }, [carrito]);

    // Si el carrito cambia en otra pestaña, esta se actualiza
    useEffect(() => {
        function alCambiarAlmacenamiento(e) {
            if (e.key === CLAVE_CARRITO) setCarrito(cargarCarrito());
        }
        window.addEventListener("storage", alCambiarAlmacenamiento);
        return () => window.removeEventListener("storage", alCambiarAlmacenamiento);
    }, []);

    // Lista lista para mostrar: [{ producto, cantidad }]
    const items = useMemo(
        () => Object.entries(carrito)
            .filter(([id]) => catalogo.has(id))
            .map(([id, cantidad]) => ({ producto: catalogo.get(id), cantidad: Math.min(cantidad, limiteDe(catalogo.get(id))) }))
            .filter((item) => item.cantidad > 0),
        [carrito, catalogo]
    );
    const unidades = items.reduce((suma, item) => suma + item.cantidad, 0);
    const total = items.reduce((suma, item) => suma + item.producto.precio * item.cantidad, 0);

    // Devuelve { agregado, producto, limite } para que quien llama pueda avisar al usuario
    function agregar(id) {
        const producto = catalogo.get(id);
        if (!producto) return { agregado: false, producto: null, limite: 0 };
        const limite = limiteDe(producto);
        if ((carrito[id] ?? 0) >= limite) return { agregado: false, producto, limite };
        setCarrito((previo) => ({ ...previo, [id]: (previo[id] ?? 0) + 1 }));
        return { agregado: true, producto, limite };
    }

    function cambiarCantidad(id, delta) {
        setCarrito((previo) => {
            const producto = catalogo.get(id);
            const nueva = (previo[id] ?? 0) + delta;
            if (!producto || nueva < 1 || nueva > limiteDe(producto)) return previo;
            return { ...previo, [id]: nueva };
        });
    }

    function quitar(id) {
        setCarrito((previo) => {
            const { [id]: _quitado, ...resto } = previo;
            return resto;
        });
    }

    function vaciar() {
        setCarrito({});
    }

    return { items, unidades, total, agregar, cambiarCantidad, quitar, vaciar };
}
