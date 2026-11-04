# Simulador y análisis de ruleta europea

Aplicación Java que reúne toda la investigación sobre la estructura estadística
del azar en la ruleta europea: datos reales, generación de bases locales,
fórmulas de mapa de calor, estrategias de apuesta y análisis estadístico.

## Requisitos
- Java 17 o superior (probado con OpenJDK 25).

## Compilar
```bash
javac -d build src/main/java/ruleta/*.java
```

## Ejecutar
```bash
java -cp "build:src/main/resources" ruleta.App
```
(En Windows usar `;` en vez de `:` en el classpath.)

## Menú
1. Cargar muestra **real limpia** (465 registros, sin bloques duplicados).
2. Cargar muestra **real cruda** (542).
3. **Generar/cargar base local de 1.000.000** (se guarda en `base_1M.csv`; si ya
   existe, se recarga en vez de regenerar).
4. **Tabla de rachas** por dimensión (color, paridad, alto/bajo, docena, columna, updown).
5. **Firma de updown**: P(continuar | racha de k) — la única dimensión con sesgo real.
6. **Convergencia al gris**: proporción de rojo vs la banda 1/√n.
7. **Mapa de calor** (fórmulas V1_CRECIENTE, V2_GEOMETRICA, CALENDARIO) apostando
   a los 18 plenos más calientes; muestra que todas convergen a EV −2.7% (el "embudo").
8. **Estrategias**: anti-bajadas de updown (progresión acotada) y columnas-rompe.
9. Cargar un archivo externo (csv/txt) con una secuencia de números.

## Datos incluidos (`src/main/resources`)
- `real_542.csv` — captura real de casino (bloque duplicado grande ya removido).
- `real_465_clean.csv` — la anterior sin ningún k-grama repetido.

## Estructura del código (`src/main/java/ruleta`)
- `Ruleta.java` — modelo de la ruleta y mapeo de dimensiones; generador reproducible.
- `Datos.java` — carga/escritura de CSV, base local de 1M, limpieza de repetidos.
- `Rachas.java` — detección de rachas y de rachas de alternancia (noupdown).
- `MapaCalor.java` — mapa de calor puntual con las tres fórmulas + Ajuste 1.
- `Estrategias.java` — esquemas de apuesta con su EV.
- `Analisis.java` — tablas de rachas, firma de updown, convergencia al gris.
- `App.java` — menú interactivo.

## Documentación
- `DOCUMENTACION.md` — hallazgos confirmados, el "embudo", azar vs azar forzado, dato de campo.
- `IDEAS_FALLIDAS.md` — hipótesis probadas sin ventaja (registro documental).
- `CATALOGO.md` — catálogo vivo de piezas, fórmulas y observaciones.

## Conclusión central
Sobre un sistema de giros independientes, ninguna fórmula que use el pasado
supera la ventaja de la casa (−2.70%): el "embudo". La única dimensión con
dependencia estadística real es **updown** (reversión a la media numérica), pero
su sesgo no se traduce en una apuesta cobrable sin que la tabla de pagos lo
diluya. A escala de ~500 giros, un casino en línea real es indistinguible de
`random`, por lo que las simulaciones son trasladables a esa escala.
