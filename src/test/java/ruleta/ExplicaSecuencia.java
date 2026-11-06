package ruleta;

/**
 * Explica, para una secuencia de números, qué calor recibe cada lado de cada dimensión
 * para el SIGUIENTE giro, componente por componente.
 *
 * Uso:  java ruleta.ExplicaSecuencia 12 36 1 13 8 6 1 5
 *
 * Los calores que se muestran son SOLO lo que suma cada componente (no incluyen el calor base 1/37,
 * que es igual para todos los números).
 */
public class ExplicaSecuencia {

    // Cada dimensión tiene un lado "+1" y un lado "-1", en el orden de Dimension:
    // COLOR, PARIDAD, ALTOBAJO, FRANJA
    private static final String[] NOMBRE_LADO_MAS = {"ROJO", "PAR", "ALTO", "SUPERIOR"};
    private static final String[] NOMBRE_LADO_MENOS = {"NEGRO", "IMPAR", "BAJO", "INFERIOR"};

    private static final int NIVELES_A_MOSTRAR = 4;

    public static void main(String[] argumentos) {
        int[] numeros = leerNumeros(argumentos);
        Ruleta ruleta = new Ruleta();
        ruleta.juego = convertirAValores(numeros);
        int posicionUltimo = ruleta.juego.length - 1;

        System.out.println("SECUENCIA: " + texto(numeros) + "   (" + numeros.length + " giros)");
        System.out.println();

        for (Dimension dimension : Dimension.values()) {
            explicarDimension(ruleta, dimension, posicionUltimo);
        }

        explicarNumerosMasCalientes(ruleta, posicionUltimo);
    }

    // ------------------------------------------------------------------
    //  Una dimensión
    // ------------------------------------------------------------------

