import { useState } from "react";
import Modal from "react-bootstrap/Modal";
import CampoFormulario from "./CampoFormulario.jsx";
import { moneda } from "../utils/formato.js";
import { errorComuna, errorDireccion } from "../utils/validacionesCore.js";

const VACIO = { direccion: "", comuna: "" };
const METODOS = [
    { valor: "TARJETA_CREDITO", texto: "Tarjeta de crédito" },
    { valor: "TARJETA_DEBITO", texto: "Tarjeta de débito" },
    { valor: "TRANSFERENCIA", texto: "Transferencia" }
];

// Datos que pide el backend para despachar y pagar el pedido.
// No guarda nada del carrito: solo valida y entrega los datos con onConfirmar.
export default function ModalCompra({ show, onHide, total, enviando, error, onConfirmar }) {
    const [valores, setValores] = useState(VACIO);
    const [errores, setErrores] = useState(VACIO);
    const [metodo, setMetodo] = useState(METODOS[0].valor);

    const cambiar = (campo) => (valor) => setValores((previo) => ({ ...previo, [campo]: valor }));

    function enviar(e) {
        e.preventDefault();
        const nuevosErrores = {
            direccion: errorDireccion(valores.direccion),
            comuna: errorComuna(valores.comuna)
        };
        setErrores(nuevosErrores);
        if (Object.values(nuevosErrores).some((m) => m !== "")) return;
        onConfirmar({ direccion: valores.direccion.trim(), comuna: valores.comuna.trim(), metodo });
    }

    return (
        <Modal show={show} onHide={enviando ? undefined : onHide} centered backdrop={enviando ? "static" : true}>
            <Modal.Header closeButton={!enviando} closeLabel="Cerrar">
                <Modal.Title as="h2" className="h5 text-white">Finalizar compra</Modal.Title>
            </Modal.Header>
            <form id="formularioCompra" noValidate onSubmit={enviar}>
                <Modal.Body>
                    <p className="mb-3">Total a pagar: <strong className="text-white">{moneda.format(total)}</strong></p>
                    <CampoFormulario
                        id="direccion"
                        idError="errorDireccion"
                        etiqueta="Dirección de despacho"
                        placeholder="Ej: Av. Providencia 1234"
                        valor={valores.direccion}
                        onChange={cambiar("direccion")}
                        error={errores.direccion}
                    />
                    <CampoFormulario
                        id="comuna"
                        idError="errorComuna"
                        etiqueta="Comuna"
                        placeholder="Ej: Providencia"
                        valor={valores.comuna}
                        onChange={cambiar("comuna")}
                        error={errores.comuna}
                    />
                    <div className="mb-3">
                        <label htmlFor="metodoPago" className="form-label">Método de pago</label>
                        <select id="metodoPago" className="form-select" value={metodo} onChange={(e) => setMetodo(e.target.value)}>
                            {METODOS.map((m) => <option key={m.valor} value={m.valor}>{m.texto}</option>)}
                        </select>
                    </div>
                    <p className="small mb-0">Simulación educativa: no se realiza ningún cobro real.</p>
                    <div aria-live="polite">
                        {enviando && <div className="alert alert-info mt-3 mb-0">Procesando tu compra… puede tardar un poco si el servidor estaba dormido.</div>}
                        {error && <div className="alert alert-danger mt-3 mb-0" role="alert">{error}</div>}
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <button type="button" className="btn btn-outline-secondary" onClick={onHide} disabled={enviando}>Cancelar</button>
                    <button type="submit" className="btn btn-gamego" disabled={enviando}>
                        {enviando ? "Procesando…" : "Confirmar compra"}
                    </button>
                </Modal.Footer>
            </form>
        </Modal>
    );
}
