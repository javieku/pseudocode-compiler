package compiler;
/**
 *
 */

import compiler.Propiedades.Clase;
import compiler.Traductor.Modo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Clase almac�n que contiene todas las funciones sem�nticas implementadas como m�todos
 * est�ticos
 *
 * @author javieku, vlad
 */
public class FuncionesSemanticas {

    /*------------------- M�todos asociados a la Tabla de S�mbolos ------------------------------*/

    /**
     * A�ade un elemento a la tabla de s�mbolos ts con propiedades propiedades y nombre id.
     *
     * @param ts          Tabla de s�mbolos sobre la que se aplica.
     * @param id          Nombre de la variable.
     * @param propiedades Tipo y direcci�n de la variable.
     * @return Propiedades
     */
    public static TablaDeSimbolos anadeID(TablaDeSimbolos ts, String id,
                                          Propiedades propiedades, int linea, int col) {
        if (ts.anadeTS(id, propiedades))
            return ts;
        else {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.idDuplicado("ERROR: Identificador duplicado: " + id, linea, col);
            return null;
        }

    }

    /**
     * Comprueba si existe la variable id en el tabla de s�mbolos ts.
     *
     * @param ts Tabla de s�mbolos sobre la que se aplica la funci�n.
     * @param id Nombre de la variable.
     * @return Propiedades
     */
    public static boolean existeID(TablaDeSimbolos ts, String id, int linea, int col) {
        if (ts.existeID(id))
            return true;
        else {
            //Aplicacion.inhibeGenCod();
            //ManejadorDeErrores.idNoDeclarado(id,linea, col);
            return false;
        }
    }

    /**
     * Crea una tabla de s�mbolos vac�a
     *
     * @return Propiedades
     */
    public static TablaDeSimbolos creaTs() {
        return new TablaDeSimbolos();
    }

    /**
     * Crea una tabla con el contenido de la dada por par�metro y la apila.
     *
     * @return Propiedades
     */
    public static TablaDeSimbolos creaTs(TablaDeSimbolos ts) {
        return (TablaDeSimbolos) ts.clone();
    }

    /**
     * Devuelve la fila asociados a id.
     *
     * @param ts Tabla de s�mbolos sobre la que se aplica la funci�n.
     * @param id Nombre de la variable.
     * @return Propiedades
     */
    public static Propiedades damePropiedades(TablaDeSimbolos ts, String id, int linea, int col) {
        if (ts.existeID(id))
            return ts.getPropiedades(id);
        else {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.idNoDeclarado(id, linea, col);
            return new Propiedades(new Tipo("err", -1), -1, Clase.var);
        }

    }

    /**
     * Desapila de ts una tabla de simbolos.
     *
     * @param ts pila de tabla de s�mbolos que se quiere restaurar a un contexto previo.
     * @return TablaDeSimbolos con una tabla menos.
     */
    public static TablaDeSimbolos getAnterior(TablaDeSimbolos ts) {
        ts.desapila();
        return ts;
    }

    /*-------------------- M�todos asociados a las restricciones contextuales ------------------------------*/

