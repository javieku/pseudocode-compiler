package compiler;
/**
 *
 */


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Clase que representa una pila de tablas de s�mbolos.
 *
 * @author javieku
 */
public class TablaDeSimbolos implements Cloneable {
    /**
     * Pila de tablas de s�mbolos implementada como tabla hash.
     * Clave: ID.
     * Objeto asociado: Propiedades.
     */
    private ArrayList<Hashtable<String, Propiedades>> pilaTs;

    /**
     * Constructora. Inicializa la tabla.
     */
    public TablaDeSimbolos() {
        pilaTs = new ArrayList<Hashtable<String, Propiedades>>();
        pilaTs.add(0, new Hashtable<String, Propiedades>(20));
    }

    /**
     * Constructora que apila un nuevo elemento.
     *
     * @param ts tabla a apilar.
     */
    public TablaDeSimbolos(Hashtable<String, Propiedades> ts) {
        this.pilaTs.add(0, ts);
    }

    /**
     * A�ade un nuevo elemento a la tabla situada en la cima
     *
     * @param id
     * @param props
     * @return
     */
    public boolean anadeTS(String id, Propiedades props) {
        if (pilaTs.get(0).containsKey(id)) {
            pilaTs.get(0).put(id, new Propiedades(new Tipo("err", 1), props.dir, props.clase));
            return false;
        } else {
            pilaTs.get(0).put(id, props);
            return true;
        }
    }

    /**
     * Funci�n que mira si existe una variable o una constante en la tabla situada en la cima.
     *
     * @param id identificador, clave de la tabla
     * @return devuelve true si existe ese identificador
     */
    public boolean existeID(String id) {
        Iterator<Hashtable<String, Propiedades>> it = pilaTs.iterator();
        boolean enc = false;
        Hashtable<String, Propiedades> oneTs;
        while (it.hasNext() && !enc) {
            oneTs = (Hashtable<String, Propiedades>) it.next();
            enc = oneTs.containsKey(id);
        }
        return enc;
    }

    /**
     * Comprueba el valor asociado a una clave en la tabla situada en la cima
     *
     * @param id Identificador cuya valora asociado en la tabla queremos obtener.
     * @return Valor correspondiente. Null si no existe.
     */
    public Propiedades getPropiedades(String id) {
        Iterator<Hashtable<String, Propiedades>> it = pilaTs.iterator();
        boolean enc = false;
        Hashtable<String, Propiedades> oneTs;
        Propiedades p = null;
        while (it.hasNext() && !enc) {
            oneTs = (Hashtable<String, Propiedades>) it.next();
            enc = oneTs.containsKey(id);
            p = oneTs.get(id);
        }
        return p;
    }

    /**
     * Implementamos la interfaz Cloneable para copia de objectos.
     * clone en este caso copia la cima y la apila nuevamente.
     */
    public Object clone() {
        Hashtable<String, Propiedades> auxTs = new Hashtable<String, Propiedades>();
        pilaTs.add(0, auxTs);
        return this;
    }

    public void desapila() {
        pilaTs.remove(0);
    }
}