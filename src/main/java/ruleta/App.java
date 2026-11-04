package ruleta;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Aplicación de simulación y análisis de ruleta europea.
 *
 * Reúne: datos reales (CSV), generación de base local de 1M, todas las fórmulas
 * de mapa de calor y estrategias probadas, y el análisis estadístico.
 * Ver DOCUMENTACION.md para el registro completo de hallazgos e ideas.
 */
public class App {

    static int[] base;          // base de trabajo actual
    static String origen = "ninguna";
    static final Path BASE_1M = Paths.get("base_1M.csv");

    public static void main(String[] args) throws IOException {
        System.out.println("=== Simulador y análisis de ruleta europea ===");
        cargarReal();           // por defecto carga la muestra real limpia
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            menu();
            String op = sc.hasNextLine() ? sc.nextLine().trim() : "0";
            try {
                switch (op) {
                    case "1" -> cargarReal();
                    case "2" -> cargarRealCruda();
                    case "3" -> generar1M();
                    case "4" -> Analisis.tablaRachas(base);
                    case "5" -> Analisis.firmaUpdown(base);
                    case "6" -> Analisis.convergenciaGris(base);
                    case "7" -> mapaCalor();
                    case "8" -> estrategias();
                    case "9" -> cargarArchivo(sc);
                    case "0" -> salir = true;
                    default -> System.out.println("opción no válida");
                }
            } catch (Exception e) {
                System.out.println("error: " + e.getMessage());
            }
        }
        System.out.println("fin.");
    }

    static void menu() {
        System.out.println("\nBase actual: " + origen + " (" + (base == null ? 0 : base.length) + " registros)");
        System.out.println(" 1) cargar muestra REAL limpia (465)");
        System.out.println(" 2) cargar muestra REAL cruda (542)");
        System.out.println(" 3) generar/cargar base local de 1.000.000 (seed configurable)");
        System.out.println(" 4) tabla de rachas por dimensión");
        System.out.println(" 5) firma de updown (reversión)");
        System.out.println(" 6) convergencia al gris (ley 1/√n)");
        System.out.println(" 7) mapa de calor (V1 / V2 / CALENDARIO) apostando a plenos");
        System.out.println(" 8) estrategias de apuesta (anti-bajadas updown / columnas)");
        System.out.println(" 9) cargar archivo externo (csv/txt)");
        System.out.println(" 0) salir");
        System.out.print("opción> ");
    }

    static void cargarReal() throws IOException {
        base = Datos.leerRecurso("real_465_clean.csv");
        origen = "real limpia 465";
        System.out.println("cargada muestra real limpia: " + base.length + " registros");
    }
    static void cargarRealCruda() throws IOException {
        base = Datos.leerRecurso("real_542.csv");
        origen = "real cruda 542";
        System.out.println("cargada muestra real cruda: " + base.length + " registros");
    }
    static void generar1M() throws IOException {
        long seed = 1;
        base = Datos.baseLocal(BASE_1M, 1_000_000, seed);
        origen = "generada 1M seed=" + seed;
        System.out.println("base local de 1.000.000 lista (archivo " + BASE_1M + ")");
    }
    static void cargarArchivo(Scanner sc) throws IOException {
        System.out.print("ruta del archivo> ");
        Path p = Paths.get(sc.nextLine().trim());
        base = Datos.leer(p);
        origen = "archivo " + p.getFileName();
        System.out.println("cargados " + base.length + " registros");
    }

    static void mapaCalor() {
        for (MapaCalor.Formula f : MapaCalor.Formula.values()) {
            var r = MapaCalor.simular(base, f, 18);
            System.out.printf("  %-14s hit=%.4f base=%.4f ventaja=%+.4f EV=%+.2f%%%n",
                    f, r.hitRate(), r.coberturaMedia() / 36,
                    r.hitRate() - r.coberturaMedia() / 36, r.ev() * 100);
        }
        System.out.println("  (en datos independientes las 3 -> EV ~ -2.70%: el 'embudo')");
    }

    static void estrategias() {
        var b6 = Estrategias.antiBajadasUpdown(base, 4, new double[]{1, 3, 8, 20});
        System.out.printf("  B6 anti-bajadas updown (1,3,8,20): ciclos=%d ganados=%d perdidos=%d balance=%+.0f EV=%+.4f%n",
                b6.ciclos(), b6.ganados(), b6.perdidos(), b6.beneficio(), b6.ev());
        System.out.println("     (gana en sim. solo porque 'sube/baja' NO es apuesta real de mesa)");
        var f7 = Estrategias.columnasRompe(base, 3, 8, 1.0);
        System.out.printf("  F7 columnas rompe (kMin=3): apuestas=%d aciertos=%d balance=%+.0f EV=%+.4f%n",
                f7.ciclos(), f7.ganados(), f7.beneficio(), f7.ev());
        System.out.println("     (columnas = moneda ternaria justa -> EV negativo)");
    }
}
