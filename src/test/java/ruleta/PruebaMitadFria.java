package ruleta;

import java.util.Arrays;
import java.util.Random;

/** Apuesta a los 18 números de MENOR calor de cada giro (ranking; empates por orden del tablero). Uso: real | azar secuencias largo semilla */
public class PruebaMitadFria {
    static final int CUANTOS = 18;
    static long giros = 0, aciertos = 0;

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
        long fichas = giros * CUANTOS;
        double esperados = fichas / 37.0;
        double desviacion = Math.sqrt(esperados * (1 - 1.0 / 37));
        long retorno = 36 * aciertos - fichas;
        System.out.printf("giros = %d, fichas = %d, aciertos = %d (%.2f%%), esperados = %.1f (z = %.2f)%n", giros, fichas,
                aciertos, 100.0 * aciertos / giros, esperados, (aciertos - esperados) / desviacion);
        System.out.printf("retorno = %d fichas -> EV = %.2f%% por ficha%n", retorno, 100.0 * retorno / fichas);
    }

    static void procesar(Ruleta ruleta) {
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            Valor[] orden = Arrays.stream(ruleta.tablero).filter(v -> v.valor != 0).toArray(Valor[]::new);
            Arrays.sort(orden, (a, b) -> a.calorTotal().compareTo(b.calorTotal()));   // de más frío a más caliente
            for (int k = 0; k < CUANTOS; k++) if (orden[k] == ruleta.juego[i]) { aciertos++; break; }
            giros++;
        }
    }
}
