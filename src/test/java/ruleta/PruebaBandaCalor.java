package ruleta;

import java.util.Random;

/**
 * Posición normalizada del calor = (calor - mínimo) / (máximo - mínimo), entre los 36 números del giro.
 * Mide (1) la posición media del resultado y su reparto por deciles frente al de todos los números,
 * (2) la apuesta a los números con posición entre BAJO y ALTO (1 ficha a cada uno).
 * Uso: real   |   azar secuencias largo semilla
 */
public class PruebaBandaCalor {
    static final double BAJO = 0.25, ALTO = 0.75;

    static double sumaPosicionResultado = 0;
    static long giros = 0, girosConResultado = 0;
    static long[] decilesResultado = new long[10];
    static long[] decilesTodos = new long[10];
    static long fichas = 0, aciertos = 0;
    static double aciertosEsperados = 0;

    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        if (args[0].equals("real")) {
            ruleta.cargarReal("real_465_clean.csv");
            procesar(ruleta);
        } else {
            int secuencias = Integer.parseInt(args[1]);
            int largo = Integer.parseInt(args[2]);
            Valor[] todos = Valor.values();
            for (int s = 0; s < secuencias; s++) {
                Random azar = new Random(Long.parseLong(args[3]) * 7919L + s * 104729L + 99L);
                ruleta.juego = new Valor[largo];
                for (int g = 0; g < largo; g++) ruleta.juego[g] = todos[azar.nextInt(37)];
                procesar(ruleta);
            }
        }
        System.out.printf("giros apostados = %d, con resultado distinto de cero = %d%n", giros, girosConResultado);
        System.out.printf("posición media del resultado (0 = mínimo, 1 = máximo) = %.4f%n", sumaPosicionResultado / girosConResultado);
        System.out.println("reparto por deciles de la posición (%):   decil  resultado  todos-los-números");
        long totalR = 0, totalT = 0;
        for (int d = 0; d < 10; d++) { totalR += decilesResultado[d]; totalT += decilesTodos[d]; }
        for (int d = 0; d < 10; d++) {
            System.out.printf("                                          %d   %6.2f     %6.2f%n", d,
                    100.0 * decilesResultado[d] / totalR, 100.0 * decilesTodos[d] / totalT);
        }
        double desviacion = Math.sqrt(aciertosEsperados * (1 - aciertosEsperados / fichas));
        System.out.printf("APUESTA banda [%.2f, %.2f]: fichas = %d, aciertos = %d, esperados al azar = %.1f (z = %.2f)%n",
                BAJO, ALTO, fichas, aciertos, aciertosEsperados, (aciertos - aciertosEsperados) / desviacion);
        long retorno = 36 * aciertos - fichas;
        System.out.printf("retorno = %d fichas  ->  EV = %.2f%% por ficha  (referencia a plenos: -2.70%%)%n", retorno, 100.0 * retorno / fichas);
        System.out.printf("promedio de números apostados por giro = %.1f%n", (double) fichas / giros);
    }

    static void procesar(Ruleta ruleta) {
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            double maximo = -1e18, minimo = 1e18;
            for (Valor v : ruleta.tablero) {
                if (v.valor == 0) continue;
                double c = v.calorTotal().doubleValue();
                if (c > maximo) maximo = c;
                if (c < minimo) minimo = c;
            }
            double rango = maximo - minimo;
            int cubiertos = 0;
            boolean acierto = false;
            Valor salio = ruleta.juego[i];
            for (Valor v : ruleta.tablero) {
                if (v.valor == 0) continue;
                double pos = rango == 0 ? 0.5 : (v.calorTotal().doubleValue() - minimo) / rango;
                decilesTodos[Math.min(9, (int) (pos * 10))]++;
                if (pos >= BAJO && pos <= ALTO) {
                    cubiertos++;
                    if (v == salio) acierto = true;
                }
                if (v == salio) {
                    sumaPosicionResultado += pos;
                    girosConResultado++;
                    decilesResultado[Math.min(9, (int) (pos * 10))]++;
                }
            }
            giros++;
            fichas += cubiertos;
            aciertosEsperados += cubiertos / 37.0;
            if (acierto) aciertos++;
        }
    }
}
