package ruleta;

import java.math.BigDecimal;

import static ruleta.Atributo.*;

public enum Valor {

    V0(0, VERDE, PAR_NONE, AB_NONE, FR_NONE, BigDecimal.ZERO),
    V1(1, ROJO, IMPAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V2(2, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V3(3, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V4(4, NEGRO, PAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V5(5, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V6(6, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V7(7, ROJO, IMPAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V8(8, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V9(9, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V10(10, NEGRO, PAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V11(11, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V12(12, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V13(13, NEGRO, IMPAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V14(14, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V15(15, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V16(16, ROJO, PAR, BAJO, INFERIOR, BigDecimal.ZERO),
    V17(17, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V18(18, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.ZERO),
    V19(19, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V20(20, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V21(21, ROJO, IMPAR, ALTO, SUPERIOR, BigDecimal.ZERO),
    V22(22, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V23(23, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V24(24, NEGRO, PAR, ALTO, SUPERIOR, BigDecimal.ZERO),
    V25(25, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V26(26, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V27(27, ROJO, IMPAR, ALTO, SUPERIOR, BigDecimal.ZERO),
    V28(28, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V29(29, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V30(30, ROJO, PAR, ALTO, SUPERIOR, BigDecimal.ZERO),
    V31(31, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V32(32, ROJO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V33(33, NEGRO, IMPAR, ALTO, SUPERIOR, BigDecimal.ZERO),
    V34(34, ROJO, PAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V35(35, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.ZERO),
    V36(36, ROJO, PAR, ALTO, SUPERIOR, BigDecimal.ZERO);

    public int valor, color, paridad, altobajo, franja;
    public BigDecimal calor;

    Valor(int valor, int color, int paridad, int altobajo, int franja, BigDecimal calor) {
        this.valor = valor;
        this.color = color;
        this.paridad = paridad;
        this.altobajo = altobajo;
        this.franja = franja;
        this.calor = calor;
    }

    public void sumarCalor(BigDecimal calor) {
        this.calor=this.calor.add(calor);
    }

    public void limpiarCalor() {
        this.calor=BigDecimal.ZERO;
    }

}
