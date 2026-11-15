# Sokoban — Trabajo Integrador PDS

Versión del clásico Sokoban en Java + Swing, hecha para aplicar principios y patrones de diseño (SOLID, GRASP, MVC, GoF).

## Cómo ejecutarlo

Requiere JDK 17 o superior. Desde la raíz del proyecto:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

```bash
java -cp out sokoban.Main
```

En Windows con PowerShell, el compilado es:

```bash
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | % FullName)
```

Hay que ejecutar **desde la raíz del proyecto**, porque los niveles se leen de `niveles/`.

## Controles

| Tecla | Acción |
|---|---|
| Flechas | Mover al jugador |
| `Z` | Deshacer (retrocede 5 movimientos) |
| `R` | Reiniciar el nivel |

También están los botones **Deshacer** y **Reiniciar nivel** en el HUD.

## Reglas implementadas

- El jugador empuja cajas, nunca tira de ellas, y no empuja más de una a la vez (salvo con el power-up Fuerza).
- **Caja normal**: se empuja sin desgastarse.
- **Caja frágil**: cada empuje le baja la resistencia; al llegar a cero se rompe.
- **Caja llave**: al quedar sobre un casillero cerrojo, abre los muros asociados.
- **Terreno resbaladizo**: lo que entra sigue deslizándose en la misma dirección hasta chocar o salir del hielo.
- **Muros**: solo son transitables cuando están abiertos.
- **Deshacer**: se recuerdan los últimos 15 movimientos, cada uso retrocede 5, hasta 3 usos consecutivos.
- **Victoria**: cuando todas las casillas destino están ocupadas. Al completar el nivel se muestra el resumen con movimientos, empujes, usos de deshacer y puntaje.

## Funcionalidades adicionales

1. **Sistema de logros** (patrón Observer) — 5 logros que se desbloquean escuchando los eventos de la partida.
2. **Power-ups** (patrón Decorator) — Fuerza, Antideslizante, Fantasma y Manos de Acero, que modifican las reglas del jugador sin tocar la clase `Jugador`.

## Pruebas

Hay una batería de pruebas de humo que corre **sin interfaz gráfica**: resuelve los tres niveles, verifica las reglas de empuje, rotura, cerrojos y deslizamiento, y prueba el undo y los power-ups.

```bash
javac -encoding UTF-8 -cp out -d out test/PruebaSokoban.java
```

```bash
java -cp out PruebaSokoban
```

Que corra sin Swing es la demostración práctica de que el modelo no depende de la vista.

Hay una segunda prueba, `PruebaFoco`, que **sí necesita pantalla**: abre la ventana y usa `Robot` para hacer clic en los botones y después mover con las flechas. Cubre el bug de foco que hacía que el teclado dejara de responder después de usar Deshacer o Reiniciar.

```bash
javac -encoding UTF-8 -cp out -d out test/PruebaFoco.java
```

```bash
java -cp out PruebaFoco
```

## Formato de los niveles

Los niveles son archivos de texto en `niveles/nivelN.txt`, un carácter por casilla:

| Carácter | Elemento |
|---|---|
| `#` | Pared |
| (espacio) | Espacio vacío |
| `.` | Casilla destino |
| `@` | Jugador |
| `+` | Jugador sobre destino |
| `$` | Caja normal |
| `*` | Caja normal sobre destino |
| `F` | Caja frágil |
| `L` | Caja llave |
| `~` | Terreno resbaladizo |
| `c` | Casillero cerrojo |
| `M` / `m` | Muro cerrado / abierto |
| `1` `2` `3` `4` | Power-up: Fuerza, Antideslizante, Fantasma, Manos de Acero |

Para agregar un nivel alcanza con crear `niveles/nivel4.txt`: el juego lo detecta solo.

## Assets

Los gráficos son del pack **[Sokoban de Kenney](https://kenney.nl/assets/sokoban)**, con licencia **Creative Commons Zero (CC0 1.0)** — dominio público. Están en `assets/imagenes/`, y en [assets/CREDITS.txt](assets/CREDITS.txt) está anotado de qué archivo del pack original salió cada uno.

El tile de terreno resbaladizo es obra derivada: el pack no trae hielo, así que se generó virando a azul el piso gris de Kenney. CC0 permite la modificación.

Los efectos de sonido son de los packs **[Impact Sounds](https://kenney.nl/assets/impact-sounds)** e **[Interface Sounds](https://kenney.nl/assets/interface-sounds)**, del mismo autor y también **CC0**. Están en `assets/sonidos/`.

Los packs vienen en `.ogg` y `javax.sound.sampled` solo lee **WAV, AU y AIFF**, así que se convirtieron a WAV PCM 16 bits con ffmpeg. El comando exacto está en `CREDITS.txt`. Si agregás un sonido nuevo, convertilo: un `.ogg` en esa carpeta no rompe nada, pero ese evento queda mudo sin avisar.

## Documentación de diseño

Las decisiones de diseño y su justificación están en [Bitacora de decisiones.md](Bitacora%20de%20decisiones.md), que es la base del informe técnico.
