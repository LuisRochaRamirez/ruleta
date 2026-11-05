package ruleta;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static ruleta.Valor.*;

public final class Ruleta {

    public Valor[] tablero = new Valor[]{V0, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20, V21, V22, V23, V24, V25, V26, V27, V28, V29, V30, V31, V32, V33, V34, V35, V36};
    public Valor[] juego;

    public void cargarReal(String path) throws IOException {

        juego = Datos.leerValores(path);
        System.out.println("cargada muestra real limpia: " + juego.length + " registros");
    }

    public void cargar1M(String path, int cantidad, int seed) throws IOException {

        juego = Datos.generarValores(path, cantidad, seed);
        System.out.println("cargada muestra nuevo de :" + cantidad + " registros");
    }

    public BigDecimal probarApuesta (int desde, int hasta) {
        //todo tener esquemas de apuestas

    }

    public void asignarCalor() throws IOException {

        Arrays.stream(juego).forEach(valor -> {
            calculaCalor(valor);
        });
    }

    public void calculaCalor(Valor valor) {
        //TODO aqui va el algoritmo
    }

}
