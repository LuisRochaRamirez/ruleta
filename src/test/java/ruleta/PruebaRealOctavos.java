package ruleta;

import java.math.BigDecimal;

/** Algoritmo actual sobre la muestra real: aciertos y fallos por octavos de la muestra (9 plenos por giro). */
public class PruebaRealOctavos {
    public static void main(String[] args) throws Exception {
        Ruleta ruleta = new Ruleta();
        ruleta.cargarReal("real_465_clean.csv");
        int top = Ruleta.TOP_APUESTA;
        int apuestasTotales = ruleta.juego.length - 1;          // se apuesta desde el giro 1 (el 0 solo da historia)
        int tamano = apuestasTotales / 8;
        int resto = apuestasTotales % 8;
        int desde = 1;
        int aciertosTotal = 0;
        System.out.println("octavo;giros;aciertos;fallos;%aciertos;retorno");
        for (int octavo = 1; octavo <= 8; octavo++) {
            int cantidad = tamano + (octavo <= resto ? 1 : 0);
            int hasta = desde + cantidad;
            BigDecimal retorno = ruleta.probarApuesta(desde, hasta, top);
            int aciertos = (retorno.intValue() + top * cantidad) / 36;   // retorno = 36*aciertos - top*apuestas
            aciertosTotal += aciertos;
            System.out.printf("%d;%d;%d;%d;%.1f;%d  (giros %d..%d)%n", octavo, cantidad, aciertos, cantidad - aciertos,
                    100.0 * aciertos / cantidad, retorno.intValue(), desde, hasta - 1);
            desde = hasta;
        }
        System.out.printf("TOTAL;%d;%d;%d;%.1f%n", apuestasTotales, aciertosTotal, apuestasTotales - aciertosTotal,
                100.0 * aciertosTotal / apuestasTotales);
    }
}
