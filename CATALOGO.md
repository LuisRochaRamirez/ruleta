# Catálogo de piezas del motor de scoring

Registro vivo de **cálculos (A)**, **heurísticas (B)**, **pesos (C)** y
**condicionantes (D)**. Lo actualizamos durante la sesión: cada pieza tiene un
estado y una definición. Convención de signo en todas las features:

> `valor > 0` → inclina a **True** · `valor < 0` → inclina a **False** · `~0` → neutro

Lados por dimensión: color(True=rojo), parity(True=par), highlow(True=alto),
updown(True=sube).

---

## Base verificada
- ✅ Propiedades por número (color/paridad/altura) validadas contra ruleta europea
  estándar. Rojos y negros coinciden con fuente oficial. Cero = no participa.

---

## A) Cálculos (features) — implementados en `engine.py`

| id | nombre | definición | estado |
|----|--------|------------|--------|
| A1 | `f_freq_deviation` | `(0.5 - p_true)*2`; desvío de la frecuencia acumulada respecto a 50%. Inclina al lado atrasado. | activa |
| A2 | `f_current_streak_signed` | longitud de la racha actual, con signo del lado. | activa |
| A3 | `make_f_gap_since_long(min_len)` | giros desde el fin de la última racha ≥ min_len, normalizado /12.5. | activa |
| A4 | `f_progress` | posición relativa en la muestra (0..1). Para gates. | activa |

### Ley geométrica de rachas — MEDIDA CON PRECISIÓN (1M, 5 pobs)
- color/parity/highlow: razón count(k+1)/count(k) = **0.4865 ± 0.002**, estable
  para k=1..7. NO es 0.5: es exactamente 18/37 (prob. de repetir lado dado que
  el cero rompe). Corrección al matiz del usuario: la razón es LIGERAMENTE
  INFERIOR a 0.5 (no superior), por efecto del cero.
  → "valor esperado estable" para A5 = 0.4865, no 0.5.
- updown: la razón NO es constante, DECAE: 0.416→0.263→0.195→0.155→0.122...
  (rachas largas mucho más raras por la fuerte reversión a la media numérica).
  updown se rige por ley propia, distinta a las 3 dimensiones binarias.

### Ley de gaps (observación del usuario, confirmada)
- Espacio mínimo entre rachas = 1. Rachas con gap>1 son mucho más frecuentes
  que con gap=1. La distancia media entre rachas CRECE con la longitud de la
  racha (medido en rachas de 4+). → ya visto: media ~12.5 (≥4), ~30 (≥5).

### Ideas de cálculos pendientes de implementar
- A5 — deuda de conteo de rachas por longitud: observado hasta ahora vs. el
  esperado geométrico. Déficit de un nivel → posible "presión" a que aparezca.
- A6 — indicador de nº extremo (qué tan cerca de 0 o 36 está el nº actual),
  insumo para B3/B4 como filtro.

---

## B) Heurísticas (reglas condicionales que emiten inclinación)

Observaciones del usuario clasificadas. Lado updown: True=sube.

| id | heurística | definición | estado |
|----|-----------|------------|--------|
| B1 | reversión updown tras bajadas desde nº muy bajo | si vienes bajando y estás en nº muy bajo → inclina a SUBIR | confirmada (deriva de updown; hit alto) |
| B2 | reversión updown tras subidas desde nº muy alto | si vienes subiendo y estás en nº muy alto → inclina a BAJAR | confirmada |
| B3 | posición extrema baja → subir | estar en nº muy bajo (indep. de historia) → inclina a SUBIR | confirmada (info del nº actual) |
| B4 | posición extrema alta → bajar | estar en nº muy alto → inclina a BAJAR | confirmada |
| B5 | deuda de racha por nivel geométrico | ver A5: usar la ley k+1≈½·k para detectar déficit/exceso de rachas de un nivel y emitir inclinación | por implementar |
| B6 | anti-racha de bajadas con progresión acotada (updown) | tras una racha de 4 bajadas, apostar a "sube" (fin de racha) con progresión de stake ACOTADA (ej. 1-3-8-20, tope 20). En azar limpio da balance positivo con pérdida máx fija por ciclo (32 USD). | confirmada en simulación, CON condiciones (ver notas) |

### Notas de B6 (condiciones de validez — honestidad del hallazgo)
- Señal base REAL: updown tiene sesgo de reversión (P(sube tras N bajadas) > 50%)
  porque los números "se agotan" (a diferencia del color, que es moneda justa).
