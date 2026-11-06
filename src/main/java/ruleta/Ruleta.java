package ruleta;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static ruleta.Valor.*;

public final class Ruleta {

    public Valor[] tablero = new Valor[]{V0, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20, V21, V22, V23, V24, V25, V26, V27, V28, V29, V30, V31, V32, V33, V34, V35, V36};
    public Valor[] juego;

    /** Calor inicial de cada número y al que vuelve cuando ocurridas == esperadas. */
    private static final double CALOR_BASE = 1.0 / 37;

    /**
     * Zona de "igual": si |esperadas - ocurridas| <= TOLERANCIA * esperadas se considera igual
     * (diferencia 0). Tu criterio conservador del 3% / 5% sería 0.03 / 0.05. Con 0.0 solo es
     * "igual" cuando coinciden exactamente (con decimales casi nunca ocurre).
     */
    private static final double TOLERANCIA = 0.0;

    /** Niveles de racha que se contabilizan (1..NIVEL_MAX). */
    private static final int NIVEL_MAX = 8;

    public void cargarReal(String path) throws IOException {
        juego = Datos.leerValores(path);
        System.out.println("cargada muestra real limpia: " + juego.length + " registros");
    }

    public void cargar1M(String path, int cantidad, int seed) throws IOException {
        juego = Datos.generarValores(path, cantidad, seed);
        System.out.println("cargada muestra nueva de: " + cantidad + " registros");
    }

    /** Cuántos números (los más calientes) se apuestan a pleno cada giro. */
    public static final int TOP_APUESTA = 9;

    /** Prueba la apuesta con TOP_APUESTA números por giro. */
    public BigDecimal probarApuesta(int desde, int hasta) {
        return probarApuesta(desde, hasta, TOP_APUESTA);
    }

    /**
     * Prueba un esquema de apuesta recorriendo el 'juego' y devuelve el retorno
     * acumulado (beneficio neto). Por ahora esqueleto: recorre, reparte el calor
     * y apuesta a los 'top' números más calientes cada giro (pleno, paga 35:1).
     *
     * @param desde índice inicial del juego (inclusive)
     * @param hasta índice final (exclusive)
     * @param top   cuántos números (los más calientes) se apuestan a pleno cada giro
     *
     * Cada giro cuesta 'top' fichas. Acierto: +(36 - top). Fallo (incluido el cero): -top.
     * Por eso retorno = 36 * aciertos - top * apuestas.
     */
    public BigDecimal probarApuesta(int desde, int hasta, int top) {
        BigDecimal retorno = BigDecimal.ZERO;
        final BigDecimal PAGO = BigDecimal.valueOf(35);

        for (int i = Math.max(desde, 1); i < Math.min(hasta, juego.length); i++) {
            // 1) el calor para predecir juego[i] se calcula SOLO con juego[0..i-1]
            limpiarCalor();
            calculaCalor(i - 1);

            // 2) elegir los 'top' más calientes (excluyendo el 0)
            Valor[] calientes = Arrays.stream(tablero)
                    .filter(v -> v.valor != 0)
                    .sorted((a, b) -> b.calorTotal().compareTo(a.calorTotal()))
                    .limit(top)
                    .toArray(Valor[]::new);

            // 3) apostar 1 a cada uno; el resultado real es juego[i]
            Valor salio = juego[i];
            boolean acierto = Arrays.asList(calientes).contains(salio);
            retorno = acierto
                    ? retorno.add(PAGO.subtract(BigDecimal.valueOf(top - 1))) // +35 del acertado, -1 por cada otro pleno
                    : retorno.subtract(BigDecimal.valueOf(top));
        }
        return retorno;
    }

    /** Calor para predecir el SIGUIENTE giro a partir de todo lo cargado. */
    public void asignarCalor() {
        limpiarCalor();
        calculaCalor(juego.length - 1);
    }

    public void limpiarCalor() {
        Arrays.stream(tablero).forEach(Valor::limpiarCalor);
    }

    // ------------------------------------------------------------------
    //  CONTEO DE RACHAS
    // ------------------------------------------------------------------

