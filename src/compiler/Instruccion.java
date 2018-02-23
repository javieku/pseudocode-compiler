package compiler;

import java.io.Serializable;

/**
 * Clase que implementa el interface serializable para poder escribir sus elementos
 * sobre un fichero binario.
 *
 * @author Javieku
 */
public class Instruccion implements Serializable {

    private String ins;

    public Instruccion(String ins) {
        this.ins = ins;
    }

    public String toString() {
        return ins;
    }
}