- Simulación: 50 seeds × 1M, progresión [1,3,8,20]. 0 seeds perdedoras; solo 49
  de 290.276 ciclos perdieron (racha ≥9 bajadas = −32 USD fijo). Balance +~7k/1M.
- Ventaja de la progresión ACOTADA vs. martingala clásica: pérdida máxima fija y
  conocida (32 USD/ciclo), sin riesgo de ruina exponencial. Punto correcto del usuario.
- CONDICIONES/limitaciones (reconocidas por el usuario):
  (1) requiere apostar "sube/baja", que el casino NO ofrece -> solo sirve como
      FILTRO hacia una apuesta real (traducción aún no lograda).
  (2) riesgo operativo: en casino real esperar la racha puede tardar horas/días;
      sesiones que se cortan pueden hacer perder el evento.
  (3) válida SOLO en azar limpio; fracasaría si el RNG del casino se cubre de
      estos patrones.
- PENDIENTE de medir: hit-rate exacto de "sube tras 4 bajadas" y EV teórico del
  esquema, para separar "ventaja estructural real" de "azar favorable de muestra".

### Contexto en la literatura (verificado por búsqueda)
- El ESQUEMA (martingala con disparador + progresión acotada/truncada) SÍ está
  documentado desde s.XVIII (martingala) y como "progresión negativa con tope".
- La DIMENSIÓN updown (apostar nº mayor/menor que el anterior) NO está
  documentada como estrategia de ruleta: porque NO es una apuesta que exista.
- Teoremas clásicos ("ningún sistema vence a la casa") aplican a apuestas sobre
  eventos INDEPENDIENTES y SIMÉTRICOS (rojo/negro). arXiv 1602.06943 añade:
  con capital y stakes limitados, en ruleta ideal el jugador siempre pierde.
- CLAVE: updown escapa a esos teoremas porque NO es independiente ni simétrico
  (reversión numérica real). De ahí que el esquema gane en simulación. El muro
  persiste: updown no tiene ventanilla de apuesta real. El hallazgo genuino es
  haber identificado una dimensión con estructura estadística fuera del alcance
  de los teoremas clásicos.

### Principios rectores (observaciones del usuario, no heurísticas apostables)
- P1: ocurrencias de eventos NO son predecibles a nivel individuo dentro de
  muestras de cierto tamaño (solo el agregado es estable). [límite reconocido]
- P2: apuestas con fórmulas simples → ~50% efectividad (línea base a superar).

### Ideas de heurísticas pendientes
- traducir B1–B4 a un FILTRO del próximo número que se cruce con otras dims.

---

## C) Pesos
- Cada término lleva un `weight`. Se definirán/optimizarán por el buscador.

### PESO POR DIMENSIÓN — derivado de evidencia (observación del usuario confirmada)
- Métrica: peso(dim) = Σ_k freq(k)·|P_romper(k) − 0.5| = "ventaja esperada por
  giro si apostáramos a la ruptura". Medido (1M):
    color 0.0010 · parity 0.0010 · highlow 0.0009 · updown **0.1761**
  -> peso relativo: color/parity/highlow ≈ 0.005-0.006 ; **updown = 1.000**.
- CONCLUSIÓN: updown merece ~175× el peso de las otras dimensiones. Confirma la
  intuición del usuario. Color/paridad/alto-bajo son ruido (moneda justa).
- El sesgo de updown CRECE con la longitud de racha (eventos raros = más
  favorables): P(romper) = 0.63(k1) → 0.75(k2) → 0.81(k3) → 0.84(k4) →
  0.87(k5) → 0.89(k6). Respalda B6 y "peso mayor a rachas largas de updown".
- Regla de diseño: los pesos se derivan de |P_romper−0.5| medido, NO a mano.

---

## D) Condicionantes de activación (gates) — en `engine.py`
| id | nombre | definición | estado |
|----|--------|------------|--------|
| D0 | `state_always` | siempre activo. | activa |
| D1 | `make_state_progress_gate(p)` | activo solo si progreso de muestra ≥ p (p.ej. 0.60). | activa |

---

## Observaciones confirmadas (base empírica de la sesión)
1. Conteos de rachas por longitud: estables ±3% entre poblaciones (1M). ✔
2. Gaps medios por longitud (≥k): muy estables entre poblaciones. ✔
3. updown tiene señal condicional fuerte y persistente (reversión a la media
   numérica) — real pero no apostable por sí sola; sirve como **filtro**.
