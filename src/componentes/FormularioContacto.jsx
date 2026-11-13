import { useState } from "react";
import CampoFormulario from "./CampoFormulario.jsx";
import MensajeFinal from "./MensajeFinal.jsx";
import { enviarContacto } from "../api/cliente.js";
import { errorComentario, errorCorreo, errorNombre } from "../utils/validacionesCore.js";

const VACIO = { nombre: "", correo: "", comentario: "" };

export default function FormularioContacto() {
    const [valores, setValores] = useState(VACIO);
    const [errores, setErrores] = useState(VACIO);
    const [mensaje, setMensaje] = useState(null);
    const [enviando, setEnviando] = useState(false);

    const cambiar = (campo) => (valor) => setValores((previo) => ({ ...previo, [campo]: valor }));

    async function enviar(e) {
        e.preventDefault();
        if (enviando) return;

        const nuevosErrores = {
            nombre: errorNombre(valores.nombre),
            correo: errorCorreo(valores.correo),
            comentario: errorComentario(valores.comentario)
        };
        const formularioValido = Object.values(nuevosErrores).every((error) => error === "");

        setErrores(nuevosErrores);
        if (!formularioValido) {
            setMensaje({ tipo: "danger", texto: "Revisa los campos marcados en rojo antes de enviar." });
            return;
        }

        setEnviando(true);
        setMensaje({ tipo: "info", texto: "Enviando…" });
        try {
            await enviarContacto({
                nombre: valores.nombre.trim(),
                email: valores.correo.trim(),
                comentario: valores.comentario.trim()
            });
            setMensaje({ tipo: "success", texto: "¡Mensaje enviado correctamente! Te contactaremos pronto." });
            setValores(VACIO);
        } catch (error) {
            setMensaje({ tipo: "danger", texto: error.message });
        } finally {
            setEnviando(false);
        }
    }

    return (
        <form id="formularioContacto" className="card card-body p-4" noValidate onSubmit={enviar}>
            <CampoFormulario
                id="nombre"
                idError="errorNombre"
                etiqueta="Nombre"
                placeholder="Ej: Carolina"
                valor={valores.nombre}
                onChange={cambiar("nombre")}
                error={errores.nombre}
            />
            <CampoFormulario
                id="correo"
                idError="errorCorreo"
                etiqueta="Correo"
                placeholder="Ej: correo@gmail.com"
                valor={valores.correo}
                onChange={cambiar("correo")}
                error={errores.correo}
            />
            <CampoFormulario
                id="comentario"
                idError="errorComentario"
                etiqueta="Comentario"
                tipo="textarea"
                filas={5}
                placeholder="Escribe tu mensaje"
                valor={valores.comentario}
                onChange={cambiar("comentario")}
                error={errores.comentario}
            />
            <button type="submit" className="btn btn-gamego" disabled={enviando}>
                {enviando ? "Enviando…" : "Enviar mensaje"}
            </button>

            <MensajeFinal mensaje={mensaje} />
        </form>
    );
}
