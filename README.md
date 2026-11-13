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

El juego busca imágenes en `assets/imagenes/*.png` y sonidos en `assets/sonidos/*.wav`. Si no están, dibuja figuras de color y no reproduce sonido, así que corre igual. Los nombres que busca están en `PanelTablero` y `ReproductorSonido`.

## Documentación de diseño

Las decisiones de diseño y su justificación están en [Bitacora de decisiones.md](Bitacora%20de%20decisiones.md), que es la base del informe técnico.
