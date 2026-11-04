package ruleta;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Carga y persistencia de datos locales:
 *  - muestra real (CSV en resources o archivo externo)
 *  - corrida generada de 1M (o N) registros, guardable/recargable como base local
 */
public final class Datos {

    private Datos() {}

    /** Lee una lista de enteros (uno por línea, admite comas/espacios) desde un archivo. */
    public static int[] leer(Path archivo) throws IOException {
        List<Integer> nums = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo)) {
            for (String tok : linea.trim().split("[,;\\s]+")) {
                if (!tok.isBlank()) nums.add(Integer.parseInt(tok.trim()));
            }
        }
        return nums.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Lee un recurso del classpath (los CSV reales empaquetados). */
    public static int[] leerRecurso(String nombre) throws IOException {
        try (InputStream in = Datos.class.getResourceAsStream("/" + nombre)) {
            if (in == null) throw new FileNotFoundException("recurso no encontrado: " + nombre);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            List<Integer> nums = new ArrayList<>();
            String linea;
            while ((linea = br.readLine()) != null) {
                for (String tok : linea.trim().split("[,;\\s]+")) {
                    if (!tok.isBlank()) nums.add(Integer.parseInt(tok.trim()));
                }
            }
            return nums.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    /** Escribe una secuencia a un archivo (uno por línea). */
    public static void escribir(Path archivo, int[] datos) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(archivo)) {
            for (int v : datos) { w.write(Integer.toString(v)); w.newLine(); }
        }
    }

    /**
     * Genera una corrida de n registros con la seed dada y la guarda como base
     * local (archivo .csv). Si el archivo ya existe, lo recarga en vez de regenerar.
     */
    public static int[] baseLocal(Path archivo, int n, long seed) throws IOException {
        if (Files.exists(archivo)) {
            int[] d = leer(archivo);
            if (d.length == n) return d;
        }
        int[] d = Ruleta.generate(n, seed);
        escribir(archivo, d);
        return d;
    }

    /** Elimina los bloques repetidos (k-gramas) de una captura real. */
    public static int[] quitarRepetidos(int[] datos, int k) {
        List<Integer> d = new ArrayList<>();
        for (int v : datos) d.add(v);
        while (true) {
            Map<List<Integer>, Integer> primero = new HashMap<>();
            Set<Integer> borrar = new TreeSet<>();
            for (int i = 0; i + k <= d.size(); i++) {
                List<Integer> g = d.subList(i, i + k);
                List<Integer> key = new ArrayList<>(g);
                Integer j = primero.get(key);
                if (j != null) { for (int x = i; x < i + k; x++) borrar.add(x); }
                else primero.put(key, i);
            }
            if (borrar.isEmpty()) break;
            List<Integer> nd = new ArrayList<>();
            for (int i = 0; i < d.size(); i++) if (!borrar.contains(i)) nd.add(d.get(i));
            d = nd;
        }
        return d.stream().mapToInt(Integer::intValue).toArray();
    }
}
