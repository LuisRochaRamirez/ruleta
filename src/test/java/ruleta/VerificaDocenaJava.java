package ruleta;

public class VerificaDocenaJava {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        for (int i : new int[]{9, 49, 99, 199, 299, 463}) {
            r.limpiarCalor();
            r.calculaCalor(i);
            double[] c = new double[4];
            for (Valor v : r.tablero) {
                if (v.valor == 0) continue;
                int lado = Dimension.DOCENA.de(v);
                c[lado] = v.calorDocena.doubleValue();
            }
            System.out.printf("giro %d: calorDocena(java) = {1=%.6f, 2=%.6f, 3=%.6f}%n", i, c[1], c[2], c[3]);
        }
    }
}
