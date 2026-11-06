package ruleta;

import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Llama a calorRacha para AMBOS lados de cada dimensión en todas las posiciones de la muestra real
 * e imprime el calor (sin base) de un número con atributo +1 y de uno con atributo -1.
 */
public class PruebaDimensiones {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        System.out.println("dim;i;pLado18;calorMas;calorMenos");
        for (Dimension dim : Dimension.values()) {
            Valor mas = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == 1).findFirst().get();
            Valor menos = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == -1).findFirst().get();
            long nMas = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == 1).count();
            long nMenos = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == -1).count();
            System.err.println(dim + ": +1 tiene " + nMas + " números, -1 tiene " + nMenos);
            for (int i = 0; i < r.juego.length; i++) {
                r.limpiarCalor();
                r.calorRacha(dim, 1, i);
                r.calorRacha(dim, -1, i);
                System.out.printf("%s;%d;%d;%s;%s%n", dim, i, nMas,
                        mas.calorTotal().setScale(6, RoundingMode.HALF_UP),
                        menos.calorTotal().setScale(6, RoundingMode.HALF_UP));
            }
        }
    }
}
