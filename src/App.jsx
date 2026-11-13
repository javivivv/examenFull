import { useState } from "react";
import ToastContainer from "react-bootstrap/ToastContainer";
import BarraNavegacion from "./componentes/BarraNavegacion.jsx";
import Cabecera from "./componentes/Cabecera.jsx";
import PanelCarrito from "./componentes/PanelCarrito.jsx";
import PieDePagina from "./componentes/PieDePagina.jsx";
import SeccionContacto from "./componentes/SeccionContacto.jsx";
import SeccionProductos from "./componentes/SeccionProductos.jsx";
import SeccionSugerencias from "./componentes/SeccionSugerencias.jsx";
import ModalCompra from "./componentes/ModalCompra.jsx";
import ToastAviso from "./componentes/ToastAviso.jsx";
import { realizarCompra } from "./api/cliente.js";
import { useCarrito } from "./hooks/useCarrito.js";
import { useProductos } from "./hooks/useProductos.js";
import { useSesion } from "./hooks/useSesion.js";
import { moneda } from "./utils/formato.js";

// "2026-11-13" -> "13-11-2026" (sin pasar por Date para evitar desfases de zona horaria)
function formatearFecha(iso) {
    const [anio, mes, dia] = String(iso).split("-");
    return anio && mes && dia ? `${dia}-${mes}-${anio}` : String(iso);
}

