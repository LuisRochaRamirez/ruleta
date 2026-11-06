package ruleta;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Todos los giros de la muestra real, marcando ganado/perdido (9 plenos por giro): calor del resultado, calor máximo y mínimo de ese giro
 * (entre los 36 números, sin el cero) y cuántos números tenían calor estrictamente superior / inferior.
 */
public class AciertosConCalor {
    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        System.out.println("giro;salio;calor;calorMax;calorMin;countSuperior;countInferior;resultado");
        int aciertos = 0;
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            Valor[] ordenados = Arrays.stream(ruleta.tablero).filter(v -> v.valor != 0).toArray(Valor[]::new);
            Arrays.sort(ordenados, (a, b) -> b.calorTotal().compareTo(a.calorTotal()));
            Valor salio = ruleta.juego[i];
            boolean acierto = false;
            for (int k = 0; k < Ruleta.TOP_APUESTA; k++) if (ordenados[k] == salio) acierto = true;
            if (acierto) aciertos++;
            BigDecimal calor = salio.calorTotal();
            int superior = 0, inferior = 0;
            for (Valor v : ordenados) {
                int comparacion = v.calorTotal().compareTo(calor);
                if (comparacion > 0) superior++;
                if (comparacion < 0) inferior++;
            }
            System.out.printf("%d;%d;%.3f;%.3f;%.3f;%d;%d;%s%n", i, salio.valor, calor, ordenados[0].calorTotal(),
                    ordenados[ordenados.length - 1].calorTotal(), superior, inferior, acierto ? "ganado" : "perdido");
        }
        System.out.println("aciertos = " + aciertos);
    }
}
