# Investigación sobre estructura del azar en la ruleta europea

Registro completo de hallazgos, ideas y fórmulas desarrollados durante la
investigación. La aplicación Java (`ruleta.App`) implementa todo lo aquí descrito.

## Premisa del estudio

El sistema observado no es solo "la bola" (que no tiene memoria), sino el
conjunto **observador + historial + ruleta**. La hipótesis es que el azar, aun
siendo impredecible giro a giro, se ordena de forma medible a nivel agregado, y
que podrían existir regularidades aprovechables. Caso de aplicación: ruleta
europea (37 casillas). Se trabaja primero con azar simulado (`random`) y se
contrasta con datos reales de casino.

## Dimensiones analizadas

| dimensión | lados | nota |
|-----------|-------|------|
| color | rojo / negro | el 0 no participa |
| paridad | par / impar | |
| alto/bajo | 19-36 / 1-18 | |
| updown | sube / baja respecto al anterior | **única con sesgo real** |
| noupdown | alternancia que inicia up / down | derivada de updown |
| docena | 3 lados (ternaria) | |
| columna | 3 lados (ternaria) | |
| decena | 1-9,10-19,20-29,30-36 | imprecisa, pendiente |

Propiedades de cada número verificadas contra la ruleta europea estándar.

## Hallazgos confirmados

1. **Ley geométrica de rachas.** En dimensiones binarias (serie resuelta, sin
   ceros) la razón count(k+1)/count(k) = 0.5000. Sobre la serie cruda (el cero
   rompe) = 0.4865 = 18/37. En ternarias ≈ 1/3 (12/37). Updown DECAE acelerado.
2. **Estabilidad agregada.** Conteos de rachas estables ±3% entre poblaciones de
   1M. En ventanas de ~200-500 la estabilidad baja (CV 10-20% niveles 1-3; peor
   en niveles altos). El agregado se estabiliza mucho antes que cada número.
3. **Convergencia al gris (ley 1/√n).** La proporción de color → 50% con desvío
   acotado por ~1/(2√n). A 500k giros el desvío es 0.018%. Predicción correcta y
   cuantificable del usuario. Es predictivo sobre el AGREGADO, no sobre el giro.
4. **Simetría por lado.** Rojo≈negro, sube≈baja, inicia-up≈inicia-down,
   docena1≈2≈3. Todas las dimensiones son simétricas entre sus lados.
5. **Memoria del sistema = complementariedad + acoplamiento.** Cada dimensión se
   equilibra con su complemento (identidad contable) y converge 50/50 (LGN). Las
   dimensiones comparten los mismos giros (acoplamiento). La "memoria" está en el
   conjunto de datos, no en la bola.
6. **UPDOWN tiene sesgo REAL (reversión a la media numérica).** P(romper | racha
   de k bajadas) crece: k1≈0.64, k2≈0.75, k3≈0.81, k4≈0.84, k5≈0.87, k6≈0.89.
   Es la ÚNICA dimensión cuya probabilidad por giro cambia con la racha. Peso por
   evidencia ~175× el de las dimensiones simétricas.
7. **El azar se auto-ordena en estructura (cristal de sal).** Las rachas se
   organizan en capas (niveles) de tamaño geométrico decreciente; forma
   piramidal simétrica para binarias, aguja para updown.

## El "embudo"

Cualquier regla causal (mapa de calor, deuda, espera acumulada) que elija números
usando solo el pasado, sobre un sistema de giros independientes, converge a la
ventaja de la casa (EV −2.70%), por absurda o sofisticada que sea. La única
excepción es una dimensión con dependencia real (updown), pero su sesgo no se
puede concentrar en una apuesta cobrable sin que el pago lo diluya.

## Azar vs. azar forzado (dominios de validez)

- **Azar ingenuo** (random simple): tiene la estructura descrita; el estudio
  aplica. Es el terreno de simulación.
- **CSPRNG / RNG certificado**: sigue siendo justo e independiente, por lo que
  EXHIBE las mismas leyes (rachas geométricas, reversión updown). Lo que cambia
  entre generadores es la predecibilidad desde el estado interno, NO la
  estadística de rachas. El 2.7% sale de la TABLA DE PAGOS (35:1 cuando lo justo
  sería 36:1), no de manipular resultados.
- **Resultados manipulados**: ya no es azar; sería fraude. A ~500 giros no se
  puede detectar.
- **Azar ≠ impredecible.** El azar exige impredecibilidad + imparcialidad. Un
  "cristal forzado" (reordenado con intención) deja de ser azar aunque sea
  impredecible. Método de detección: comparar la FORMA del cristal (distribución
  de rachas, firma de updown) con la del azar natural; no hace falta predecir
  giros individuales.

## Dato de campo

542 giros reales de un casino en línea; tras quitar bloques duplicados de la
captura quedan 465. A esa escala NO se distinguen de `random.random`: frecuencias
por número, conteos de rachas (6 dimensiones), firma de updown y mapas de calor
caen dentro del rango aleatorio. Conclusión: a ~500 giros las simulaciones son
trasladables a un casino real, sin alta precisión. Para más, hace falta muestra
grande sin duplicar.

Ver `IDEAS_FALLIDAS.md` para el registro de hipótesis probadas sin ventaja.
