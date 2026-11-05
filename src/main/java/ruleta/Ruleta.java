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
        final BigDecimal UNO = BigDecimal.ONE;
        final BigDecimal PAGO = BigDecimal.valueOf(35);

        for (int i = Math.max(desde, 1); i < Math.min(hasta, juego.length); i++) {
            // 1) limpiar y recalcular el calor con el historial hasta i-1
            limpiarCalor();
            for (int j = 0; j < i; j++) calculaCalor(juego[j]);

            // 2) elegir los TOP más calientes (excluyendo el 0)
            Valor[] calientes = Arrays.stream(tablero)
                    .filter(v -> v.valor != 0)
                    .sorted((a, b) -> b.calor.compareTo(a.calor))
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

    public void asignarCalor() {
        limpiarCalor();
        Arrays.stream(juego).forEach(this::calculaCalor);
    }

    public void limpiarCalor() {
        Arrays.stream(tablero).forEach(Valor::limpiarCalor);
    }

    /**
     * Algoritmo de calor. ESQUELETO: aquí va tu lógica.
     * Recibe el resultado 'valor' recién salido y actualiza el calor del tablero.
     */
    public void calculaCalor(Valor valor) {
        // TODO: aquí va el algoritmo (asignar calor según rachas por atributo)
    }
}
