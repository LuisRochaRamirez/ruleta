package ruleta;

import java.util.function.ToIntFunction;

/**
 * Dimensión binaria sobre la que se cuentan rachas.
 *
 * Existe porque las constantes de Atributo se repiten (ROJO=1, PAR=1, ALTO=1,
 * SUPERIOR=1...): con solo el número 1 no se sabe si hablamos de color, paridad,
 * alto/bajo o franja. La dimensión dice QUÉ campo de Valor se mira.
 */
public enum Dimension {

    COLOR(v -> v.color),
    PARIDAD(v -> v.paridad),
    ALTOBAJO(v -> v.altobajo),
    FRANJA(v -> v.franja);

    private final ToIntFunction<Valor> campo;

    Dimension(ToIntFunction<Valor> campo) {
        this.campo = campo;
    }

    /** Atributo (+1 / -1 / 0) que tiene este Valor en esta dimensión. */
    public int de(Valor v) {
        return campo.applyAsInt(v);
    }
}
