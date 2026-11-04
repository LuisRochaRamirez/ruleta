package ruleta;

/**
 * Estrategias de apuesta probadas durante la investigación, con su resultado
 * en dinero. Convención: EV = beneficio / total apostado.
 *
 * RESUMEN DE HALLAZGOS (todas convergen a la ventaja de la casa salvo el sesgo
 * real de updown, que no es cobrable):
 *  - B6  anti-racha de bajadas (updown, pago 1:1 asumido): gana en simulación
 *        SOLO porque updown no es apostable realmente (sube/baja no existe en mesa).
 *  - F6/F7 columnas: moneda ternaria justa, sin memoria de racha -> EV ~ -2.7%/-5.5%.
 *  - F8  updown+columnas+números fijos: el sesgo de updown no se concentra -> EV<0.
 */
public final class Estrategias {

    public record Resultado(int ciclos, int ganados, int perdidos,
                            double beneficio, double apostado) {
        public double ev() { return apostado == 0 ? 0 : beneficio / apostado; }
    }

    private Estrategias() {}

    /**
     * B6: espera racha de 'disparo' bajadas en updown y apuesta a "sube" con una
     * progresión ACOTADA (p.ej. {1,3,8,20}). Pago even-money 1:1 ASUMIDO.
     * Ojo: sube/baja NO es una apuesta real de ruleta (ver documentación).
     */
    public static Resultado antiBajadasUpdown(int[] datos, int disparo, double[] progresion) {
        Boolean[] ud = Rachas.serieUpdown(datos);
        double balance = 0, apostado = 0;
        int ciclos = 0, ganados = 0, perdidos = 0, downRun = 0, n = ud.length, i = 0;
        while (i < n) {
            Boolean v = ud[i];
            if (Boolean.FALSE.equals(v)) downRun++;
            else if (Boolean.TRUE.equals(v)) downRun = 0;
            if (downRun == disparo) {
                ciclos++;
                int paso = 0, j = i + 1; boolean won = false;
                while (j < n && paso < progresion.length) {
                    Boolean nv = ud[j];
                    if (nv == null) { j++; continue; } // igual: no resuelve
                    double stake = progresion[paso]; apostado += stake;
                    if (Boolean.TRUE.equals(nv)) { balance += stake; ganados++; won = true; break; }
                    else { balance -= stake; paso++; }
                    j++;
                }
                if (!won) perdidos++;
                i = j; downRun = 0; continue;
            }
            i++;
        }
        return new Resultado(ciclos, ganados, perdidos, balance, apostado);
    }

    /**
     * F7: tras racha de columna de nivel >= kMin, apuesta FIJA 'stake' a las
     * otras 2 columnas (apuesta a que se rompe). Columna paga 2:1.
     */
    public static Resultado columnasRompe(int[] datos, int kMin, int kMax, double stake) {
        double balance = 0, apostado = 0; int bets = 0, hits = 0;
        Integer cur = null; int clen = 0, n = datos.length;
        for (int i = 0; i < n; i++) {
            Integer c = Ruleta.column(datos[i]);
            if (c == null) { cur = null; clen = 0; continue; }
            if (c.equals(cur)) clen++; else { cur = c; clen = 1; }
            if (clen >= kMin && clen <= kMax && i + 1 < n) {
                Integer nxt = Ruleta.column(datos[i + 1]);
                bets++; apostado += 2 * stake;
                if (nxt != null && !nxt.equals(cur)) { balance += stake; hits++; } // +2 premio -1 otra col
                else balance -= 2 * stake;
            }
        }
        return new Resultado(bets, hits, bets - hits, balance, apostado);
    }
}
