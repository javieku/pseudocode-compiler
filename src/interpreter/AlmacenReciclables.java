package interpreter;

import java.util.Stack;

public class AlmacenReciclables {

    private Stack<CeldaInt> almacenInt;
    private Stack<CeldaReal> almacenReal;

    AlmacenReciclables() {
        almacenInt = new Stack<CeldaInt>();
        almacenReal = new Stack<CeldaReal>();
    }

    public CeldaInt getCeldaInt() {
        return almacenInt.pop();
    }

    public int sizeInt() {
        return almacenInt.size();
    }

    public void putCeldaInt(CeldaInt c) {
        almacenInt.add(c);
    }

    public CeldaReal getCeldaReal() {
        return almacenReal.pop();
    }

    public int sizeReal() {
        return almacenReal.size();
    }

    public void putCeldaReal(CeldaReal c) {
        almacenReal.add(c);
    }
}
