package ruleta;

import java.math.BigDecimal;

import static ruleta.Atributo.*;

public enum Valor {

    V0(0, VERDE, PAR_NONE, AB_NONE, FR_NONE, BigDecimal.valueOf(1/37)),
    V1(1, ROJO, IMPAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V2(2, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V3(3, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V4(4, NEGRO, PAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V5(5, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V6(6, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V7(7, ROJO, IMPAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V8(8, NEGRO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V9(9, ROJO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V10(10, NEGRO, PAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V11(11, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V12(12, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V13(13, NEGRO, IMPAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V14(14, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V15(15, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V16(16, ROJO, PAR, BAJO, INFERIOR, BigDecimal.valueOf(1/37)),
    V17(17, NEGRO, IMPAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V18(18, ROJO, PAR, BAJO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V19(19, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V20(20, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V21(21, ROJO, IMPAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V22(22, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V23(23, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V24(24, NEGRO, PAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V25(25, ROJO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V26(26, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V27(27, ROJO, IMPAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V28(28, NEGRO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V29(29, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V30(30, ROJO, PAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V31(31, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V32(32, ROJO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V33(33, NEGRO, IMPAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37)),
    V34(34, ROJO, PAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V35(35, NEGRO, IMPAR, ALTO, INFERIOR, BigDecimal.valueOf(1/37)),
    V36(36, ROJO, PAR, ALTO, SUPERIOR, BigDecimal.valueOf(1/37));

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
