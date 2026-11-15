# Bitácora de decisiones de diseño — Sokoban

**Materia:** Proceso de Desarrollo de Software (PDS) — Prof. Pepe, UADE
**Iniciada:** 12/11/2026, al cerrar el diagrama de clases y antes de empezar a codear.

Este documento NO es el informe técnico. Es el registro de por qué el diseño quedó como quedó, escrito mientras las razones están frescas. Al terminar el código se convierte en el informe: se le agrega la estructura de paquetes, el apartado de MVC, las capturas y la declaración de uso de IA que pide el enunciado.

Formato de cada entrada: qué se decidió, qué se descartó, y el problema concreto que la decisión evita. La parte que vale en la defensa es la del medio.

---

## 1. `GestorEventos` como publisher separado

**DECISIÓN.** El rol de *Sujeto* del Observer lo cumple una clase propia, `GestorEventos`, que `Juego` contiene por composición. Los observadores (`GestorLogros`, `PanelHUD`, `ReproductorSonido`, `PanelNotificaciones`) se suscriben a ella, no a `Juego`.

**ALTERNATIVA DESCARTADA.** Que `Juego` implemente `Sujeto` directamente y mantenga adentro la lista de observadores.

**POR QUÉ.** Por dos motivos distintos:

- *Responsabilidad única.* `Juego` ya es el controlador de la partida: carga niveles, maneja la entrada, verifica victoria, coordina el undo. El registro y la notificación de observadores es una responsabilidad que cambia por motivos independientes — agregar un logro nuevo o un panel nuevo no debería obligar a tocar la clase que lleva la partida.
- *Dirección de las dependencias.* Varios eventos nacen en `Tablero`, no en `Juego`: el empuje, la caja rota, el cerrojo abierto. Si el Sujeto fuera `Juego`, `Tablero` tendría que conocer al controlador, que es una dependencia hacia arriba en la jerarquía. Dependiendo de `GestorEventos` —un servicio sin lógica de juego— `Tablero` apunta a algo estable y chico.

**ESPERABLE EN LA DEFENSA.** *"¿Por qué no hiciste que `Juego` fuera el Sujeto, si ya es el que coordina todo?"*

---

## 2. Introducir la interfaz `IJugador`

**DECISIÓN.** Se crea `IJugador`, que declara las preguntas que `Tablero` le hace al jugador antes de resolver un movimiento (`cajasEmpujables()`, `puedeAtravesar()`, `resisteDeslizamiento()`, `rompeFragiles()`, más posición y movimiento). `Jugador` la implementa, y `PowerUpDecorator` también. `Juego` y `Tablero` declaran su campo con el tipo de la interfaz, no con el de la clase concreta.

**ALTERNATIVA DESCARTADA.** Que `PowerUpDecorator` heredara de `Jugador` o de `EntidadMovil`, evitando agregar un tipo nuevo.

**POR QUÉ.**

- *Técnico.* El Decorator exige que decorador y decorado compartan un tipo y que el decorador guarde una referencia a ese tipo. Si `PowerUpDecorator extends Jugador`, cada capa arrastraría una copia del estado del jugador (posición, contadores) y habría dos objetos reclamando la misma casilla del tablero. Heredando de una clase concreta el patrón no cierra.
- *De diseño.* La interfaz obliga a declarar explícitamente qué reglas del juego son modificables por un power-up. Cada decorador concreto pisa exactamente un método y delega el resto. Si mañana se pide un power-up para el que no hay método que pisar, la interfaz avisa que esa regla todavía no está expuesta.
- *Consecuencia buscada.* Después del cambio, a `Jugador` no le entra ninguna dependencia: nadie la nombra, todos hablan por `IJugador`. Eso es lo que permite que `Juego` guarde una cadena decorada en el mismo campo donde antes guardaba al jugador crudo.

**ESPERABLE EN LA DEFENSA.** *"¿Por qué una interfaz nueva y no reusás `Jugador`?"*

