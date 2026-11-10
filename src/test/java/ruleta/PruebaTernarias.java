package ruleta;

import java.util.Arrays;
import java.util.Random;

/** Verificación de DOCENA y COLUMNA: asignación de atributos, simetría 1/3 en muestra grande, y calculaCalor sin errores. */
public class PruebaTernarias {
    public static void main(String[] args) throws Exception {
        // 1) asignación de docena/columna de cada número (contra la definición estándar de ruleta)
        System.out.println("docena: " + Arrays.toString(docenasDe()));
        System.out.println("columna: " + Arrays.toString(columnasDe()));

        // 2) simetría y ley 1/3 en 2M de giros aleatorios
        Random azar = new Random(9);
        Valor[] todos = Valor.values();
        Ruleta r = new Ruleta();
        int n = 2_000_000;
        r.juego = new Valor[n];
        for (int i = 0; i < n; i++) r.juego[i] = todos[azar.nextInt(37)];
        for (Dimension dim : new Dimension[]{Dimension.DOCENA, Dimension.COLUMNA}) {
            System.out.println("--- " + dim + " ---");
            for (int lado = 1; lado <= 3; lado++) {
                int[] c = r.contarRachasConAcumulado(dim, lado, n - 1, r.juego, 6);
                System.out.printf("  lado %d: niveles 1..6 = %s%n", lado, Arrays.toString(Arrays.copyOfRange(c, 1, 7)));
            }
        }

        // 3) calculaCalor no revienta y no da NaN, en la muestra real
        Ruleta real = new Ruleta();
        real.cargarReal("real_465_clean.csv");
        boolean algunNaN = false;
        for (int i = 1; i < real.juego.length; i++) {
            real.limpiarCalor();
            real.calculaCalor(i - 1);
            for (Valor v : real.tablero) {
                double c = v.calorTotal().doubleValue();
                if (Double.isNaN(c) || Double.isInfinite(c)) { algunNaN = true; System.out.println("NaN/Inf en giro " + i + " valor " + v.valor); }
            }
        }
        System.out.println("calculaCalor con docena+columna sobre la muestra real: " + (algunNaN ? "ERROR (NaN/Inf)" : "OK, sin NaN/Inf"));

        // ejemplo de calor de docena/columna en el último giro
        real.limpiarCalor();
        real.calculaCalor(real.juego.length - 2);
        System.out.println("\ncalor DOCENA en el último giro (por lado, un representante cada uno):");
        for (int lado = 1; lado <= 3; lado++) {
            final int ladoFinal = lado;
            Valor rep = Arrays.stream(real.tablero).filter(v -> v.valor != 0 && Dimension.DOCENA.de(v) == ladoFinal).findFirst().get();
            System.out.printf("  docena %d (ej. %d): calorDocena=%.4f%n", lado, rep.valor, rep.calorDocena.doubleValue());
        }
        System.out.println("calor COLUMNA en el último giro (por lado, un representante cada uno):");
        for (int lado = 1; lado <= 3; lado++) {
            final int ladoFinal = lado;
            Valor rep = Arrays.stream(real.tablero).filter(v -> v.valor != 0 && Dimension.COLUMNA.de(v) == ladoFinal).findFirst().get();
            System.out.printf("  columna %d (ej. %d): calorColumna=%.4f%n", lado, rep.valor, rep.calorColumna.doubleValue());
        }
    }

    static int[] docenasDe() {
        int[] d = new int[37];
        for (Valor v : Valor.values()) d[v.valor] = v.docena;
        return d;
    }
    static int[] columnasDe() {
        int[] c = new int[37];
        for (Valor v : Valor.values()) c[v.valor] = v.columna;
        return c;
    }
}
