package ruleta;

import java.math.RoundingMode;

/** Imprime, para TODAS las posiciones de la muestra real, el calor de un negro (V2) y de un rojo (V1). */
public class PruebaCalorTodo {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        System.out.println("i;numero;largo;calorNegroV2;calorRojoV1;calorCeroV0");
        for (int i = 0; i < r.juego.length; i++) {
            r.limpiarCalor();
            r.calculaCalor(i);
            int largo = r.largoRachaActual(Dimension.COLOR, i, r.juego);
            System.out.printf("%d;%d;%d;%s;%s;%s%n", i, r.juego[i].valor, largo,
                    Valor.V2.calorTotal().setScale(6, RoundingMode.HALF_UP),
                    Valor.V1.calorTotal().setScale(6, RoundingMode.HALF_UP),
                    Valor.V0.calorTotal().setScale(6, RoundingMode.HALF_UP));
        }
    }
}
