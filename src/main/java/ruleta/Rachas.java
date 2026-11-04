package ruleta;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Detección de rachas por dimensión y de rachas de ALTERNANCIA (noupdown).
 *
 * Racha: valores binarios consecutivos iguales; un null (cero) ROMPE la racha.
 * Racha de alternancia (noupdown): tramo maximal de zigzag en la serie updown.
 *   En un quiebre (dos iguales), el 1º cierra el zigzag y el 2º abre el siguiente
 *   (bordes compartidos); en 3+ iguales los del medio no participan.
 *   No existen rachas de alternancia de nivel 1.
 */
public final class Rachas {

    public record Racha(Boolean valor, int longitud, int inicio, int fin) {}
    public record RachaAlt(boolean iniciaArriba, int longitud, int inicio, int fin) {}

    private Rachas() {}

    /** Serie binaria de una dimensión (usa Boolean[]; null = no participa). */
    public static Boolean[] serie(int[] datos, IntFunction<Boolean> dim) {
        Boolean[] s = new Boolean[datos.length];
        for (int i = 0; i < datos.length; i++) s[i] = dim.apply(datos[i]);
        return s;
    }

    /** Serie de una dimensión TERNARIA (dozen/column), convertida a Boolean por
     *  valor no sirve; para rachas usamos igualdad de entero. Se codifica como
     *  Boolean[] no es adecuado -> se ofrece variante Integer. */
    public static Boolean[] serieInt(int[] datos, IntFunction<Integer> dim) {
        // Para reutilizar rachas(Boolean[]) con ternarias, comparamos igualdad
        // mediante un truco: no es binario, así que generamos rachas aparte.
        throw new UnsupportedOperationException("usar rachasTernaria");
    }

    /** Rachas sobre una dimensión ternaria (null=cero rompe). */
    public static List<Racha> rachasTernaria(int[] datos, IntFunction<Integer> dim) {
        List<Racha> out = new ArrayList<>();
        int i = 0, n = datos.length;
        while (i < n) {
            Integer v = dim.apply(datos[i]);
            if (v == null) { i++; continue; }
            int j = i;
            while (j + 1 < n && v.equals(dim.apply(datos[j + 1]))) j++;
            out.add(new Racha(v != 0, j - i + 1, i, j)); // valor no se usa para ternaria
            i = j + 1;
        }
        return out;
    }

    /** Serie updown (requiere el número anterior). */
    public static Boolean[] serieUpdown(int[] datos) {
        Boolean[] s = new Boolean[datos.length];
        Integer prev = null;
        for (int i = 0; i < datos.length; i++) { s[i] = Ruleta.updown(datos[i], prev); prev = datos[i]; }
        return s;
    }

    /** Extrae todas las rachas de una serie binaria (null rompe). */
    public static List<Racha> rachas(Boolean[] s) {
        List<Racha> out = new ArrayList<>();
        int i = 0, n = s.length;
        while (i < n) {
            Boolean v = s[i];
            if (v == null) { i++; continue; }
            int j = i;
            while (j + 1 < n && v.equals(s[j + 1])) j++;
            out.add(new Racha(v, j - i + 1, i, j));
            i = j + 1;
        }
        return out;
    }

    /** Conteo de rachas por longitud. */
    public static Map<Integer,Integer> conteoPorNivel(List<Racha> rachas) {
        Map<Integer,Integer> m = new TreeMap<>();
        for (Racha r : rachas) m.merge(r.longitud(), 1, Integer::sum);
        return m;
    }

    /** Rachas de ALTERNANCIA sobre la serie updown RESUELTA (sin null). */
    public static List<RachaAlt> rachasAlternancia(Boolean[] updownSerie) {
        List<Boolean> res = new ArrayList<>();
        for (Boolean v : updownSerie) if (v != null) res.add(v);
        List<RachaAlt> out = new ArrayList<>();
        int n = res.size();
        if (n == 0) return out;
        int start = 0, i = 1;
        while (i < n) {
            if (!res.get(i).equals(res.get(i - 1))) { i++; continue; }
            // quiebre: cerrar [start..i-1]
            out.add(new RachaAlt(res.get(start), (i - 1) - start + 1, start, i - 1));
            int j = i;
            while (j < n && res.get(j).equals(res.get(i - 1))) j++;
            start = j - 1; i = j; // abrir en el último del run de iguales
        }
        out.add(new RachaAlt(res.get(start), (n - 1) - start + 1, start, n - 1));
        return out;
    }
}
