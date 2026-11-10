package ruleta;

import java.math.BigDecimal;

import static ruleta.Atributo.*;

public enum Valor {

    V0(0, VERDE, PAR_NONE, AB_NONE, FR_NONE),
    V1(1, ROJO, IMPAR, BAJO, INFERIOR),
    V2(2, NEGRO, PAR, BAJO, SUPERIOR),
    V3(3, ROJO, IMPAR, BAJO, SUPERIOR),
    V4(4, NEGRO, PAR, BAJO, INFERIOR),
    V5(5, ROJO, IMPAR, BAJO, SUPERIOR),
    V6(6, NEGRO, PAR, BAJO, SUPERIOR),
    V7(7, ROJO, IMPAR, BAJO, INFERIOR),
    V8(8, NEGRO, PAR, BAJO, SUPERIOR),
    V9(9, ROJO, IMPAR, BAJO, SUPERIOR),
    V10(10, NEGRO, PAR, BAJO, INFERIOR),
    V11(11, NEGRO, IMPAR, BAJO, SUPERIOR),
    V12(12, ROJO, PAR, BAJO, SUPERIOR),
    V13(13, NEGRO, IMPAR, BAJO, INFERIOR),
    V14(14, ROJO, PAR, BAJO, SUPERIOR),
    V15(15, NEGRO, IMPAR, BAJO, SUPERIOR),
    V16(16, ROJO, PAR, BAJO, INFERIOR),
    V17(17, NEGRO, IMPAR, BAJO, SUPERIOR),
    V18(18, ROJO, PAR, BAJO, SUPERIOR),
    V19(19, ROJO, IMPAR, ALTO, INFERIOR),
    V20(20, NEGRO, PAR, ALTO, INFERIOR),
    V21(21, ROJO, IMPAR, ALTO, SUPERIOR),
    V22(22, NEGRO, PAR, ALTO, INFERIOR),
    V23(23, ROJO, IMPAR, ALTO, INFERIOR),
    V24(24, NEGRO, PAR, ALTO, SUPERIOR),
    V25(25, ROJO, IMPAR, ALTO, INFERIOR),
    V26(26, NEGRO, PAR, ALTO, INFERIOR),
    V27(27, ROJO, IMPAR, ALTO, SUPERIOR),
    V28(28, NEGRO, PAR, ALTO, INFERIOR),
    V29(29, NEGRO, IMPAR, ALTO, INFERIOR),
    V30(30, ROJO, PAR, ALTO, SUPERIOR),
    V31(31, NEGRO, IMPAR, ALTO, INFERIOR),
    V32(32, ROJO, PAR, ALTO, INFERIOR),
    V33(33, NEGRO, IMPAR, ALTO, SUPERIOR),
    V34(34, ROJO, PAR, ALTO, INFERIOR),
    V35(35, NEGRO, IMPAR, ALTO, INFERIOR),
    V36(36, ROJO, PAR, ALTO, SUPERIOR);

    public int valor, color, paridad, altobajo, franja, docena, columna;

    // Calor por dimensión. Inicializados en ZERO: antes eran null y cualquier
    // .add() lanzaba NullPointerException.
    public BigDecimal calorColor = BigDecimal.ZERO;
    public BigDecimal calorParidad = BigDecimal.ZERO;
    public BigDecimal calorUpdown = BigDecimal.ZERO;
    public BigDecimal calorFranja = BigDecimal.ZERO;
    public BigDecimal calorAltoBajo = BigDecimal.ZERO;

    // Dimensiones TERNARIAS. Cada campo acumula DOS piezas que se suman juntas, sin separarlas
    // (decisión del usuario): (A) racha de "misma docena/columna exacta" (p=12/37 de seguir) y
    // (B) racha de "esta docena/columna contra las otras dos" (p=24/37 de seguir, rompe = cambia).
    public BigDecimal calorDocena = BigDecimal.ZERO;
    public BigDecimal calorColumna = BigDecimal.ZERO;

    public BigDecimal calorColorAlterno = BigDecimal.ZERO;
    public BigDecimal calorParidadAlterno = BigDecimal.ZERO;
    public BigDecimal calorUpdownAlterno = BigDecimal.ZERO;
    public BigDecimal calorAltoBajoAlterno = BigDecimal.ZERO;

    /** Calor total del número = suma del calor de todas las dimensiones. */
    public BigDecimal calorTotal() {
        return calorColor.add(calorParidad).add(calorUpdown).add(calorFranja).add(calorAltoBajo)
                .add(calorDocena).add(calorColumna)
                .add(calorColorAlterno).add(calorParidadAlterno).add(calorUpdownAlterno)
                .add(calorAltoBajoAlterno);
    }

    /** Suma calor al campo de la dimensión indicada (calorColor, calorParidad, calorAltoBajo, calorFranja, calorDocena, calorColumna). */
    public void sumarCalor(Dimension dim, BigDecimal calor) {
        switch (dim) {
            case COLOR -> calorColor = calorColor.add(calor);
            case PARIDAD -> calorParidad = calorParidad.add(calor);
            case ALTOBAJO -> calorAltoBajo = calorAltoBajo.add(calor);
            case FRANJA -> calorFranja = calorFranja.add(calor);
            case DOCENA -> calorDocena = calorDocena.add(calor);
            case COLUMNA -> calorColumna = calorColumna.add(calor);
        }
    }

    /**
     * Suma calor al campo de ALTERNANCIA de la dimensión (calorColorAlterno, calorParidadAlterno,
     * calorAltoBajoAlterno). La franja no tiene alternancia.
     */
    public void sumarCalorAlterno(Dimension dim, BigDecimal calor) {
        switch (dim) {
            case COLOR -> calorColorAlterno = calorColorAlterno.add(calor);
            case PARIDAD -> calorParidadAlterno = calorParidadAlterno.add(calor);
            case ALTOBAJO -> calorAltoBajoAlterno = calorAltoBajoAlterno.add(calor);
            case FRANJA, DOCENA, COLUMNA -> throw new UnsupportedOperationException(dim + " no tiene alternancia");
        }
    }

    /** Pone en cero el calor de todas las dimensiones. */
    public void limpiarCalor() {
        calorColor = calorParidad = calorUpdown = calorFranja = calorAltoBajo = BigDecimal.ZERO;
        calorDocena = calorColumna = BigDecimal.ZERO;
        calorColorAlterno = calorParidadAlterno = calorUpdownAlterno = calorAltoBajoAlterno = BigDecimal.ZERO;
    }

    Valor(int valor, int color, int paridad, int altobajo, int franja) {
        this.valor = valor;
        this.color = color;
        this.paridad = paridad;
        this.altobajo = altobajo;
        this.franja = franja;
        this.docena = valor == 0 ? DOCENA_NONE : (valor <= 12 ? DOCENA1 : valor <= 24 ? DOCENA2 : DOCENA3);
        // columnas de la mesa: COLUMNA1 = ...,34 (valor % 3 == 1), COLUMNA2 = ...,35 (% 3 == 2), COLUMNA3 = ...,36 (% 3 == 0)
        this.columna = valor == 0 ? COLUMNA_NONE : (valor % 3 == 1 ? COLUMNA1 : valor % 3 == 2 ? COLUMNA2 : COLUMNA3);
    }

}
