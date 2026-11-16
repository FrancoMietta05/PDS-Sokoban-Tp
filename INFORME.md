# Sokoban: Informe técnico

**Materia:** Proceso de Desarrollo de Software (PDS)
**Universidad:** UADE
**Profesor:** Pepe
**Integrantes:** [COMPLETAR: nombre y legajo de cada integrante]
**Fecha de entrega:** [COMPLETAR]
**Repositorio:** https://github.com/FrancoMietta05/PDS-Sokoban-Tp

> **BORRADOR.** Lo marcado con `[COMPLETAR]` falta. Las decisiones de diseño están desarrolladas en detalle en [Bitacora de decisiones.md](Bitacora%20de%20decisiones.md); este informe las resume y remite a ellas por número.

---

## Índice

1. [Introducción](#1-introducción)
2. [Cómo ejecutar el juego](#2-cómo-ejecutar-el-juego)
3. [Reglas y funcionalidades](#3-reglas-y-funcionalidades)
4. [Arquitectura: MVC y estructura de paquetes](#4-arquitectura-mvc-y-estructura-de-paquetes)
5. [Patrones de diseño aplicados](#5-patrones-de-diseño-aplicados)
6. [Principios SOLID](#6-principios-solid)
7. [Patrones GRASP](#7-patrones-grasp)
8. [Decisiones de diseño principales](#8-decisiones-de-diseño-principales)
9. [Niveles](#9-niveles)
10. [Pruebas](#10-pruebas)
11. [Diagrama de clases](#11-diagrama-de-clases)
12. [Capturas del juego](#12-capturas-del-juego)
13. [Assets y licencias](#13-assets-y-licencias)
14. [Declaración de uso de herramientas de IA](#14-declaración-de-uso-de-herramientas-de-ia)
15. [Conclusiones](#15-conclusiones)

---

## 1. Introducción

El trabajo es una versión del Sokoban en Java con interfaz Swing. El jugador empuja cajas dentro de un almacén hasta dejarlas sobre las casillas destino. A las reglas clásicas se le suman cajas frágiles que se rompen, cajas llave que abren muros, terreno resbaladizo, un undo limitado y, como funcionalidades adicionales, un sistema de logros y power-ups.

El objetivo no es solo que el juego funcione, sino aplicar los principios y patrones vistos en la materia: separación MVC, principios SOLID y GRASP, y los patrones GoF Observer, Memento, Decorator, Singleton, Factory Method y Builder. Cada patrón está aplicado donde resuelve un problema concreto del juego, y en la sección 8 se justifica por qué se eligió frente a las alternativas.

## 2. Cómo ejecutar el juego

Requiere JDK 17 o superior y se ejecuta **desde la raíz del proyecto**, porque los niveles se leen de `niveles/`.

En PowerShell:

```bash
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | % FullName)
```

```bash
java -cp out sokoban.Main
```

En bash, el compilado es `javac -encoding UTF-8 -d out $(find src -name "*.java")`.

| Tecla | Acción |
|---|---|
| Flechas | Mover al jugador |
| `Z` | Deshacer (retrocede 5 movimientos) |
| `R` | Reiniciar el nivel |

En pantalla están además los botones **Deshacer** y **Reiniciar**.

## 3. Reglas y funcionalidades

**Reglas del enunciado**

- El jugador empuja cajas, nunca tira de ellas, y empuja una sola a la vez.
- **Caja normal:** se empuja sin desgastarse.
- **Caja frágil:** cada empuje le baja la resistencia y al llegar a cero se rompe. En el archivo de nivel hay tres durezas: `F` (3), `G` (5) y `H` (8).
- **Caja llave:** al quedar sobre un cerrojo, abre los muros del nivel.
- **Terreno resbaladizo:** lo que entra sigue deslizándose en la misma dirección hasta chocar o salir del hielo. Vale para las cajas y para el jugador.
- **Muros:** solo se pueden atravesar cuando están abiertos.
- **Deshacer:** se recuerdan los últimos 15 movimientos, cada uso retrocede 5 y se permiten hasta 3 usos consecutivos.
- **Victoria:** cuando todas las casillas destino están ocupadas. Al completar un nivel se muestra un resumen con movimientos, empujes, usos de deshacer y puntaje. Al terminar el último nivel aparece una pantalla final para volver a iniciar (desde el nivel 1, con los logros en cero) o cerrar.

**Puntaje.** Criterio del equipo: se parte de 1000 y se descuentan 5 puntos por movimiento, 10 por empuje y 100 por uso de deshacer, sin bajar de cero. Los empujes pesan más que los movimientos porque son la jugada que hay que planificar, y el deshacer pesa más todavía porque es la ayuda.

**Funcionalidad adicional 1: logros (Observer).** Cinco logros que se desbloquean escuchando los eventos de la partida: *Primer paso*, *Pulso firme* (un nivel sin deshacer), *Eficiente*, *Demoledor* (romper cajas frágiles) y *Cerrajero*. Agregar un logro es agregar una subclase de `Logro`; no se toca ni `Juego` ni `Tablero`.

**Funcionalidad adicional 2: power-ups (Decorator).** Cuatro power-ups que se recogen en el tablero y cambian una regla del jugador sin modificar la clase `Jugador`:

| Power-up | Efecto | Vigencia |
|---|---|---|
| Fuerza | Empuja dos cajas en fila | 15 movimientos |
| Antideslizante | No resbala sobre el hielo | 20 movimientos |
| Fantasma | Atraviesa paredes y muros cerrados | 3 paredes |
| Manos de Acero | Rompe una caja frágil de un empuje | 10 movimientos |

## 4. Arquitectura: MVC y estructura de paquetes

```
src/sokoban/
├── Main.java                  punto de entrada
├── modelo/                    Direccion, TipoPowerUp, Tablero, ResultadoMovimiento,
│   │                          TableroBuilder, DirectorNivel                   [Builder]
│   ├── celdas/                Celda y sus 7 subclases
│   ├── entidades/             EntidadMovil, IJugador, Jugador, Caja y sus 3 subclases
│   ├── fabricas/              FabricaCelda, FabricaCaja y sus fábricas        [Factory Method]
│   └── memento/               Memento (foto) e HistorialMovimientos (caretaker)
├── eventos/                   Sujeto, Observador, GestorEventos, EventoJuego, TipoEvento
├── logros/                    Logro, sus 5 subclases y GestorLogros        [adicional 1]
├── powerups/                  PowerUpDecorator, sus 4 subclases, GestorPowerUps [adicional 2]
│   └── fabricas/              FabricaPowerUp y sus 4 fábricas                 [Factory Method]
├── controlador/               Juego
└── vista/                     VentanaPrincipal, PanelTablero, PanelHUD,
                               PanelNotificaciones, ReproductorSonido
```

| Capa | Clases | Responsabilidad |
|---|---|---|
| **Modelo** | `modelo`, `eventos`, `logros`, `powerups` | Estado del almacén y reglas del juego |
| **Controlador** | `Juego` | Carga niveles, recibe la entrada ya traducida, coordina undo y power-ups, decide la victoria |
| **Vista** | `vista` | Dibuja el tablero y el HUD, reproduce sonidos, traduce el teclado |

**La regla que ordena todo:** las dependencias van de la vista y el controlador hacia el modelo, nunca al revés. Ninguna clase del modelo ni del controlador importa nada de Swing ni de AWT. Es verificable:

```bash
grep -rlE "import (javax\.swing|java\.awt)" src/sokoban/modelo src/sokoban/eventos src/sokoban/logros src/sokoban/powerups src/sokoban/controlador
```

no devuelve nada. Dos decisiones lo sostienen. Por un lado, el controlador recibe una `Direccion` y no un `KeyEvent`: es `VentanaPrincipal` la que traduce la tecla (decisión 17). Por otro, el `Memento` guarda la posición como dos `int` y no como `java.awt.Point` (decisión 22).

La consecuencia práctica es que toda la batería de pruebas del modelo corre sin abrir una ventana (sección 10). La vista se entera de los cambios como observador del modelo (Observer), y le habla al controlador llamando a sus métodos.

## 5. Patrones de diseño aplicados

| Patrón | Clases | Problema que resuelve |
|---|---|---|
| **Observer** | `Sujeto`, `Observador`, `GestorEventos`; observadores: `GestorLogros`, `PanelHUD`, `PanelTablero`, `PanelNotificaciones`, `ReproductorSonido`, `VentanaPrincipal` | Que el modelo avise qué pasó sin saber quién escucha |
| **Memento** | `Juego` (originator), `Memento` (foto), `HistorialMovimientos` (caretaker) | Deshacer sin abrir el estado interno de la partida |
| **Decorator** | `IJugador`, `Jugador`, `PowerUpDecorator` y sus 4 subclases | Power-ups que cambian reglas sin modificar `Jugador` |
| **Singleton** | `Juego` | Una única partida por aplicación |
| **Factory Method** | `FabricaCelda`, `FabricaCaja`, `FabricaPowerUp` y sus fábricas concretas | Crear celdas, cajas y decoradores sin `switch` ni `new` en quien los usa |
| **Builder** | `DirectorNivel` (director), `TableroBuilder` (builder), `Tablero` (producto) | Separar la construcción del tablero de sus reglas |
| **Strategy (implícito)** | `Logro` y sus 5 subclases | Una condición de desbloqueo por clase |

**Observer.** `GestorEventos` implementa `Sujeto` y `Juego` lo contiene por composición. Los eventos (`TipoEvento`: nivel iniciado, movimiento, empuje, caja rota, cerrojo abierto, undo, power-up recogido, nivel completado y logro desbloqueado) nacen en `Tablero`, en `Juego` y en `GestorLogros`. El sonido es el ejemplo más claro: `ReproductorSonido` se suscribe y el modelo no sabe que existe. Por qué el sujeto no es `Juego`: decisión 1.

**Memento.** `Juego` es el único que sabe armar la foto (`crearMemento`) y volver a ella (`restaurar`). `HistorialMovimientos` la apila sin poder leerla, y es donde viven las reglas de 15 movimientos, retroceso de 5 y 3 usos consecutivos. La foto guarda todo lo que un movimiento puede cambiar: posición y contadores del jugador, cajas (posición, resistencia y rotura), muros, cerrojos, power-ups recogidos y la cadena de power-ups activa (decisiones 4 y 8).

**Decorator.** `PowerUpDecorator` implementa `IJugador`, guarda una referencia al `IJugador` que envuelve y delega todo. Cada decorador concreto pisa exactamente el método de la regla que modifica, por ejemplo `cajasEmpujables()` en Fuerza. Las capas se apilan: Fuerza sobre Antideslizante sobre el jugador. `Tablero` y `Juego` solo conocen la interfaz (decisiones 2, 3 y 11).

**Singleton.** `Juego.getInstance()`, con constructor privado. Para limitar el costo del estado global, la vista recibe `juego` por constructor en lugar de llamar a `getInstance()` desde cualquier lado (decisión 19).

**Factory Method.** `DirectorNivel` tiene un mapa de carácter a fábrica de celda y otro de carácter a fábrica de caja. `GestorPowerUps` tiene un mapa de tipo a fábrica de power-up. Sumar un elemento es sumar una fábrica y registrarla. Por ejemplo, las frágiles `G` y `H` se agregaron con dos líneas, usando la misma `FabricaCajaFragil` parametrizada con la resistencia (decisiones 20 y 23).

**Builder.** `DirectorNivel` es el único que conoce el formato `.txt` y le pide los pasos a `TableroBuilder` (`dimensiones`, `agregarCelda`, `agregarCaja`, `colocarJugador`, `build`). El builder valida que el nivel esté completo antes de entregar el `Tablero`, cuyo constructor es package-private (decisión 21).

## 6. Principios SOLID

**S, responsabilidad única.**
- `Tablero` solo aplica reglas de movimiento: leer el archivo es tarea de `DirectorNivel` y armarlo, de `TableroBuilder` (decisión 21).
- `HistorialMovimientos` solo decide cuándo y cuánto se deshace. Si cambian los números del enunciado, se toca esa clase y nada más.
- `GestorEventos` está separado de `Juego` porque registrar observadores cambia por motivos distintos que llevar la partida (decisión 1).

**O, abierto/cerrado.**
- Los tipos de casilla son subclases de `Celda`. `CasillaPowerUp` entró sin modificar el código de movimiento (decisión 5).
- Las fábricas permiten sumar cajas, celdas y power-ups sin editar `DirectorNivel` más allá de registrarlas.
- Un logro nuevo es una subclase de `Logro`. El único punto que se edita es la lista de `GestorLogros`.

**L, sustitución de Liskov.**
- Cualquier `PowerUpDecorator` se puede usar donde se espera un `IJugador`, y el tablero no nota la diferencia. Es lo que permite reemplazar al jugador por una cadena decorada en el mismo campo.
- Las tres cajas responden a los mismos ganchos (`registrarEmpuje`, `alPosicionarseEn`). `Tablero` nunca pregunta de qué tipo es una caja.

**I, segregación de interfaces.**
- `Observador` tiene un solo método (`actualizar`) y `Sujeto` tiene tres. Cada observador implementa solo lo que usa.
- **Punto débil, conocido:** `IJugador` tiene 14 métodos. Creció porque, al declarar el jugador con el tipo de la interfaz, todo lo que alguien necesite del jugador tiene que pasar por ella (decisión 9). Se podría partir en una interfaz de reglas, para los power-ups, y otra de estado (posición y contadores). [COMPLETAR: decidir si se deja así y se defiende, o se parte.]

**D, inversión de dependencias.**
- `Juego` y `Tablero` dependen de `IJugador`, no de `Jugador`. A `Jugador` no le entra ninguna dependencia (decisión 2).
- La vista depende de la abstracción `Observador` para enterarse de los cambios.
- `PanelFinal` recibe lo que tiene que hacer cada botón como un `Runnable`: no conoce ni al juego ni a `GestorLogros` (decisión 24).

## 7. Patrones GRASP

| Patrón | Dónde | Por qué |
|---|---|---|
| **Experto en información** | `Tablero.moverJugador()`; `CajaFragil.registrarEmpuje()`; `Juego.calcularPuntaje()` | El tablero es el único que conoce a la vez las celdas vecinas y las cajas. La caja frágil conoce su resistencia. `Juego` tiene los contadores |
| **Creador** | `TableroBuilder` crea `Tablero`; `Juego` crea `Memento` | El builder tiene todos los datos para inicializar el tablero. `Juego` tiene los datos de la foto |
| **Controlador** | `Juego` | Recibe los eventos del sistema (moverse, deshacer, reiniciar) y coordina sin hacer el trabajo de las reglas |
| **Bajo acoplamiento** | El modelo no conoce la vista; `Tablero` publica en `GestorEventos` y no en `Juego` | Se puede cambiar o probar la vista sin tocar el modelo, y el tablero no depende del controlador |
| **Alta cohesión** | `HistorialMovimientos`, `GestorPowerUps`, `DirectorNivel` | Cada uno tiene un solo tema: undo, cadena de decoradores, formato de nivel |
| **Polimorfismo** | `Celda.esTransitable()`, `Caja.alPosicionarseEn()`, `Logro.verificar()`, los decoradores | El comportamiento que varía por tipo se resuelve por subclase, sin `if` ni `switch` sobre el tipo |
| **Fabricación pura** | `GestorEventos`, `HistorialMovimientos`, `GestorPowerUps`, las fábricas | Clases que no existen en el dominio del Sokoban, creadas para mantener la cohesión y el bajo acoplamiento |
| **Indirección** | `TipoPowerUp` entre `CasillaPowerUp` y los decoradores; `GestorEventos` entre quien publica y quien escucha | La celda no conoce los decoradores (decisión 6) y el modelo no conoce a sus observadores |
| **Variaciones protegidas** | `IJugador` | Protege a `Tablero` de los power-ups: puede aparecer uno nuevo sin que el tablero cambie |

## 8. Decisiones de diseño principales

Resumen de las decisiones registradas en la bitácora. Cada una está desarrollada allá con la alternativa descartada y el porqué.

| # | Decisión | En una línea |
|---|---|---|
| 1 | `GestorEventos` como sujeto separado | Responsabilidad única, y el tablero no depende del controlador |
| 2 | Interfaz `IJugador` | El Decorator necesita un tipo común; heredar de `Jugador` duplicaría el estado |
| 3 | Agregación entre decorador y envuelto | El jugador sobrevive al vencimiento del power-up |
| 4 | `HistorialMovimientos` como caretaker | Encapsulamiento: nadie más lee la foto |
| 5 | `CasillaPowerUp` como subclase de `Celda` | Abierto/cerrado en lugar de un flag nulo |
| 6 | `TipoPowerUp` como intermediario | La celda no conoce el subsistema de decoradores |
| 7 | `Caja` hereda de `EntidadMovil` | Evitar duplicar posición y movimiento |
| 8 | El `Memento` guarda todo el tablero y los power-ups | Si no, el undo deja el tablero mezclado |
| 10 | La consulta `puedeAtravesar()` no tiene efectos | Un intento cancelado no gasta una pared |
| 12 | `moverJugador()` devuelve `ResultadoMovimiento` | El historial no se llena de intentos fallidos |
| 13 | Evento `NIVEL_INICIADO` | `Juego` no conoce a los logros |
| 14 | Victoria por destinos ocupados | Si no, la caja llave y la frágil rotas rompen la regla |
| 16 | Interpretación del deslizamiento | El enunciado, tomado literal, no permitía deslizar nunca |
| 17 | El controlador recibe `Direccion` | MVC: el controlador no conoce Swing |
| 19–21 | Singleton, Factory Method y Builder | Ver sección 5 |
| 22 | Dos `int` en lugar de `java.awt.Point` | Modelo libre de AWT |
| 23 | La resistencia viaja en la fábrica | Sumar una dureza es una línea |
| 24 | La partida nueva la resuelve la vista | `Juego` no conoce a `GestorLogros` |
| 25 | Solucionador sobre el juego real | Probar los niveles con las reglas verdaderas, no con una copia |

## 9. Niveles

Ocho niveles en `niveles/nivelN.txt`, un carácter por casilla. Los tres primeros recorren las mecánicas del enunciado, y del 4 al 7 cada uno exige un power-up distinto:

| Nivel | Qué introduce |
|---|---|
| 1 | Tres cajas a tres destinos |
| 2 | Caja llave, cerrojo, muro y caja frágil |
| 3 | Hielo, cerrojo, muro y caja frágil |
| 4 | Fuerza: dos cajas en fila que hay que empujar juntas |
| 5 | Antideslizante: hay que frenar sobre el hielo para empujar |
| 6 | Fantasma: la caja está en una sala cerrada |
| 7 | Manos de Acero: una frágil dura tapa el pasillo y hay que romperla |
| 8 | Combinación: llave, muro, hielo y frágil |

Los niveles 4 a 7 están diseñados para que **no se puedan resolver sin su power-up**, y la batería de pruebas lo demuestra (sección 10). Así las dos funcionalidades adicionales se pueden mostrar jugando.

## 10. Pruebas

**`PruebaSokoban`**: corre sin interfaz gráfica, lo que demuestra en la práctica que el modelo no depende de la vista. Cubre:

- Las reglas: empuje, rotura, cerrojos, deslizamiento y victoria.
- El undo: límites y restauración de la cadena de power-ups.
- Los power-ups: efecto, delegación entre capas y vencimiento.
- Los patrones: Singleton, Factory Method y Builder.
- Las resistencias `F`, `G` y `H`, y el reinicio de logros.
- **Cada nivel con su secuencia de solución.** Si se agrega un nivel sin su solución, la batería falla.

**`Solucionador`**: una búsqueda en anchura que avanza con `Juego.manejarEntrada()` y vuelve atrás con el propio `Memento` del juego, así que no reimplementa ninguna regla. Recorriendo todos los estados alcanzables y sacándole al jugador los power-ups después de cada paso, prueba que los niveles 4 a 7 no tienen ninguna solución sin ellos (decisión 25).

**`PruebaFoco`**: necesita pantalla. Usa `Robot` para hacer clic en los botones del HUD y después mover con las flechas. Es la regresión del bug por el que el teclado dejaba de responder al tocar un botón: la entrada se leía con un `KeyListener` y el foco se iba al botón. Se arregló con key bindings `WHEN_IN_FOCUSED_WINDOW` y botones no enfocables.

Resultado actual: [COMPLETAR: pegar la salida final de `java -cp out PruebaSokoban` antes de entregar].

## 11. Diagrama de clases

[COMPLETAR: insertar la imagen exportada del diagrama.]

Pendiente: confirmar con el profesor si acepta draw.io o exige StarUML (SOK-18), y actualizar el diagrama con las desviaciones de la implementación (SOK-19). Las que cambian lo dibujado son:

- `Caja` hereda de `EntidadMovil`.
- `IJugador` tiene 14 métodos.
- `Memento` guarda `filaJugador` y `columnaJugador` (`int`) en vez de `posJugador: Point`, y suma muros, cerrojos, power-ups recogidos y power-ups activos.
- `TipoEvento` tiene 9 valores.
- Clases nuevas: `ResultadoMovimiento`, `EstadoPowerUp`, `Direccion`, `TableroBuilder`, `DirectorNivel` y las fábricas.
- `Tablero.moverJugador()` devuelve `ResultadoMovimiento`, y `Tablero` expone `setJugador()`.
- `PowerUpDecorator` suma `consumirTurno()` y `capturarEstado()`.
- `FabricaCajaFragil` tiene el atributo `resistencia`.

## 12. Capturas del juego

[COMPLETAR: una captura por mecánica. Sugeridas: nivel 2 con el muro abierto, nivel 3 con la caja deslizándose, un nivel con un power-up activo en el HUD, el resumen de nivel, una notificación de logro y la pantalla de fin.]

## 13. Assets y licencias

Todos los assets son de dominio público, como pide el enunciado. El detalle archivo por archivo está en [assets/CREDITS.txt](assets/CREDITS.txt).

| Assets | Origen | Licencia |
|---|---|---|
| 18 sprites (`assets/imagenes/`) | Pack *Sokoban* de Kenney, https://kenney.nl/assets/sokoban | CC0 1.0 |
| `resbaladizo.png` | Obra derivada: el piso de Kenney virado a azul | CC0 1.0 (permitido por la licencia original) |
| 8 efectos de sonido (`assets/sonidos/`) | Packs *Impact Sounds* e *Interface Sounds* de Kenney, convertidos de `.ogg` a WAV | CC0 1.0 |
| Fuente *Press Start 2P* (`assets/fuentes/`) | CodeMan38 | SIL Open Font License 1.1 |
| `Fondo.jpg`, `Final.png`, `Template resumen de nivel.png` | [COMPLETAR: origen y licencia; no figuran en `CREDITS.txt`] | [COMPLETAR] |

## 14. Declaración de uso de herramientas de IA

> [COMPLETAR por el equipo. El enunciado exige esta declaración.]

**Herramientas utilizadas:** [COMPLETAR]

**Para qué se usaron, y en qué partes del trabajo:** [COMPLETAR]

**Qué decidió el equipo y cómo se verificó lo producido:** [COMPLETAR]

## 15. Conclusiones

[COMPLETAR. Ideas para desarrollar:]

- Separar modelo y vista desde el principio permitió probar todas las reglas sin interfaz, y verificar los niveles automáticamente.
- El Memento, pensado para el undo, terminó sirviendo también para explorar todas las jugadas posibles de un nivel sin agregar código al modelo.
- El Decorator obligó a introducir `IJugador`, y esa interfaz es a la vez lo que hace funcionar los power-ups y el punto más discutible del diseño (sección 6, I).
- Lo que se haría distinto: [COMPLETAR].