    /**
     * Funci�n que calcula el tipo del resultado aplicando la regla de tipo asociada a in.
     *
     * @param ts    Tabla de s�mbolos sobre la que se aplica la funci�n.
     * @param lex   Nombre de la variable.
     * @param linea
     * @return Tipo
     * @columna columnas
     */
    public static Tipo tipoDeEnt(TablaDeSimbolos ts, String lex, int linea, int col) {
        Tipo tipoSinRef = ref_(damePropiedades(ts, lex, linea, col).tipo, ts, linea, col);
        if (tipoSinRef.t.equals("err")) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("in", damePropiedades(ts, lex, linea, col).tipo, linea, col);
            return new Tipo("err", -1);
        } else {

            if (tipoSinRef.t.equals("real") || tipoSinRef.t.equals("integer"))
                return tipoSinRef;
            else {
                Aplicacion.inhibeGenCod();
                ManejadorDeErrores.tipoIncompatible("in", damePropiedades(ts, lex, linea, col).tipo, linea, col);
                return new Tipo("err", -1);
            }
        }
    }

    /**
     * Funci�n que calcula el tipo del resultado aplicando la regla de tipo asociada a out.
     *
     * @param tipo    tipo de la expresi�n de salida.
     * @param linea   fila del token
     * @param columna del token
     * @return Tipo
     */
    public static Tipo tipoDeSal(TablaDeSimbolos ts, Tipo tipo, int linea, int col) {
        Tipo tipoSinRef = ref_(tipo, ts, linea, col);
        if (tipo.t.equals("err")) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("out", tipo, linea, col);
            return tipo;
        } else if (tipoSinRef.t.equals("real") || tipoSinRef.t.equals("integer"))    //solo reconoce tipos b�sicos
            return tipoSinRef;
        else {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("out", tipo, linea, col);
            return new Tipo("err", -1);
        }

    }

    /**
     * Funci�n que calcula el tipo del resultado aplicando la regla de tipo asociada a  asig.
     *
     * @param ts      tabla de s�mbolos.
     * @param tipo1   tipo del id sobre el que se aplica la asignaci�n.
     * @param tipo2   tipo de la expresi�n que calcula el valor de la asignaci�n.
     * @param linea   fila del token
     * @param columna del token
     * @return Tipo
     */
    public static Tipo tipoDeAsig(TablaDeSimbolos ts, Tipo tipo1, Tipo tipo2, int linea, int col) {
        if (tipo1.t.equals("err") || tipo2.t.equals("err")) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("=", tipo1, tipo2, linea, col);
            return new Tipo("err", -1);
        } else {
            if (tipoCompatible(tipo1, tipo2, ts) || (tipo1.t.equals("real") && tipo2.t.equals("integer")))
                return tipo1;
            else {
                Aplicacion.inhibeGenCod();
                ManejadorDeErrores.tipoIncompatible("=", tipo1, tipo2, linea, col);
                return new Tipo("err", -1);
            }
        }
    }

    /**
     * Funci�n que calcula el tipo del resultado aplicando la regla de tipo asociada a  la operaci�n binaria correspondiente.
     *
     * @param oph     operaci�n binaria.
     * @param tipo1   tipo del operador 1.
     * @param tipo2   tipo del operador 2.
     * @param linea   fila del token
     * @param columna del token
     * @return Tipo
     */
    public static Tipo tipoDeB(String oph, Tipo tipo1, Tipo tipo2, TablaDeSimbolos ts, int linea, int col) {
        if (tipo1.t.equals("err") || tipo2.t.equals("err")) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
            return new Tipo("err", -1);
        } else {
            if (oph.equals("op_Menor") || oph.equals("op_Mayor") || oph.equals("op_Meoig") ||
                    oph.equals("op_Maoig") || oph.equals("op_Igual") || oph.equals("op_NoIgual")) {
                if (oph.equals("op_Igual") || oph.equals("op_NoIgual")) {
                    Tipo tipo1aux = ref_(tipo1, ts, linea, col);
                    Tipo tipo2aux = ref_(tipo2, ts, linea, col);
                    if ((tipo1aux.t.equals("punt") || tipo1aux.t.equals("nulo")) && (tipo2aux.t.equals("punt") || tipo2aux.t.equals("nulo")))
                        return new Tipo("integer", 1);
                }
                if (tipo1.t.equals("real") || tipo1.t.equals("integer") &&
                        tipo2.t.equals("real") || tipo2.t.equals("integer"))
                    return new Tipo("integer", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_Suma") || oph.equals("op_Resta") || oph.equals("op_Por") ||
                    oph.equals("op_Div")) {
                if (tipo1.t.equals("integer") && tipo2.t.equals("integer"))
                    return new Tipo("integer", 1);
                else if (tipo1.t.equals("real") && tipo2.t.equals("real"))
                    return new Tipo("real", 1);
                else if (tipo1.t.equals("real") && tipo2.t.equals("integer") ||
                        tipo1.t.equals("integer") && tipo2.t.equals("real"))
                    return new Tipo("real", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_O") || oph.equals("op_Y")) {
                if (tipo1.t.equals("integer") && tipo2.t.equals("integer"))
                    return new Tipo("integer", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_Mod")) {
                if (tipo1.t.equals("integer") && tipo2.t.equals("integer"))
                    return new Tipo("integer", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
                    return new Tipo("err", -1);
                }
            } else {
                Aplicacion.inhibeGenCod();
                ManejadorDeErrores.tipoIncompatible(oph, tipo1, tipo2, linea, col);
                return new Tipo("err", -1);
            }
        }

    }

    /**
     * Funci�n que calcula el tipo del resultado aplicando la regla de tipo asociada a  la operaci�n unaria correspondiente.
     *
     * @param oph     operaci�n unaria
     * @param tipo    tipo del operador.
     * @param linea   fila del token
     * @param columna del token
     * @return String
     */
    public static Tipo tipoDeU(String oph, Tipo tipo, int linea, int col) {
        if (tipo.t.equals("err")) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible(oph, tipo, linea, col);
            return tipo;
        } else {
            if (oph.equals("op_Neg")) {
                if (tipo.t.equals("integer") || tipo.t.equals("real"))
                    return tipo; //tipo s�lo puede ser o real o integer
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_No")) {
                if (tipo.t.equals("integer"))
                    return new Tipo("integer", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_CasReal")) {
                if (tipo.t.equals("integer") || tipo.t.equals("real"))
                    return new Tipo("real", 1);
                else {
                    Aplicacion.inhibeGenCod();
                    ManejadorDeErrores.tipoIncompatible(oph, tipo, linea, col);
                    return new Tipo("err", -1);
                }
            } else if (oph.equals("op_CasInt")) {
                if (tipo.t.equals("integer") || tipo.t.equals("real"))
                    return new Tipo("integer", 1);
                else {

                    return new Tipo("err", -1);
                }
            } else {
                Aplicacion.inhibeGenCod();
                ManejadorDeErrores.tipoIncompatible(oph, tipo, linea, col);
                return new Tipo("err", -1);
            }
        }
    }

    /*------------------------ M�todos asociados a la traducci�n -----------------------------------*/

    /**
     * Funci�n que concatena bloques de c�digo.
     *
     * @param cod1 c�digo por la izquierda.
     * @param cod2 c�digo por la derecha.
     * @return List<Instruccion>
     */
    public static List<Instruccion> concatena(List<Instruccion> cod1, List<Instruccion> cod2) {
        ArrayList<Instruccion> l1 = (ArrayList<Instruccion>) cod1;
        ArrayList<Instruccion> l2 = (ArrayList<Instruccion>) cod2;
        l1.addAll(l2);
        return l1;
    }

    /**
     * Funci�n que genera el c�digo de in.
     *
     * @param ts  tabla de s�mbolos
     * @param lex identificador de la variable en la que se almacenan los datos de entrada.
     * @param cod
     * @return List<Instruccion>
     */
    public static List<Instruccion> genCodEnt(TablaDeSimbolos ts, Tipo tipo, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        if (!tipo.t.equals("err")) {
            //accesoVar(ts.getPropiedades(lex),l);
            if (tipo.t.equals("integer"))
                l.add(new Instruccion("EntradaEntero"));
            else
                l.add(new Instruccion("EntradaReal"));

            l.add(new Instruccion("desapila_ind"));
            l.add(new Instruccion("apila_ind"));
        }
        return concatena(cod, l);
    }

    /**
     * Funci�n que genera el c�digo de sal.
     *
     * @param cod  c�digo de la expresi�n de salida.
     * @param j
     * @param i
     * @param ts
     * @param tipo
     * @return List<Instruccion>
     */
    public static List<Instruccion> genCodSal(List<Instruccion> cod, boolean desig) {
        if (desig)
            cod.add(new Instruccion("apila_ind"));
        cod.add(new Instruccion("Salida"));
        return cod;
    }

    /**
     * Funci�n que genera el c�digo de Asignar.
     *
     * @param ts   tabla de s�mbolos
     * @param lex  identificador de la variable en la que se almacenan los datos de entrada.
     * @param cod  c�digo de la expresi�n que calcula el valor asignado.
     * @param tipo tipo  de la expresi�n que calcula el valor asignado.
     * @return List<Instruccion>
     */
    public static List<Instruccion> genCodAsig(TablaDeSimbolos ts, String lex, List<Instruccion> cod, Tipo tipo, int linea, int col) {
		/*	//l.add(0, new Instruccion("apilaDir " + damePropiedades(ts,lex).dir));
			if(damePropiedades(ts,lex).tipo.t.equals("real") && tipo.t.equals("integer"))
				l.add(new Instruccion("CasR"));
			l.add(new Instruccion("desapilaDir " + damePropiedades(ts,lex).dir));
			l.add(new Instruccion("apilaDir " + damePropiedades(ts,lex).dir));*/
        if (ts.existeID(lex) && !tipo.t.equals("err")) {
            if (damePropiedades(ts, lex, linea, col).tipo.t.equals("real") && tipo.t.equals("integer"))
                cod.add(new Instruccion("CasR"));

            if (tipo.t.equals("ref")) // si es una referencia, buscamos el tipo al que se refiere
                tipo = ref_(tipo, ts, linea, col);

            if (tipoBasico(ts, tipo, linea, col))
                cod.add(new Instruccion("desapila_ind"));
            else
                cod.add(new Instruccion("mueve " + tipo.tam));

            //accesoVar(ts.getPropiedades(lex),cod);
            cod.add(new Instruccion("apila_ind")); // apilamos el valor de la direcci�n que hab�amos copiado anteriormente al principio de la asignaci�n
        }

        return cod;
    }

    /**
     * Funci�n que genera el c�digo de la operaci�n binaria correspondiente.
     *
     * @param oph   operaci�n binaria
     * @param cod   c�digo del operando 1
     * @param tipo1 tipo del operando 1
     * @param tipo2 tipo del operando 2
     * @return List<Instruccion>
     */
    public static List<Instruccion> genCodOp(String oph, List<Instruccion> cod, Tipo tipo1, int etqOp1, Tipo tipo2) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        if (!tipo1.t.equals("err") && !tipo2.t.equals("err")) {
            boolean castop1 = false;    //para la regla de alineamiento
            boolean castop2 = false;

            if (tipo1.t.equals("integer") && tipo2.t.equals("real"))
                castop1 = true;
            else if (tipo1.t.equals("real") && tipo2.t.equals("integer"))
                castop2 = true;

            if (oph.equals("op_Menor")) {
                l.add(new Instruccion("Menor"));
            } else if (oph.equals("op_Mayor")) {
                l.add(new Instruccion("Mayor"));
            } else if (oph.equals("op_Meoig")) {
                l.add(new Instruccion("MenorIg"));
            } else if (oph.equals("op_Maoig")) {
                l.add(new Instruccion("MayorIg"));
            } else if (oph.equals("op_Igual")) {
                l.add(new Instruccion("Igual"));
            } else if (oph.equals("op_NoIgual")) {
                l.add(new Instruccion("NoIgual"));
            } else if (oph.equals("op_Suma")) {
                l.add(new Instruccion("Suma"));
            } else if (oph.equals("op_Resta")) {
                l.add(new Instruccion("Resta"));
            } else if (oph.equals("op_Por")) {
                l.add(new Instruccion("Mult"));
            } else if (oph.equals("op_Div")) {
                l.add(new Instruccion("Div"));
            } else if (oph == "op_O") {
                l.add(new Instruccion("Or"));
            } else if (oph == "op_Y") {
                l.add(new Instruccion("And"));
            } else if (oph == "op_Mod") {
                l.add(new Instruccion("Mod"));
                castop1 = false;    //no hay alineamiento de tipos en '%'
                castop2 = false;
            }
            if (castop1)
                //cod1.add(new Instruccion("CasR")); necesario a�adirla a mano x_x
                cod.add(etqOp1, new Instruccion("CasR"));
            else if (castop2)
                cod.add(new Instruccion("CasR"));
        }

        return concatena(cod, l);
    }

    /**
     * Funci�n que genera el c�digo de  del operador unario dado.
     *
     * @param oph operador unario
     * @param cod identificador de la variable en la que se almacenan los datos de entrada.
     * @return List<Instruccion>
     */
    public static List<Instruccion> genCodOp(String oph, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        if (oph.equals("op_Neg")) {
            l.add(new Instruccion("CambSig"));
        } else if (oph.equals("op_No")) {
            l.add(new Instruccion("Neg"));
        } else if (oph == "op_CasReal") {
            l.add(new Instruccion("CasR"));
        } else if (oph == "op_CasInt") {
            l.add(new Instruccion("CasI"));
        }
        return concatena(cod, l);
    }

    /**
     * Obtiene el valor n�merico entero del lexema correspondiente.
     *
     * @param lex cadena de caracteres que representa un n�mero.
     * @return int
     */
    public static int valorDeN(String lex) {
        return Integer.valueOf(lex);
    }

    /**
     * Obtiene el valor n�merico real del lexema correspondiente.
     *
     * @param lex cadena de caracteres que representa un n�mero.
     * @return int
     */
    public static float valorDeR(String lex) {
        return Float.valueOf(lex);
    }
    /*------------------------ M�todos asociados a la Manipulaci�n de Memoria o C�digo -----------------------------------*/

    /*------------------------ LAS NUEVAS FUNCIONES!!!!!!!!!!!!!!!!!!!!!!!!! -----------------------------------*/

    /**
     * Crea el display inicial del programa
     *
     * @param cod : C�digo que tenemos hasta el momento
     */
    public static List<Instruccion> inicio(List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        l.add(new Instruccion("apilaEntero "));  // esta instrucci�n se va a parchear el argumento
        l.add(new Instruccion("desapilaDir 1"));
        l.add(new Instruccion("apilaEntero "));  // esta instrucci�n tambi�n se va a parchear el argumento
        l.add(new Instruccion("desapilaDir 0"));
        return concatena(cod, l);
    }

    /**
     * M�todo que a�ade un argumento a una instrucci�n ya emitida
     *
     * @param posCod : Posici�n de la instrucci�n dentro del c�digo
     * @param arg    : argumento que se le va a a�adir a la instrucci�n
     * @param cod    : el c�digo que llevamos hasta el momento
     * @return el nuevo c�digo
     */
    public static List<Instruccion> parchea(int posCod, int arg, List<Instruccion> cod) {
        if (Aplicacion.getGeneraCod()) {
            String instr = cod.get(posCod).toString();
            cod.set(posCod, new Instruccion(instr + arg));
        }
        return cod;
    }

    /**
     * M�todo que apila una instrucci�n dada
     *
     * @param instr : instrucci�n que se quiere emitir
     * @param cod   : C�digo que llevamos hasta el momento
     * @return el nuevo c�digo
     */
    public static List<Instruccion> emite(String instr, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        l.add(new Instruccion(instr));
        return concatena(cod, l);
    }

    /**
     * M�todo encargado de realizar la operaciones necesarias para ejecutar un nuevo bloque
     *
     * @param nivel
     * @param tam
     * @param cod
     * @return
     */
    public static List<Instruccion> prologo(int nivel, int tam, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        // Salva pila
        l.add(new Instruccion("salvaPila")); //a�ade un s�mbolo en la cima que indica d�nde empieza el uso de la pila
        // Mem[ Mem[0]+2 ] <- Mem[n+1]
        l.add(new Instruccion("apilaDir 0"));  // apilamos la �ltima posici�n de uso de memoria pila para los displays
        l.add(new Instruccion("apilaEntero 2"));  //apilamos desplazamiento en la pila: ant_disp
        l.add(new Instruccion("Suma"));  // desplazamos
        l.add(new Instruccion("apilaDir " + (nivel + 1))); // ahora apilamos la direcci�n del display antiguo
        l.add(new Instruccion("desapila_ind")); // guardamos en ant_disp del nuevo display la direcci�n del antiguo display
        // Mem[n+1] <- Mem[0] + 3
        l.add(new Instruccion("apilaDir 0")); // apilamos el l�mite de la pila
        l.add(new Instruccion("apilaEntero 3")); // dir_ret, ant_disp + variables
        l.add(new Instruccion("Suma")); // nos movemos por la pila
        l.add(new Instruccion("desapilaDir " + (nivel + 1))); //guardamos la nueva direcci�n del display nivel n.
        // Mem[0] <- Mem[0] + tam + 2
        l.add(new Instruccion("apilaDir 0")); //valor del l�mite de la pila
        l.add(new Instruccion("apilaEntero " + (tam + 2))); //tama�o variables + dir_ret + ant_disp
        l.add(new Instruccion("Suma"));
        l.add(new Instruccion("desapilaDir 0")); // Guardamos el nuevo l�mite de la pila
        // apilamos el estado de la pila.
		/*l.add(new Instruccion("apilaDir 0")); // apilamos el l�mite de la pila
		l.add(new Instruccion("apilaEntero 3")); // dir_ret, ant_disp + variables
		l.add(new Instruccion("Suma")); // nos movemos por la pila
		l.add(new Instruccion("desapilaDir "+(nivel))); *///guardamos la nueva direcci�n del display nivel n.
        return concatena(cod, l);
    }


    /**
     * M�todo que realiza las operaciones necesarias para volver de la ejecuci�n de un bloque
     *
     * @param nivel
     * @param tam
     * @param cod
     * @return
     */
    public static List<Instruccion> epilogo(int nivel, int tam, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        //Borramos todos los valores de m�s que hay en la pila
        l.add(new Instruccion("restauraPila"));    //Restaura al valor anterior de la llamada a funci�n sin quitar el valor retorno.
        // Mem[0] <- Mem[0] - (tam+2)
        l.add(new Instruccion("apilaDir 0"));  // apilamos la �ltima posici�n de uso de memoria pila
        l.add(new Instruccion("apilaEntero " + (tam + 2)));  //apilamos el tama�o entero del display (tam variables + ant_disp + dir_ret
        l.add(new Instruccion("Resta"));  // restamos el puntero
        l.add(new Instruccion("desapilaDir 0")); // ahora guardamos el nuevo l�mite de la memoria de pila (final del anterior display apilado)
        // Mem[n+1] <- Mem[ Mem[0]+2 ]
        l.add(new Instruccion("apilaDir 0")); // apilamos el nuevo l�mite de la pila
        l.add(new Instruccion("apilaEntero 2")); // dir_ret, ant_disp (queremos acceder a la celda ant_disp
        l.add(new Instruccion("Suma")); // nos movemos por la pila
        l.add(new Instruccion("apila_ind")); //cargamos el valor de la direcci�n apilada, cargamos ant_disp
        l.add(new Instruccion("desapilaDir " + (nivel + 1))); //cargamos el valor de la direcci�n apilada, cargamos ant_disp.
        // ir a Mem[ Mem[0]+1 ]
        l.add(new Instruccion("apilaDir 0")); //valor del nuevo l�mite de la pila
        l.add(new Instruccion("apilaEntero 1")); //queremos la celda dir_ret (retorno de la func)
        l.add(new Instruccion("Suma"));
        l.add(new Instruccion("apila_ind")); //Obtenemos la direcci�n almacenada en dir_ret
        //ESTO LO TENGO EN LOS APUNTES AS� AUNQUE TENGO MIS DUDAS!!
        l.add(new Instruccion("ir_ind")); //Cambiamos el CP (contador de programa) de la m�quina-P a la instrucci�n despu�s de la llamada.
        //ir_ind coge la cima de la pila y lo pone en el CP. Por eso no hace falta una Postllamada
        return concatena(cod, l);
    }

    /**
     * Metodo que guarda en el nuevo display la direcci�n de retorno proporcionada
     *
     * @param dirRet
     * @param cod
     * @return
     */
    public static List<Instruccion> apilaRet(int dirRet, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        // Mem[0]+1 <- dirRet
        l.add(new Instruccion("apilaDir 0")); //Apilamos la �ltima posici�n de la pila
        l.add(new Instruccion("apilaEntero 1")); //Sumamos el deplazamiento para acceder a dir_ret
        l.add(new Instruccion("Suma")); //Nos desplazamos en la pila
        if (dirRet == -1) //en caso de que todav�a no disponemos de la direcci�n real y m�s adelante vaya a ser parcheada
            l.add(new Instruccion("apilaEntero ")); //Apilamos la direcci�n de retorno proporcionada
        else
            l.add(new Instruccion("apilaEntero " + dirRet)); //Apilamos la direcci�n de retorno proporcionada
        l.add(new Instruccion("desapila_ind")); // Guardamos la direcci�n en el nuevo display en dir_ret
        return concatena(cod, l);
    }

    /**
     * Metodo que genera c�digo de iniciar el paso de par�metros en una llamada a una funci�n
     *
     * @param cod
     * @return
     */
    public static List<Instruccion> iniciaPaso(List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        // Pila <- Mem[0]+3
        l.add(new Instruccion("apilaDir 0")); //Apilamos la �ltima posici�n de la pila
        l.add(new Instruccion("apilaEntero 3")); //Sumamos el deplazamiento para acceder a la primera posici�n de variables
        l.add(new Instruccion("Suma")); //Dejamos la direcci�n lista
        return concatena(cod, l);
    }

    /**
     * M�todo que genera c�digo para terminar el paso de par�metros en una llamada a una funci�n
     *
     * @param cod
     * @return
     */
    public static List<Instruccion> finPaso(List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        // Pila <- Mem[0]+3
        l.add(new Instruccion("apilaDir 0")); //Apilamos la �ltima posici�n de la pila
        return concatena(cod, l);
    }

    /**
     * M�todo que genera c�digo para acceder a la direcci�n de una variable
     *
     * @param paramFormal
     * @param cod
     * @return
     */
    public static List<Instruccion> direccionParFormal(Param paramFormal, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        l.add(new Instruccion("apilaEntero " + (paramFormal.dir)));
        l.add(new Instruccion("Suma"));
        return concatena(cod, l);
    }

    //De este m�todo no estoy muy seguro, le falta una revisi�n que creo que la he liado

    /**
     * M�todo que genera c�digo para hacer el paso de un par�metro dado y el modo en que se quiere hacer.
     *
     * @param modo
     * @param paramFormal
     * @param cod
     * @return
     */
    public static List<Instruccion> pasoParametro(Traductor.Modo modo, Param paramFormal, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        if (modo == Traductor.Modo.VAR && paramFormal.modo == Traductor.Modo.VAL)
            l.add(new Instruccion("mueve " + paramFormal.tipo.tam));
        else {
            if (modo == Traductor.Modo.VAL)
                l.add(new Instruccion("flip"));
            l.add(new Instruccion("desapila_ind"));
        }
        return concatena(cod, l);
    }

    /**
     * M�todo que genera c�digo para poder acceder a una variable.
     *
     * @param propVar
     * @param cod
     * @return
     */
    public static List<Instruccion> accesoVar(Propiedades propVar, List<Instruccion> cod) {
        ArrayList<Instruccion> l = new ArrayList<Instruccion>();
        if (propVar != null) {
            l.add(new Instruccion("apilaDir " + (propVar.nivel + 1)));  //indireccionamiento por display
            l.add(new Instruccion("apilaEntero " + propVar.dir));        //dir de la var dentro del display
            l.add(new Instruccion("Suma"));
            if (propVar.clase == Clase.pvar)
                l.add(new Instruccion("apila_ind"));        //si es por referencia, nosotros tenemos su dir
            //Al final lo que nos queda es la direcci�n de la variable que se va a acceder en la cima de la pila.
        }
        return concatena(cod, l);
    }

    //Dado que el acceso a variable tiene longitud variable respecto a su clase, necesitamos esta funci�n
    public static int longAccesoVar(Propiedades propVar) {
        if (propVar != null && propVar.clase == Clase.pvar)
            return 4;
        else
            return 3;
    }
/*******************************************FALTAN IMPLEMENTAR****************************************************************************************************/

    /**
     * Comprueba si nombre existe en camposh.
     *
     * @param camposh lsita de campos del registro procesador hasta el momento
     * @param nombre  id del nuevo campo que se desea a�adir a la lista.
     * @return boolean indicando si nombre existe en camposh
     */
    public static boolean existeCampo(List<Campo> camposh, String nombre) {
        Iterator<Campo> it = camposh.iterator();
        boolean enc = false;
        Campo c;
        while (it.hasNext() && !enc) {
            c = (Campo) it.next();
            enc = c.nombre.equals(nombre);
        }
        return enc;
    }

    /**
     * Comprueba si tipo es b�sico esto es integer o real o una referencia a estos.
     *
     * @param ts   tabla de s�mbolos del contexto actual
     * @param tipo tipo sobre el que se realiza la comprobaci�n
     * @return boolean que indica si tipo es b�sico en ts
     */
    public static boolean tipoBasico(TablaDeSimbolos ts, Tipo tipo, int linea, int columna) {
        String tipoAux;
        tipoAux = ref_(tipo, ts, linea, columna).t;
        return (tipoAux.equals("integer") || tipoAux.equals("real") || tipoAux.equals("punt"));
    }

    /**
     * Comprueba si lex es un campo de tipoh declarado en ts
     *
     * @param tipoh tipo de clase reg sobre el que se comprueba el campo
     * @param ts    tabla de s�mbolos del contexto actual
     * @param campo nombre del campo cuya existencia se va a comprobar
     * @return devuelve el resultado de la comprobaci�n
     */
    public static boolean campoDe(Tipo tipoh, TablaDeSimbolos ts, String campo, int linea, int columna) {
        if (tipoh.t.equals("err"))
            return false;
        Tipo tAux = FuncionesSemanticas.ref_(tipoh, ts, linea, columna);
        if (tAux instanceof TipoReg) {
            TipoReg treg = (TipoReg) tAux;
            boolean enc = false;
            int i = 0;
            while (!enc && i < treg.campos.size()) {
                enc = campo.equals(treg.campos.get(i).nombre);
                i++;
            }
            return enc;
        } else
            return false;
    }

    /**
     * Devuelve si existe el tipo de campo, que no es sino un campo de tipoh de clase reg
     *
     * @param tipoh tpo de clase reg sobre el que se busca el tipo en ts
     * @param ts    tabla de s�mbolos del contexto actual
     * @param campo nombre del campo cuyo tipo se busca
     * @return devuelve el tipo encontrado sino existe el tipoh o campo, "err"
     */
    public static Tipo tipoDeCampo(Tipo tipoh, TablaDeSimbolos ts, String campo, int linea, int columna) {
        Tipo tAux = FuncionesSemanticas.ref_(tipoh, ts, linea, columna);
        if (!(tAux instanceof TipoReg)) {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("alloc", tAux, linea, columna);
            return new Tipo("err", -1);
        } else {
            TipoReg treg = (TipoReg) tAux;
            boolean enc = false;
            int i = 0;
            while (!enc && i < treg.campos.size()) {
                enc = campo.equals(treg.campos.get(i).nombre);
                i++;
            }
            if (enc)
                return treg.campos.get(i - 1).tipo;
            else {
                Aplicacion.inhibeGenCod();
                ManejadorDeErrores.CampoInexistente("alloc", campo, linea, columna);
                return new Tipo("err", -1);
            }

        }
    }

    /**
     * Genera el c�digo necesario para reservar n celdas de memoria seg�n tipo
     *
     * @param tipo refleja el n�mero de celdas de memoria que vamos a reservar.
     * @param ts   tabla de s�mbolos del contexto actual.
     * @param cod  lista de instrucciones generadas hasta el momento.
     * @return
     */
    public static List<Instruccion> genCodReserva(Tipo tipo, TablaDeSimbolos ts, List<Instruccion> cod, int linea, int col) {
        if (tipo.t.equals("ref")) // si es una referencia, obtenemos el tipo al que se refiere
            tipo = FuncionesSemanticas.ref_(tipo, ts, linea, col);

        if (!(tipo instanceof TipoPunt)) { // si no es un puntero, abortamos la generaci�n de c�digo
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("alloc", tipo, linea, col);
            return cod;
        } else {// si es un puntero realizamos la reserva
            if (((TipoPunt) tipo).tBase.t.equals("ref")) // si es una referencia, obtenemos el tipo al que se refiere
                tipo = FuncionesSemanticas.ref_(((TipoPunt) tipo).tBase, ts, linea, col);
            else // si no es una referencia, el tipo buscado es el tipo base del puntero
                tipo = ((TipoPunt) tipo).tBase;

            ArrayList<Instruccion> l = new ArrayList<Instruccion>();
            l.add(new Instruccion("alloc " + tipo.tam));
            l.add(new Instruccion("desapila_ind"));
            return concatena(cod, l);
        }
    }

    /**
     * Genera el c�digo necesario para liberar n celdas de memoria seg�n tipo
     *
     * @param tipo refleja el n�mero de celdas de memoria que vamos a reservar.
     * @param ts   tabla de s�mbolos del contexto actual.
     * @param cod  lista de instrucciones generadas hasta el momento.
     * @return
     */
    public static List<Instruccion> genCodLibera(Tipo tipo, TablaDeSimbolos ts, List<Instruccion> cod, int linea, int col) {
        //TipoRef tref = (TipoRef)ref_(tipo,ts);
        if (tipo.t.equals("ref")) // si es una referencia, obtenemos el tipo al que se refiere
            tipo = FuncionesSemanticas.ref_(tipo, ts, linea, col);

        if (!(tipo instanceof TipoPunt)) { // si no es un puntero, abortamos la generaci�n de c�digo
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tipoIncompatible("free", tipo, linea, col); // m... de donde las saco, deber�an ser globales?
            return cod;
        } else {// si es un puntero realizamos la liberaci�n
            if (((TipoPunt) tipo).tBase.t.equals("ref")) // si es una referencia, obtenemos el tipo al que se refiere
                tipo = FuncionesSemanticas.ref_(((TipoPunt) tipo).tBase, ts, linea, col);
            else // si no es una referencia, el tipo buscado es el tipo base del puntero
                tipo = ((TipoPunt) tipo).tBase;

            ArrayList<Instruccion> l = new ArrayList<Instruccion>();
            // nos interesa apilar la direcci�n a la que apunta el puntero, para hacer el free sobre esa direcci�n
            l.add(new Instruccion("apila_ind"));
            l.add(new Instruccion("free " + tipo.tam));
            return concatena(cod, l);
        }
    }

    /**
     * Genera el conjunto de instrucciones necesarias para acceder sobre el mapeado de memoria al designador tipoDesig
     *
     * @param tipoDesig determina el tipo de c�digo que genera.(indirecci�n, array, registro, referencia...)
     * @param cod       lista de instrucciones traducidas hasta el momento
     * @param tipoh
     * @param ts        tabla de s�mbolos en el contexto actual.
     * @param campo     en acso de que sea tipoDesig= registro genera
     * @return
     */
    public static List<Instruccion> genCodDesig(String tipoDesig, List<Instruccion> cod, Tipo tipoh, TablaDeSimbolos ts,
                                                String campo, int linea, int col) {
        ArrayList<Instruccion> codAux = new ArrayList<Instruccion>();
        if (tipoh != null && !tipoh.t.equals("err")) {
            if (tipoDesig.equals("Registro")) {
                TipoReg treg = (TipoReg) ref_(tipoh, ts, linea, col);
                Iterator<Campo> it = treg.campos.iterator();
                boolean enc = false;
                Campo c = null;
                while (it.hasNext() && !enc) {
                    c = (Campo) it.next();
                    enc = c.nombre.equals(campo);
                }
                codAux.add(new Instruccion("apilaEntero " + c.despl));
                codAux.add(new Instruccion("Suma"));
                return concatena(cod, codAux);
            } else if (tipoDesig.equals("Indireccion")) {
                codAux.add(new Instruccion("apila_ind"));
                return concatena(cod, codAux);
            }
            if (tipoDesig.equals("Array")) {
                codAux.add(new Instruccion("apilaEntero " + ((TipoArray) ref_(tipoh, ts, 1, 1)).tBase.tam));
                codAux.add(new Instruccion("Mult"));
                codAux.add(new Instruccion("Suma"));
                return concatena(cod, codAux);
            }
        }
        return cod;
    }

    /**
     * Realiza una comprobaci�n estructural de compatibilidad de tipos
     *
     * @param t1 tipo de la expresi�n 1.
     * @param t2 tipo de la expresi�n 2.
     * @param ts tabla de s�mbolos en el contexto actual.
     */
    public static boolean tipoCompatible(Tipo t1, Tipo t2, TablaDeSimbolos ts) {
        if (t1.t.equals("err") || t2.t.equals("err"))
            return false;
        HashSet<Tipo> visitados = new HashSet<Tipo>();
        return tipoCompatible2(t1, t2, ts, visitados);
    }

    public static boolean tipoCompatible2(Tipo t1, Tipo t2, TablaDeSimbolos ts, HashSet<Tipo> visitados) {

        if (visitados.contains(t1) && visitados.contains(t2)) return true;
        else {
            visitados.add(t1);
            visitados.add(t2);
        }
        ;
        if ((t1.t.equals("integer") && t2.t.equals("integer"))
                || (t1.t.equals("real") && t2.t.equals("real"))) {
            return true;
        } else if (t1.t.equals("ref")) {
            TipoRef tr1 = (TipoRef) t1;
            return tipoCompatible2(ts.getPropiedades(tr1.nombre).tipo, t2, ts, visitados);
        } else if (t2.t.equals("ref")) {
            TipoRef tr2 = (TipoRef) t2;
            return tipoCompatible2(t1, ts.getPropiedades(tr2.nombre).tipo, ts, visitados);
        } else if (t1.t.equals("array") && t2.t.equals("array")) {
            TipoArray ta1 = (TipoArray) t1;
            TipoArray ta2 = (TipoArray) t2;
            if (ta1.nElem == ta2.nElem)
                return tipoCompatible2(ta1.tBase, ta2.tBase, ts, visitados);
        } else {

            if (t1.t.equals("reg") && t2.t.equals("reg")) {
                TipoReg trg1 = (TipoReg) t1;
                TipoReg trg2 = (TipoReg) t2;
                if (trg1.campos.size() == trg2.campos.size()) {
                    for (int i = 0; i < trg1.campos.size(); i++) {
                        if (!tipoCompatible2(trg1.campos.get(i).tipo, trg2.campos.get(i).tipo, ts, visitados))
                            return false;
                    }
                    return true;
                }
            } else if (t1.t.equals("punt") && (t2.t.equals("punt") || t2.t.equals("nulo"))) {
                if (t2.t.equals("nulo"))
                    return true;

                TipoPunt tp1 = (TipoPunt) t1;
                TipoPunt tp2 = (TipoPunt) t2;
                return tipoCompatible2(tp1.tBase, tp2.tBase, ts, visitados);
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Funci�n que busca sin�nimos hasta encontrar un tipo que sea diferente de t:ref
     *
     * @param tipo  puede tomar integer, real o cualquier nombre de tipo (es este caso ser�a una refererencia).
     * @param ts    tabla de s�mbolos en el contexto actual.
     * @param linea gestio�n de errores.
     * @param col   gesti�n de errores.
     * @return devuelve la entrada de la tabla tipo asociada a tipo.
     */
    public static Tipo ref_(Tipo tipo, TablaDeSimbolos ts, int linea, int col) {
        if (tipo.t.equals("ref"))
            if (existeID(ts, ((TipoRef) tipo).nombre, linea, col))
                return ref_(damePropiedades(ts, ((TipoRef) tipo).nombre, linea, col).tipo, ts, linea, col);
            else
                return new Tipo("err", -1);
        else return tipo;
    }

    public static void parcheaDirInicioFunc(TablaDeSimbolos ts, String id, int dirInicio, int linea, int col) {
        if (ts.existeID(id))
            damePropiedades(ts, id, linea, col).setInicio(dirInicio);
    }

    /**
     * Funci�n que devuelve el n�mero de instrucciones que hay en caso de casting o de que no haya casting
     *
     * @param tipo1 tipo del operando 1
     * @param tipo2 tipo del operando 2
     * @return devuelve el n�mero de instrucciones
     */
    public static int longCastOp(Tipo tipo1, Tipo tipo2) {
        if (tipo1.t.equals("integer") && tipo2.t.equals("real") || tipo1.t.equals("real") && tipo2.t.equals("integer"))
            return 1;
        else
            return 0;
    }

    public static int longCodSal(boolean desig) {
        if (desig)
            return 2;
        else
            return 1;
    }

    public static int LONGPASOPARAMETRO(Modo modo, Param param) {
        if (modo == Traductor.Modo.VAR && param.modo == Traductor.Modo.VAL)
            return 1;
        else {
            if (modo == Traductor.Modo.VAL)
                return 2;
            return 1;
        }
    }
}

