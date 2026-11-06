package ruleta;

import java.math.RoundingMode;
import java.util.Arrays;

/** calorRachaTotal en las 4 dimensiones y todas las posiciones: calor de un +1 y de un -1 (sin base). */
public class PruebaTotal {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        System.out.println("dim;i;calorMas;calorMenos");
        for (Dimension dim : Dimension.values()) {
            Valor mas = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == 1).findFirst().get();
            Valor menos = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == -1).findFirst().get();
            for (int i = 0; i < r.juego.length; i++) {
                r.limpiarCalor();
                r.calorRachaTotal(dim, i);
                System.out.printf("%s;%d;%s;%s%n", dim, i,
                        mas.calorTotal().setScale(6, RoundingMode.HALF_UP),
                        menos.calorTotal().setScale(6, RoundingMode.HALF_UP));
            }
        }
    }
}
