package ruleta;

import java.util.Random;

/**
 * Prueba de la hipótesis del usuario: si la fórmula de calor no aporta nada, reemplazar el calor de cada número por
 * un valor AL AZAR debe dar el mismo histograma de la posición del resultado. Compara 3 "calores" sobre la muestra real:
 *   (A) el calor del algoritmo    (B) calor totalmente aleatorio    (C) todos los números con el MISMO calor.
 */
public class CalorAzaroso {
    static final String[] ETI = {"0-0.1","0.1-0.2","0.2-0.3","0.3-0.4","0.4-0.5","0.5-0.6","0.6-0.7","0.7-0.8","0.8-0.9","0.9-1.0","=1.0"};

    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        Random azar = new Random(7);
        int[] algoritmo = new int[11], aleatorio = new int[11], igual = new int[11];
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            Valor salio = ruleta.juego[i];
            if (salio.valor == 0) continue;
            double[] calorReal = new double[37], calorRnd = new double[37];
            for (int n = 1; n <= 36; n++) {
                calorReal[n] = ruleta.tablero[n].calorTotal().doubleValue();
                calorRnd[n] = azar.nextDouble();
            }
            acumula(algoritmo, calorReal, salio.valor);
            acumula(aleatorio, calorRnd, salio.valor);
            igual[5]++;                                   // con todos iguales la posición es 0.5 -> siempre el mismo tramo
        }
        imprime("(A) calor del algoritmo", algoritmo);
        imprime("(B) calor TOTALMENTE ALEATORIO", aleatorio);
        imprime("(C) todos con el mismo calor (control)", igual);
    }

    static void acumula(int[] conteo, double[] calor, int salio) {
        double max = -1e18, min = 1e18;
        for (int n = 1; n <= 36; n++) { if (calor[n] > max) max = calor[n]; if (calor[n] < min) min = calor[n]; }
        double rango = max - min;
        double pos = rango == 0 ? 0.5 : (calor[salio] - min) / rango;
        int bin = (int) Math.floor(pos * 10 + 1e-9);
        conteo[Math.min(bin, 10)]++;
    }

    static void imprime(String titulo, int[] c) {
        System.out.println(titulo + ":");
        int total = 0; for (int x : c) total += x;
        for (int b = 0; b <= 10; b++) System.out.printf("  %-9s %3d  (%5.1f%%)%n", ETI[b], c[b], 100.0 * c[b] / total);
        System.out.println();
    }
}
