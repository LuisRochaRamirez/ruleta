package ruleta;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Carga y persistencia de datos locales:
 * - muestra real (CSV en resources o archivo externo)
 * - corrida generada de 1M (o N) registros, guardable/recargable como base local
 */
public final class Datos {

    public static String ORIGEN = "real_465_clean.csv";
    public static String BASE1M = "base_1m.csv";

    public Datos() {
    }

    /**
     * Lee un recurso del classpath (los CSV reales empaquetados).
     */
    public static Valor[] leerValores(String nombre) throws IOException {
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
            return nums.stream()
                    .map(n -> Arrays.stream(Valor.values())
                            .filter(v -> v.valor == n)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("valor fuera de rango: " + n)))
                    .toArray(Valor[]::new);
        }
    }


    /**
     * Escribe una secuencia a un archivo (uno por línea).
     */
    public static void escribir(Path archivo, Valor[] datos) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(archivo)) {
            for (Valor v : datos) {
                w.write(Integer.toString(v.valor));
                w.newLine();
            }
        }
    }

    /**
     * Genera una corrida de n registros con la seed dada y la guarda como base
     * local (archivo .csv). Si el archivo ya existe, lo recarga en vez de regenerar.
     */
    public static Valor[] generarValores(String archivo, int cantidad, long seed) throws IOException {

        //Datos.class.getResourceAsStream("/" + archivo);
        if (Files.exists(Path.of(archivo))) {
            return leerValores(archivo);
        } else {

            Random random = new Random(seed);

            var valores = random.ints(cantidad).
                    mapToObj(n -> Arrays.stream(Valor.values())
                            .filter(v -> v.valor == n)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("valor fuera de rango: " + n)))
                    .toArray(Valor[]::new);

            escribir(Path.of(archivo), valores);

            return valores;
        }

    }


}