---

## 3. Agregación, y no composición, entre `PowerUpDecorator` e `IJugador`

**DECISIÓN.** La relación que expresa "el decorador envuelve a un `IJugador`" se modela como agregación (rombo hueco apoyado en `PowerUpDecorator`).

**ALTERNATIVA DESCARTADA.** Composición (rombo relleno).

**POR QUÉ.** La composición significa que el todo gobierna el ciclo de vida de la parte: si muere el decorador, muere el envuelto. Acá es falso. El `Jugador` lo crea `Juego` al cargar el nivel, existe desde antes de que aparezca el power-up, y sigue existiendo cuando el power-up se vence: `GestorPowerUps.quitarExpirados()` desarma la capa y devuelve intacto lo que había adentro. Modelarlo como composición diría que al expirar el power-up se destruye el jugador, que además de incorrecto rompería la partida.

---

## 4. `HistorialMovimientos` como caretaker, separado de `Juego`

**DECISIÓN.** Memento con sus tres roles repartidos: `Juego` es el *originator* (sabe hacerse la foto con `crearMemento()` y volver a una con `restaurar()`), `Memento` es la foto (datos, sin comportamiento) y `HistorialMovimientos` es el *caretaker*: almacena los mementos sin abrirlos. Las reglas del enunciado —guardar 15 movimientos, poder retroceder de a 5, permitir 3 usos consecutivos— viven en el caretaker.

**ALTERNATIVA DESCARTADA.** Que `Juego` mantuviera su propia pila de estados, o exponer getters públicos de posición, cajas y contadores para que otra clase armara el snapshot desde afuera.

**POR QUÉ.**

- *Encapsulamiento.* La segunda alternativa obliga a abrir el estado interno de `Juego` al mundo entero solo para poder deshacer: cualquiera podría leerlo y modificarlo. Con Memento el estado sale encapsulado, se guarda, y vuelve a entrar, y nadie salvo `Juego` sabe interpretarlo. El caretaker apila y desapila objetos cuyo contenido no puede ver.
- *Separación de responsabilidades.* "Saber restaurar" y "decidir cuándo y cuánto restaurar" son dos cosas distintas. Por eso los límites de 15/5/3 están en `HistorialMovimientos` y no desparramados en `Juego`: si mañana cambian los números, se toca una sola clase.

**ESPERABLE EN LA DEFENSA.** *"¿Quién es el caretaker y por qué no puede leer el memento?"*

---

## 5. `CasillaPowerUp` como subclase de `Celda`

**DECISIÓN.** El power-up tirado en el nivel es una subclase más de `Celda`, al mismo nivel que `TerrenoResbaladizo` o `CasillaCerrojo`.

**ALTERNATIVA DESCARTADA.** Agregarle a `Celda` un atributo `tipoPowerUp` que pueda ser nulo, y resolver con un `if` en el código de movimiento.

**POR QUÉ.** Abierto/cerrado. El tablero ya resuelve el comportamiento de cada casilla por polimorfismo. Un flag en la clase base obligaría a modificar `Celda` y el código de movimiento cada vez que aparece un tipo nuevo de casilla, y dejaría a todas las celdas del nivel cargando un campo que casi ninguna usa. Como subclase, el power-up entra sin modificar nada de lo que ya funciona, exactamente igual que entró el terreno resbaladizo.

---

## 6. `TipoPowerUp` como intermediario entre la celda y el decorador

**DECISIÓN.** `CasillaPowerUp.recoger()` devuelve un valor del enum `TipoPowerUp`. Es `GestorPowerUps.aplicar()` el que traduce ese valor al decorador concreto que corresponde.

**ALTERNATIVA DESCARTADA.** Que `CasillaPowerUp` guardara e instanciara directamente el `PowerUpDecorator`.

**POR QUÉ.**

