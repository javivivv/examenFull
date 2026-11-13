// Cliente HTTP de GameGo. Toda la comunicación con el backend pasa por el API Gateway.
import { rutaImagen } from "../utils/imagenes.js";

// En desarrollo apunta al gateway local; en Render se define VITE_API_URL al compilar.
export const API_URL = (import.meta.env.VITE_API_URL || "http://localhost:8080").replace(/\/+$/, "");

// Los servicios gratuitos de Render tardan hasta ~1 minuto en despertar tras estar inactivos.
const TIEMPO_MAXIMO_MS = 90000;

export class ErrorApi extends Error {
    constructor(mensaje, estado = 0) {
        super(mensaje);
        this.name = "ErrorApi";
        this.estado = estado;
    }
}

async function solicitar(ruta, { metodo = "GET", cuerpo } = {}) {
    const control = new AbortController();
    const temporizador = setTimeout(() => control.abort(), TIEMPO_MAXIMO_MS);

    let respuesta;
    try {
        respuesta = await fetch(`${API_URL}${ruta}`, {
            method: metodo,
            headers: cuerpo ? { "Content-Type": "application/json", Accept: "application/json" } : { Accept: "application/json" },
            body: cuerpo ? JSON.stringify(cuerpo) : undefined,
            signal: control.signal
        });
    } catch {
        throw new ErrorApi("No se pudo conectar con el servidor. Inténtalo de nuevo en unos segundos.");
    } finally {
        clearTimeout(temporizador);
    }

    let datos = null;
    try {
        datos = await respuesta.json();
    } catch {
        // respuesta sin cuerpo JSON
    }

    if (!respuesta.ok) {
        // Los servicios responden { estado, error, mensajes: [...] }
        const detalle = Array.isArray(datos?.mensajes) && datos.mensajes.length > 0
            ? datos.mensajes.join(" ")
            : `El servidor respondió con error ${respuesta.status}.`;
        throw new ErrorApi(detalle, respuesta.status);
    }
    return datos;
}

// Spring HATEOAS entrega las listas como { _embedded: { productoList: [...] } }
function extraerLista(datos) {
    if (Array.isArray(datos)) return datos;
    const incrustado = datos?._embedded ? Object.values(datos._embedded)[0] : null;
    return Array.isArray(incrustado) ? incrustado : [];
}

// ---------- Productos ----------

// Convierte un producto de la API a la forma que usa la tienda.
// "id" es el código legible (clave del carrito) y "backendId" el id numérico del servidor.
function aProductoTienda(p) {
    return {
        id: p.codigo,
        backendId: p.id,
        nombre: p.nombre,
        precio: p.precio,
        imagen: rutaImagen(p.imagen),
        trailer: p.trailer,
        seccion: p.seccion,
        stock: p.stock
    };
}

export async function listarProductos() {
    const datos = await solicitar("/api/productos/");
    return extraerLista(datos).map(aProductoTienda);
}

// ---------- Clientes ----------

const aCliente = (c) => ({ id: c.id, nombre: c.nombre, email: c.email });

export async function registrarCliente({ nombre, email, password }) {
    const c = await solicitar("/api/clientes/registro", { metodo: "POST", cuerpo: { nombre, email, password } });
    return aCliente(c);
}

export async function iniciarSesionApi({ email, password }) {
    const c = await solicitar("/api/clientes/login", { metodo: "POST", cuerpo: { email, password } });
    return aCliente(c);
}

export function enviarContacto({ nombre, email, comentario }) {
    return solicitar("/api/clientes/contacto", { metodo: "POST", cuerpo: { nombre, email, comentario } });
}

// ---------- Compra: pedido -> pago -> despacho ----------

export async function realizarCompra({ clienteId, items, direccion, comuna, metodo }) {
    const pedido = await solicitar("/api/pedidos/", {
        metodo: "POST",
        cuerpo: {
            clienteId,
            items: items.map(({ producto, cantidad }) => ({ productoId: producto.backendId, cantidad }))
        }
    });

    let pago;
    try {
        pago = await solicitar("/api/pagos/", { metodo: "POST", cuerpo: { pedidoId: pedido.id, metodo } });
    } catch (e) {
        throw new ErrorApi(`Se creó el pedido #${pedido.id}, pero el pago falló: ${e.message}`, e.estado);
    }

    let despacho;
    try {
        despacho = await solicitar("/api/despachos/", {
            metodo: "POST",
            cuerpo: { pedidoId: pedido.id, direccion, comuna }
        });
    } catch (e) {
        throw new ErrorApi(`El pedido #${pedido.id} fue pagado, pero no se pudo programar el despacho: ${e.message}`, e.estado);
    }

    return { pedido, pago, despacho };
}

// ---------- Despertar servicios ----------

// En Render Free cada microservicio se duerme por separado. Estas consultas baratas (devuelven 404)
// los despiertan en segundo plano para que la compra no sea lenta. No devuelven datos.
export function despertarServicios() {
    ["clientes", "pedidos", "pagos", "despachos"].forEach((recurso) => {
        fetch(`${API_URL}/api/${recurso}/0`, { headers: { Accept: "application/json" } }).catch(() => {});
    });
}
