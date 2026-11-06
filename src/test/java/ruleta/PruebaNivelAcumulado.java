package ruleta;

import java.util.Random;

/** Compara, en una muestra grande al azar, lo ocurrido contra lo esperado por nivel (el nivel 8 es "8 o más"). */
public class PruebaNivelAcumulado {
    public static void main(String[] args) throws Exception {
        int giros = Integer.parseInt(args[0]);
        Random azar = new Random(42);
        Valor[] todos = Valor.values();
        Ruleta ruleta = new Ruleta();
        ruleta.juego = new Valor[giros];
        for (int g = 0; g < giros; g++) ruleta.juego[g] = todos[azar.nextInt(37)];
        int ultimo = giros - 1;
        System.out.println("giros = " + giros + "   (esperadas con n = giros)");
        for (Dimension dim : new Dimension[]{Dimension.COLOR, Dimension.PARIDAD}) {
            int[] ocurridas = ruleta.contarRachasConAcumulado(dim, 1, ultimo, ruleta.juego, 8);
            double[] esperadas = ruleta.esperadasRachasParaPrueba(giros, dim, 1);
            System.out.println(dim + " lado +1, rachas por nivel (nivel 8 = 8 o más):");
            for (int k = 1; k <= 8; k++) {
                System.out.printf("  nivel %d  ocurridas %8d   esperadas %12.1f%n", k, ocurridas[k], esperadas[k]);
            }
        }
    }
}
