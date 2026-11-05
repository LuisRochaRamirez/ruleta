package ruleta;

/** Imprime, para varias posiciones de la muestra real, el calor que recibe un negro (V2) y un rojo (V1). */
public class PruebaCalor {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        int[] posiciones = {10, 25, 40, 60, 90, 120, 150, 200, 250, 300, 350, 400, 464};
        System.out.println("i;numero;largo;calorNegro(V2);calorRojo(V1);calorCero(V0)");
        for (int i : posiciones) {
            r.limpiarCalor();
            r.calculaCalor(i);
            int largo = r.largoRachaActual(Dimension.COLOR, i, r.juego);
            System.out.printf("%d;%d;%d;%s;%s;%s%n", i, r.juego[i].valor, largo,
                    Valor.V2.calorTotal().setScale(6, java.math.RoundingMode.HALF_UP),
                    Valor.V1.calorTotal().setScale(6, java.math.RoundingMode.HALF_UP),
                    Valor.V0.calorTotal().setScale(6, java.math.RoundingMode.HALF_UP));
        }
        // suma de calor en todos los negros vs rojos en la última posición donde disparó
        r.limpiarCalor();
        r.asignarCalor();
        System.out.println("asignarCalor() (siguiente giro tras el último registro): negro V2=" + Valor.V2.calorTotal()
                + " rojo V1=" + Valor.V1.calorTotal());
    }
}
