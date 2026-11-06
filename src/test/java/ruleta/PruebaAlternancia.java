package ruleta;

import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Alternancia en COLOR, PARIDAD y ALTOBAJO, todas las posiciones de la muestra real.
 * Imprime el calor (sin base) de un número con atributo +1 y uno con -1, por lado de inicio y total.
 */
public class PruebaAlternancia {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        System.out.println("dim;i;largo;ladoIni;ultimo;lado+_mas;lado+_menos;lado-_mas;lado-_menos;total_mas;total_menos");
        for (Dimension dim : new Dimension[]{Dimension.COLOR, Dimension.PARIDAD, Dimension.ALTOBAJO}) {
            Valor mas = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == 1).findFirst().get();
            Valor menos = Arrays.stream(r.tablero).filter(v -> v.valor != 0 && dim.de(v) == -1).findFirst().get();
            for (int i = 0; i < r.juego.length; i++) {
                Ruleta.EstadoAlternancia e = r.estadoAlternancia(dim, i, r.juego);
                String[] c = new String[6];
                int[] modo = {0, 0, 1, 1, 2, 2};
                for (int m = 0; m < 3; m++) {
                    r.limpiarCalor();
                    if (m == 0) r.calorAlternancia(dim, 1, i);
                    if (m == 1) r.calorAlternancia(dim, -1, i);
                    if (m == 2) r.calorAlternanciaTotal(dim, i);
                    c[2 * m] = mas.calorTotal().setScale(6, RoundingMode.HALF_UP).toPlainString();
                    c[2 * m + 1] = menos.calorTotal().setScale(6, RoundingMode.HALF_UP).toPlainString();
                }
                System.out.printf("%s;%d;%d;%d;%d;%s;%s;%s;%s;%s;%s%n", dim, i, e.largo(), e.ladoInicio(), e.ultimo(),
                        c[0], c[1], c[2], c[3], c[4], c[5]);
            }
        }
    }
}