- Los niveles se cargan desde archivos .txt: de ahí sale un carácter, que mapea natural a un valor de enumeración, no a una instancia de clase.
- Concentra en un solo lugar el conocimiento de qué clase corresponde a cada tipo. Si cambia la forma de construir un decorador, se toca `GestorPowerUps` y nada más; el resto del sistema maneja solo el enum.
- Deja a la jerarquía de `Celda` sin ninguna dependencia hacia el subsistema de decoradores. Las celdas no saben que los power-ups se implementan con Decorator.

---

## Pendiente de resolver al codear

- ~~**El `Memento` todavía no guarda los power-ups activos.**~~ **RESUELTO** — ver decisión 8.
- ~~**`Jugador` tiene que implementar los 8 métodos de `IJugador`**, no solo `mover()`.~~ **RESUELTO** — implementa los 8, y la interfaz además creció; ver decisión 9.
- ~~**Revisar si `Caja` debería heredar de `EntidadMovil`.**~~ **RESUELTO** — sí hereda; ver decisión 7.

---

## Decisiones tomadas durante la implementación

*(Mismo formato. Todo lo que sigue es desviación del diagrama o cosa que el diagrama no contemplaba.)*

### 7. `Caja` pasa a heredar de `EntidadMovil`

**DECISIÓN.** `Caja extends EntidadMovil`, igual que `Jugador`.

**ALTERNATIVA DESCARTADA.** Dejar `Caja` como jerarquía suelta, como estaba en el diagrama, y darle sus propios `fila`, `columna` y `mover()`.

**POR QUÉ.** La alternativa es copiar y pegar: `fila`, `columna`, los dos getters y `mover(fila, columna)` quedaban duplicados palabra por palabra en dos jerarquías. `EntidadMovil` ya es exactamente la abstracción "cosa que ocupa una celda del tablero y se desplaza", y la caja lo es tanto como el jugador. Era el pendiente 3 y se resolvió a favor de la herencia.

**OJO EN LA DEFENSA.** Esto cambia el diagrama: hay que redibujar la flecha de `Caja` a `EntidadMovil` antes de entregar.

---

### 8. El `Memento` guarda bastante más que lo que decía el diagrama

**DECISIÓN.** Además de posición del jugador, cajas, movimientos y empujes, la foto guarda: resistencia y rotura de cada caja, qué muros estaban abiertos, qué cerrojos activados, qué power-ups del nivel ya estaban recogidos, y la cadena de power-ups activa sobre el jugador (`List<EstadoPowerUp>`, con tipo, duración y usos restantes de cada capa).

**ALTERNATIVA DESCARTADA.** La del pendiente 1: guardar solo `List<TipoPowerUp>` y rearmar los decoradores con sus valores por defecto.

**POR QUÉ.** Un memento tiene que poder reponer *todo* lo que un movimiento pudo haber cambiado, o el undo deja el tablero mezclado: cajas de hace 5 movimientos conviviendo con un muro que se abrió hace 2. Y guardar solo el tipo del power-up no alcanza: si agarré Fuerza hace 12 movimientos y deshago 5, me tiene que quedar la duración que tenía en ese momento, no una Fuerza nueva de 15. Por eso `EstadoPowerUp` lleva los tres datos.

**DÓNDE VIVE `EstadoPowerUp`.** En el paquete `powerups`, no en `memento`. Así la dependencia va `memento → powerups` y el subsistema de power-ups no tiene que conocer al Memento.

**ESPERABLE EN LA DEFENSA.** *"Agarrás un power-up y hacés undo: ¿qué pasa?"* — Está cubierto por una prueba: la cadena se desarma y el power-up vuelve a estar en el tablero sin recoger.

---

### 9. `IJugador` creció seis métodos

**DECISIÓN.** A los 8 del diagrama se les sumaron `getMovimientos()`, `getEmpujes()`, `registrarEmpuje()`, `posicionar(fila, columna)`, `restaurarContadores(mov, emp)` y `notificarAtravesoPared()`.

