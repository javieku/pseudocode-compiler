package interpreter;

/**
 * Celdas que sirven para inicializar la memoria.
 */

public class CeldaDefault extends Celda {
    float valor;

    CeldaDefault() {
        valor = 0;
    }

    CeldaDefault(float v) {
        valor = v;
    }

    public CeldaDefault(CeldaDefault c) {
        valor = c.valor;
    }

    @Override
    public String getTipo() {
        return "Default";
    }

    @Override
    public String getStringValor() {
        return "null";
    }

    @Override
    public void setStringValor(String v) {
        valor = Float.parseFloat(v);
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float v) {
        valor = v;
    }

    @Override
    public String toString() {
        String aux = "Tipo: " + getTipo() + ", Valor: " + getStringValor();
        return aux;
    }

    @Override
    public Object clone() {
        CeldaDefault cd = new CeldaDefault(this.valor);
        return cd;
    }
}