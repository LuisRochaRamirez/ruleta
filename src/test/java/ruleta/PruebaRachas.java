package ruleta;

import java.util.Arrays;

/** Prueba de contadorRacha / largoRachaActual con casos hechos a mano y con la muestra real. */
public class PruebaRachas {

    static Valor[] seq(int... nums) {
        return Arrays.stream(nums)
                .mapToObj(n -> Arrays.stream(Valor.values()).filter(v -> v.valor == n).findFirst().get())
                .toArray(Valor[]::new);
    }

    static int fallos = 0;

    static void esperar(String que, int obtenido, int esperado) {
        boolean ok = obtenido == esperado;
        if (!ok) fallos++;
        System.out.printf("  %-62s obtenido=%d esperado=%d %s%n", que, obtenido, esperado, ok ? "OK" : "<<< FALLA");
    }

    public static void main(String[] args) throws Exception {
        Ruleta r = new Ruleta();
        // R=1,3,5,7  N=2,4,6  cero=0
        Valor[] j = seq(1, 2, 3, 5, 4, 7);   // R N R R N R
        System.out.println("Caso 1: R N R R N R");
        esperar("rojas nivel1 hasta i=4 (R cerrada en 0)", r.contadorRacha(Atributo.ROJO, 1, 4, j), 1);
        esperar("rojas nivel2 hasta i=4 (RR 2-3 cerrada por N en 4)", r.contadorRacha(Atributo.ROJO, 2, 4, j), 1);
        esperar("rojas nivel2 hasta i=3 (RR sigue ABIERTA)", r.contadorRacha(Atributo.ROJO, 2, 3, j), 0);
        esperar("rojas nivel1 hasta i=5 (la R final está abierta)", r.contadorRacha(Atributo.ROJO, 1, 5, j), 1);
        esperar("negras nivel1 hasta i=5 (N en 1 y N en 4)", r.contadorRacha(Dimension.COLOR, Atributo.NEGRO, 1, 5, j), 2);
        esperar("largoRachaActual en i=3 (RR)", r.largoRachaActual(Dimension.COLOR, 3, j), 2);
        esperar("largoRachaActual en i=4 (N)", r.largoRachaActual(Dimension.COLOR, 4, j), 1);

        Valor[] z = seq(1, 0, 1, 2);          // R 0 R N : el cero rompe
        System.out.println("Caso 2: R 0 R N (el cero rompe la racha)");
        esperar("rojas nivel1 hasta i=3", r.contadorRacha(Atributo.ROJO, 1, 3, z), 2);
        esperar("rojas nivel2 hasta i=3", r.contadorRacha(Atributo.ROJO, 2, 3, z), 0);
        esperar("largoRachaActual en i=1 (cero)", r.largoRachaActual(Dimension.COLOR, 1, z), 0);

        Valor[] p = seq(2, 4, 3, 6, 8);       // P P I P P : paridad
        System.out.println("Caso 3: paridad P P I P P");
        esperar("pares nivel2 hasta i=2 (PP cerrada por I)", r.contadorRacha(Dimension.PARIDAD, Atributo.PAR, 2, 2, p), 1);
        esperar("pares nivel2 hasta i=4 (2da PP abierta)", r.contadorRacha(Dimension.PARIDAD, Atributo.PAR, 2, 4, p), 1);

        // muestra real: conteo por nivel de rojas cerradas hasta el último registro
        r.cargarReal("real_465_clean.csv");
        int[] c = r.contarRachas(Dimension.COLOR, Atributo.ROJO, r.juego.length - 1, r.juego, 9);
        System.out.println("MUESTRA REAL: rojas cerradas por nivel (1..9)  = " + Arrays.toString(Arrays.copyOfRange(c, 1, 10)));
        int[] cn = r.contarRachas(Dimension.COLOR, Atributo.NEGRO, r.juego.length - 1, r.juego, 9);
        System.out.println("MUESTRA REAL: negras cerradas por nivel (1..9) = " + Arrays.toString(Arrays.copyOfRange(cn, 1, 10)));
        boolean igual = true;
        for (int k = 1; k <= 9; k++)
            igual &= r.contadorRacha(Atributo.ROJO, k, r.juego.length - 1, r.juego) == c[k];
        System.out.println("contadorRacha == contarRachas en los 9 niveles: " + igual);
        System.out.println(fallos == 0 ? "TODOS LOS CASOS A MANO OK" : ("FALLARON " + fallos));
    }
}
