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

    /**
     * Prueba un esquema de apuesta recorriendo el 'juego' y devuelve el retorno
     * acumulado (beneficio neto). Por ahora esqueleto: recorre, reparte el calor
     * y apuesta a los 'top' números más calientes cada giro (pleno, paga 35:1).
     *
     * @param desde índice inicial del juego (inclusive)
     * @param hasta índice final (exclusive)
     */
    public BigDecimal probarApuesta(int desde, int hasta) {
        BigDecimal retorno = BigDecimal.ZERO;
        final int TOP = 18;                 // nº de plenos a cubrir por giro
        final BigDecimal PAGO = BigDecimal.valueOf(35);

        for (int i = Math.max(desde, 1); i < Math.min(hasta, juego.length); i++) {
            // 1) el calor para predecir juego[i] se calcula SOLO con juego[0..i-1]
            limpiarCalor();
            calculaCalor(i - 1);

            // 2) elegir los TOP más calientes (excluyendo el 0)
            Valor[] calientes = Arrays.stream(tablero)
                    .filter(v -> v.valor != 0)
                    .sorted((a, b) -> b.calorTotal().compareTo(a.calorTotal()))
                    .limit(TOP)
                    .toArray(Valor[]::new);

            // 3) apostar 1 a cada uno; el resultado real es juego[i]
            Valor salio = juego[i];
            boolean acierto = Arrays.asList(calientes).contains(salio);
            retorno = acierto
                    ? retorno.add(PAGO.subtract(BigDecimal.valueOf(TOP - 1))) // +35 del acertado, -1 por cada otro
                    : retorno.subtract(BigDecimal.valueOf(TOP));
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
        fijarCalorBase(Dimension.COLOR);

        // Rachas de color: cada llamada actúa solo si el último resultado es de ese lado.
        calorRacha(Dimension.COLOR, Atributo.ROJO, i);
        calorRacha(Dimension.COLOR, Atributo.NEGRO, i);
        // Color total: rojas + negras juntas (también cumplen la regla de la mitad por nivel)
        calorRachaTotal(Dimension.COLOR, i);

        // PENDIENTE (en este orden): alternancia roja, alternancia negra, color total,
        // alternancia total, y después las demás dimensiones:
        //   calorRacha(Dimension.PARIDAD, Atributo.PAR, i);   calorRacha(Dimension.PARIDAD, Atributo.IMPAR, i);
        //   calorRacha(Dimension.ALTOBAJO, ...);  calorRacha(Dimension.FRANJA, ...);
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
        repartirCalor(dim, lado, i, pLado, ocurridas, esperadas);
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
        repartirCalor(dim, lado, i, probLado(dim, lado), ocurridas, esperadas);
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
    private void repartirCalor(Dimension dim, int lado, int i, double pLado, int[] ocurridas, double[] esperadas) {
        int largoActual = largoRachaActual(dim, i, juego);

        // ROMPER -> valores del lado contrario (la racha se cierra con exactamente L)
        if (largoActual <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[largoActual], ocurridas[largoActual]);
            sumarCalor(dim, -lado, probRachaTermina(largoActual, pLado) * diferencia);
        }

        // PROLONGAR -> valores del mismo lado (la racha llegará al menos a L+1)
        int siguiente = largoActual + 1;
        if (siguiente <= NIVEL_MAX) {
            double diferencia = diferenciaDeRachas(esperadas[siguiente], ocurridas[siguiente]);
            sumarCalor(dim, lado, probRachaTermina(siguiente, pLado) * diferencia);
        }
    }

    /** Probabilidad de que un giro caiga en ese lado de esa dimensión (el cero no está en ningún lado). */
    private double probLado(Dimension dim, int lado) {
        long cuantos = Arrays.stream(tablero).filter(v -> v.valor != 0 && dim.de(v) == lado).count();
        return cuantos / 37.0;
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
