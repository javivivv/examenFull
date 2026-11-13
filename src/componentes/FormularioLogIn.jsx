import { useEffect, useRef, useState } from "react";
import CampoFormulario from "./CampoFormulario.jsx";
import MensajeFinal from "./MensajeFinal.jsx";
import { iniciarSesionApi, registrarCliente } from "../api/cliente.js";
import { errorContraseña, errorCorreo, errorNombre } from "../utils/validacionesCore.js";
import { iniciarSesion } from "../utils/sesion.js";

const VACIO = { nombre: "", correo: "", contraseña: "" };

// Un solo formulario con dos modos: iniciar sesión o crear cuenta.
// Las credenciales las valida cliente-service (la contraseña se guarda cifrada).
export default function FormularioLogIn() {
    const [modo, setModo] = useState("login"); // "login" | "registro"
    const [valores, setValores] = useState(VACIO);
    const [errores, setErrores] = useState(VACIO);
    const [mensaje, setMensaje] = useState(null);
    const [enviando, setEnviando] = useState(false);
    const redireccion = useRef(null);

    const esRegistro = modo === "registro";

    // Si el componente se desmonta antes de los 2 segundos, se cancela la redirección
    useEffect(() => () => clearTimeout(redireccion.current), []);

    const cambiar = (campo) => (valor) => setValores((previo) => ({ ...previo, [campo]: valor }));

    function cambiarModo() {
        setModo(esRegistro ? "login" : "registro");
        setErrores(VACIO);
        setMensaje(null);
    }

    async function enviar(e) {
        e.preventDefault();
        if (enviando) return;

        const nuevosErrores = {
            nombre: esRegistro ? errorNombre(valores.nombre) : "",
            correo: errorCorreo(valores.correo),
            contraseña: errorContraseña(valores.contraseña)
        };
        const formularioValido = Object.values(nuevosErrores).every((error) => error === "");

        setErrores(nuevosErrores);
        if (!formularioValido) {
            setMensaje({ tipo: "danger", texto: "Revisa los campos marcados en rojo antes de enviar." });
            return;
        }

        setEnviando(true);
        setMensaje({ tipo: "info", texto: "Conectando con el servidor… la primera vez puede tardar un poco." });
        try {
            const credenciales = { email: valores.correo.trim(), password: valores.contraseña.trim() };
            const cliente = esRegistro
                ? await registrarCliente({ ...credenciales, nombre: valores.nombre.trim() })
                : await iniciarSesionApi(credenciales);

            iniciarSesion(cliente);
            setMensaje({
                tipo: "success",
                texto: esRegistro ? `¡Cuenta creada! Bienvenido, ${cliente.nombre}.` : `¡Bienvenido, ${cliente.nombre}!`
            });
            setValores(VACIO);
            redireccion.current = setTimeout(() => {
                window.location.href = "index.html";
            }, 2000);
        } catch (error) {
            setMensaje({ tipo: "danger", texto: error.message });
            setEnviando(false);
        }
    }

    return (
        <form id="formularioLogIn" className="card card-body p-4" noValidate onSubmit={enviar}>
            {esRegistro && (
                <CampoFormulario
                    id="nombre"
                    idError="errorNombre"
                    etiqueta="Nombre"
                    placeholder="Ej: Carolina"
                    valor={valores.nombre}
                    onChange={cambiar("nombre")}
                    error={errores.nombre}
                />
            )}
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
                id="contraseña"
                idError="errorContraseña"
                etiqueta="Contraseña"
                tipo="password"
                placeholder="Ingrese su contraseña"
                valor={valores.contraseña}
                onChange={cambiar("contraseña")}
                error={errores.contraseña}
            />
            <button type="submit" className="btn btn-gamego" disabled={enviando}>
                {enviando ? "Un momento…" : esRegistro ? "Crear cuenta" : "Iniciar sesión"}
            </button>
            <button type="button" className="btn btn-link mt-2" onClick={cambiarModo} disabled={enviando}>
                {esRegistro ? "¿Ya tienes cuenta? Inicia sesión" : "¿No tienes cuenta? Regístrate"}
            </button>

            <MensajeFinal mensaje={mensaje} />
        </form>
    );
}
