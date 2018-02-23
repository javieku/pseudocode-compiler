package interpreter;

public class CeldaMarca extends Celda {

    @Override
    public String getTipo() {
        return "Marca";
    }

    @Override
    public String getStringValor() {
        return "Marca";
    }

    @Override
    public void setStringValor(String v) {

    }

    @Override
    public String toString() {
        return "Marca";
    }

    @Override
    public Object clone() {
        CeldaMarca c = new CeldaMarca();
        return c;
    }

}
