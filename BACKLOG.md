# Backlog — Sokoban PDS

Estado al cerrar el código inicial. Todo lo que figura acá está **pendiente**: lo que ya funciona no se lista (ver README).

**Prioridades**
- **P0** — bloquea la entrega: lo pide el enunciado explícitamente.
- **P1** — no bloquea, pero se nota en la defensa.
- **P2** — pulido.

**Tamaños:** S = un rato · M = media tarde · L = una tarde larga o más.

---

## Épica A — Assets gráficos y sonoros

> El enunciado: *"Todos los elementos del tablero deben ser representados por imágenes y las acciones del juego deben estar acompañadas de efectos de sonido apropiados. Todos los assets utilizados deben ser de dominio público."*
>
> Hoy el juego dibuja figuras de color y no suena. El código ya está preparado: busca los archivos y si no están usa el fallback. **No hay que tocar código para que los assets entren** (salvo SOK-13).

### ~~SOK-01~~ · Crear la estructura de carpetas de assets · HECHO
Crear `assets/imagenes/` y `assets/sonidos/` y sacarlas del `.gitignore` si hiciera falta.
**Listo cuando:** las dos carpetas existen y están versionadas.

### ~~SOK-02~~ · Conseguir los sprites de las celdas · HECHO
PNG cuadrados de 48×48 px, en `assets/imagenes/`. Nombres exactos que busca `PanelTablero`:

| Archivo | Elemento |
|---|---|
| `piso.png` | Espacio vacío |
| `pared.png` | Pared |
| `destino.png` | Casilla destino |
| `resbaladizo.png` | Terreno resbaladizo |
| `cerrojo.png` | Cerrojo sin activar |
| `cerrojo_abierto.png` | Cerrojo activado |
| `muro_cerrado.png` | Muro cerrado |
| `muro_abierto.png` | Muro abierto |
| `powerup_fuerza.png` | Power-up Fuerza |
| `powerup_antideslizante.png` | Power-up Antideslizante |
| `powerup_fantasma.png` | Power-up Fantasma |
| `powerup_manos_de_acero.png` | Power-up Manos de Acero |

**Listo cuando:** los 12 archivos están y al correr el juego no se ve ninguna figura del fallback.
**Ojo:** guardar de dónde salió cada asset y su licencia — hace falta para el informe (SOK-21).

### ~~SOK-03~~ · Conseguir los sprites del jugador y las cajas · HECHO
Depende de **SOK-13** (los nombres de las cajas cambian con ese ticket).

| Archivo | Elemento |
|---|---|
| `jugador.png` | Sokoban |
| `caja_normal.png` / `caja_normal_ok.png` | Caja normal, fuera y sobre destino |
| `caja_fragil.png` / `caja_fragil_ok.png` | Caja frágil |
| `caja_llave.png` / `caja_llave_ok.png` | Caja llave |

**Listo cuando:** las tres cajas se distinguen a simple vista y se nota cuándo están sobre destino.

### SOK-04 · Conseguir los efectos de sonido · P0 · M
Formato `.wav` en `assets/sonidos/`. Nombres exactos que busca `ReproductorSonido`:
`movimiento.wav` · `empuje.wav` · `caja_rota.wav` · `cerrojo.wav` · `undo.wav` · `powerup.wav` · `nivel_completado.wav` · `logro.wav`

**Listo cuando:** las 8 acciones suenan. Que `movimiento.wav` sea corto y bajo: se dispara en cada paso.

### SOK-05 · Mostrar la resistencia de la caja frágil con el sprite · P1 · S
Hoy el número de resistencia se dibuja como texto arriba de la caja. Con sprites queda feo.
**Listo cuando:** la resistencia se ve por el aspecto de la caja (3 sprites de desgaste) o por un overlay prolijo.

---

## Épica B — Niveles

> **El problema de fondo:** de los 4 power-ups, solo Fuerza aparece en un nivel (`nivel3.txt`), y el nivel se resuelve sin usarlo. Fantasma, Antideslizante y Manos de Acero **no se pueden conseguir jugando**. Una de las dos funcionalidades adicionales no se puede demostrar.

### SOK-06 · Nivel que obligue a usar Fuerza · P0 · M
Dos cajas en fila que haya que empujar juntas. Sin el power-up el nivel no sale.
**Listo cuando:** el nivel es irresoluble sin agarrar el `1`, y hay una prueba en `PruebaSokoban` que lo resuelve.

### SOK-07 · Nivel que obligue a usar Fantasma · P0 · M
Una pared que haya que atravesar para llegar a una zona encerrada. Recordar que Fantasma tiene 3 usos: el nivel tiene que ser resoluble con 3 atravesadas o menos.
**Listo cuando:** ídem SOK-06 con el `3`.

