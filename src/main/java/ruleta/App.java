package ruleta;

import java.io.IOException;
import java.math.BigDecimal;
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

    public void main(String[] args) throws IOException {
        System.out.println("=== Simulador y análisis de ruleta europea ===");

        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal(Datos.ORIGEN);
        //ruleta.cargar1M(Datos.BASE1M, 1_000_000, 123);
        ruleta.asignarCalor();

        BigDecimal retorno = ruleta.probarApuesta(1, ruleta.juego.length);

        System.out.println("Retorno de apuesta: " + retorno.doubleValue());
    }


}
