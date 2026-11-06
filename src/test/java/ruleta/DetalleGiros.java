package ruleta;

import java.util.Arrays;

/** Para un rango de giros de la muestra real: dimensiones del resultado, calor de los 9 apostados y calor del resultado. Uso: desde hasta */
public class DetalleGiros {
    public static void main(String[] args) throws Exception {
        int desde = Integer.parseInt(args[0]);
        int hasta = Integer.parseInt(args[1]);
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        for (int i = desde; i <= hasta; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            Valor[] ordenados = Arrays.stream(ruleta.tablero).filter(v -> v.valor != 0).toArray(Valor[]::new);
            Arrays.sort(ordenados, (a, b) -> b.calorTotal().compareTo(a.calorTotal()));
            Valor salio = ruleta.juego[i];
            StringBuilder apostados = new StringBuilder();
            boolean acierto = false;
            int puesto = 0;
            for (int k = 0; k < ordenados.length; k++) {
                if (k < Ruleta.TOP_APUESTA) {
                    apostados.append(ordenados[k].valor).append("(").append(String.format("%.3f", ordenados[k].calorTotal())).append(") ");
                    if (ordenados[k] == salio) acierto = true;
                }
                if (ordenados[k] == salio) puesto = k + 1;
            }
            System.out.printf("giro %d: salió %d -> %s, %s, %s, %s   %s%n", i, salio.valor,
                    nombre(Dimension.COLOR, salio), nombre(Dimension.PARIDAD, salio),
                    nombre(Dimension.ALTOBAJO, salio), nombre(Dimension.FRANJA, salio), acierto ? "ACIERTO" : "fallo");
            System.out.println("   calor apostados: " + apostados.toString().trim());
            System.out.printf("   calor del resultado: %s (puesto %d de 36)%n", salio.valor == 0 ? "0 (no recibe calor)" : String.format("%.3f", salio.calorTotal()), puesto);
        }
    }

    private static String nombre(Dimension dim, Valor v) {
        int a = dim.de(v);
        switch (dim) {
            case COLOR: return a == 1 ? "ROJO" : a == -1 ? "NEGRO" : "VERDE";
            case PARIDAD: return a == 1 ? "PAR" : a == -1 ? "IMPAR" : "-";
            case ALTOBAJO: return a == 1 ? "ALTO" : a == -1 ? "BAJO" : "-";
            default: return a == 1 ? "SUPERIOR" : a == -1 ? "INFERIOR" : "-";
        }
    }
}
