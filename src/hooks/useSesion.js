import { useEffect, useState } from "react";
import { CLAVE_SESION, cerrarSesion as borrarSesion, leerCliente } from "../utils/sesion.js";

// Entrega el cliente con sesión iniciada (o null) y una función para cerrarla.
// Si la sesión cambia en otra pestaña, esta también se entera.
export function useSesion() {
    const [cliente, setCliente] = useState(leerCliente);

    useEffect(() => {
        function alCambiarAlmacenamiento(e) {
            if (e.key === CLAVE_SESION) setCliente(leerCliente());
        }
        window.addEventListener("storage", alCambiarAlmacenamiento);
        return () => window.removeEventListener("storage", alCambiarAlmacenamiento);
    }, []);

    function cerrarSesion() {
        borrarSesion();
        setCliente(null);
    }

    return { cliente, sesionIniciada: cliente !== null, cerrarSesion };
}
