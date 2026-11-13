# GameGo

Tienda de videojuegos retro. Frontend en **React + Vite** y backend en **microservicios Spring Boot**
(API Gateway, cliente, producto, pedido, pago y despacho), con despliegue en **Render**.

```
GameGo/
├── src/, public/, index.html, logIn.html   Frontend (React + Vite + Bootstrap)
├── backend/                                Microservicios Spring Boot (Java 21, Maven multimódulo)
│   ├── api-gateway/                        Puerta única de entrada (:8080) + CORS
│   ├── cliente-service/                    Registro, login (BCrypt) y mensajes de contacto (:8081)
│   ├── producto-service/                   Catálogo de juegos y stock (:8082)
│   ├── pedido-service/                     Pedidos con varios juegos; valida cliente y descuenta stock (:8083)
│   ├── pago-service/                       Pagos; marca el pedido como PAGADO (:8084)
│   ├── despacho-service/                   Despachos programados (:8085)
│   ├── eureka-server/                      Descubrimiento de servicios (solo Docker local, :8761)
│   ├── render/docker/                      Dockerfiles multietapa que usa Render
│   └── docker-compose*.yml                 Ejecución local
├── render.yaml / render.yaml.template      Blueprint de Render (6 servicios + sitio estático)
├── scripts/                                Scripts PowerShell (verificar, validar, probar local)
└── postman_*.json                          Colecciones de prueba
```

## Qué cambió en el frontend

| Antes | Ahora |
|---|---|
| Catálogo fijo en `productos.js` | Se carga desde `GET /api/productos/` (queda `productos.js` como respaldo si el servidor no responde) |
| Login: cualquier correo/clave válida entraba | Login y registro reales contra `cliente-service` (contraseña cifrada con BCrypt) |
| Formulario de contacto solo mostraba un mensaje | Guarda el mensaje en `cliente-service` |
| "Finalizar compra" vaciaba el carrito | Crea **pedido → pago → despacho**, descuenta stock y muestra el número de pedido |
| Imágenes en `src/assests/imagenes/` | Ahora en `public/imagenes/` (necesario para que `vite build` las publique) |

Cuenta de prueba (se crea al arrancar `cliente-service`): `demo@gmail.com` / `Demo1234`.

## Ejecutar en local

Requisitos: JDK 21, Maven, Docker Desktop, Node 20.19+ o 22.12+.

```powershell
.\scripts\00_verificar_requisitos.ps1     # revisa herramientas
.\scripts\02_probar_render_local.ps1      # compila el backend y lo levanta con Docker (gateway en :8080)
npm install
npm run dev                               # frontend en http://localhost:5173
.\scripts\03_detener_render_local.ps1     # detiene los contenedores
```

El frontend usa `http://localhost:8080` como API por defecto. Para otra URL: copiar `.env.example` a `.env`.

Variante con Eureka (descubrimiento dinámico): `cd backend && mvn clean package -DskipTests && docker compose up --build`.

## Desplegar en Render

1. Generar el Blueprint con un prefijo único (minúsculas, sin espacios):
   ```powershell
   .\scripts\configurar-render.ps1 -Prefijo nombreunico
   .\scripts\01_validar_estructura.ps1
   ```
2. Subir el proyecto completo a GitHub.
3. En Render: **New → Blueprint** y elegir el repositorio. Se crean 7 servicios:
   `…-gamego-cliente`, `-producto`, `-pedido`, `-pago`, `-despacho`, `-gateway` y `-web` (la tienda).
4. Abrir `https://nombreunico-gamego-web.onrender.com`.
5. Probar el backend con `postman_render_online.json` (cambiar `gateway_url` al prefijo elegido).

Cómo se conectan: el sitio estático se compila con `VITE_API_URL` = URL pública del gateway; el gateway
acepta CORS solo desde la URL de la tienda (`CORS_ALLOWED_ORIGINS`); y los servicios se hablan por sus URLs
HTTPS públicas, con Eureka desactivado (Render Free no tiene red privada entre Web Services).

### Limitaciones de Render Free (importantes)

- **Se duermen tras ~15 min sin uso** y tardan hasta ~1 min en despertar. La tienda muestra un aviso mientras
  conecta y despierta en segundo plano los demás servicios.
- **H2 es en memoria**: cada vez que un servicio se reinicia pierde sus datos (clientes registrados, pedidos,
  pagos, stock). El catálogo y la cuenta demo se vuelven a crear solos. Para datos persistentes habría que
  cambiar H2 por PostgreSQL.
- Mientras un servicio duerme y otro lo llama puede haber un primer error por tiempo de espera: reintentar.

## Endpoints (todos vía gateway)

| Servicio | Endpoints |
|---|---|
| Productos | `GET /api/productos/`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `POST /{id}/descontar-stock` |
| Clientes | `POST /api/clientes/registro`, `POST /login`, `POST /contacto`, `GET /{id}`, `GET /`, `DELETE /{id}` |
| Pedidos | `POST /api/pedidos/` (`{clienteId, items:[{productoId, cantidad}]}`), `GET /{id}`, `GET /`, `PATCH /{id}/estado`, `DELETE /{id}` |
| Pagos | `POST /api/pagos/` (`{pedidoId, metodo}`; el monto lo toma del pedido), `GET`, `DELETE` |
| Despachos | `POST /api/despachos/` (`{pedidoId, direccion, comuna}`), `GET`, `DELETE` |

Swagger de cada servicio: `/swagger-ui.html` en su puerto (en local).

## Alcance y límites conocidos

Es un proyecto educativo, igual que la plataforma de referencia:

- **No hay JWT ni Spring Security**: el login valida correo y contraseña, pero el backend no exige un token en
  las demás llamadas, y `GET /api/clientes/` lista los clientes (sin contraseñas). No es apto para producción.
- Si falla un descuento de stock a mitad de un pedido de varios juegos no hay compensación automática (el
  stock se valida completo antes de descontar para minimizar el riesgo).
- El pago es una simulación: siempre queda `APROBADO`.
- `karma.conf.cjs` apunta a `src/js/validacionesCore.js`, que no existe (la ruta real es
  `src/utils/validacionesCore.js`). Ya venía así; `npm test` necesita corregirlo.
