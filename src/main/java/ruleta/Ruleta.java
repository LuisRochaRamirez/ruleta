package ruleta;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static ruleta.Valor.*;

public final class Ruleta {

    public Valor[] tablero = new Valor[]{V0, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20, V21, V22, V23, V24, V25, V26, V27, V28, V29, V30, V31, V32, V33, V34, V35, V36};
    public Valor[] juego;

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

    /** Atajo con la firma que usas en calculaCalor: dimensión COLOR. */
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
        // TODO: aqui va el algoritmo (asignar calor según rachas por dimension)

        /**
         * Para cada dimension binaria asignar la formula de asignacion de calor
         *
         * empezaremos revisando los rojos
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

        //empezamos por racha de rojos
        int rachas1RojoOcurridas = contadorRacha(Atributo.ROJO, 1, i, juego);
        int rachas2RojoOcurridas = contadorRacha(Atributo.ROJO, 2, i, juego);
        int rachas3RojoOcurridas = contadorRacha(Atributo.ROJO, 3, i, juego);
        int rachas4RojoOcurridas = contadorRacha(Atributo.ROJO, 4, i, juego);
        int rachas5RojoOcurridas = contadorRacha(Atributo.ROJO, 5, i, juego);
        int rachas6RojoOcurridas = contadorRacha(Atributo.ROJO, 6, i, juego);
        int rachas7RojoOcurridas = contadorRacha(Atributo.ROJO, 7, i, juego);
        int rachas8RojoOcurridas = contadorRacha(Atributo.ROJO, 8, i, juego);

        // FIX 1: (18/37) en Java es división ENTERA = 0 -> todo lo esperado daba 0. Usar 18.0/37.
        // FIX 2: n = datos leídos hasta i (i+1), no juego.length (eso usaba el futuro).
        // FIX 3: double en vez de int; (int) truncaba a 0 en muestras chicas y las
        //        divisiones /2 encadenadas perdían todo.
        // FIX 4: rachas8 copiaba rachas7 (faltaba el /2).
        double n = i + 1;
        double pRojo = 18.0 / 37;      // un giro cualquiera es rojo
        double pNoRojo = 19.0 / 37;    // negro o cero: lo que rompe una racha roja
        // Racha roja de EXACTAMENTE 1 = [no rojo][rojo][no rojo] -> n * pNoRojo * pRojo * pNoRojo ≈ 0.128*n.
        // Tu fórmula n*(18/37)*0.5 ≈ 0.243*n era el DOBLE: la mitad de las RACHAS son de 1,
        // pero rachas ≈ n*pRojo*pNoRojo, no n*pRojo (eso son los giros rojos).
        // Entre un nivel y el siguiente la razón real es pRojo (0.4865); es tu "1/2" pero
        // incluyendo que el cero también rompe.
        double rachas1RojoEsperadas = n * pNoRojo * pRojo * pNoRojo;
        double rachas2RojoEsperadas = rachas1RojoEsperadas * pRojo;
        double rachas3RojoEsperadas = rachas2RojoEsperadas * pRojo;
        double rachas4RojoEsperadas = rachas3RojoEsperadas * pRojo;
        double rachas5RojoEsperadas = rachas4RojoEsperadas * pRojo;
        double rachas6RojoEsperadas = rachas5RojoEsperadas * pRojo;
        double rachas7RojoEsperadas = rachas6RojoEsperadas * pRojo;
        double rachas8RojoEsperadas = rachas7RojoEsperadas * pRojo;

        //este es el valor de este giro
        Valor resultadoActual = juego[i];

        //si el resultado es R estoy en una racha de 1 a n, debo ver en cual de esas rachas podria estoy y hasta cual podria llegar
        boolean iniciaRacha2Rojo = (resultadoActual.color == Atributo.ROJO);
        if (iniciaRacha2Rojo) {

            // cuántos rojos seguidos llevo (R = 1, RR = 2, ...)
            int largoActual = largoRachaActual(Dimension.COLOR, i, juego);

            // para indexar por nivel k = 1..8 (los mismos valores de arriba)
            int[] ocurridas = {0, rachas1RojoOcurridas, rachas2RojoOcurridas, rachas3RojoOcurridas, rachas4RojoOcurridas,
                    rachas5RojoOcurridas, rachas6RojoOcurridas, rachas7RojoOcurridas, rachas8RojoOcurridas};
            double[] esperadas = {0, rachas1RojoEsperadas, rachas2RojoEsperadas, rachas3RojoEsperadas, rachas4RojoEsperadas,
                    rachas5RojoEsperadas, rachas6RojoEsperadas, rachas7RojoEsperadas, rachas8RojoEsperadas};

            // criterio conservador (tu idea del 3% / 5%): exigir que falte MÁS de esta fracción de lo esperado.
            // 0 = sin tolerancia. Ejemplo: 0.03 -> solo se activa si ocurridas < 97% de las esperadas.
            final double TOLERANCIA = 0.0;

            // ROMPER (sale negro/cero): la racha se cierra con EXACTAMENTE 'largoActual' rojos,
            // así que la ayudan los faltantes de ESE nivel.
            if (largoActual <= 8 && ocurridas[largoActual] < esperadas[largoActual] * (1 - TOLERANCIA)) {
                double faltantes = esperadas[largoActual] - ocurridas[largoActual];
                // CADA negro recibe el valor completo (no se reparte)
                sumarCalorColor(Atributo.NEGRO, probRachaTermina(largoActual) * faltantes);
            }

            // PROLONGAR (sale otro rojo): la racha llegará al menos a largoActual+1,
            // así que la ayudan los faltantes del nivel SIGUIENTE (si falta una RR y estoy en R, el rojo suma).
            int siguiente = largoActual + 1;
            if (siguiente <= 8 && ocurridas[siguiente] < esperadas[siguiente] * (1 - TOLERANCIA)) {
                double faltantes = esperadas[siguiente] - ocurridas[siguiente];
                // CADA rojo recibe el valor completo
                sumarCalorColor(Atributo.ROJO, probRachaTermina(siguiente) * faltantes);
            }
        }
        // PENDIENTE: ¿y cuando el resultado actual es NEGRO? (racha negra / ruptura de rojos)
    }

    /**
     * Probabilidad de que una racha roja ya iniciada termine EXACTAMENTE en k elementos:
     * (k-1 rojos más y luego un no-rojo) = (18/37)^(k-1) * (19/37).  Es tu "0.5^k" con el cero
     * incluido: k=1 -> 0.5135, k=2 -> 0.2498, k=3 -> 0.1215 (tu 0.5, 0.25, 0.125).
     * Ya contiene la probabilidad de que salga rojo, por eso NO hay que volver a multiplicar por 18/37.
     */
    private double probRachaTermina(int k) {
        return (19.0 / 37) * Math.pow(18.0 / 37, k - 1);
    }

    /**
     * Suma 'calor' a calorColor de CADA valor del tablero cuyo color es 'colorBuscado'
     * (Atributo.ROJO / NEGRO). Supuesto: este calor va a calorColor (pregunta 3).
     */
    private void sumarCalorColor(int colorBuscado, double calor) {
        BigDecimal c = BigDecimal.valueOf(calor);
        for (Valor v : tablero) {
            if (v.color == colorBuscado) v.calorColor = v.calorColor.add(c);
        }
    }
}
