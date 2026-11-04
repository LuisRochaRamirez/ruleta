package ruleta;

import java.util.Random;

/**
 * Modelo base de la ruleta europea (0..36) y mapeo de cada número a sus
 * dimensiones. Replica exactamente la lógica validada en las simulaciones Python.
 *
 * Dimensiones binarias (valor Boolean; null = no participa, p.ej. el cero):
 *   color   : true=ROJO,  false=NEGRO
 *   parity  : true=PAR,   false=IMPAR
 *   highlow : true=ALTO(19-36), false=BAJO(1-18)
 *   updown  : true=SUBE,  false=BAJA  (respecto al número anterior; null si igual)
 *
 * Dimensiones ternarias (0,1,2; null=cero):
 *   dozen   : docena 0(1-12), 1(13-24), 2(25-36)
 *   column  : columna 0,1,2
 */
public final class Ruleta {

    public static final int SLOTS = 37; // 0..36 ruleta europea

    // Rojos en ruleta europea estándar (verificado contra fuente oficial).
    private static final boolean[] RED = new boolean[37];
    static {
        int[] reds = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
        for (int r : reds) RED[r] = true;
    }

    private Ruleta() {}

    /** true=rojo, false=negro, null=cero. */
    public static Boolean color(int n) {
        if (n == 0) return null;
        return RED[n];
    }

    /** true=par, false=impar, null=cero. */
    public static Boolean parity(int n) {
        if (n == 0) return null;
        return n % 2 == 0;
    }

    /** true=alto(19-36), false=bajo(1-18), null=cero. */
    public static Boolean highlow(int n) {
        if (n == 0) return null;
        return n >= 19;
    }

    /** true=sube, false=baja, null=igual o sin anterior. */
    public static Boolean updown(int n, Integer prev) {
        if (prev == null || n == prev) return null;
        return n > prev;
    }

    /** docena: 0,1,2 ; null=cero. */
    public static Integer dozen(int n) {
        if (n == 0) return null;
        return (n - 1) / 12;
    }

    /** columna: 0,1,2 ; null=cero. */
    public static Integer column(int n) {
        if (n == 0) return null;
        return (n - 1) % 3;
    }

    /** decena: 0(1-9),1(10-19),2(20-29),3(30-36) ; null=cero. */
    public static Integer decade(int n) {
        if (n == 0) return null;
        if (n <= 9) return 0;
        if (n <= 19) return 1;
        if (n <= 29) return 2;
        return 3;
    }

    /**
     * Genera n resultados de ruleta europea mapeando un double [0,1) uniforme a
     * 0..36, replicando int(u*37) de Python. Reproducible por seed.
     */
    public static int[] generate(int n, long seed) {
        Random rng = new Random(seed);
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = (int) (rng.nextDouble() * SLOTS); // 0..36 uniforme
        }
        return out;
    }
}