4. Proyecciones simples de updown sobre docenas/columnas/color NO superan el
   break-even fuera de muestra (puerta cerrada, no veredicto global).

## Ideas del usuario sobre estructura de rachas (verificadas)
5. ACOPLAMIENTO (idea 1): cada giro es un escalón que prolonga o rompe rachas
   en TODAS las dimensiones a la vez; las longitudes/dimensiones no son
   independientes entre sí. [principio estructural para el cruce multidimensional]
6. SIMETRÍA POR LADO (idea 2): la razón count(k+1)/count(k) se mantiene al
   aislar un solo lado (solo rojas / solo negras, etc.). CONFIRMADA (~0.50).
7. AUTO-SIMILARIDAD (idea 3): la serie de CAMBIOS (cambio=1/repite=0) también
   cumple la ley geométrica (~0.50). CONFIRMADA en 3 pobs × 3 dims.
8. SIMETRÍA DE RACHAS UPDOWN (sube vs baja): las rachas de updown que SUBEN y
   las que BAJAN están equilibradas en cantidad, nivel a nivel. Medido (1M,
   seed=1): k1 sube=214.960 / baja=214.938; k2 89.520/89.530; k3 23.416/23.292;
   k4 4.575/4.565; espacios promedio idénticos por nivel (k1=3.65 ambos, etc.).
   CONFIRMADA. (Nota: la ASIMETRÍA de updown está en la reversión —P(romper)
   crece con k— NO en preferir subir vs bajar; eso es perfectamente simétrico.)
9. SIMETRÍA DE RACHAS NOUPDOWN (alternancia): las rachas de noupdown que
   INICIAN UP y las que INICIAN DOWN están equilibradas en cantidad. Medido
   (1M, seed=1): total inicia-up=118.275 vs inicia-down=118.346; diferencia
   0.060%. Coinciden nivel a nivel en cantidad y en espacio promedio. CONFIRMADA.
10. MEMORIA DEL SISTEMA = complementariedad + acoplamiento (principio rector del
    usuario). La "memoria" NO está en la bola (el giro no recuerda), sino en la
    estructura del CONJUNTO de datos. Dos niveles:
    (a) Complementariedad CONTABLE: en cada giro resuelto el resultado es una
        cosa o su complemento (rojo+negro=100%, sube+baja=100%, inicia-up +
        inicia-down = 100% de rachas noupdown). Identidad exacta, siempre cierta.
    (b) Equilibrio ESTADÍSTICO: esos complementos tienden a ~50/50 de forma
        estable (ley grandes números, cota 1/√n). Cierto en el agregado.
    + ACOPLAMIENTO (obs.5): un mismo número es a la vez rojo/negro, par/impar,
      sube/baja... todas las dimensiones sobre el MISMO flujo de giros. La
      memoria rica del sistema es ese tejido completo, no una dimensión aislada.
    CLAVE para la función: la ventaja, de existir, vendría de ese acoplamiento
    entre dimensiones, no del equilibrio de una dimensión sola (que es ley de
    azar independiente). Aún NO explotado.
11. SIMETRÍA DE DOCENAS y COLUMNAS (ternarias): los 3 lados de cada dimensión
    están equilibrados en cantidad de rachas, nivel a nivel. Medido (1M, seed=1):
    docenas totales 219.050/219.113/219.104 (dif 0.03%); columnas 218.861/
    219.175/219.286 (dif 0.19%). razón count(k+1)/count(k) ≈ 1/3 (=12/37).
    En interpretación "2 de 3" las 3 agrupaciones también son idénticas entre sí.
    CONFIRMADA: son "color ternario", simétricas, sin sesgo de reversión propio.
    (detalle técnico y ambas interpretaciones A/B: ver sección Docenas/Columnas.)
   - LÓGICA de qué es una racha NOUPDOWN (definición del usuario): tramo maximal
     donde updown ZIGZAGUEA (símbolos consecutivos distintos: up-down-up-...).
     longitud = nº de símbolos; lado = símbolo inicial (up/down). En un quiebre
     (dos iguales consecutivos) el 1º CIERRA el zigzag y el 2º ABRE el siguiente
     (bordes compartidos); en 3+ iguales los del medio no participan. No existen
     rachas noupdown de nivel 1 (un símbolo solo no zigzaguea). Implementación:
     detail_alt_fixed.py (verificada contra ejemplo: R4up,R4down,R4up,R3down).

