package ruleta;

/**
 * Histograma de la POSICIÓN de calor del resultado (igual que FRECUENCIA de Excel con bordes 0,0.1,...,1).
 * Imprime el conteo por intervalo y además la posición de cada giro, para cotejar celda por celda con Excel.
 */
public class HistogramaResultado {
    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        int[] conteo = new int[11];              // 0..9 = intervalos [b/10,(b+1)/10); 10 = exactamente 1.0
        int conResultado = 0;
        boolean listar = args.length > 0 && args[0].equals("lista");
        for (int i = 1; i < ruleta.juego.length; i++) {
            ruleta.limpiarCalor();
            ruleta.calculaCalor(i - 1);
            Valor salio = ruleta.juego[i];
            if (salio.valor == 0) continue;
            double maximo = -1e18, minimo = 1e18;
            for (Valor v : ruleta.tablero) {
                if (v.valor == 0) continue;
                double c = v.calorTotal().doubleValue();
                if (c > maximo) maximo = c;
                if (c < minimo) minimo = c;
            }
            double rango = maximo - minimo;
            double pos = rango == 0 ? 0.5 : (salio.calorTotal().doubleValue() - minimo) / rango;
            int bin = (int) Math.floor(pos * 10 + 1e-9);
            if (bin > 10) bin = 10;
            conteo[bin]++;
            conResultado++;
            if (listar) System.out.printf("%d;%.6f%n", i, pos);
        }
        if (!listar) {
            System.out.println("intervalo;frecuencia");
            String[] etiqueta = {"0 - 0.1","0.1 - 0.2","0.2 - 0.3","0.3 - 0.4","0.4 - 0.5","0.5 - 0.6","0.6 - 0.7","0.7 - 0.8","0.8 - 0.9","0.9 - 1.0","= 1.0 exacto"};
            int suma = 0;
            for (int b = 0; b <= 10; b++) { System.out.printf("%s;%d%n", etiqueta[b], conteo[b]); suma += conteo[b]; }
            System.out.println("total;" + suma + "   (giros con resultado != 0 = " + conResultado + ")");
            System.out.println("nota: agrupando el intervalo '=1.0' con '0.9-1.0' -> " + (conteo[9] + conteo[10]));
        }
    }
}
