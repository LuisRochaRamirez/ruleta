package ruleta;

import java.util.*;

/** Análisis estadístico: conteos de rachas por dimensión y firma de reversión de updown. */
public final class Analisis {

    public Analisis() {}

    /** Tabla de conteo de rachas por nivel para las dimensiones principales. */
    public void tablaRachas(int[] d) {
        System.out.println("CANTIDAD DE RACHAS POR NIVEL (n=" + d.length + ")");
        imprimir("color",   Rachas.conteoPorNivel(Rachas.rachas(Rachas.serie(d, Ruleta::color))));
        imprimir("parity",  Rachas.conteoPorNivel(Rachas.rachas(Rachas.serie(d, Ruleta::parity))));
        imprimir("highlow", Rachas.conteoPorNivel(Rachas.rachas(Rachas.serie(d, Ruleta::highlow))));
        imprimir("dozen",   Rachas.conteoPorNivel(Rachas.rachasTernaria(d, Ruleta::dozen)));
        imprimir("column",  Rachas.conteoPorNivel(Rachas.rachasTernaria(d, Ruleta::column)));
        imprimir("updown",  Rachas.conteoPorNivel(Rachas.rachas(Rachas.serieUpdown(d))));
    }

    public static void imprimir(String nombre, Map<Integer,Integer> c) {
        StringBuilder sb = new StringBuilder(String.format("  %-8s:", nombre));
        for (int k = 1; k <= 9; k++) sb.append(String.format(" k%d=%d", k, c.getOrDefault(k, 0)));
        System.out.println(sb);
    }

    /**
     * Firma de updown: P(que una racha de bajadas continúe | ya lleva k).
     * En azar natural crece: ~0.36, 0.25, 0.19, 0.16... (sube a 0.5->0.88 la de romper).
     */
    public void firmaUpdown(int[] d) {
        Boolean[] ud = Rachas.serieUpdown(d);
        // contar longitudes de rachas de BAJADA
        int[] cont = new int[12];
        int cur = 0;
        for (Boolean v : ud) {
            if (Boolean.FALSE.equals(v)) cur++;
            else { if (cur > 0 && cur < cont.length) cont[cur]++; if (Boolean.TRUE.equals(v)) cur = 0; }
        }
        if (cur > 0 && cur < cont.length) cont[cur]++;
        // ge[k] = nº de rachas de bajada de longitud >= k
        int[] ge = new int[13];
        for (int k = 1; k <= 11; k++) { int s = 0; for (int j = k; j <= 11; j++) s += cont[j]; ge[k] = s; }
        System.out.println("FIRMA UPDOWN (bajadas): P(continuar | racha de k)");
        for (int k = 1; k <= 7; k++) {
            if (ge[k] > 0) {
                double pCont = ge[k + 1] / (double) ge[k];
                System.out.printf("  en %d bajadas -> sube %.3f  | sigue %.3f  (n=%d)%n",
                        k, 1 - pCont, pCont, ge[k]);
            }
        }
    }

    /** Convergencia de la proporción de rojo al 50% (ley 1/sqrt(n)). */
    public void convergenciaGris(int[] d) {
        int reds = 0, resolved = 0;
        int[] cps = {100, 500, 1000, 5000, 10000, 50000, 100000, 500000, d.length};
        Set<Integer> set = new TreeSet<>();
        for (int c : cps) if (c <= d.length) set.add(c);
        System.out.println("CONVERGENCIA AL GRIS (proporción de rojo):");
        for (int x : d) {
            Boolean c = Ruleta.color(x);
            if (c == null) continue;
            resolved++; if (c) reds++;
            if (set.contains(resolved)) {
                double p = reds / (double) resolved;
                double band = 0.5 / Math.sqrt(resolved);
                System.out.printf("  n=%-8d prop=%.5f desvío=%.5f banda1σ=%.5f%n",
                        resolved, p, Math.abs(p - 0.5), band);
            }
        }
    }
}