### SOK-08 · Nivel que obligue a usar Antideslizante · P0 · M
Una zona de hielo donde el jugador, resbalando, no pueda posicionarse para empujar.
**Listo cuando:** ídem SOK-06 con el `2`.

### SOK-09 · Nivel que obligue a usar Manos de Acero · P0 · M
Una caja frágil que haya que romper a propósito para despejar el paso. **Depende de SOK-12**: hoy toda frágil tiene resistencia 3 y no se puede declarar una más resistente desde el `.txt`.
**Listo cuando:** ídem SOK-06 con el `4`.

### SOK-10 · Llevar el juego a 8 niveles con dificultad progresiva · P1 · L
Hoy hay 3. Ordenarlos para que cada mecánica se introduzca de a una: empuje → frágil → llave/cerrojo → hielo → power-ups → combinaciones.
**Listo cuando:** `niveles/nivel1.txt` … `nivel8.txt`, todos resueltos por una prueba.

### SOK-11 · Prueba de solvabilidad para cada nivel · P1 · M
Extender `PruebaSokoban` para que cada nivel nuevo tenga su secuencia de solución verificada.
**Listo cuando:** agregar un nivel sin su prueba hace fallar la batería.
**Por qué importa:** un nivel irresoluble en la demo frente al profesor es el peor escenario posible.

---

## Épica C — Deuda técnica

### SOK-12 · Permitir declarar la resistencia de una caja frágil en el `.txt` · P1 · S
`CajaFragil` ya tiene constructor con resistencia, pero `Tablero.interpretar()` siempre usa el de 3 por defecto. Propuesta: que `F` siga siendo 3 y agregar `G` (5) y `H` (8), o un dígito al lado.
**Listo cuando:** un nivel puede tener frágiles de distinta resistencia. **Bloquea SOK-09.**

### ~~SOK-13~~ · Renombrar las claves de sprite de las cajas · HECHO
`PanelTablero` arma el nombre del archivo con `caja_ + getSimbolo()`, o sea busca `caja_$.png`, `caja_F.png`, `caja_L.png`. Un `$` en un nombre de archivo es un problema esperando.
**Listo cuando:** la clave sale de un método con nombre propio (`caja_normal`, `caja_fragil`, `caja_llave`) y no del símbolo del `.txt`. **Bloquea SOK-03.**

### SOK-14 · `GestorLogros.reiniciar()` es código muerto · P1 · S
El método existe y nadie lo llama: no hay forma de empezar una partida nueva. Dos salidas: agregar un botón "Partida nueva" que lo use, o borrarlo.
**Listo cuando:** o se usa, o no está. Código muerto en la entrega es una pregunta regalada en la defensa.

### SOK-15 · Decidir qué hacer con `java.awt.Point` · P1 · S
`Memento` y `Juego` lo importan porque el diagrama declara `-posJugador: Point`. Es la única mención a AWT en el modelo. Dejarlo y saber defenderlo, o reemplazarlo por dos `int` (5 líneas) y tocar el diagrama.
**Listo cuando:** está decidido y, si se cambia, el diagrama acompaña.

### SOK-16 · Pantalla de fin de juego · P2 · S
Al terminar el último nivel aparece un diálogo y el juego queda congelado en el tablero resuelto, sin salida.
**Listo cuando:** se muestra el total acumulado (puntaje, logros) y se puede empezar de nuevo o cerrar.

### SOK-17 · Indicador visual del power-up activo · P2 · S
Hoy solo se ve como texto en el HUD (`Estado: Fuerza + Sokoban`).
**Listo cuando:** se ve un ícono con los movimientos o usos que le quedan.

---

## Épica D — Diagrama

### SOK-18 · Pasar el diagrama de clases a StarUML · P0 · L
El entregable pide **StarUML**; hoy está en draw.io.
**Listo cuando:** existe el `.mdj` y exportado a imagen.
**Antes de empezar:** confirmar con el profesor si acepta draw.io. Si acepta, este ticket se cae y queda solo SOK-19.

### SOK-19 · Actualizar el diagrama con las desviaciones de la implementación · P0 · M
Son las 12 registradas en `Bitacora de decisiones.md`. Las que cambian lo dibujado:
- `Caja` ahora hereda de `EntidadMovil` → flecha nueva
- `IJugador` tiene 14 métodos, no 8
- `Memento` tiene 8 campos, no 4
- `TipoEvento` tiene 9 valores (se agregó `NIVEL_INICIADO`)
- Clases que no están en el diagrama: `ResultadoMovimiento`, `EstadoPowerUp`, `Direccion`
- `Tablero.moverJugador()` devuelve `ResultadoMovimiento`, no `void`
- `Tablero` expone `setJugador()`
- `PowerUpDecorator` suma `consumirTurno()` y `capturarEstado()`

**Listo cuando:** cada clase del diagrama coincide con su `.java`.

