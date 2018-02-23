package compiler;


public class ManejadorDeErrores {

    /**
     * Clase encargada de proveer al procesador de lenguajes durante sus diferentes etapas
     * de un sofisticado mecanismo de gesti�n de errores.
     *
     * @author: Javieku
     */
    /*----------------------------------- Errores L�xicos ------------------------------------------*/
    public static void CaracterNoEsperado(int linea, int columna) {
        muestraCabecera("ERROR: Car�cter no esperado: ", linea, columna);
    }

    public static void CaracterNoReconocido(int sigCar, int linea, int col) {
        muestraCabecera("ERROR: Car�cter no reconocido: " + (char) sigCar, linea, col);
    }

    /*----------------------------------- Errores Sint�cticos --------------------------------------*/
    public static void tokenNoEsperado(AnalizadorLexico.CategoriaLexica cat, AnalizadorLexico.Token tk) {
        muestraCabecera("ERROR: Categoria l�xica no esperada: esperado " + cat,
                tk.linea, tk.columna);
        System.exit(1);
    }

    public static void tipoIncompatible(String op, Tipo tipo1, Tipo tipo2, int linea, int col) {
        String tipoDer = "", tipoIzq = "";
        if (tipo1.t.equals("ref"))
            tipoDer = ((TipoRef) tipo1).nombre;
        else
            tipoDer = tipo1.t;
        if (tipo2.t.equals("ref"))
            tipoIzq = ((TipoRef) tipo2).nombre;
        else
            tipoIzq = tipo2.t;
        muestraCabecera("ERROR: Tipo no Compatible en " + op + " :" + tipoDer + "  " + tipoIzq + ". ", linea, col);
    }

    public static void tipoIncompatible(String op, Tipo tipo1, int linea, int col) {
        String tipoIzq = "";
        if (tipo1.t.equals("ref"))
            tipoIzq = ((TipoRef) tipo1).nombre;
        else
            tipoIzq = tipo1.t;
        muestraCabecera("ERROR: Tipo no Compatible en " + op + " :" + tipoIzq + ". ", linea, col);
    }

    public static void idNoDeclarado(String id, int linea, int col) {
        muestraCabecera("ERROR: Identificador no declarado:  " + id, linea, col);
        System.exit(1);
    }

    public static void idDuplicado(String id, int linea, int col) {
        muestraCabecera("ERROR: Identificador duplicado: ", linea, col);
    }

    public static void muestraCabecera(String s, int linea, int col) {
        System.err.println(s + "    (" + linea + "," + col + ")");
    }

    public static void CampoInexistente(String string, String campo, int linea,
                                        int columna) {
        muestraCabecera("ERROR: Nombre de campo no v�lido: " + campo, linea, columna);

    }
}
