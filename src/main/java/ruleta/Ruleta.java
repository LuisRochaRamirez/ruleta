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
        calculaCalor();
    }

    public void limpiarCalor() {
        Arrays.stream(tablero).forEach(Valor::limpiarCalor);
    }

    /**
     * Algoritmo de calor. ESQUELETO: aquí va tu lógica.
     * Recibe el resultado 'valor' recién salido y actualiza el calor del tablero.
     */
    public void calculaCalor() {
        // TODO: aquí va el algoritmo (asignar calor según rachas por dimension)

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
         *
         *
         */

        //empezamos por racha de rojos
        for (int i=0;i<juego.length;i++) {

            int rachas1RojoOcurridas=contadorRacha(Atributo.ROJO, 1, i, juego);
            int rachas2RojoOcurridas=contadorRacha(Atributo.ROJO, 2, i, juego);
            int rachas3RojoOcurridas=contadorRacha(Atributo.ROJO, 3, i, juego);
            int rachas4RojoOcurridas=contadorRacha(Atributo.ROJO, 4, i, juego);
            int rachas5RojoOcurridas=contadorRacha(Atributo.ROJO, 5, i, juego);
            int rachas6RojoOcurridas=contadorRacha(Atributo.ROJO, 6, i, juego);
            int rachas7RojoOcurridas=contadorRacha(Atributo.ROJO, 7, i, juego);
            int rachas8RojoOcurridas=contadorRacha(Atributo.ROJO, 8, i, juego);

            int rachas1RojoEsperadas=(int)(juego.length*(18/37)*0.5);
            int rachas2RojoEsperadas=(int)(rachas1RojoEsperadas/2);
            int rachas3RojoEsperadas=(int)(rachas2RojoEsperadas/2);
            int rachas4RojoEsperadas=(int)(rachas3RojoEsperadas/2);
            int rachas5RojoEsperadas=(int)(rachas4RojoEsperadas/2);
            int rachas6RojoEsperadas=(int)(rachas5RojoEsperadas/2);
            int rachas7RojoEsperadas=(int)(rachas6RojoEsperadas/2);
            int rachas8RojoEsperadas=rachas7RojoEsperadas;

            //este es el valor de este giro
            Valor resultadoActual = juego [i];

            //si el resultado es R estoy en una racha de 1 a n, debo ver en cual de esas rachas podria estoy y hasta cual podria llegar
            boolean iniciaRacha2Rojo = (resultadoActual.color == Atributo.ROJO);
            if (iniciaRacha2Rojo) {

                if (rachas1RojoOcurridas < rachas1RojoEsperadas){//quiza poner un criterio conservador de una variacion esperada segun las mediciones, quiza de un 3%
                    //espero que sea racha de 1, es decir los valores del tablero que rompen la racha suman un calor de 0.5 * rachas1RojoEsperadas - rachas1RojoOcurridas
                }

                //Si el resulatdo es R o RR y ademas hay una deficiencia conservadora de rachas de 2 rojo
                boolean esRR= (i>0 && juego[i-1].color == Atributo.ROJO);
                if (iniciaRacha2Rojo && rachas2RojoOcurridas < rachas2RojoEsperadas){//quiza poner un criterio conservador de una variacion esperada segun las mediciones, quiza de un 5%
                    //espero una racha de 2, es decir los valores del tablero que rompen la racha suman un valor de 0.5*0.5 * cada giro faltante para equiparar las rachas de 2 con sus esperadas

                }
            }



        }



    }
}
