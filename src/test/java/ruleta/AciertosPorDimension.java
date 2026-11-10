package ruleta;

import java.util.Random;

/**
 * Para cada giro: compara, dimensión por dimensión, el lado más caliente contra el atributo REAL del número
 * que salió. No exige acertar el pleno exacto: cuenta cuántas de las 4 dimensiones binarias "aciertan" por
 * separado (color, paridad, alto/bajo, franja). Reporta la distribución de 0 a 4 aciertos.
 *
 * El lado más caliente de una dimensión se mide sumando el calor base + el de alternancia de esa dimensión
 * (campos calorColor/calorColorAlterno, etc., usando V1 como representante de cada lado).
 *
 * Uso: real  |  azar <secuencias> <largo> <semilla>
 */
public class AciertosPorDimension {

    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        int[] conteo = new int[5];           // conteo[k] = giros con exactamente k dimensiones acertadas
        long[] aciertoPorDim = new long[4];   // aciertos de cada dimensión por separado
        long giros = 0;

        if (args[0].equals("real")) {
            ruleta.cargarReal("real_465_clean.csv");
            procesar(ruleta, conteo, aciertoPorDim);
            giros = contarGiros(ruleta);
        } else {
            int secuencias = Integer.parseInt(args[1]);
            int largo = Integer.parseInt(args[2]);
            Valor[] todos = Valor.values();
            for (int s = 0; s < secuencias; s++) {
                Random azar = new Random(Long.parseLong(args[3]) * 7919L + s * 104729L + 55L);
                ruleta.juego = new Valor[largo];
                for (int g = 0; g < largo; g++) ruleta.juego[g] = todos[azar.nextInt(37)];
                procesar(ruleta, conteo, aciertoPorDim);
            }
            giros = contarGiros(ruleta) * secuencias;
        }

        long total = 0;
        for (int k = 0; k <= 4; k++) total += conteo[k];
        System.out.println("giros con resultado != 0 analizados = " + total);
        System.out.println();
        System.out.println("dimensiones acertadas (de 4) | giros | % | % esperado al azar (binomial p=0.5)");
        double[] esperadoBinomial = {1, 4, 6, 4, 1};   // combinaciones C(4,k), se normalizan abajo
        double sumaComb = 16;
        for (int k = 0; k <= 4; k++) {
            System.out.printf("   %d              %8d   %5.1f%%      %5.1f%%%n", k, conteo[k],
                    100.0 * conteo[k] / total, 100.0 * esperadoBinomial[k] / sumaComb);
        }
        System.out.println();
        String[] nombres = {"COLOR", "PARIDAD", "ALTOBAJO", "FRANJA"};
        System.out.println("acierto de CADA dimensión por separado (esperado al azar: 50%):");
        for (int d = 0; d < 4; d++) {
            System.out.printf("  %-9s %6d / %d  (%.1f%%)%n", nombres[d], aciertoPorDim[d], total,
                    100.0 * aciertoPorDim[d] / total);
        }
    }

    static long contarGiros(Ruleta ruleta) {
        long c = 0;
        for (int i = 1; i < ruleta.juego.length; i++) if (ruleta.juego[i].valor != 0) c++;
        return c;
    }

    static void procesar(Ruleta ruleta, int[] conteo, long[] aciertoPorDim) {
        Dimension[] dims = {Dimension.COLOR, Dimension.PARIDAD, Dimension.ALTOBAJO, Dimension.FRANJA};
        for (int i = 1; i < ruleta.juego.length; i++) {
            Valor salio = ruleta.juego[i];
            if (salio.valor == 0) continue;                // el cero no tiene atributo en ninguna dimensión
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);

            int aciertos = 0;
            for (int d = 0; d < dims.length; d++) {
                Dimension dim = dims[d];
                double calorMas = calorDeLado(ruleta, dim, 1);
                double calorMenos = calorDeLado(ruleta, dim, -1);
                int ladoEsperado = calorMas >= calorMenos ? 1 : -1;   // empate: se queda con +1 (arbitrario)
                int ladoReal = dim.de(salio);
                if (ladoEsperado == ladoReal) { aciertos++; aciertoPorDim[d]++; }
            }
            conteo[aciertos]++;
        }
    }

    /** Calor (base + alternancia) de un lado de una dimensión, usando como representante cualquier Valor de ese lado. */
    static double calorDeLado(Ruleta ruleta, Dimension dim, int lado) {
        for (Valor v : ruleta.tablero) {
            if (v.valor != 0 && dim.de(v) == lado) return v.calorTotal().doubleValue();
        }
        return 0;
    }
}
