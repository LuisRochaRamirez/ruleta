package ruleta;

import java.util.function.ToIntFunction;

/**
 * Dimensión sobre la que se cuentan rachas. La mayoría son BINARIAS (2 lados: +1/-1, con 0 = no
 * participa). DOCENA y COLUMNA son TERNARIAS (3 lados: 1/2/3, con 0 = no participa, el cero de
 * la ruleta). lados() dice cuántos lados reales tiene cada una, para que el código de rachas sepa
 * si debe tratarla como 2 o como 3.
 *
 * Existe porque las constantes de Atributo se repiten (ROJO=1, PAR=1, ALTO=1,
 * SUPERIOR=1...): con solo el número 1 no se sabe si hablamos de color, paridad,
 * alto/bajo o franja. La dimensión dice QUÉ campo de Valor se mira.
 */
public enum Dimension {

    COLOR(v -> v.color, 2),
    PARIDAD(v -> v.paridad, 2),
    ALTOBAJO(v -> v.altobajo, 2),
    FRANJA(v -> v.franja, 2),
    DOCENA(v -> v.docena, 3),
    COLUMNA(v -> v.columna, 3);

    private final ToIntFunction<Valor> campo;
    private final int lados;

    Dimension(ToIntFunction<Valor> campo, int lados) {
        this.campo = campo;
        this.lados = lados;
    }

    /** Atributo que tiene este Valor en esta dimensión: +1/-1 (binarias) o 1/2/3 (ternarias); 0 = no participa. */
    public int de(Valor v) {
        return campo.applyAsInt(v);
    }

    /** Cuántos lados reales tiene la dimensión: 2 (binaria) o 3 (ternaria). */
    public int lados() {
        return lados;
    }

    public boolean esTernaria() {
        return lados == 3;
    }
}
