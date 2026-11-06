package ruleta;

import java.math.RoundingMode;

/** calculaCalor(i) COMPLETO en todas las posiciones: calor total de los 37 números (incluye la base). */
public class PruebaCalculaCalor {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        for (int i = 0; i < r.juego.length; i++) {
            r.limpiarCalor();
            r.calculaCalor(i);
            StringBuilder sb = new StringBuilder().append(i);
            for (Valor v : r.tablero) sb.append(';').append(v.calorTotal().setScale(6, RoundingMode.HALF_UP).toPlainString());
            System.out.println(sb);
        }
    }
}