    /**
     * Cuenta las rachas CERRADAS de exactamente 'nivel' elementos, cuyo atributo
     * en la dimensión 'dim' vale 'lado', dentro de juego[0..i] (i inclusive).
     *
     * "Cerrada" = ya la rompió un elemento posterior (que también está en 0..i).
     * La racha que termina justo en la posición i sigue ABIERTA: no se cuenta
     * (puede seguir creciendo); su longitud actual la da largoRachaActual().
     * El cero rompe cualquier racha (su atributo es 0, distinto de +1/-1).
     * Cuenta longitud EXACTA: RRR cuenta como nivel 3, no como nivel 1 ni 2.
     */
    public int contadorRacha(Dimension dim, int lado, int nivel, int i, Valor[] juego) {
        return contarRachas(dim, lado, i, juego, nivel)[nivel];
    }

    /** Atajo con la firma original de tu borrador: dimensión COLOR. */
    public int contadorRacha(int lado, int nivel, int i, Valor[] juego) {
        return contadorRacha(Dimension.COLOR, lado, nivel, i, juego);
    }

    /**
     * Versión de UNA sola pasada: devuelve cuenta[nivel] para nivel 1..maxNivel
     * (rachas cerradas de ese lado hasta la posición i). Las rachas más largas que
     * maxNivel no se cuentan. Útil cuando necesitas varios niveles a la vez:
     * llamar 8 veces a contadorRacha recorre el historial 8 veces.
     */
    public int[] contarRachas(Dimension dim, int lado, int i, Valor[] juego, int maxNivel) {
        int[] cuenta = new int[maxNivel + 1];      // índice = nivel (el 0 no se usa)
        int ultimo = Math.min(i, juego.length - 1);
        int largo = 0;
        for (int j = 0; j <= ultimo; j++) {
            if (dim.de(juego[j]) == lado) {
                largo++;
            } else {
                if (largo >= 1 && largo <= maxNivel) cuenta[largo]++;   // se cerró una racha
                largo = 0;
            }
        }
        // si largo > 0 aquí, esa racha sigue abierta en la posición i: no se cuenta
        return cuenta;
    }

    /**
     * Longitud de la racha ABIERTA que termina en la posición i: cuántos elementos
     * seguidos, hacia atrás desde i, comparten el atributo de juego[i] en 'dim'.
     * Devuelve 0 si juego[i] no tiene atributo en esa dimensión (el cero).
     */
    public int largoRachaActual(Dimension dim, int i, Valor[] juego) {
        int atributo = dim.de(juego[i]);
        if (atributo == 0) return 0;
        int largo = 0;
        for (int j = i; j >= 0 && dim.de(juego[j]) == atributo; j--) largo++;
        return largo;
    }

    // ------------------------------------------------------------------
    //  ALGORITMO DE CALOR
    // ------------------------------------------------------------------

    /**
     * Calor para predecir juego[i+1] usando SOLO juego[0..i].
     * (Antes este método recorría todo 'juego' con un for; ese for ahora es el
     * parámetro i, así el calor en la posición i no depende del futuro.)
     */
    public void calculaCalor(int i) {
        /**
         * Para cada dimension binaria asignar la formula de asignacion de calor
         *
         * para el numero de datos leidos previamente se espera que exista 1/2 de rojos
         * de todos los rojos se espera que 1/2 sean rachas de 1
         * de todas las las rachas de 2 se espera que 1/2 sean rachas de 3
         * sucesivamente
         *
         * OPCIONAL: el valor de calor inicial para cada numero es 1/37 que es la probabilidad individual de numero pleno
         *
         * debo saber en que elemento estoy y ver el pasado, la cantidad de rachas esperadas al momento vs las no cumplidas y esperar a que se cumplan
         */

        // el calor de cada número parte de CALOR_BASE = 1/37 (probabilidad de un pleno).
        // Como todos parten igual, no cambia cuál es el más caliente: solo marca el "igual".
        for (Dimension dim : Dimension.values()) {          // COLOR, PARIDAD, ALTOBAJO, FRANJA
            // 1) rachas del propio atributo (calor<Dim>): cada lado y el total de la dimensión
            fijarCalorBase(dim);
            calorRacha(dim, 1, i);        // lado +1: ROJO / PAR / ALTO / SUPERIOR
            calorRacha(dim, -1, i);       // lado -1: NEGRO / IMPAR / BAJO / INFERIOR
            calorRachaTotal(dim, i);

            // 2) alternancia: dimensión PROPIA con calor independiente (calor<Dim>Alterno).
            //    La franja no tiene alternancia.
            if (dim != Dimension.FRANJA) {
                fijarCalorBaseAlterno(dim);
                calorAlternancia(dim, 1, i);       // zigzag que INICIÓ en el lado +1
                calorAlternancia(dim, -1, i);      // zigzag que INICIÓ en el lado -1
                calorAlternanciaTotal(dim, i);     // los dos juntos
            }
        }

        // PENDIENTE: updown (calorUpdown / calorUpdownAlterno). No entra en este bucle porque no es un
        // atributo fijo del número sino de la transición con el anterior, y sus rachas NO siguen la ley
        // geométrica de arriba (decaen más rápido por la reversión), así que estas esperadas no le sirven.
    }

