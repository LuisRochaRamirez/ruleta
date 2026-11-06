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

    public int valor, color, paridad, altobajo, franja;

    // Calor por dimensión. Inicializados en ZERO: antes eran null y cualquier
    // .add() lanzaba NullPointerException.
    public BigDecimal calorColor = BigDecimal.ZERO;
    public BigDecimal calorParidad = BigDecimal.ZERO;
    public BigDecimal calorUpdown = BigDecimal.ZERO;
    public BigDecimal calorFranja = BigDecimal.ZERO;
    public BigDecimal calorAltoBajo = BigDecimal.ZERO;

    public BigDecimal calorColorAlterno = BigDecimal.ZERO;
    public BigDecimal calorParidadAlterno = BigDecimal.ZERO;
    public BigDecimal calorUpdownAlterno = BigDecimal.ZERO;
    public BigDecimal calorAltoBajoAlterno = BigDecimal.ZERO;

    /** Calor total del número = suma del calor de todas las dimensiones. */
    public BigDecimal calorTotal() {
        return calorColor.add(calorParidad).add(calorUpdown).add(calorFranja).add(calorAltoBajo)
                .add(calorColorAlterno).add(calorParidadAlterno).add(calorUpdownAlterno)
                .add(calorAltoBajoAlterno);
    }

    /** Suma calor al campo de la dimensión indicada (calorColor, calorParidad, calorAltoBajo, calorFranja). */
    public void sumarCalor(Dimension dim, BigDecimal calor) {
        switch (dim) {
            case COLOR -> calorColor = calorColor.add(calor);
            case PARIDAD -> calorParidad = calorParidad.add(calor);
            case ALTOBAJO -> calorAltoBajo = calorAltoBajo.add(calor);
            case FRANJA -> calorFranja = calorFranja.add(calor);
        }
    }

    /** Pone en cero el calor de todas las dimensiones. */
    public void limpiarCalor() {
        calorColor = calorParidad = calorUpdown = calorFranja = calorAltoBajo = BigDecimal.ZERO;
        calorColorAlterno = calorParidadAlterno = calorUpdownAlterno = calorAltoBajoAlterno = BigDecimal.ZERO;
    }

    Valor(int valor, int color, int paridad, int altobajo, int franja) {
        this.valor = valor;
        this.color = color;
        this.paridad = paridad;
        this.altobajo = altobajo;
        this.franja = franja;
    }

}