## Hallazgo clave sobre el cero (precisa la "ley geométrica")
- Serie CRUDA (el cero rompe rachas): razón = 0.4865 = 18/37.
- Serie RESUELTA (ignorando ceros): razón = 0.5000 exacta.
- Interpretación: la estructura rica (simetría + auto-similaridad) es la FIRMA
  de un proceso de Bernoulli 50/50 independiente en la serie resuelta. Rica y
  bella, pero consistente con independencia. El 0.4865 es solo el efecto del
  cero, no una tendencia explotable adicional.

## Predicción del usuario CONFIRMADA: convergencia al gris (ley 1/√n)
- Desde el primer registro es predecible que la proporción de color converge a
  50/50 (gris uniforme, ni blancuzco ni oscuro) y lo hace "más temprano que
  tarde", con desvío acotado por ~1/(2√n). Medido: a 500k giros el desvío es
  0.018% del 50%. ESTO ES PREDICTIVO SOBRE EL AGREGADO (ley de grandes números
  + CLT), y es una afirmación correcta del usuario.
- Frontera exacta (clave del proyecto): lo predecible es el GRIS (proporción
  agregada y su cota de error), NO el color del próximo giro individual. El gris
  se forma por DOMINANCIA DE VOLUMEN (los giros nuevos 50/50 dominan al
  desequilibrio pasado), no por compensación dirigida de los desequilibrios.

## Punto de estabilización del gris (observación del usuario CONFIRMADA)
- Existe un n a partir del cual el gris 50/50 se asienta y no vuelve a salir de
  una banda de tolerancia. NO es un n único: escala con la precisión exigida
  (ley 1/√n). Medido (color, 5 pobs):
    ±5%   -> ~15–170 giros
    ±2%   -> ~200–1100
    ±1%   -> ~800–15.000   (gris estable "práctico")
    ±0.5% -> ~6.000–60.000
    ±0.2% -> ~20.000–200.000
- El umbral VARÍA entre poblaciones (es variable aleatoria) -> usar cota
  conservadora (peor caso), no promedio.
- Implicación de diseño: respalda el gate del 60% (D1). Antes del punto de
  estabilización el agregado no es confiable; después, las heurísticas basadas
  en "tendencias formadas" tienen base. Para muestras ~10k+ el gris ±1% ya es
  fiable en la mayoría de casos.

## Estabilización de conteos de rachas en ventanas CORTAS (200 giros, color)
- A 200 giros los conteos de rachas tienen los promedios correctos (n1~51,
  n2~26, n3~13) PERO alta variabilidad entre intervalos, que CRECE con el nivel:
    nivel1 CV=15%, nivel2 CV=19%, nivel3 CV=30% (rango n3: 3–20 por ventana).
- Contraste: a 1M la cantidad de rachas es estable ±3%; a 200 NO lo es.
  Causa: ley 1/√n por nivel (nivel k tiene ~r^(k-1) de las muestras de nivel 1,
  así que su error relativo crece). El gris de PROPORCIÓN se asienta antes que
  la ESTRUCTURA FINA de rachas.
- Implicación de diseño: A5 (deuda de rachas) sobre ventanas de ~200 es RUIDOSO,
  sobre todo nivel 3+. Opciones: ventanas mayores, o ponderar confianza de cada
  nivel por su tamaño muestral.

## Conclusión estructural: dominios de validez del estudio
- Si un casino usa CSPRNG / semillas rotativas, ESO YA NO ES AZAR PURO: es un
  algoritmo determinista diseñado para PARECER azar borrando los patrones
  explotables. Implicación profunda (y que valida la tesis): la existencia de
  esos algoritmos demuestra que el azar "ingenuo" (p.ej. random de Python) SÍ
  tiene estructura explotable — tanto que hubo que inventar máquinas para
  borrarla. El estudio NO se rompe; se delimita su dominio de aplicación.
- Tres escenarios distintos (NO confundir):
  (a) Azar ingenuo (random simple): tiene estructura (updown, rachas). El
      estudio aplica. Es nuestro terreno de simulación.
  (b) Casino regulado con RNG certificado (eCOGRA/iTechLabs...): estadísticamente
      justo; la casa gana SOLO por su ventaja matemática declarada (2.7% euro).
      Patrones de rachas borrados -> vía "estructura del azar" no aplica; pero
      NO hay manipulación de resultados (sería ilegal / pérdida de licencia).
  (c) Casino no regulado/fraudulento: podría inclinar resultados -> ya no es
      azar ni juego justo, es fraude. Fuera del alcance estadístico del estudio.
