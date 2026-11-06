package ruleta;

import java.math.BigDecimal;
import java.util.Random;

/** Corre probarApuesta (el algoritmo completo actual) sobre muchas secuencias aleatorias. */
public class PruebaAzar {
    public static void main(String[] args) throws Exception {
        int secuencias = Integer.parseInt(args[0]);
        int largo = Integer.parseInt(args[1]);
        int top = args.length > 3 ? Integer.parseInt(args[3]) : Ruleta.TOP_APUESTA;
        Valor[] todos = Valor.values();
        double suma = 0, suma2 = 0;
        long apuestas = 0;
        double[] evs = new double[secuencias];
        for (int s = 0; s < secuencias; s++) {
            Random rnd = new Random(Long.parseLong(args[2]) * 7919L + s * 104729L + 12345L);
            Ruleta r = new Ruleta();
            r.juego = rnd.ints(largo, 0, 37).mapToObj(n -> todos[n]).toArray(Valor[]::new);
            BigDecimal ret = r.probarApuesta(1, largo, top);
            int n = largo - 1;
            evs[s] = ret.doubleValue() / (top * (double) n);          // retorno por unidad apostada
            suma += evs[s]; suma2 += evs[s] * evs[s];
            apuestas += n;
        }
        double media = suma / secuencias;
        double sd = Math.sqrt(suma2 / secuencias - media * media);
        System.out.printf("%d secuencias aleatorias de %d giros, %d plenos por giro (%d apuestas en total)%n", secuencias, largo, top, apuestas);
        System.out.printf("EV por unidad apostada: media = %+.4f (%.2f%%)   desviación entre secuencias = %.4f   error estándar de la media = %.4f%n",
                media, media * 100, sd, sd / Math.sqrt(secuencias));
        System.out.printf("referencia teórica de apostar a plenos (cualquier cantidad): -0.0270 (-2.70%%)%n");
        System.out.printf("una secuencia de %d giros tiene desviación típica de ±%.1f%% (así de ruidosa es la muestra real de 465)%n", largo, sd * 100);
    }
}
