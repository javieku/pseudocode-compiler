package interpreter;

/**
 * Celdas en las que se guardan los enteros
 */

public class CeldaReal extends Celda {
    float valor;

    CeldaReal() {
        valor = 0;
    }

    CeldaReal(float v) {
        valor = v;
    }

    public CeldaReal(CeldaReal c) {
        valor = c.valor;
    }

    @Override
    public String getTipo() {
        return "Real";
    }

    @Override
    public String getStringValor() {
        return Float.toString(valor);
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
        CeldaReal cd = new CeldaReal(this.valor);
        return cd;
    }
}