    /**
     * Reparte calor por la racha ACTUAL de 'lado' en la dimensión 'dim'. Solo actúa si el último
     * resultado juego[i] pertenece a ese lado (si no, no hay racha de ese lado en curso).
     *
     * Sea L cuántos elementos seguidos llevo en ese lado:
     *  - ROMPER (sale el lado contrario o el cero): la racha se cierra con EXACTAMENTE L,
     *    así que cuenta el nivel L.   -> calor a los valores del lado CONTRARIO.
     *  - PROLONGAR (sale otro del mismo lado): llegará al menos a L+1,
     *    así que cuenta el nivel L+1. -> calor a los valores del MISMO lado.
     *
     * Calor de cada valor del destino (el valor completo, no se reparte):
     *     P(racha termina en k) * (esperadas[k] - ocurridas[k])      con signo:
     *     faltan rachas -> suma ; sobran -> resta ; iguales -> 0 (queda en CALOR_BASE).
     *
     * Esperadas: con pLado = probabilidad de un giro en ese lado y pNoLado = 1 - pLado
     * (el otro lado más el cero), una racha de EXACTAMENTE 1 es [no lado][lado][no lado]:
     *     esperadas[1] = n * pNoLado * pLado * pNoLado ;   esperadas[k] = esperadas[k-1] * pLado
     * Para rojo/negro, par/impar, alto/bajo y franja pLado = 18/37.
     */
    public void calorRacha(Dimension dim, int lado, int i) {
        if (dim.de(juego[i]) != lado) return;

        double pLado = probLado(dim, lado);
        int[] ocurridas = contarRachas(dim, lado, i, juego, NIVEL_MAX);
        double[] esperadas = esperadasPorNivel(i + 1, pLado);   // n = datos leídos hasta i (no el futuro)

        // Al romper se abre una racha del lado contrario: sus rachas de nivel 1 también cuentan.
        int ladoContrario = -lado;
        int[] ocurridasContrario = contarRachas(dim, ladoContrario, i, juego, NIVEL_MAX);
        double[] esperadasContrario = esperadasPorNivel(i + 1, probLado(dim, ladoContrario));

        repartirCalor(dim, lado, i, pLado, ocurridas, esperadas, ocurridasContrario[1], esperadasContrario[1]);
    }

    /**
     * Igual que calorRacha, pero midiendo la dimensión COMPLETA ("color total"): las rachas de ambos
     * lados juntas (rojas + negras). La regla de la mitad por nivel también se cumple en el total:
     *     esperadas_total[k] = esperadas_lado+[k] + esperadas_lado-[k]     (= 2·n·q²·p^k si los lados son 18/18)
     *     ocurridas_total[k] = ocurridas_lado+[k] + ocurridas_lado-[k]
     * El calor se reparte igual que por lado: ROMPER al lado contrario del último resultado y
     * PROLONGAR a su mismo lado, ponderado con la probabilidad de ESE lado.
     * Actúa siempre que el último resultado tenga lado (no es el cero).
     */
    public void calorRachaTotal(Dimension dim, int i) {
        int lado = dim.de(juego[i]);
        if (lado == 0) return;

        int[] ocMas = contarRachas(dim, 1, i, juego, NIVEL_MAX);
        int[] ocMenos = contarRachas(dim, -1, i, juego, NIVEL_MAX);
        double[] esMas = esperadasPorNivel(i + 1, probLado(dim, 1));
        double[] esMenos = esperadasPorNivel(i + 1, probLado(dim, -1));

        int[] ocurridas = new int[NIVEL_MAX + 1];
        double[] esperadas = new double[NIVEL_MAX + 1];
        for (int k = 1; k <= NIVEL_MAX; k++) {
            ocurridas[k] = ocMas[k] + ocMenos[k];
            esperadas[k] = esMas[k] + esMenos[k];
        }
        // el nivel 1 que se abre al romper, medido también en el total de la dimensión
        repartirCalor(dim, lado, i, probLado(dim, lado), ocurridas, esperadas, ocurridas[1], esperadas[1]);
    }

