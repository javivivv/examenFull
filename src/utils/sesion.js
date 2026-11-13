// La sesión guarda en localStorage los datos básicos del cliente que inició sesión
// ({ id, nombre, email }). Las credenciales las valida cliente-service.
export const CLAVE_SESION = "gamegoCliente";

export function leerCliente() {
    try {
        const cliente = JSON.parse(localStorage.getItem(CLAVE_SESION) || "null");
        return cliente && cliente.id != null ? cliente : null;
    } catch {
        return null;
    }
}

export function leerSesion() {
    return leerCliente() !== null;
}

export function iniciarSesion(cliente) {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(cliente));
}

export function cerrarSesion() {
    localStorage.removeItem(CLAVE_SESION);
}
