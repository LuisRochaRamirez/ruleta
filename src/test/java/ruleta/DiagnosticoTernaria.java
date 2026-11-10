package ruleta;

public class DiagnosticoTernaria {
    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        r.cargarReal("real_465_clean.csv");
        // repetir calculaCalor muchas veces sobre el MISMO i, sin tocar nada mas, a ver si crece
        for (int rep = 0; rep < 5; rep++) {
            r.limpiarCalor();
            r.calculaCalor(463);
            double max=-1e18; for (Valor v: r.tablero) if (v.valor!=0 && v.calorDocena.doubleValue()>max) max=v.calorDocena.doubleValue();
            System.out.println("repeticion " + rep + ": calorDocena max = " + max);
        }
        // aislar solo DOCENA, giro por giro hasta el 463, mirar magnitud creciente
        for (int i : new int[]{463}) {
            r.limpiarCalor();
            r.calculaCalor(i);
            double max = -1e18, min = 1e18;
            for (Valor v : r.tablero) if (v.valor != 0) { double c = v.calorDocena.doubleValue(); if (c>max) max=c; if (c<min) min=c; }
            System.out.printf("hasta giro %d: calorDocena max=%.3f min=%.3f%n", i, max, min);
        }
    }
}