### SOK-20 · Mover `PDSSokoban.drawio.xml` al repo · P1 · S
Quedó afuera, en la carpeta padre. El diagrama debería viajar con el código.

---

## Épica E — Entregables

### SOK-21 · Informe técnico · P0 · L
`Bitacora de decisiones.md` ya tiene las 18 decisiones justificadas, la estructura de paquetes, el apartado de MVC y la tabla de patrones. Falta:
- Principios SOLID y GRASP con el ejemplo concreto de cada uno
- Capturas del juego (después de la Épica A, que se vea con sprites)
- Listado de assets con su licencia de dominio público
- **Declaración de uso de herramientas de IA** — el enunciado la exige

### SOK-22 · Video del gameplay · P0 · M
Tiene que mostrar **todas** las funcionalidades: los 3 tipos de caja, hielo, cerrojo y muro, undo, los 4 power-ups, logros, resumen de nivel.
**Depende de:** Épicas A y B. Grabarlo último.

### SOK-23 · Capturas para el README · P2 · S
Un par de imágenes en el README, después de los sprites.

### SOK-24 · Preparar la defensa oral · P0 · M
Cada integrante defiende el proyecto entero. Las preguntas que ya están identificadas en la bitácora:
- ¿Por qué `GestorEventos` separado y no `Juego` como Sujeto?
- ¿Por qué la interfaz `IJugador` y no reusar `Jugador`?
- ¿Quién es el caretaker y por qué no puede leer el memento?
- Agarrás un power-up y hacés undo, ¿qué pasa?
- ¿Por qué agregación y no composición entre decorador y envuelto?

---

## Orden sugerido

1. **SOK-12 y SOK-13** primero: son chicos y desbloquean otros dos.
2. **Épica A** (assets) y **Épica B** (niveles) en paralelo, si son dos personas.
3. **SOK-14, SOK-15** mientras tanto: son de media hora cada uno.
4. **Épica D** (diagrama) cuando el código esté quieto.
5. **SOK-22** (video) y **SOK-21** (informe) al final.

## Resumen

| Prioridad | Tickets | |
|---|---|---|
| **P0** | 13 | Bloquean la entrega |
| **P1** | 8 | Se notan en la defensa |
| **P2** | 3 | Pulido |

---

## Hecho el 12/11/2026 — integración de assets gráficos

- **SOK-01, SOK-02, SOK-03** — los 19 sprites están en `assets/imagenes/`, del pack **Sokoban de Kenney (CC0 1.0)**. `assets/CREDITS.txt` deja anotado de qué archivo del pack salió cada uno, para el informe.
- **SOK-13** — la clave del sprite ya no sale de `Caja.getSimbolo()` (que era el carácter del `.txt` y hacía buscar `caja_$.png`). Ahora la traduce `PanelTablero.claveDeCaja()`, del lado de la vista.
- **`LADO_CELDA` de 48 a 64** — los sprites de Kenney son de 64×64; a 48 se escalaban 0.75× y el pixel art quedaba borroso.
- **Marcas sobre el piso** — el cerrojo y los power-ups se dibujan como overlay transparente encima de `piso.png`, en vez de ser tiles completos. Así el mismo sprite sirve sobre cualquier piso.
- **`resbaladizo.png` es obra derivada** — el pack no trae hielo; se generó virando a azul el piso gris de Kenney. Permitido por CC0 y anotado en `CREDITS.txt`.

Sigue pendiente de la Épica A: **SOK-04 (sonidos)**. La carpeta `assets/sonidos/` está vacía y el juego anda igual, sin audio.

---

## Bug corregido el 14/11/2026 — el teclado moría al tocar un botón

**Síntoma.** Después de hacer clic en *Reiniciar nivel* (o en *Deshacer*), las flechas dejaban de mover al jugador.

**Causa.** La entrada se leía con un `KeyListener` sobre `PanelTablero`. Un `KeyListener` solo recibe teclas si su componente tiene el foco, y al clickear un botón el foco se iba al `JButton`. Confirmado midiendo el `focusOwner` antes y después de un clic real.

**Arreglo.** Se reemplazó el `KeyListener` por **key bindings** con `WHEN_IN_FOCUSED_WINDOW`, que entregan la tecla mientras la ventana esté activa, sin importar qué componente tenga el foco. Además los botones del HUD ahora son `setFocusable(false)`, así el tablero conserva el foco y no aparece el recuadro saltando entre botones.

**Regresión.** `test/PruebaFoco.java` reproduce el escenario con `Robot`: clic en cada botón y después movimiento con flechas. Necesita pantalla, por eso está separada de `PruebaSokoban`.

**Para la defensa.** Es un buen ejemplo de por qué en Swing se prefieren key bindings sobre `KeyListener`, y de que el bug estaba en la vista sin tocar una línea del modelo.