- CAUTELA honesta: en un casino regulado, "la casa gana" se explica por el 2.7%
  + varianza de muestra pequeña, NO necesariamente por manipulación. No asumir
  trampa sin evidencia (muestra grande + análisis).

## Precisión técnica clave: de dónde sale el 2.7% (NO de alterar resultados)
- El 2.7% NO se consigue manipulando giros. Sale de la TABLA DE PAGOS sobre
  resultados genuinamente aleatorios: p.ej. pleno paga 35:1 cuando lo justo
  sería 36:1 (hay 37 casillas). Esa diferencia ES la ventaja de la casa.
- Por tanto un RNG puede ser PERFECTAMENTE aleatorio y el casino gana igual su
  2.7% — sin tocar ningún resultado. No necesita "hacerte perder la siguiente
  para compensar"; eso sería manipulación (ilegal, lo que las certificaciones
  verifican que NO pasa).
- DISTINCIÓN esencial para el estudio:
  * "azar sin estructura explotable" (CSPRNG certificado) = justo, pero sin los
    patrones de rachas que estudiamos. NO es trampa.
  * "azar manipulado" (resultados alterados) = fraude, ilegal, otra categoría.
  No confundir ambos. Punto FILOSÓFICO válido del usuario: el jugador espera el
  azar ingenuo (con estructura) y recibe un algoritmo que la borró; esa brecha
  de expectativa es legítima como crítica, pero NO equivale a manipulación de
  resultados.

## DATO CLAVE: el casino en línea real (~500 giros) se comporta como random.random
- Muestra: 542 giros reales de un casino en línea; al quitar bloques repetidos
  (duplicación de captura; 5-gramas repetidos) quedan 465. Comparada con 1000
  muestras de random.random del mismo tamaño:
  * Frecuencia por número: chi²=41.6 (el 24.5% de los aleatorios es igual o más
    desigual). Más frecuente 22 (típico 18–24), menos frecuente 7 (típico 4–8).
    Los números "calientes/fríos" son lo normal en ~500 giros (el más frecuente
    promedia 1.64× lo esperado).
  * Conteos de rachas por nivel 1–5 en color, paridad, alto/bajo, docena,
    columna y updown: 28 de 29 comparaciones dentro de ±2σ. Extremos: updown
    nivel 3 z=−2.30 y paridad nivel 4 z=+1.95 (esperable con 29 comparaciones).
  * Firma de updown (P romper crece con k: k1≈0.67, k2≈0.85) presente (medida
    en el set de 542 antes de limpiar).
  * Mapas de calor (puntual, deuda, calendario): resultados dentro del nulo
    aleatorio.
- CONCLUSIÓN (usuario): a ~500 giros el casino real da resultados similares al
  random; las simulaciones con random.random podrían trasladarse a un casino
  real a esa escala, SIN necesariamente alta precisión.
- LÍMITES (potencia baja): solo detectaría desviaciones grandes (~±20% en niveles
  1–3 de rachas, ~±30% por número); captura de origen incierto (había
  duplicados); un casino, una sesión. Para afirmar más: miles de giros sin
  duplicar y repetir calibración (F10) y firma de updown.
- La impresión inicial ("en pocos minutos no seguían la misma distribución")
  corresponde a variación de muestra pequeña (ver ventanas de 200 giros).
- CORRECCIÓN a la sección "Conclusión estructural / dominios de validez": NO es
  cierto que un CSPRNG o un RNG certificado "borre" las rachas geométricas ni la
  reversión de updown. Esas leyes son consecuencia de giros independientes y
  uniformes, y las exhibe cualquier buen generador (Mersenne Twister, CSPRNG).
  La diferencia entre generadores es la PREDECIBILIDAD desde el estado interno
  (MT se reconstruye con 624 salidas; un CSPRNG no), no la estadística de rachas.
  Lo único que se desviaría de estas firmas sería una intervención deliberada
  sobre los resultados, que a 500 giros no se podría detectar.

## Observación de campo (PENDIENTE de verificar con datos suficientes)
- El usuario observó, en pocos minutos de una ruleta de casino real, que los
  resultados NO parecían seguir la misma distribución que nuestro random.