**POR QUÉ.** Es la consecuencia directa de la decisión 2, y conviene tenerla clara: si `Juego` y `Tablero` declaran el campo como `IJugador`, entonces *todo* lo que alguien necesite del jugador tiene que pasar por la interfaz. El HUD muestra los contadores y el Memento los guarda, así que los contadores tienen que ser visibles por ahí. Lo mismo `posicionar()`: el deslizamiento y la restauración reubican al jugador sin que eso cuente como un movimiento nuevo, y `mover(Direccion)` no sirve porque incrementa el contador.

**LO QUE NO SE HIZO.** No se bajaron los contadores a `Juego` para achicar la interfaz. El diagrama los pone en `Jugador` y es donde corresponden: son datos del jugador, no del controlador.

---

### 10. `notificarAtravesoPared()`: la consulta no tiene efectos

**DECISIÓN.** `puedeAtravesar(Celda)` es una consulta pura, no descuenta nada. `PowerUpFantasma` descuenta una pared recién cuando el tablero le avisa, con `notificarAtravesoPared()`, y eso pasa después de que el movimiento se concretó.

**ALTERNATIVA DESCARTADA.** Descontar adentro de `puedeAtravesar()`, que es lo que sale natural.

**POR QUÉ.** Separación consulta/comando. El tablero pregunta `puedeAtravesar()` mientras está evaluando el movimiento, y ese movimiento todavía puede caerse por otra regla (una caja atrás sin lugar, el borde del tablero). Si la pregunta descontara, un intento cancelado igual te comería una pared.

---

### 11. `Tablero.setJugador()`

**DECISIÓN.** El tablero expone un setter del jugador, y `Juego` se lo reasigna cada vez que agrega o quita un power-up.

**POR QUÉ.** Otra consecuencia del Decorator, y vale la pena saber explicarla: envolver al jugador no modifica el objeto, **crea otro** que es la nueva punta de la cadena. El tablero tiene que preguntarle a la punta, porque es la que sabe que ahora se empujan dos cajas. Si se quedara con la referencia vieja, el power-up no tendría ningún efecto sobre el movimiento.

---

### 12. `moverJugador()` devuelve un `ResultadoMovimiento` en vez de `void`

**DECISIÓN.** Devuelve un objeto chico con tres datos: si se movió, si hubo empuje, y qué power-up se recogió.

**POR QUÉ.** `Juego` saca la foto del estado *antes* de mover y la apila *después*, pero solo si el movimiento realmente pasó. Si no, el historial se llenaría de estados repetidos por cada vez que el jugador choca contra una pared, y un undo de "5 movimientos" retrocedería 5 intentos fallidos, o sea nada. Con `void`, `Juego` tendría que deducirlo comparando estados.

---

### 13. `TipoEvento.NIVEL_INICIADO`

**DECISIÓN.** Se agregó un noveno valor al enum de eventos.

**POR QUÉ.** `LogroSinDeshacer` lleva estado dentro del nivel (si se usó undo o no) y tiene que olvidarlo cuando arranca el siguiente. La opción era que `Juego` llamara a un `reiniciar()` de `GestorLogros`, pero eso obliga al controlador a conocer al gestor de logros, que es justo lo que el Observer vino a evitar: hoy `Juego` publica y no sabe quién escucha. Un evento más es más barato que una dependencia más.

---

### 14. La victoria se evalúa por destinos ocupados, no por cajas en destino

**DECISIÓN.** Se gana cuando **todas las casillas destino tienen una caja encima**.

**ALTERNATIVA DESCARTADA.** Recorrer las cajas y pedir que todas estén sobre un destino.

**POR QUÉ.** La alternativa rompe con dos elementos del propio enunciado. La caja llave termina apoyada sobre un **cerrojo**, no sobre un destino: con la otra regla, un nivel con caja llave sería inganable. Y una caja frágil se puede **romper**: con la otra regla, romper una caja te daría la victoria más fácil, en vez de dejar el nivel irresoluble, que es lo lógico. Contando destinos ocupados los dos casos salen bien solos.