    /**
     * Rachas esperadas de EXACTAMENTE k elementos (k = 1..NIVEL_MAX) de un lado con probabilidad pLado,
     * entre n giros. Una racha de 1 es [no lado][lado][no lado]; cada nivel es pLado veces el anterior.
     */
    private double[] esperadasPorNivel(double n, double pLado) {
        double[] esperadas = new double[NIVEL_MAX + 1];
        esperadas[1] = n * (1 - pLado) * pLado * (1 - pLado);
        for (int k = 2; k <= NIVEL_MAX; k++) esperadas[k] = esperadas[k - 1] * pLado;
        return esperadas;
    }

    /**
     * Reparte el calor de la racha ACTUAL (del lado 'lado', largo L) a partir de lo ocurrido y lo
     * esperado por nivel (por lado o de la dimensión completa; el reparto es el mismo).
     */
    private void repartirCalor(Dimension dim, int lado, int i, double pLado, int[] ocurridas, double[] esperadas,
                               int ocurridasNivel1QueSeAbre, double esperadasNivel1QueSeAbre) {
        int largoActual = largoRachaActual(dim, i, juego);

        // ROMPER -> valores del lado contrario (la racha se cierra con exactamente L)
        if (largoActual <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[largoActual], ocurridas[largoActual]);
            sumarCalor(dim, -lado, probRachaTermina(largoActual, pLado) * diferencia);
        }

        // ROMPER, además: la ruptura abre una racha del lado contrario, y lo más probable es que termine en
        // el nivel 1. Si faltan rachas de nivel 1 de ese lado, suma calor a ese lado; si sobran, resta.
        double diferenciaNivel1 = diferenciaDeRachas(esperadasNivel1QueSeAbre, ocurridasNivel1QueSeAbre);
        sumarCalor(dim, -lado, probRachaTermina(1, probLado(dim, -lado)) * diferenciaNivel1);

