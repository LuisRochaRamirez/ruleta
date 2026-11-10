package ruleta;

import java.util.Random;

/**
 * EXPLORACIÓN de señales. No busca la fórmula exacta: prueba varias señales distintas de "calor" y, para cada una,
 * mide en qué parte de su escala cae el número que SALIÓ. Reparte los giros en 10 tramos de la escala (de la señal
 * más fría a la más caliente) y cuenta cuántos resultados caen en cada tramo.
 *
 * Idea: si una señal predijera, los resultados se concentrarían en los tramos altos en la MUESTRA REAL, y no en
 * datos ALEATORIOS. Por eso cada señal se corre en ambos y se comparan. Un pico que aparece también en lo aleatorio
 * NO es una pista: es la forma de la señal, no una relación con lo que sale.
 *
 * Uso: real  |  azar <secuencias> <largo> <semilla>
 */
public class ExploraSenales {

    // --- cada señal da un valor (más alto = más caliente) a un número, mirando SOLO el pasado juego[0..i-1] ---
    interface Senal {
        String nombre();
        double valor(Ruleta ruleta, int numero, int i);
    }

    static final Senal[] SENALES = new Senal[]{
            // 1) El calor actual del algoritmo (suma de las 4 dimensiones).
            new Senal() {
                public String nombre() { return "calor del algoritmo (4 dimensiones)"; }
                public double valor(Ruleta r, int numero, int i) { return r.tablero[numero].calorTotal().doubleValue(); }
            },
            // 2) Hace cuántos giros que no sale este número (cuanto más ausente, más caliente). "Ley del rezagado".
            new Senal() {
                public String nombre() { return "ausencia: giros sin salir este número"; }
                public double valor(Ruleta r, int numero, int i) {
                    for (int j = i; j >= 0; j--) if (r.juego[j].valor == numero) return i - j;
                    return i + 1;
                }
            },
            // 3) Lo contrario: cuántas veces ha salido el número en los últimos 37 giros (número "de moda").
            new Senal() {
                public String nombre() { return "frecuencia: veces en los últimos 37 giros"; }
                public double valor(Ruleta r, int numero, int i) {
                    int cuenta = 0;
                    for (int j = i; j >= 0 && j > i - 37; j--) if (r.juego[j].valor == numero) cuenta++;
                    return cuenta;
                }
            },
            // 4) Cercanía en el tablero físico al último número que salió (vecinos del último resultado).
            new Senal() {
                public String nombre() { return "cercanía al último número (0..36)"; }
                public double valor(Ruleta r, int numero, int i) {
                    int distancia = Math.abs(numero - r.juego[i].valor);
                    return -Math.min(distancia, 37 - distancia);          // más cerca = más caliente
                }
            },
    };

    static final int TRAMOS = 10;
    static long[][] porTramo = new long[SENALES.length][TRAMOS];
    static long[][] porTramoTodos = new long[SENALES.length][TRAMOS];
    static long giros = 0;

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
        System.out.println("giros analizados = " + giros + "\n");
        for (int s = 0; s < SENALES.length; s++) {
            System.out.println("SEÑAL: " + SENALES[s].nombre());
            System.out.println("  tramo   % resultados   % todos-los-números   (tramo 0 = más frío, 9 = más caliente)");
            long totalR = 0, totalT = 0;
            for (int t = 0; t < TRAMOS; t++) { totalR += porTramo[s][t]; totalT += porTramoTodos[s][t]; }
            double chi = 0;
            for (int t = 0; t < TRAMOS; t++) {
                double pR = 100.0 * porTramo[s][t] / totalR, pT = 100.0 * porTramoTodos[s][t] / totalT;
                double esperado = totalR * porTramoTodos[s][t] / (double) totalT;
                if (esperado > 0) chi += Math.pow(porTramo[s][t] - esperado, 2) / esperado;
                System.out.printf("    %d       %6.2f          %6.2f%n", t, pR, pT);
            }
            // chi-cuadrado: compara el reparto de los resultados con el de todos los números (9 grados de libertad).
            System.out.printf("  chi-cuadrado (resultados vs todos) = %.1f  (al azar ~9; grande = reparto distinto)%n%n", chi);
        }
    }

    static void procesar(Ruleta ruleta) {
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            int salio = ruleta.juego[i].valor;
            if (salio == 0) continue;                       // el cero no recibe calor en varias señales
            for (int s = 0; s < SENALES.length; s++) {
                double maximo = -1e18, minimo = 1e18, valorSalio = 0;
                for (int n = 1; n <= 36; n++) {
                    double v = SENALES[s].valor(ruleta, n, i - 1);
                    if (v > maximo) maximo = v;
                    if (v < minimo) minimo = v;
                    if (n == salio) valorSalio = v;
                }
                double rango = maximo - minimo;
                for (int n = 1; n <= 36; n++) {
                    double v = SENALES[s].valor(ruleta, n, i - 1);
                    double pos = rango == 0 ? 0.5 : (v - minimo) / rango;
                    porTramoTodos[s][Math.min(TRAMOS - 1, (int) (pos * TRAMOS))]++;
                }
                double posSalio = rango == 0 ? 0.5 : (valorSalio - minimo) / rango;
                porTramo[s][Math.min(TRAMOS - 1, (int) (posSalio * TRAMOS))]++;
            }
            giros++;
        }
    }
}