    private static void explicarDimension(Ruleta ruleta, Dimension dimension, int posicionUltimo) {
        String ladoMas = NOMBRE_LADO_MAS[dimension.ordinal()];
        String ladoMenos = NOMBRE_LADO_MENOS[dimension.ordinal()];
        Valor ejemploMas = primerValorConAtributo(ruleta, dimension, 1);
        Valor ejemploMenos = primerValorConAtributo(ruleta, dimension, -1);

        System.out.println(dimension + "  (" + ladoMas + " / " + ladoMenos + ")");

        // --- estado de las rachas ---
        Valor ultimoValor = ruleta.juego[posicionUltimo];
        int atributoUltimo = dimension.de(ultimoValor);
        int largoActual = ruleta.largoRachaActual(dimension, posicionUltimo, ruleta.juego);
        if (atributoUltimo == 0) {
            System.out.println("  último número: " + ultimoValor.valor + " (cero): no hay racha en curso");
        } else {
            String ladoUltimo = (atributoUltimo == 1) ? ladoMas : ladoMenos;
            System.out.println("  último número: " + ultimoValor.valor + " -> " + ladoUltimo
                    + ", lleva " + largoActual + " seguido(s)");
        }
        int[] cerradasMas = ruleta.contarRachas(dimension, 1, posicionUltimo, ruleta.juego, NIVELES_A_MOSTRAR);
        int[] cerradasMenos = ruleta.contarRachas(dimension, -1, posicionUltimo, ruleta.juego, NIVELES_A_MOSTRAR);
        System.out.println("  rachas CERRADAS de " + ladoMas + " por nivel 1.." + NIVELES_A_MOSTRAR + ": " + niveles(cerradasMas));
        System.out.println("  rachas CERRADAS de " + ladoMenos + " por nivel 1.." + NIVELES_A_MOSTRAR + ": " + niveles(cerradasMenos));

        // --- calor de cada componente ---
        System.out.println("  calor que recibe cada lado para el SIGUIENTE giro:");
        System.out.printf("  %-26s %-9s %-9s%n", "componente", ladoMas, ladoMenos);

        // 1) rachas por lado
        ruleta.limpiarCalor();
        ruleta.calorRacha(dimension, 1, posicionUltimo);
        ruleta.calorRacha(dimension, -1, posicionUltimo);
        double rachasLadoMas = calorDe(ejemploMas);
        double rachasLadoMenos = calorDe(ejemploMenos);
        mostrarComponente("rachas por lado", rachasLadoMas, rachasLadoMenos);

        // 2) rachas total
        ruleta.limpiarCalor();
        ruleta.calorRachaTotal(dimension, posicionUltimo);
        double rachasTotalMas = calorDe(ejemploMas);
        double rachasTotalMenos = calorDe(ejemploMenos);
        mostrarComponente("rachas total", rachasTotalMas, rachasTotalMenos);

        double alternanciaLadoMas = 0, alternanciaLadoMenos = 0, alternanciaTotalMas = 0, alternanciaTotalMenos = 0;
        if (dimension != Dimension.FRANJA) {
            Ruleta.EstadoAlternancia estado = ruleta.estadoAlternancia(dimension, posicionUltimo, ruleta.juego);
            if (estado.largo() == 0) {
                System.out.println("  (alternancia: no hay zigzag en curso)");
            } else {
                String inicio = (estado.ladoInicio() == 1) ? ladoMas : ladoMenos;
                String ultimo = (estado.ultimo() == 1) ? ladoMas : ladoMenos;
                System.out.println("  (zigzag en curso: " + estado.largo() + " símbolo(s), inició en " + inicio
                        + ", último " + ultimo + ")");
            }

            // 3) alternancia por lado de inicio
            ruleta.limpiarCalor();
            ruleta.calorAlternancia(dimension, 1, posicionUltimo);
            ruleta.calorAlternancia(dimension, -1, posicionUltimo);
            alternanciaLadoMas = calorDe(ejemploMas);
            alternanciaLadoMenos = calorDe(ejemploMenos);
            mostrarComponente("alternancia por lado", alternanciaLadoMas, alternanciaLadoMenos);

            // 4) alternancia total
            ruleta.limpiarCalor();
            ruleta.calorAlternanciaTotal(dimension, posicionUltimo);
            alternanciaTotalMas = calorDe(ejemploMas);
            alternanciaTotalMenos = calorDe(ejemploMenos);
            mostrarComponente("alternancia total", alternanciaTotalMas, alternanciaTotalMenos);
        } else {
            System.out.println("  (la franja no tiene alternancia)");
        }

        double sumaRachasMas = rachasLadoMas + rachasTotalMas;
        double sumaRachasMenos = rachasLadoMenos + rachasTotalMenos;
        double sumaAlternanciaMas = alternanciaLadoMas + alternanciaTotalMas;
        double sumaAlternanciaMenos = alternanciaLadoMenos + alternanciaTotalMenos;
        mostrarComponente("SUMA rachas", sumaRachasMas, sumaRachasMenos);
        if (dimension != Dimension.FRANJA) {
            mostrarComponente("SUMA alternancia", sumaAlternanciaMas, sumaAlternanciaMenos);
        }
        double sumaTodoMas = sumaRachasMas + sumaAlternanciaMas;
        double sumaTodoMenos = sumaRachasMenos + sumaAlternanciaMenos;
        mostrarComponente("SUMA DE LA DIMENSIÓN", sumaTodoMas, sumaTodoMenos);

        String favorecido = sumaTodoMas > sumaTodoMenos ? ladoMas : ladoMenos;
        double diferencia = Math.abs(sumaTodoMas - sumaTodoMenos);
        System.out.printf("  --> lado más caliente: %s (ventaja de calor %.4f)%n", favorecido, diferencia);
        System.out.println();

        // línea para comparar con un cálculo independiente
        System.out.printf("DATO;%s;rachas_lado;%.6f;%.6f%n", dimension, rachasLadoMas, rachasLadoMenos);
        System.out.printf("DATO;%s;rachas_total;%.6f;%.6f%n", dimension, rachasTotalMas, rachasTotalMenos);
        System.out.printf("DATO;%s;alt_lado;%.6f;%.6f%n", dimension, alternanciaLadoMas, alternanciaLadoMenos);
        System.out.printf("DATO;%s;alt_total;%.6f;%.6f%n", dimension, alternanciaTotalMas, alternanciaTotalMenos);
    }

    // ------------------------------------------------------------------
    //  Los números más calientes (calculaCalor completo)
    // ------------------------------------------------------------------

