package ruleta;

import java.util.*;

/**
 * Mapa de calor PUNTUAL multidimensional (color, paridad, alto/bajo).
 * Cada número 1..36 acumula calor; el 0 no participa. Se apuesta a los 'tam'
 * números más calientes (plenos, pago 35:1).
 *
 * Tres fórmulas de 'calor por prolongar' una racha de nivel K (lado s):
 *   V1_CRECIENTE : prolonga = 18*0.5*K      (sube con K)        [fórmula inicial]
 *   V2_GEOMETRICA: prolonga = 18*(0.5)^K    (baja con K)        [reversión fija]
 *   CALENDARIO   : p = f(vencimiento por lado/nivel, ε=0.15)    [formulación usuario]
 *
 * Ajuste 1: empate exacto 0.5 vs 0.5 -> se elige la ALTERNANCIA (romper).
 *
 * HALLAZGO: en datos independientes las tres convergen a EV ~ -2.7% (apostar a
 * N plenos "calientes" = apostar a N plenos cualesquiera). El "embudo": ninguna
 * regla causal sobre un sistema independiente supera la ventaja de la casa.
 */
public final class MapaCalor {

    public enum Formula { V1_CRECIENTE, V2_GEOMETRICA, CALENDARIO }

    public record Resultado(int apuestas, int aciertos, double coberturaMedia,
                            double ev) {
        public double hitRate() { return apuestas == 0 ? 0 : (double) aciertos / apuestas; }
    }

    public static final double PUNTO = 18.0;
    public static final double EPS = 0.15;

    public MapaCalor() {}

    // estado de racha por dimensión
    public static final class Reloj {
        Boolean lado; int K;
        // para CALENDARIO: créditos por lado hacia cada nivel
        final Map<Boolean, Map<Integer,Integer>> tau = new HashMap<>();
        Reloj() { tau.put(true, new HashMap<>()); tau.put(false, new HashMap<>()); }

        double u(boolean s, int k) {
            if (k <= 1) return 1.0;
            int t = tau.get(s).getOrDefault(k, 0);
            return Math.min(1.0, t / 2.0);
        }
        void push(Boolean s) {
            if (s == null) { cerrar(); lado = null; K = 0; return; }
            if (s.equals(lado)) K++;
            else { cerrar(); lado = s; K = 1; }
        }
        public void cerrar() {
            if (K > 0 && lado != null) {
                var m = tau.get(lado);
                m.merge(K + 1, 1, Integer::sum);
                m.put(K, 0);
            }
        }
        double pProl(Formula f) {
            if (lado == null) return Double.NaN;
            return switch (f) {
                case V1_CRECIENTE -> { // calor relativo: prolonga vs rompe
                    double prol = PUNTO * 0.5 * K, rompe = PUNTO;
                    yield prol / (prol + rompe);
                }
                case V2_GEOMETRICA -> {
                    double prol = PUNTO * Math.pow(0.5, K), rompe = PUNTO;
                    yield prol / (prol + rompe);
                }
                case CALENDARIO -> {
                    double early = 1 - u(lado, K);
                    double push = u(lado, K + 1);
                    double A = EPS + push, B = EPS + early;
                    yield A / (A + B);
                }
            };
        }
    }

    public Resultado simular(int[] datos, Formula formula, int tam) {
        Reloj rc = new Reloj(), rp = new Reloj(), rh = new Reloj();
        int apuestas = 0, aciertos = 0; long cobertura = 0;
        double beneficio = 0, apostado = 0;

        for (int i = 0; i < datos.length; i++) {
            int x = datos[i];
            if (i > 0) {
                double[] heat = calor(rc, rp, rh, formula);
                Set<Integer> hs = calientes(heat, tam);
                apuestas++; cobertura += hs.size(); apostado += hs.size();
                if (hs.contains(x)) { aciertos++; beneficio += (36 - hs.size()); }
                else beneficio -= hs.size();
            }
            rc.push(Ruleta.color(x));
            rp.push(Ruleta.parity(x));
            rh.push(Ruleta.highlow(x));
        }
        double cob = apuestas == 0 ? 0 : (double) cobertura / apuestas;
        return new Resultado(apuestas, aciertos, cob, apostado == 0 ? 0 : beneficio / apostado);
    }

    public static double[] calor(Reloj rc, Reloj rp, Reloj rh, Formula f) {
        double[] heat = new double[37];
        aporte(heat, rc, Ruleta::color, f);
        aporte(heat, rp, Ruleta::parity, f);
        aporte(heat, rh, Ruleta::highlow, f);
        return heat;
    }

    public static void aporte(double[] heat, Reloj r,
                               java.util.function.IntFunction<Boolean> dim, Formula f) {
        double p = r.pProl(f);
        if (Double.isNaN(p)) return;
        if (Math.abs(p - 0.5) < 1e-9) p = 0.5 - 0.01; // Ajuste 1: empate -> alternancia
        for (int v = 1; v <= 36; v++) {
            Boolean mv = dim.apply(v);
            if (mv == null) continue;
            heat[v] += mv.equals(r.lado) ? p : (1 - p);
        }
    }

    public static Set<Integer> calientes(double[] heat, int tam) {
        Integer[] vals = new Integer[36];
        for (int v = 1; v <= 36; v++) vals[v - 1] = v;
        Arrays.sort(vals, (a, b) -> Double.compare(heat[b], heat[a]));
        double umbral = heat[vals[Math.min(tam, 36) - 1]];
        Set<Integer> hs = new TreeSet<>();
        for (int v = 1; v <= 36; v++) if (heat[v] >= umbral - 1e-9) hs.add(v);
        return hs;
    }
}