        // PROLONGAR -> valores del mismo lado (la racha llegará al menos a L+1)
        int siguiente = largoActual + 1;
        if (siguiente <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[siguiente], ocurridas[siguiente]);
            sumarCalor(dim, lado, probRachaTermina(siguiente, pLado) * diferencia);
        }
    }

    // ------------------------------------------------------------------
    //  ALTERNANCIA (dimensión propia; su calor va a calor<Dim>Alterno)
    // ------------------------------------------------------------------
    //
    // Una racha de alternancia es un tramo de ZIGZAG (símbolos consecutivos de lados distintos).
    //  - El CERO ROMPE el zigzag (lo cierra y no abre otro), igual que rompe las rachas.
    //  - Cuando un símbolo repite al anterior se cierra el zigzag en curso. Ese símbolo repetido es el
    //    NÚMERO FIN DE RACHA (cierra una racha directa) y la alternancia NO LO CONTEMPLA: abre el zigzag
    //    siguiente pero no forma parte de él. Por eso el cambio aislado entre dos rachas (el P->I de PPII)
    //    no es una alternancia.
    //  - Si al zigzag lo precede un cero, o es el inicio de la muestra, su primer símbolo SÍ cuenta
    //    (no cierra ninguna racha).
    //  - El "lado" de un zigzag es el de su primer símbolo contado. El nivel mínimo es 2.
    //
    // Esperadas en n giros (cero incluido). p = probabilidad de un lado (18/37), q = 1 - p, z = prob. del cero.
    // Un zigzag contado de m símbolos que empieza en el lado t viene de:
    //   (a) detrás de una repetición: un zigzag completo de m+1 símbolos que empieza en el lado contrario
    //       (-t) y lo precede un símbolo igual -> n * p_(-t) * [m+1 símbolos alternando] * q
    //   (b) detrás de un cero:  zigzag de m símbolos que empieza en t  -> n * z * [m símbolos alternando] * q
    //
    // Reparto del calor, con el zigzag contado en curso de largo L y último símbolo u:
    //     ROMPER    (el siguiente repite u, o es el cero) -> valores con atributo  u , cuenta el nivel L
    //     PROLONGAR (el siguiente alterna: -u)            -> valores con atributo -u , cuenta el nivel L+1
    //     peso = P(el zigzag termina en k) = q * p^(k-1), con p = probabilidad de alternar; y la
    //     diferencia (esperadas - ocurridas) con signo, igual que en las rachas.
    // Si el último número acaba de cerrar una racha, todavía no hay zigzag contado (largo 0): no hay calor.
    // El cero no recibe calor (por ahora no participa), aunque sí rompe.

    /** Zigzag contado en curso: lado de su primer símbolo contado, cuántos símbolos lleva y el último (largo 0 si no hay). */
    public record EstadoAlternancia(int ladoInicio, int largo, int ultimo) {}

    /** Estado del zigzag contado en curso al llegar a la posición i. */
    public EstadoAlternancia estadoAlternancia(Dimension dim, int i, Valor[] juego) {
        int ladoInicio = 0, largo = 0, ultimo = 0;
        boolean primeroExcluido = false;
        for (int j = 0; j <= Math.min(i, juego.length - 1); j++) {
            int simbolo = dim.de(juego[j]);
            if (simbolo == 0) {                            // el cero ROMPE: no hay zigzag en curso
                ladoInicio = 0;
                largo = 0;
                ultimo = 0;
                primeroExcluido = false;
                continue;
            }
            if (largo > 0 && simbolo != ultimo) {
                largo++;                                   // alterna: el zigzag crece
            } else {
                primeroExcluido = (largo > 0 && simbolo == ultimo);   // repite: este símbolo es un FIN DE RACHA
                ladoInicio = simbolo;
                largo = 1;
            }
            ultimo = simbolo;
        }
        int largoContado = primeroExcluido ? largo - 1 : largo;
        int ladoContado = primeroExcluido ? -ladoInicio : ladoInicio;
        if (largoContado <= 0) {
            return new EstadoAlternancia(0, 0, ultimo);
        }
        return new EstadoAlternancia(ladoContado, largoContado, ultimo);
    }

    /**
     * Zigzags CERRADOS de exactamente k símbolos contados (k = 2..maxNivel) hasta la posición i, cuyo primer
     * símbolo contado es de 'ladoInicio' (+1 / -1), o de ambos lados si ladoInicio == 0. El zigzag en curso
     * no se cuenta. Cierran un zigzag: una repetición, el cero, o el primer símbolo.
     */
    public int[] contarAlternancias(Dimension dim, int ladoInicio, int i, Valor[] juego, int maxNivel) {
        int[] cuenta = new int[maxNivel + 1];
        int inicio = 0, largo = 0, ultimo = 0;
        boolean primeroExcluido = false;
        for (int j = 0; j <= Math.min(i, juego.length - 1); j++) {
            int simbolo = dim.de(juego[j]);
            if (simbolo == 0 || largo == 0 || simbolo == ultimo) {
                contarZigzagCerrado(cuenta, ladoInicio, inicio, largo, primeroExcluido, maxNivel);
                if (simbolo == 0) {                        // el cero cierra y NO abre otro
                    inicio = 0;
                    largo = 0;
                    ultimo = 0;
                    primeroExcluido = false;
                    continue;
                }
                primeroExcluido = (largo > 0 && simbolo == ultimo);   // repite: este símbolo es un FIN DE RACHA
                inicio = simbolo;
                largo = 1;
            } else {
                largo++;
            }
            ultimo = simbolo;
        }
        return cuenta;
    }

    /** Suma un zigzag cerrado a la cuenta, quitándole su primer símbolo si era un fin de racha. */
    private void contarZigzagCerrado(int[] cuenta, int ladoInicioBuscado, int inicio, int largo,
                                     boolean primeroExcluido, int maxNivel) {
        int largoContado = primeroExcluido ? largo - 1 : largo;
        int ladoContado = primeroExcluido ? -inicio : inicio;
        boolean ladoCoincide = (ladoInicioBuscado == 0) || (ladoContado == ladoInicioBuscado);
        if (largoContado >= 2 && largoContado <= maxNivel && ladoCoincide) {
            cuenta[largoContado]++;
        }
    }

    /**
     * Igual que estadoAlternancia, pero si el último número cerró una racha (no hay zigzag contado) el siguiente
     * número que alterne ABRIRÁ uno: se trata como un zigzag de largo 1 que empezaría en el lado opuesto al último.
     * Así ese lado recibe el déficit de zigzags de nivel 2 (alternancias que "se deben"). Tras un cero no hay calor.
     */
    private EstadoAlternancia estadoAbierto(Dimension dim, int i) {
        EstadoAlternancia estado = estadoAlternancia(dim, i, juego);
        if (estado.largo() == 0 && estado.ultimo() != 0) {
            return new EstadoAlternancia(-estado.ultimo(), 1, estado.ultimo());
        }
        return estado;
    }

    /** Calor por la alternancia en curso, solo si el zigzag contado actual empezó en 'ladoInicio'. */
    public void calorAlternancia(Dimension dim, int ladoInicio, int i) {
        if (dim == Dimension.FRANJA) return;               // la franja no tiene alternancia
        EstadoAlternancia estado = estadoAbierto(dim, i);
        if (estado.largo() == 0 || estado.ladoInicio() != ladoInicio) return;

        int[] ocurridas = contarAlternancias(dim, ladoInicio, i, juego, NIVEL_MAX);
        double[] esperadas = esperadasAlternancia(dim, ladoInicio, i + 1);
        repartirCalorAlternancia(dim, estado, ocurridas, esperadas);
    }

    /** Igual, midiendo la alternancia COMPLETA: zigzags de los dos lados de inicio juntos. */
    public void calorAlternanciaTotal(Dimension dim, int i) {
        if (dim == Dimension.FRANJA) return;
        EstadoAlternancia estado = estadoAbierto(dim, i);
        if (estado.largo() == 0) return;

        int[] ocurridas = contarAlternancias(dim, 0, i, juego, NIVEL_MAX);
        double[] esperadas = esperadasAlternancia(dim, 0, i + 1);
        repartirCalorAlternancia(dim, estado, ocurridas, esperadas);
    }

    /** Zigzags contados esperados de exactamente k símbolos (k >= 2) en n giros; ladoInicio == 0 suma los dos lados. */
    private double[] esperadasAlternancia(Dimension dim, int ladoInicio, double n) {
        double[] esperadas = new double[NIVEL_MAX + 1];       // el nivel 1 no existe (queda en 0)
        for (int nivel = 2; nivel <= NIVEL_MAX; nivel++) {
            if (ladoInicio == 0) {
                esperadas[nivel] = esperadaZigzagContado(dim, 1, nivel, n) + esperadaZigzagContado(dim, -1, nivel, n);
            } else {
                esperadas[nivel] = esperadaZigzagContado(dim, ladoInicio, nivel, n);
            }
        }
        return esperadas;
    }

    /**
     * Zigzags contados esperados de 'nivel' símbolos cuyo primer símbolo contado es de 'ladoPrimeroContado':
     * los que vienen detrás de una repetición (con un símbolo más, el fin de racha) más los que vienen detrás de un cero.
     */
    private double esperadaZigzagContado(Dimension dim, int ladoPrimeroContado, int nivel, double n) {
        int ladoDelFinDeRacha = -ladoPrimeroContado;
        double probabilidadDelCero = 1 - probLado(dim, 1) - probLado(dim, -1);
        double detrasDeRepeticion = esperadaZigzagCompleto(dim, ladoDelFinDeRacha, nivel + 1, n,
                probLado(dim, ladoDelFinDeRacha));
        double detrasDeCero = esperadaZigzagCompleto(dim, ladoPrimeroContado, nivel, n, probabilidadDelCero);
        return detrasDeRepeticion + detrasDeCero;
    }

    /**
     * Zigzags completos de 'simbolos' símbolos que empiezan en 'ladoInicio': [lo que precede] [los símbolos
     * alternando] [no es el opuesto del último]. 'probabilidadDeLoQuePrecede' es p (una repetición) o z (un cero).
     */
    private double esperadaZigzagCompleto(Dimension dim, int ladoInicio, int simbolos, double n,
                                          double probabilidadDeLoQuePrecede) {
        double producto = 1;
        int simbolo = ladoInicio;
        for (int posicion = 0; posicion < simbolos; posicion++) {
            producto *= probLado(dim, simbolo);
            simbolo = -simbolo;
        }
        int ultimo = (simbolos % 2 == 1) ? ladoInicio : -ladoInicio;
        return n * probabilidadDeLoQuePrecede * producto * (1 - probLado(dim, -ultimo));
    }

    private void repartirCalorAlternancia(Dimension dim, EstadoAlternancia estado, int[] ocurridas, double[] esperadas) {
        int largo = estado.largo();
        double pAlterna = probLado(dim, -estado.ultimo());    // probabilidad de que el siguiente alterne

        // ROMPER: el siguiente repite el último símbolo (o es el cero) -> el zigzag se cierra con exactamente 'largo'
        if (largo <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[largo], ocurridas[largo]);
            sumarCalorAlterno(dim, estado.ultimo(), probRachaTermina(largo, pAlterna) * diferencia);
        }

        // PROLONGAR: el siguiente alterna (el opuesto) -> el zigzag llegará al menos a largo+1
        int siguiente = largo + 1;
        if (siguiente <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[siguiente], ocurridas[siguiente]);
            sumarCalorAlterno(dim, -estado.ultimo(), probRachaTermina(siguiente, pAlterna) * diferencia);
        }
    }

    /** Pone el calor de ALTERNANCIA de la dimensión en CALOR_BASE (1/37) para todos los números. */
    private void fijarCalorBaseAlterno(Dimension dim) {
        BigDecimal base = BigDecimal.valueOf(CALOR_BASE);
        for (Valor v : tablero) v.sumarCalorAlterno(dim, base);
    }

    /** Suma calor de alternancia a CADA valor cuyo atributo en 'dim' es 'atributoDestino'. */
    private void sumarCalorAlterno(Dimension dim, int atributoDestino, double calor) {
        BigDecimal calorASumar = BigDecimal.valueOf(calor);
        for (Valor valor : tablero) {
            if (dim.de(valor) == atributoDestino) valor.sumarCalorAlterno(dim, calorASumar);
        }
    }

    /** Probabilidad de un giro en cada lado de cada dimensión: [dimensión][0 = lado +1, 1 = lado -1]. */
    private static final double[][] PROB_LADO = calcularProbLado();

    private static double[][] calcularProbLado() {
        double[][] p = new double[Dimension.values().length][2];
        for (Dimension dim : Dimension.values()) {
            for (int lado : new int[]{1, -1}) {
                long cuantos = Arrays.stream(Valor.values()).filter(v -> v.valor != 0 && dim.de(v) == lado).count();
                p[dim.ordinal()][lado == 1 ? 0 : 1] = cuantos / 37.0;
            }
        }
        return p;
    }

    /** Probabilidad de que un giro caiga en ese lado de esa dimensión (el cero no está en ningún lado). */
    private double probLado(Dimension dim, int lado) {
        return PROB_LADO[dim.ordinal()][lado == 1 ? 0 : 1];
    }

    /**
     * Probabilidad de que una racha ya iniciada termine EXACTAMENTE en k elementos:
     * (k-1 más del mismo lado y luego uno que no) = pLado^(k-1) * (1 - pLado).
     * Es tu "0.5^k" con el cero incluido: en rojo k=1 -> 0.5135, k=2 -> 0.2498, k=3 -> 0.1215.
     * Ya contiene la probabilidad de que salga el lado, por eso NO se vuelve a multiplicar por pLado.
     */
    private double probRachaTermina(int k, double pLado) {
        return (1 - pLado) * Math.pow(pLado, k - 1);
    }

    /** esperadas - ocurridas con signo; 0 si están dentro de la zona de "igual". */
    private double diferenciaDeRachas(double esperadas, int ocurridas) {
        double d = esperadas - ocurridas;
        if (Math.abs(d) <= TOLERANCIA * esperadas) return 0.0;
        return d;
    }

    /** Pone el calor de la dimensión 'dim' de todos los números en CALOR_BASE (1/37). */
    private void fijarCalorBase(Dimension dim) {
        BigDecimal base = BigDecimal.valueOf(CALOR_BASE);
        for (Valor v : tablero) v.sumarCalor(dim, base);   // se asume calor limpio (en cero) antes de llamar
    }

    /**
     * Suma 'calor' en la dimensión 'dim' a CADA valor del tablero cuyo atributo en esa
     * dimensión es 'atributoDestino' (+1 / -1). El cero (atributo 0) nunca recibe calor.
     */
    private void sumarCalor(Dimension dim, int atributoDestino, double calor) {
        BigDecimal c = BigDecimal.valueOf(calor);
        for (Valor v : tablero) {
            if (dim.de(v) == atributoDestino) v.sumarCalor(dim, c);
        }
    }
}