    private static void explicarNumerosMasCalientes(Ruleta ruleta, int posicionUltimo) {
        ruleta.limpiarCalor();
        ruleta.calculaCalor(posicionUltimo);

        // ordenar los 36 números (sin el cero) de mayor a menor calor, con bucles simples
        Valor[] candidatos = new Valor[36];
        for (int posicion = 0; posicion < 36; posicion++) {
            candidatos[posicion] = Valor.values()[posicion + 1];
        }
        for (int desde = 0; desde < candidatos.length - 1; desde++) {
            int indiceMayor = desde;
            for (int otro = desde + 1; otro < candidatos.length; otro++) {
                if (candidatos[otro].calorTotal().compareTo(candidatos[indiceMayor].calorTotal()) > 0) {
                    indiceMayor = otro;
                }
            }
            Valor guardado = candidatos[desde];
            candidatos[desde] = candidatos[indiceMayor];
            candidatos[indiceMayor] = guardado;
        }

        System.out.println("NÚMEROS MÁS CALIENTES (calor total, con todas las dimensiones; el 0 no participa):");
        for (int puesto = 0; puesto < 12; puesto++) {
            Valor valor = candidatos[puesto];
            String marca = puesto < Ruleta.TOP_APUESTA ? "  <- entre los " + Ruleta.TOP_APUESTA + " de la apuesta" : "";
            System.out.printf("  %2d. número %2d  calor %.4f  (%s, %s, %s, %s)%s%n", puesto + 1, valor.valor,
                    valor.calorTotal().doubleValue(),
                    valor.color == Atributo.ROJO ? "ROJO" : "NEGRO",
                    valor.paridad == Atributo.PAR ? "PAR" : "IMPAR",
                    valor.altobajo == Atributo.ALTO ? "ALTO" : "BAJO",
                    valor.franja == Atributo.SUPERIOR ? "SUPERIOR" : "INFERIOR",
                    marca);
        }
        for (Valor valor : candidatos) {
            System.out.printf("NUM;%d;%.6f%n", valor.valor, valor.calorTotal().doubleValue());
        }
    }

    // ------------------------------------------------------------------
    //  Utilidades
    // ------------------------------------------------------------------

    private static void mostrarComponente(String nombre, double calorLadoMas, double calorLadoMenos) {
        System.out.printf("  %-26s %+9.4f %+9.4f%n", nombre, calorLadoMas, calorLadoMenos);
    }

    private static double calorDe(Valor valor) {
        return valor.calorTotal().doubleValue();
    }

    private static Valor primerValorConAtributo(Ruleta ruleta, Dimension dimension, int atributo) {
        for (Valor valor : ruleta.tablero) {
            if (valor.valor != 0 && dimension.de(valor) == atributo) {
                return valor;
            }
        }
        throw new IllegalStateException("no hay valor con ese atributo");
    }

    private static String niveles(int[] cuentaPorNivel) {
        StringBuilder texto = new StringBuilder("[");
        for (int nivel = 1; nivel < cuentaPorNivel.length; nivel++) {
            if (nivel > 1) texto.append(", ");
            texto.append(cuentaPorNivel[nivel]);
        }
        return texto.append("]").toString();
    }

    private static String texto(int[] numeros) {
        StringBuilder texto = new StringBuilder();
        for (int posicion = 0; posicion < numeros.length; posicion++) {
            if (posicion > 0) texto.append(", ");
            texto.append(numeros[posicion]);
        }
        return texto.toString();
    }

    private static int[] leerNumeros(String[] argumentos) {
        if (argumentos.length == 0) {
            return new int[]{12, 36, 1, 13, 8, 6, 1, 5};
        }
        int[] numeros = new int[argumentos.length];
        for (int posicion = 0; posicion < argumentos.length; posicion++) {
            numeros[posicion] = Integer.parseInt(argumentos[posicion].replace(",", ""));
        }
        return numeros;
    }

    private static Valor[] convertirAValores(int[] numeros) {
        Valor[] valores = new Valor[numeros.length];
        for (int posicion = 0; posicion < numeros.length; posicion++) {
            for (Valor candidato : Valor.values()) {
                if (candidato.valor == numeros[posicion]) {
                    valores[posicion] = candidato;
                }
            }
        }
        return valores;
    }
}
