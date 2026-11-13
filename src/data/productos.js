import { rutaImagen } from "../utils/imagenes.js";

// Catálogo de respaldo. La fuente real es producto-service (API), pero si el servidor no
// responde (por ejemplo, mientras Render despierta un servicio gratuito) la tienda muestra esto.
// Los "id" coinciden con el "codigo" que entrega la API.

export const productos = [
    // --- Productos destacados ---
    {
        id: "bioshock",
        nombre: "Bioshock",
        precio: 12000,
        imagen: rutaImagen("imagenes/bioshock.jpg"),
        trailer: "https://www.youtube.com/watch?v=CoYorK3E4aM",
        seccion: "destacados"
    },
    {
        id: "rayman-3",
        nombre: "Rayman 3",
        precio: 15000,
        imagen: rutaImagen("imagenes/rayman3.png"),
        trailer: "https://www.youtube.com/watch?v=2KNXCYYuHmc",
        seccion: "destacados"
    },
    {
        id: "devil-may-cry-3",
        nombre: "Devil May Cry 3",
        precio: 20000,
        imagen: rutaImagen("imagenes/dmc3.jpg"),
        trailer: "https://www.youtube.com/watch?v=KmeMu8qb_t4",
        seccion: "destacados"
    },

    // --- Otros productos ---
    {
        id: "sonic-adventure-2",
        nombre: "Sonic Adventure 2",
        precio: 12000,
        imagen: rutaImagen("imagenes/sonic-adventure-2.jpg"),
        trailer: "https://www.youtube.com/watch?v=xmbnZa9H6go",
        seccion: "otros"
    },
    {
        id: "god-of-war",
        nombre: "God of War",
        precio: 30000,
        imagen: rutaImagen("imagenes/god-of-war.webp"),
        trailer: "https://www.youtube.com/watch?v=jZkX3LJDeYQ",
        seccion: "otros"
    },
    {
        id: "resident-evil-2",
        nombre: "Resident Evil 2",
        precio: 14000,
        imagen: rutaImagen("imagenes/resident-evil-2.png"),
        trailer: "https://www.youtube.com/watch?v=kUjLG8ZxuHQ",
        seccion: "otros"
    },
    {
        id: "tony-hawk-s-pro-skater",
        nombre: "Tony Hawk's Pro Skater",
        precio: 21000,
        imagen: rutaImagen("imagenes/tony-hawks-pro-skater.jpg"),
        trailer: "https://www.youtube.com/watch?v=w3okqIHJaxA",
        seccion: "otros"
    },
    {
        id: "mario-luigi-superstar-saga",
        nombre: "Mario & Luigi Superstar Saga",
        precio: 17000,
        imagen: rutaImagen("imagenes/mario-luigi-superstar-saga.jpg"),
        trailer: "https://www.youtube.com/watch?v=d-jkNir4-wg",
        seccion: "otros"
    },
    {
        id: "donkey-kong-country",
        nombre: "Donkey Kong Country",
        precio: 19000,
        imagen: rutaImagen("imagenes/donkey-kong-country.jpg"),
        trailer: "https://www.youtube.com/watch?v=g8PJ7oiskR0",
        seccion: "otros"
    }
];

export default productos;