// Página principal (index.html). Aquí vive el estado que comparten varios componentes.
export default function App() {
    // El catálogo viene de producto-service; el carrito trabaja sobre ese catálogo
    const { productos, estado: estadoServidor, recargar: recargarProductos } = useProductos();
    const carrito = useCarrito(productos);
    const { cliente, sesionIniciada, cerrarSesion } = useSesion();

    const [compraAbierta, setCompraAbierta] = useState(false);
    const [comprando, setComprando] = useState(false);
    const [errorCompra, setErrorCompra] = useState(null);

    const [panelAbierto, setPanelAbierto] = useState(false);
    const [aviso, setAviso] = useState(null); // mensaje dentro del panel del carrito
    // Los dos toasts existen siempre (ocultos hasta que se necesitan), igual que en el HTML original
    const [toastCarrito, setToastCarrito] = useState({ id: 0, texto: "", tipo: "success", visible: false });
    const [toastSesion, setToastSesion] = useState({ id: 0, visible: false });

    // Cambiar el id hace que el toast reinicie su temporizador si se muestra otra vez
    function mostrarToastCarrito(texto, tipo) {
        setToastCarrito({ id: Date.now(), texto, tipo, visible: true });
    }

    function agregarProducto(id) {
        setAviso(null);
        const { agregado, producto, limite } = carrito.agregar(id);
        if (!producto) return;
        if (agregado) {
            mostrarToastCarrito(`${producto.nombre} se agregó al carrito.`, "success");
        } else if (limite === 0) {
            mostrarToastCarrito(`${producto.nombre} está sin stock.`, "warning");
        } else {
            mostrarToastCarrito(`Máximo ${limite} unidades de ${producto.nombre}.`, "warning");
        }
    }

    function vaciarCarrito() {
        carrito.vaciar();
        setAviso(null);
    }

    function finalizarCompra() {
        if (carrito.unidades === 0) return;

        // Solo se puede comprar con sesión iniciada
        if (!sesionIniciada) {
            setAviso({
                tipo: "warning",
                texto: "Inicia sesión para finalizar tu compra. ",
                enlace: { texto: "Ir a iniciar sesión", href: "logIn.html" }
            });
            return;
        }

        // La compra necesita el catálogo del servidor (ids reales de producto)
        if (estadoServidor !== "conectado") {
            setAviso({
                tipo: "warning",
                texto: estadoServidor === "cargando"
                    ? "Aún estamos conectando con el servidor. Espera unos segundos e inténtalo de nuevo."
                    : "No hay conexión con el servidor, por ahora no se puede finalizar la compra."
            });
            return;
        }

        setAviso(null);
        setErrorCompra(null);
        setPanelAbierto(false); // el modal y el panel lateral no se llevan bien con el foco
        setCompraAbierta(true);
    }

    async function confirmarCompra({ direccion, comuna, metodo }) {
        setComprando(true);
        setErrorCompra(null);
        try {
            const { pedido, despacho } = await realizarCompra({
                clienteId: cliente.id,
                items: carrito.items,
                direccion,
                comuna,
                metodo
            });
            carrito.vaciar();
            setCompraAbierta(false);
            setAviso({
                tipo: "success",
                texto: `¡Gracias por tu compra, ${cliente.nombre}! Pedido #${pedido.id} por ${moneda.format(pedido.total)}. `
                    + `Despacho programado para el ${formatearFecha(despacho.fechaProgramada)}.`
            });
            setPanelAbierto(true); // se vuelve a abrir el carrito para mostrar el resultado
            recargarProductos(); // el stock cambió
        } catch (error) {
            if (error.estado === 404) {
                // Los datos viven en memoria: si el servidor se reinició, la cuenta ya no existe
                cerrarSesion();
                setErrorCompra("Tu cuenta ya no existe en el servidor (se reinició). Cierra este cuadro e inicia sesión de nuevo.");
            } else {
                setErrorCompra(error.message);
            }
        } finally {
            setComprando(false);
        }
    }

    function cerrarSesionConAviso() {
        cerrarSesion();
        setToastSesion({ id: Date.now(), visible: true });
    }

    return (
        <>
            <Cabecera id="inicio" />

            <BarraNavegacion
                unidades={carrito.unidades}
                onAbrirCarrito={() => setPanelAbierto(true)}
                sesionIniciada={sesionIniciada}
                onCerrarSesion={cerrarSesionConAviso}
            />

            <main className="container py-4">
                {estadoServidor === "cargando" && (
                    <div className="alert alert-info" role="status">
                        Conectando con el servidor… la primera visita puede tardar hasta un minuto mientras despierta.
                    </div>
                )}
                {estadoServidor === "sinConexion" && (
                    <div className="alert alert-warning" role="alert">
                        No se pudo conectar con el servidor. Puedes ver el catálogo, pero no iniciar sesión ni comprar por ahora.{" "}
                        <button type="button" className="btn btn-link alert-link p-0 align-baseline" onClick={recargarProductos}>
                            Reintentar
                        </button>
                    </div>
                )}
                <SeccionProductos
                    id="productos"
                    titulo="Productos Destacados"
                    productos={productos.filter((p) => p.seccion === "destacados")}
                    onAgregar={agregarProducto}
                />
                <SeccionProductos
                    id="otros-productos"
                    titulo="Otros productos"
                    productos={productos.filter((p) => p.seccion === "otros")}
                    onAgregar={agregarProducto}
                />
                <SeccionSugerencias />
                <SeccionContacto />
            </main>

            <PieDePagina />

            <PanelCarrito
                show={panelAbierto}
                onHide={() => setPanelAbierto(false)}
                items={carrito.items}
                total={carrito.total}
                aviso={aviso}
                onSumar={(id) => carrito.cambiarCantidad(id, 1)}
                onRestar={(id) => carrito.cambiarCantidad(id, -1)}
                onQuitar={carrito.quitar}
                onFinalizar={finalizarCompra}
                onVaciar={vaciarCarrito}
            />

            <ModalCompra
                show={compraAbierta}
                onHide={() => { setCompraAbierta(false); setPanelAbierto(true); }}
                total={carrito.total}
                enviando={comprando}
                error={errorCompra}
                onConfirmar={confirmarCompra}
            />

            <ToastContainer position="bottom-start" containerPosition="fixed" className="p-3">
                <ToastAviso
                    key={`carrito-${toastCarrito.id}`}
                    show={toastCarrito.visible}
                    texto={toastCarrito.texto}
                    tipo={toastCarrito.tipo}
                    delay={2200}
                    onClose={() => setToastCarrito((t) => ({ ...t, visible: false }))}
                />
                <ToastAviso
                    key={`sesion-${toastSesion.id}`}
                    show={toastSesion.visible}
                    texto="Cerraste sesión correctamente."
                    tipo="secondary"
                    delay={2500}
                    onClose={() => setToastSesion((t) => ({ ...t, visible: false }))}
                />
            </ToastContainer>
        </>
    );
}
