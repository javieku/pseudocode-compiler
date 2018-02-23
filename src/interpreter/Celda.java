package interpreter;

/**
 * Clase abstracta para poder usar CeldasInt y CeldasReal.
 */
public abstract class Celda implements Cloneable {

    public abstract String getTipo();

    public abstract String getStringValor();

    public abstract void setStringValor(String v);

    public abstract String toString();

    public abstract Object clone();
}