- ESTADO: no concluyente. "Pocos minutos" ≈ 30-100 giros; a ese tamaño incluso
  nuestro random limpio se ve "raro" (ver prueba ventanas de 200: conteos de
  rachas varían ±15-30%). Hace falta muestra grande (miles de giros) para que
  la comparación sea válida.
- SI se confirma con datos suficientes, sería esperable y relevante: los casinos
  serios usan CSPRNG o semillas rotativas / "provably fair", diseñados para NO
  exhibir patrones y/o reiniciar secuencias — justo lo que rompería las
  distribuciones de rachas que estudiamos. Conecta con la condición (3) de B6.
- ACTUALIZACIÓN: con 465 giros limpios no se detecta diferencia respecto al
  random (ver sección "DATO CLAVE" arriba).
- PENDIENTE: capturar muestra grande real. Método: consola navegador > Network;
  OJO los casinos suelen usar WebSocket (pestaña "WS", no "Fetch/XHR") y/o
  cifrar el payload. Posible ayuda: snippet de captura automática.

## Hallazgo: deriva temporal en el SET REAL (mapa puntual) — INTERPRETAR CON CAUTELA
- Mapa puntual (color/par/alto-bajo) sobre el set real, 8 bloques: la ventaja
  DECAE monótonamente del bloque 1 (+13.9%) al bloque 8 (−6.9%). Deriva
  bloque1−bloque8 = +0.208.
- Contraste con 100 muestras aleatorias de 542: deriva media ≈ 0 (−0.007,
  std 0.086); solo 1/100 alcanzó deriva ≥ 0.208. => p≈0.01, improbable por azar
  homogéneo.
- PERO: (a) una deriva descendente NO es explotable (no sabes en qué fase estás);
  (b) lo más plausible es ARTEFACTO del set real (captura incierta, tenía bloque
  duplicado, posible concatenación de tramos de sesiones distintas), no una ley
  del azar. El aleatorio limpio NO muestra deriva (ventaja global ≈ 0).
- Para distinguir ley vs artefacto: hace falta set real nuevo, capturado limpio
  y continuo. PENDIENTE.

## Hipótesis "acierta más al inicio que al final" (mapa puntual) — REFUTADA
- Hipótesis del usuario: el método acierta más en la 1a mitad (resuelve rápido
  el gris) y aporta poco en la 2a (dilución avanzada).
- Medido en aleatorio: con 200 muestras parecía +0.0074 (56% con 1a>2a), pero
  con 1000 muestras cae a +0.0008 (51%) y con 2000 giros a +0.0011 (53%).
  => el efecto se evapora: es ~0, indistinguible de azar.
