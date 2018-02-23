package interpreter;

/**
 * Celda que contiene un entero.
 */

public class CeldaInt extends Celda {

    int valor;

    public CeldaInt() {
        valor = -1;        //que representa null
    }

    public CeldaInt(int v) {
        valor = v;
    }

    public CeldaInt(CeldaInt c) {
        valor = c.valor;
    }

    @Override
    public String getTipo() {
        return "Integer";
    }

    @Override
    public String getStringValor() {
        return Integer.toString(valor);
    }

    @Override
    public void setStringValor(String v) {
        valor = Integer.parseInt(v);
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int v) {
        valor = v;
    }

    @Override
    public String toString() {
        String aux = "Tipo: " + getTipo() + ", Valor: " + getStringValor();
        return aux;
    }

    @Override
    public Object clone() {
        CeldaInt cd = new CeldaInt(this.valor);
        return cd;
    }
}
