package ruleta;

public class Atributo {

    public static final int ROJO=1;
    public static final int NEGRO=-1;
    public static final int VERDE=0;

    public static final int PAR=1;
    public static final int IMPAR=-1;
    public static final int PAR_NONE=0;

    public static final int ALTO=1;
    public static final int BAJO=-1;
    public static final int AB_NONE=0;

    public static final int SUPERIOR=1;
    public static final int INFERIOR=-1;
    public static final int FR_NONE=0;

    // Dimensiones TERNARIAS (3 lados, no 2): no tienen signo natural, se numeran 1/2/3. El 0 queda
    // reservado para "no participa" (el cero de la ruleta), igual que en las dimensiones binarias.
    public static final int DOCENA_NONE=0;
    public static final int DOCENA1=1;     // números 1..12
    public static final int DOCENA2=2;     // números 13..24
    public static final int DOCENA3=3;     // números 25..36

    public static final int COLUMNA_NONE=0;
    public static final int COLUMNA1=1;    // 1,4,7,...,34
    public static final int COLUMNA2=2;    // 2,5,8,...,35
    public static final int COLUMNA3=3;    // 3,6,9,...,36
}