---

### 15. Una caja rota se marca, no se borra

**DECISIÓN.** Al romperse, la caja queda en la lista del tablero con un flag `rota`. `cajaEn()` la ignora para colisiones y la vista no la dibuja.

**POR QUÉ.** Si se sacara de la lista, el undo no tendría de dónde devolverla, y además se desalinearían las listas paralelas del memento (la foto de las cajas se guarda en el orden de carga del nivel, que no cambia durante la partida). Marcarla cuesta un booleano y deja el undo trivial.

---

### 16. El deslizamiento, interpretado

**DECISIÓN.** Lo que entra en terreno resbaladizo sigue avanzando en la misma dirección hasta chocar con algo o hasta pisar una casilla que no resbale. Vale para las cajas (que es lo que pide el enunciado) y también para el jugador, porque el diagrama tiene `resisteDeslizamiento()` en `IJugador` y si el jugador no resbalara ese power-up no tendría sentido.

**POR QUÉ ASÍ.** El enunciado dice que la caja se desliza "hasta chocar con otro objeto o llegar a un espacio vacío". Tomado literal no cierra: en este tablero *todo* el piso libre es espacio vacío, así que la caja no se movería nunca. La lectura que se adoptó es la que le da sentido a la regla: se desliza mientras esté sobre hielo y para al salir de él o al chocar.

**DETALLE.** El jugador que resbala **no empuja**: si hay una caja adelante, el deslizamiento se frena. Y el deslizamiento usa `posicionar()`, no `mover()`, así que todo el patinazo cuenta como **un solo movimiento**.

---

### 17. `manejarEntrada()` recibe una `Direccion`, no una tecla

**DECISIÓN.** La firma del diagrama es `manejarEntrada(tecla)`. En el código el controlador recibe un valor del enum `Direccion`, y es `VentanaPrincipal` la que traduce el `KeyEvent`.

**POR QUÉ.** Es lo que sostiene el MVC: ninguna clase del modelo ni del controlador importa nada de `java.awt` ni de `javax.swing`. Si el controlador recibiera un `KeyEvent`, el modelo quedaría atado a Swing y no se podría probar sin levantar una ventana — de hecho toda la batería de pruebas de este trabajo corre sin interfaz gráfica.

---

### 18. Un solo grupo de cerrojos por nivel

**DECISIÓN.** `CasillaCerrojo` tiene una `List<Muro>`, pero el cargador de niveles asocia todos los muros del nivel a todos sus cerrojos.

**POR QUÉ.** El formato `.txt` de un carácter por casilla no tiene cómo expresar "este cerrojo abre estos muros". La estructura de datos ya soporta grupos; lo que falta es formato, no diseño. Si hiciera falta, se agrega una cabecera al archivo y no se toca ninguna clase.

---

### 19. `Juego` es Singleton

**DECISIÓN.** `Juego` tiene constructor privado y se obtiene con `Juego.getInstance()`. Como la instancia es única, ella misma crea y posee a `GestorEventos` y `GestorPowerUps` (los expone con `getGestorEventos()`). `reiniciarInstancia()` existe solo para pruebas.

**POR QUÉ.** Hay una sola partida por aplicación y varias clases necesitan hablar con ella; el Singleton garantiza que no haya dos controladores con dos historiales distintos. El costo es el estado global, y por eso la vista y los paneles igual reciben `juego` por constructor en lugar de llamar a `getInstance()` por su cuenta.

---

### 20. Factory Method para celdas, cajas y power-ups

**DECISIÓN.** Los `switch` que instanciaban clases concretas desaparecen: `FabricaCelda`, `FabricaCaja` y `FabricaPowerUp` son los creadores, y cada `FabricaXxx` concreta decide qué subclase instancia. `DirectorNivel` y `GestorPowerUps` guardan un mapa carácter/tipo → fábrica.

