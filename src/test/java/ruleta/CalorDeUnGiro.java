package ruleta;

import java.util.Arrays;

/** Calor de los 36 números en un giro de la muestra real y posición normalizada de cada uno. Uso: giro */
public class CalorDeUnGiro {
    public static void main(String[] args) throws Exception {
        int i = Integer.parseInt(args[0]);
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        ruleta.limpiarCalor();
        ruleta.calculaCalor(i - 1);
        Valor[] orden = Arrays.stream(ruleta.tablero).filter(v -> v.valor != 0).toArray(Valor[]::new);
        Arrays.sort(orden, (a, b) -> b.calorTotal().compareTo(a.calorTotal()));
        double max = orden[0].calorTotal().doubleValue(), min = orden[35].calorTotal().doubleValue();
        System.out.println("giro " + i + ": salió " + ruleta.juego[i].valor + "   máx = " + max + "  mín = " + min);
        int dentroBanda = 0;
        for (Valor v : orden) {
            double c = v.calorTotal().doubleValue(), pos = (c - min) / (max - min);
            if (pos >= 0.25 && pos <= 0.75) dentroBanda++;
            System.out.printf("  %2d  calor %8.3f  posición %.4f %s%s%n", v.valor, c, pos,
                    (pos >= 0.25 && pos <= 0.75) ? "[banda]" : "", v == ruleta.juego[i] ? "  <- salió" : "");
        }
        System.out.println("números dentro de la banda 25%-75%: " + dentroBanda + " de 36");
    }
}