- Interpretación: el método NO aporta en NINGÚN tramo (no es "aporta al inicio
  y se agota"). Sobre dimensiones simétricas no hay información útil nunca. El
  gris se asienta tan temprano que no existe ventana inicial explotable.

## Fórmula "calendario renovable por lado" (formulación del usuario) — heat_schedule.py
- Por dimensión binaria y por LADO s, nivel k: τ_k(s) = rachas cerradas de
  longitud exacta k−1 del lado s desde la última cerrada de longitud exacta k.
  u(s,k)=min(1,τ_k/2) (nivel 1 → 1). Ley ½ por nivel: 2 rachas de k−1 ameritan 1 de k.
- Racha abierta (lado s, nivel K): early=1−u(s,K) (llegó antes de tiempo → se
  espera ruptura); push=u(s,K+1) (ya toca el siguiente nivel → se espera que
  siga); A=ε+push, B=ε+early, p_prolonga=A/(A+B), ε=0.15 ("poquito" de opción).
- Al ocurrir el evento el contador se renueva (τ_k←0, τ_(k+1)+=1): el calor es
  función del ESTADO y OSCILA (no suma sin fin).
- Ejemplo 20,14,9 → 4º giro: color y alto/bajo p(romper)=0.885, paridad 0.5;
  caliente: negro+alto {20,22,24,26,28,29,31,33,35}; frío: rojo+bajo.
- Resultado empírico: ver IDEAS_FALLIDAS F10 (en aleatorio la frecuencia real de
  prolongar es ~0.50 en todos los bins de p; sin ventaja). Pendiente: calibrar
  con datos reales grandes; incluir alternancia explícita, updown y ternarias.
- AJUSTE 1 (usuario): si una dimensión queda en empate 0.5 vs 0.5, se elige la
  ALTERNANCIA (el calor va al lado que rompe la racha). Implementado como
  desempate TIE_BREAK=0.01 en heat_now (p_prolonga=0.49). Efecto: los empates son
  ~40% de los estados por dimensión; el conjunto caliente queda en ~18 valores
  (antes variaba 18–36 por empates). Resultado: set real 465 ventaja −0.22%
  (−0.11σ), aleatorio 10×50k −0.02%: sigue sin ventaja (esperable, es solo regla
  de desempate). Ventana real pos 282–291: 5/10 con 18 valores (=cobertura 50%).

## Dimensión DECENAS (1-9, 10-19, 20-29, 30-36) — NO PRECISA, analizar después
- Origen: el usuario notó que los grupos calientes del mapa caían por tramos
  (rojo-impar-bajo ≤9, rojo-par-bajo 10-19, rojo-impar-alto 20-29). Causa: el
  color del tablero alterna en BLOQUES (cambios en 10-11, 18-19, 28-29), así que
  cada combinación color/paridad/alto-bajo ocupa un tramo continuo de números.
- Comportamiento (aleatorio 1M, control con partición al azar de iguales tamaños
  da lo mismo): frecuencia ∝ tamaño del grupo (9,10,10,7 de 36); razón de racha
  k+1/k ≈ tamaño/37 (0.243, 0.270, 0.270, 0.189); sin dependencia entre decenas
  consecutivas (chi² ≈ 16 vs crítico 16.9). Set real (542): frecuencias y
  transiciones dentro del azar.
- NO está equilibrada por decena en color: rojos/negros por decena en el tablero
  = 5/4, 5/5, 4/6, 4/3 (total 18/18). % rojo de lo que sale: ~55 / 50 / 40 / 57
  (1M) y 55 / 48 / 44 / 61 (real). Es geometría del tablero, no memoria.
- CONCLUSIÓN: cualquier partición de los números es una "dimensión" con
  probabilidad ∝ tamaño; las decenas no aportan información distinta por sí solas.
  Estado: dimensión imprecisa, pendiente de analizar más adelante (¿alguna
  partición se aparta del control? solo si hubiera dependencia real).

## Ideas de mapa de calor multidimensional (pendientes de desarrollar)
- PUNTUAL: calor del estado exacto actual -> predice próximo giro (en curso).
- ZONAL: agrupa estados en zonas/rangos, calor por zona (pendiente).
- ACUMULATIVA: calor que se suma a lo largo del historial (deuda/tendencia)
  (pendiente).
- REDIMENSIONADA: idea de mapa de calor del usuario, aún por detallar. ANOTADA
  para desarrollar más adelante.

## Definición conceptual: azar ≠ impredecible (cristal forzado)
- Pregunta del usuario: si un operador toma cada "pieza" del cristal del azar y
  la reubica donde nunca habría ido, generando una forma distinta, ¿sigue siendo
  azar por ser impredecible, o deja de serlo por estar forzado?
- RESPUESTA: deja de ser azar. "Azar" requiere DOS propiedades:
  (1) impredecibilidad (no puedo adivinar la próxima pieza), y
  (2) IMPARCIALIDAD / ausencia de intención (ninguna configuración favorecida por
      una voluntad). El operador destruye (2) aunque conserve (1).
- Clave: impredecible ≠ azar. Un dado cargado es impredecible pero NO es azar
  (está sesgado hacia un fin). El cristal reordenado = dado cargado con forma de
  cristal. Lo que define el azar es la AUSENCIA DE PROPÓSITO que selecciona el
  resultado, no la mera impredecibilidad.
- COROLARIO potente (método de detección): el azar natural produce UNA forma de
  cristal característica (su firma: rachas con razón ~0.5, reversión de updown,
  etc.). Si un operador reordena, la forma cambia. Por tanto NO hace falta
  predecir giros individuales para saber si un sistema es azar o forzado: basta
  MEDIR LA FORMA del cristal y compararla con la firma del azar natural. La forma
  es el detector. ← Este ES el experimento pendiente con datos reales.

## Observación conceptual: el azar se auto-ordena en estructura (cristal)
- El supuesto "azar", según el nivel de probabilidad de cada evento, parece
  AGRUPARSE y ORDENAR su caos en una estructura estable y repetida. No es ruido
  informe: tiene forma.
- Ejemplo: para propiedades BINARIAS (color, paridad, alto-bajo) la estructura
  recuerda a un CRISTAL DE SAL — capas = niveles de racha, cada capa a ~1/2 de
  la anterior, simétrica respecto a su eje (los dos lados). Dimensiones ternarias
  (docenas/columnas) darían un cristal que se afila a ~1/3 por capa; updown, una
  aguja que se afila aceleradamente (reversión). La FORMA del cristal codifica
  el sesgo de la dimensión.
- TESIS del usuario: existe una forma de VER/MATERIALIZAR los comportamientos del
  supuesto azar (no solo tabularlos). Pendiente: encontrar una representación que
  muestre la ESTRUCTURA EMERGENTE, no un simple gráfico de barras de la tabla.
  (Intento con barras apiladas = insuficiente; la materialización real queda
  como objetivo abierto.)

## Dimensiones DOCENAS y COLUMNAS (ternarias) — interpretaciones A y B
- Interp. A (racha = misma docena/columna repetida, 3 lados):
  - 3 lados EQUILIBRADOS: totales docenas 219.050/219.113/219.104 (dif 0.03%);
    columnas 218.861/219.175/219.286 (dif 0.19%). Simetría ternaria perfecta.
  - razón count(k+1)/count(k) ≈ 0.324 = 12/37 (prob. de repetir misma docena).
    Decaen MÁS rápido que el color (más difícil repetir 1 de 3 que 1 de 2).
  - espacio min=1 siempre; #max=1 (hueco atípico único). esp. avg crece con k.
- Interp. B (2 de 3: una docena vs las otras dos; 3 agrupaciones posibles):
  - lado "1 docena": razón 0.324 (=12/37) CONSTANTE, rachas cortas. Racha
    MÁXIMA observada (1M, seed=1) = 12.
  - lado "2 docenas": razón 0.649 (=24/37) CONSTANTE, rachas largas. Racha
    MÁXIMA observada = 33.
  - las 3 agrupaciones son estadísticamente idénticas entre sí.
  - PRINCIPIO confirmado: cada lado aparece y decae según SU PROPIA probabilidad
    de aparición (1/3 decae en 1/3; 2/3 decae en 2/3). Por eso el lado de 2
    docenas tiene rachas mucho más largas (cubre el doble de números). No se
    comportan igual entre sí; se comportan igual a su respectiva probabilidad.
- CONCLUSIÓN: docenas/columnas = "color ternario", simétricas, sin sesgo de
  reversión aprovechable por sí solas. Se comportan según probabilidades
  geométricas puras (12/37, 24/37). Confirma la intuición del usuario de que
  "sale equilibrado por donde se lo analice".

## Dimensión NOUPDOWN (alternancia / zigzag) — definición del usuario
- Una racha de noupdown = tramo maximal donde updown ZIGZAGUEA (símbolos
  consecutivos distintos: up-down-up-down...). longitud = nº de símbolos;
  lado = símbolo inicial (inicia up / inicia down).
- En un quiebre (dos símbolos IGUALES consecutivos): el 1º CIERRA el zigzag en
  curso, el 2º ABRE el siguiente. Los bordes del quiebre SE COMPARTEN entre las
  dos rachas. En 3+ iguales seguidos, los del medio no participan en ningún
  zigzag (son sólo de la racha updown).
- Por esta forma de agrupar NO existen rachas de noupdown de nivel 1 (un símbolo
  solo no zigzaguea). Acordado: se deja así por ahora; si salen anomalías se
  revisa la definición.
- Consecuencia: el "espacio" entre dos rachas del mismo lado PUEDE ser 0 cuando
  comparten el borde de un quiebre mínimo (un único par de iguales). Es correcto
  por definición, no es bug. Implementación: detail_alt_fixed.py (verificada
  contra el ejemplo del usuario: R4up, R4down, R4up, R3down).
- Simetría up/down confirmada también en noupdown (conteos ~iguales por lado).

## Propiedades geométricas confirmadas (visión cepillo/espiral, color)
- Longitud media de racha ("pelo") = 2.000 exacta (serie resuelta, r=0.5).
- Conservación: suma(exceso sobre 2 de pelos largos) ≈ suma(déficit bajo 2 de
  pelos cortos), balance <0.5%. CIERTA por definición de la media. Es propiedad
  estructural (contable), NO una señal de predicción (ver IDEAS_FALLIDAS F5).

## Convenciones de evaluación
- `evaluate_incremental` (O(n), una pasada). break-even even-money: ganar >50%
  de resueltas; EV base = -2.70%. El cero cuenta como pérdida (realista).
- Validación honesta: descubrir en una población, medir en otras no vistas.