**POR QUÉ.** Sumar un tipo de celda, caja o power-up es sumar una clase fábrica y registrarla; ya no se edita el código que arma el tablero ni el gestor de power-ups (principio abierto/cerrado).

---

### 21. Builder para armar el `Tablero`

**DECISIÓN.** `Tablero` ya no se construye ni lee archivos: es el producto. `TableroBuilder` lo arma paso a paso (`dimensiones`, `agregarCelda`, `agregarCaja`, `colocarJugador`, `build`) y `DirectorNivel`, que es el único que conoce el formato `.txt`, dirige los pasos. El constructor de `Tablero` es package-private.

**POR QUÉ.** `Tablero` mezclaba tres responsabilidades (leer archivos, interpretar el formato y aplicar reglas). Ahora solo aplica reglas, y el builder garantiza que un nivel sin jugador o con celdas sin definir nunca llegue a ser un `Tablero`.

---

### 22. El modelo deja de usar `java.awt.Point` (SOK-15)

**DECISIÓN.** `Memento` guarda la posición del jugador como dos `int` (`filaJugador`, `columnaJugador`) en lugar de un `Point`. `Juego` ya no importa nada de AWT.

**ALTERNATIVA DESCARTADA.** Dejar `Point` y defenderlo como "una clase de datos de dos `int` públicos, no un componente gráfico".

**POR QUÉ.** La regla del MVC de este trabajo es que el modelo y el controlador no dependen de la biblioteca gráfica, y `Point` era la única excepción. Además `Point` usa `x`/`y` (columna/fila) mientras todo el resto del modelo habla de fila y columna, y la traducción ya era un lugar donde equivocarse (`posicionar(pos.y, pos.x)`). Costó cinco líneas y deja la regla sin asteriscos.

**OJO EN LA DEFENSA.** Cambia el diagrama: `-posJugador: Point` pasa a `-filaJugador: int` y `-columnaJugador: int` (ver SOK-19).

---

### 23. La resistencia de la frágil viaja en la fábrica, no en la caja (SOK-12)

**DECISIÓN.** `FabricaCajaFragil` recibe la resistencia en su constructor y `DirectorNivel` registra tres instancias: `F` (3), `G` (5) y `H` (8).

**ALTERNATIVA DESCARTADA.** Un dígito al lado de la `F` en el `.txt`, o subclases `CajaFragilReforzada`.

**POR QUÉ.** El dígito rompería la regla "un carácter = una casilla" del formato y obligaría a reescribir el lector. Las subclases serían clases que solo cambian un número. Con una fábrica parametrizada, sumar otra dureza es una línea en `DirectorNivel`, y ni `CajaFragil` ni el tablero se enteran de que el formato cambió: es Factory Method haciendo exactamente su trabajo.

---

### 24. "Partida nueva" la resuelve la vista, no `Juego` (SOK-14)

**DECISIÓN.** El botón *Partida nueva* llama a `VentanaPrincipal.partidaNueva()`, que hace `gestorLogros.reiniciar()` y `juego.iniciarJuego()`. `PanelHUD` recibe esa acción como un `Runnable`.

**ALTERNATIVA DESCARTADA.** Un `Juego.partidaNueva()` que reinicie los logros, o un evento `PARTIDA_NUEVA`.

**POR QUÉ.** `Juego` no conoce a `GestorLogros` (decisión 13), y no tiene por qué empezar a conocerlo. Quien crea y posee al gestor de logros es la ventana, así que es la que sabe borrarlo. Un evento nuevo también funcionaría, pero solo tendría un suscriptor y sería indirección sin beneficio. El HUD recibe un `Runnable` para no depender de `GestorLogros` él tampoco.

---

### 25. Los niveles se prueban con un solucionador sobre el juego real (SOK-11)

**DECISIÓN.** `test/Solucionador.java` hace una búsqueda en anchura usando `Juego.manejarEntrada()` para avanzar y `Juego.restaurar(Memento)` para volver atrás. Con él se demuestra que los niveles 4 a 7 no tienen solución sin su power-up: el solucionador recorre todos los estados alcanzables y, después de cada paso, le saca al jugador la cadena de decoradores.

**ALTERNATIVA DESCARTADA.** Un solucionador con sus propias reglas de movimiento, o confiar en que "a ojo" el nivel no sale sin el power-up.

**POR QUÉ.** Un solucionador con reglas propias prueba que *su* versión del juego no tiene solución, no la del juego real. Usando el controlador y el Memento, cualquier diferencia es imposible. Y es un buen argumento para la defensa: el Memento, pensado para el undo, sirve también para explorar el árbol de jugadas sin una sola línea de código nuevo en el modelo.

---

## Estructura de paquetes

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

**La regla que ordena todo esto:** las flechas de dependencia van de `vista` y `controlador` hacia `modelo`, nunca al revés. `modelo`, `eventos`, `logros`, `powerups` y `controlador` no importan **una sola clase de Swing**. Es verificable de una:

```bash
grep -rl "javax.swing" src/sokoban/modelo src/sokoban/eventos src/sokoban/logros src/sokoban/powerups src/sokoban/controlador
```

No devuelve nada. Toda la batería de pruebas del modelo corre sin levantar una ventana.

**Y tampoco AWT:** hasta la decisión 22, `Memento` y `Juego` importaban `java.awt.Point`. Ya no, así que esto tampoco devuelve nada:

```bash
grep -rl "import java.awt" src/sokoban/modelo src/sokoban/eventos src/sokoban/logros src/sokoban/powerups src/sokoban/controlador
```

---

## Patrones aplicados, y dónde mirarlos

| Patrón | Clases | Para qué |
|---|---|---|
| **Observer** | `GestorEventos`, `Observador`, `GestorLogros`, `PanelHUD`, `PanelNotificaciones`, `ReproductorSonido`, `PanelTablero` | Que el modelo avise qué pasó sin saber quién escucha |
| **Memento** | `Juego` (originator), `Memento`, `HistorialMovimientos` (caretaker) | Undo sin abrir el estado interno de `Juego` |
| **Decorator** | `IJugador`, `PowerUpDecorator` y sus 4 subclases | Power-ups que cambian reglas sin tocar `Jugador` |
| **Singleton** | `Juego` | Una única partida por aplicación |
| **Factory Method** | `FabricaCelda`, `FabricaCaja`, `FabricaPowerUp` y sus fábricas concretas | Crear celdas, cajas y decoradores sin `switch` ni `new` en quien los usa |
| **Builder** | `DirectorNivel` (director), `TableroBuilder` (builder), `Tablero` (producto) | Separar la construcción del tablero de sus reglas |
| **Strategy (implícito)** | `Logro` y sus 5 subclases | Una condición de desbloqueo por clase |
| **MVC** | `modelo` / `vista` / `controlador` | Modelo testeable sin interfaz gráfica |

---

## Para el informe técnico final

- [x] Estructura de paquetes del proyecto — hecha más arriba
- [x] Apartado de MVC: qué clases son modelo, cuáles vista, cuál el controlador, y por qué el modelo no conoce a Swing — hecho más arriba (decisión 17 + estructura de paquetes)
- [x] Reemplazar las figuras de color por imágenes de dominio público (`assets/imagenes/`) y cargar los `.wav` (`assets/sonidos/`). Imágenes de Kenney (CC0), sonidos generados por el equipo (CC0); ver `assets/CREDITS.txt`
- [ ] Principios SOLID y GRASP aplicados, con el ejemplo concreto de cada uno
- [ ] Diagrama de clases final (verificar si el profe acepta draw.io o exige StarUML)
- [ ] Capturas del juego
- [ ] Declaración del uso de herramientas de IA (lo exige el enunciado)
- [ ] Instrucciones para ejecutar, también en el README del repo
