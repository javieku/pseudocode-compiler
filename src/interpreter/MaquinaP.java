package interpreter;

/**
 * Clase principal del Interprete. Se encarga de leer y ejecutar las instrucciones.
 *
 * @author Vlad
 * @updates javieku
 */

import compiler.Instruccion;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;


public class MaquinaP {
    private static final int TAM_MEMORIA = 300; // displays + cp + segmento est�tico de memoria

    private List<Instruccion> programa;
    private Vector<Celda> datos;
    private Stack<Celda> pila;
    private AlmacenReciclables almacenReciclables;    //sirve para reutilizar objetos.
    private int pc;
    private String path;    //direccion del archivo de instrucciones
    private boolean trazaModo;
    private boolean error;
    private String errorMensaje;
    private boolean finEjec;
    //private int nMem;	// numero de elementos guardados en la memoria estatica
    private int nHeap;    // numero de elementos guardados en la memoria dinamica

    public MaquinaP() {
        programa = new ArrayList<Instruccion>();
        datos = new Vector<Celda>(TAM_MEMORIA);
        pila = new Stack<Celda>();
        almacenReciclables = new AlmacenReciclables();
        pc = 0;
        path = "";
        trazaModo = false;
        error = false;
        //nMem = 0;
        nHeap = 0;
    }

    public MaquinaP(String p, boolean tM) {
        programa = new ArrayList<Instruccion>();
        datos = new Vector<Celda>();
        pila = new Stack<Celda>();
        almacenReciclables = new AlmacenReciclables();
        pc = 0;
        path = p;
        trazaModo = tM;
        error = false;
        nHeap = 0;
    }

    /**
     * M�todo que se encarga de cargar y ejecutar las instrucciones.
     */
    public void ejecuta_programa(String file) {
        finEjec = false;
        String instruccion = "";
        Scanner scan = new Scanner(System.in);
        try {
            FileInputStream fileIn = new FileInputStream("b");
            ObjectInputStream entrada = new ObjectInputStream(fileIn);
            programa = (ArrayList<Instruccion>) entrada.readObject();
            entrada.close();
			/*BufferedReader bf = new BufferedReader(new FileReader(path));
			String sCadena;
			programa = new ArrayList<Instruccion>();
			while ((sCadena = bf.readLine())!=null) {
				programa.add(new Instruccion(sCadena));
				}*/
        } catch (Exception e) {
            System.out.println("Error El fichero est� corrupto o no es valido");
            error = true;
        }
        if (pc <= programa.size()) {
            instruccion = programa.get(pc).toString();
            pc++;
        } else
            finEjec = true;
        while (!finEjec && !error) {
            if (trazaModo) {    //en modo traza nos paramos para ver en que estado est� la maquina
                printEstadoMaquina();
                System.out.println("Maquina-P/>Seguir? [Y/N]");
                String cad = scan.nextLine();
                if (cad.equalsIgnoreCase("n")) {
                    finEjec = true;
                } else if (cad.equalsIgnoreCase("no more")) {
                    trazaModo = false;
                }
            }
            //dado que tenemos instrucciones con dos cadenas, usamos StringTokenizer.
            StringTokenizer instrTokens = new StringTokenizer(instruccion, " ");
            if (instrTokens.countTokens() > 2) {
                error = true;
                errorMensaje = "ERROR Instrucci�n no v�lida: " + instruccion + ".";
            } else {
                if (trazaModo) {
                    System.out.println(" ----------------------------------------------");
                    System.out.println("| Instrucci�n: " + instruccion + ".");
                    System.out.println(" ----------------------------------------------");
                }
                // Reconocemos la instrucci�n y por consiguiente ejecutamos un m�todo
                if (instrTokens.countTokens() == 2) {
                    instruccion = instrTokens.nextToken();
                    if (instruccion.equalsIgnoreCase("apilaDir")) {
                        apilaDir(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("desapilaDir")) {
                        desapilaDir(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("apilaEntero")) {
                        apilaEntero(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("apilaReal")) {
                        apilaReal(Float.parseFloat(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("mueve")) {
                        mueve(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("alloc")) {
                        alloc(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("free")) {
                        free(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("ir_a")) {
                        ir_a(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("ir_f")) {
                        ir_f(Integer.parseInt(instrTokens.nextToken()));
                    } else if (instruccion.equalsIgnoreCase("ir_v")) {
                        ir_v(Integer.parseInt(instrTokens.nextToken()));
                    } else {
                        error = true;
                        errorMensaje = "ERROR Instrucci�n no v�lida: " + instruccion + " " + Float.parseFloat(instrTokens.nextToken()) + ".";
                    }
                } else {
                    if (instruccion.equalsIgnoreCase("Menor")) {
                        menor();
                    } else if (instruccion.equalsIgnoreCase("Mayor")) {
                        mayor();
                    } else if (instruccion.equalsIgnoreCase("MenorIg")) {
                        menorIgual();
                    } else if (instruccion.equalsIgnoreCase("MayorIg")) {
                        mayorIgual();
                    } else if (instruccion.equalsIgnoreCase("Igual")) {
                        igual();
                    } else if (instruccion.equalsIgnoreCase("NoIgual")) {
                        distinto();
                    } else if (instruccion.equalsIgnoreCase("Suma")) {
                        sumar();
                    } else if (instruccion.equalsIgnoreCase("Resta")) {
                        restar();
                    } else if (instruccion.equalsIgnoreCase("Mult")) {
                        multiplicar();
                    } else if (instruccion.equalsIgnoreCase("Div")) {
                        dividir();
                    } else if (instruccion.equalsIgnoreCase("Mod")) {
                        modulo();
                    } else if (instruccion.equalsIgnoreCase("Or")) {
                        oLogica();
                    } else if (instruccion.equalsIgnoreCase("And")) {
                        yLogica();
                    } else if (instruccion.equalsIgnoreCase("Neg")) {
                        negLogica();
                    } else if (instruccion.equalsIgnoreCase("Mayor")) {
                        mayor();
                    } else if (instruccion.equalsIgnoreCase("CambSig")) {
                        cambioSigno();
                    } else if (instruccion.equalsIgnoreCase("CasI")) {
                        castInteger();
                    } else if (instruccion.equalsIgnoreCase("CasR")) {
                        castReal();
                    } else if (instruccion.equalsIgnoreCase("EntradaEntero")) {
                        in("Integer");
                    } else if (instruccion.equalsIgnoreCase("EntradaReal")) {
                        in("Real");
                    } else if (instruccion.equalsIgnoreCase("Salida")) {
                        out();
                    } else if (instruccion.equalsIgnoreCase("desapila_ind")) {
                        desapila_ind();
                    } else if (instruccion.equalsIgnoreCase("apila_ind")) {
                        apila_ind();
                    } else if (instruccion.equalsIgnoreCase("ir_ind")) {
                        ir_ind();
                    } else if (instruccion.equalsIgnoreCase("copia")) {
                        copia();
                    } else if (instruccion.equalsIgnoreCase("desapila")) {
                        desapila();
                    } else if (instruccion.equalsIgnoreCase("apilaNulo")) {
                        apilaNulo();
                    } else if (instruccion.equalsIgnoreCase("stop")) {
                        stop();
                    } else if (instruccion.equalsIgnoreCase("salvaPila")) {
                        apilaMarca();
                    } else if (instruccion.equalsIgnoreCase("restauraPila")) {
                        desapilaHastaMarca();
                    } else if (instruccion.equalsIgnoreCase("flip")) {
                        flip();
                    } else {
                        error = true;
                        errorMensaje = "ERROR Instrucci�n no v�lida: " + instruccion + ".";
                    }
                }
            }
            if (!error) {
                if (pc < programa.size()) {
                    instruccion = programa.get(pc).toString();
                    pc++;
                } else
                    finEjec = true;
            } else {
                System.out.println(errorMensaje);
            }

        }//fin del while
        // Printamos el estado con el que ha terminado la m�quina si hay error o en modo traza
        if (trazaModo || error)
            printEstadoMaquina();
        System.out.println();
        System.out.println("Maquina-P/>Ha terminado la ejecuci�n.\nPulse enter para salir...");
        scan.nextLine();
    }

    // Apilamos un null
    private void apilaNulo() {
        Celda c = new CeldaInt(-1);
        pila.push(c);
    }

    // Intercambia la cima por la subcima de la pila
    private void flip() {
        if (pila.size() < 2) {
            error = true;
            errorMensaje = "ERROR No hay suficientes celdas para hace flip";
        }
        Celda toSubcima = (Celda) pila.pop();
        Celda toCima = (Celda) pila.pop();
        pila.push(toSubcima);
        pila.push(toCima);
    }

    //Funci�n que desapila celdas hasta encontrar una marca (Casilla Default)
    private void desapilaHastaMarca() {
        if (pila.size() < 1) {
            error = true;
            errorMensaje = "ERROR No se ha podido ejecutar desapilaHastaMarca: La Marca de salvaPila se ha perdido.";
        }
        Celda cRet = (Celda) pila.pop(); //Celda del valor del return o la marca
        if (cRet.getTipo().equals("Marca"))
            return;
        Celda trash = (Celda) pila.pop();
        //Ciclamos hasta que desapilamos la Marca
        while (!trash.getTipo().equals("Marca") && pila.size() > 0) {
            trash = (Celda) pila.pop();
            if (trash.getTipo().equals("Integer"))
                almacenReciclables.putCeldaInt((CeldaInt) trash);
            else if (trash.getTipo().equals("Real"))
                almacenReciclables.putCeldaReal((CeldaReal) trash);
        }
        if (!trash.getTipo().equals("Marca")) {
            error = true;
            errorMensaje = "ERROR No se ha podido ejecutar desapilaHastaMarca: La Marca de salvaPila se ha perdido.";
        }
        //Apilamos la celda de retorno de la funci�n
        pila.push(cRet);
    }

    //Introducimos una marca para que se sepa d�nde empieza la pila de la funci�n
    private void apilaMarca() {
        Celda marca = new CeldaMarca();
        pila.push(marca);
    }


    /**
     * Se encarga de llevar de la memoria una celda a la cima de la pila. Dir es la direccion de memoria
     * @param dir
     */
    private void apilaDir(int dir) {
        if (dir < 0 || dir >= datos.size()) {
            error = true;
            errorMensaje = "ERROR Direcci�n fuera de �mbito de la memoria: dir " + Integer.toString(dir) + ".";
        } else {
            Celda c = (Celda) datos.get(dir).clone();
            if (c.getTipo().equals("Default")) {
                error = true;
                errorMensaje = "ERROR Acceso a posici�n de memoria no v�lida: dir " + Integer.toString(dir) + ".";
            } else {
                Celda aux;
                if (c.getTipo().equals("Integer")) {
                    aux = new CeldaInt((CeldaInt) c);
                } else {
                    aux = new CeldaReal((CeldaReal) c);
                }
                pila.push(aux);
            }
        }
    }

    /**
     * Lo contrario de apilaDir, coge una celda de la pila y la lleva a memoria
     * @param dir
     */
    private void desapilaDir(int dir) {
        if (dir < 0 || dir >= datos.size()) {
            error = true;
            errorMensaje = "ERROR Direcci�n fuera de �mbito de la memoria: dir " + Integer.toString(dir) + ".";
        } else if (pila.isEmpty()) {
            error = true;
            errorMensaje = "ERROR Pila vac�a: desapilaDir " + Integer.toString(dir) + ".";
        } else {
            Celda c = pila.pop();
            Celda aux = null;
            if (c.getTipo().equals("Integer")) {
                aux = new CeldaInt((CeldaInt) c);
            } else if (c.getTipo().equals("Real")) {
                aux = new CeldaReal((CeldaReal) c);
            } else if (c.getTipo().equals("Default")) {
                aux = (CeldaDefault) c;
            }
            datos.set(dir, aux);
        }
    }

    /**
     * Apila en la cima de la pila un valor entero de valor el parametro valor.
     * @param valor
     */
    private void apilaEntero(int valor) {
        CeldaInt resul;
        if (almacenReciclables.sizeInt() > 0)
            resul = almacenReciclables.getCeldaInt();
        else
            resul = new CeldaInt();
        resul.setValor(valor);
        pila.push(resul);
    }

    /**
     * Apila un Real en la cima de la pila
     * @param valor
     */
    private void apilaReal(float valor) {
        CeldaReal resul;
        if (almacenReciclables.sizeReal() > 0)
            resul = almacenReciclables.getCeldaReal();
        else
            resul = new CeldaReal();
        resul.setValor(valor);
        pila.push(resul);
    }

    /**
     * Funcion menor sobre las dos celdas de la cima. Tras realizar la funci�n menor, apila el resultado en la cima de la pila
     * y manda al almac�n de reciclaje las dos celdas que desapil�
     */
    private void menor() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a < b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a < b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: menor " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila, o est�n corruptos: op menor.";
        }
    }

    private void mayor() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a > b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a > b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: mayor " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op mayor.";
        }
    }

    private void menorIgual() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a <= b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a <= b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: menorIgual " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op menorIgual.";
        }
    }

    private void mayorIgual() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a >= b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a >= b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: mayorIgual " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op mayorIgual.";
        }
    }

    private void igual() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a == b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a == b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: igual " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op igual.";
        }
    }

    private void distinto() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                int a = c1.getValor();
                int b = c2.getValor();
                if (a != b)
                    a = 1;
                else
                    a = 0;
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                float a = c1.getValor();
                float b = c2.getValor();
                int r;
                if (a != b)
                    r = 1;
                else
                    r = 0;
                resul.setValor(r);
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: distinto " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op distinto.";
        }
    }

    private void sumar() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(c1.getValor() + c2.getValor());
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaReal resul;
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(c1.getValor() + c2.getValor());
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: sumar " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op sumar.";
        }
    }

    private void restar() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(c1.getValor() - c2.getValor());
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaReal resul;
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(c1.getValor() - c2.getValor());
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: restar " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op restar.";
        }
    }

    private void multiplicar() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(c1.getValor() * c2.getValor());
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaReal resul;
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(c1.getValor() * c2.getValor());
                almacenReciclables.putCeldaReal(c1);
                almacenReciclables.putCeldaReal(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: multiplicar " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op multiplicar.";
        }
    }

    private void dividir() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                int a = c1.getValor();
                int b = c2.getValor();
                if (b != 0) {
                    if (almacenReciclables.sizeInt() > 0)
                        resul = almacenReciclables.getCeldaInt();
                    else
                        resul = new CeldaInt();
                    resul.setValor(a / b);
                    almacenReciclables.putCeldaInt(c1);
                    almacenReciclables.putCeldaInt(c2);
                    pila.push(resul);
                } else {
                    error = true;
                    errorMensaje = "ERROR Divisi�n por 0: dividir " + c1.getValor() + ", " + c2.getValor() + ".";
                    almacenReciclables.putCeldaInt(c1);
                    almacenReciclables.putCeldaInt(c2);
                }
            } else if (aux1.getTipo().equalsIgnoreCase("Real") && aux2.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal c2 = (CeldaReal) aux2;
                CeldaReal resul;
                float a = c1.getValor();
                float b = c2.getValor();
                if (b != 0) {
                    if (almacenReciclables.sizeReal() > 0)
                        resul = almacenReciclables.getCeldaReal();
                    else
                        resul = new CeldaReal();
                    resul.setValor(a / b);
                    almacenReciclables.putCeldaReal(c1);
                    almacenReciclables.putCeldaReal(c2);
                    pila.push(resul);
                } else {
                    error = true;
                    errorMensaje = "ERROR Divisi�n por 0: dividir " + c1.getValor() + ", " + c2.getValor() + ".";
                    almacenReciclables.putCeldaReal(c1);
                    almacenReciclables.putCeldaReal(c2);
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: dividir " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op dividir.";
        }
    }

    private void modulo() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                int a = c1.getValor();
                int b = c2.getValor();
                if (b > 0) {
                    if (almacenReciclables.sizeInt() > 0)
                        resul = almacenReciclables.getCeldaInt();
                    else
                        resul = new CeldaInt();
                    resul.setValor(a % b);
                    almacenReciclables.putCeldaInt(c1);
                    almacenReciclables.putCeldaInt(c2);
                    pila.push(resul);
                } else {
                    error = true;
                    errorMensaje = "ERROR Segundo operando de % no positivo: modulo " + c1.getValor() + ", " + c2.getValor() + ".";
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: modulo " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op modulo.";
        }
    }

    private void oLogica() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                int a = c1.getValor();
                int b = c2.getValor();
                if (b != 0 || a != 0)
                    a = 1;
                else
                    a = 0;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: oLogica " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op oLogica.";
        }
    }

    private void yLogica() {
        if (pila.size() >= 2) {
            Celda aux2 = pila.pop();
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer") && aux2.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt c2 = (CeldaInt) aux2;
                CeldaInt resul;
                int a = c1.getValor();
                int b = c2.getValor();
                if (b != 0 && a != 0)
                    a = 1;
                else
                    a = 0;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                almacenReciclables.putCeldaInt(c2);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: yLogica " + aux1.getTipo() + ", " + aux2.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op yLogica.";
        }
    }

    private void negLogica() {
        if (pila.size() >= 1) {
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt resul;
                int a = c1.getValor();
                if (a == 0)
                    a = 1;
                else
                    a = 0;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: negLogica " + aux1.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op negLogica.";
        }
    }

    private void cambioSigno() {
        if (pila.size() >= 1) {
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt resul;
                int a = c1.getValor();
                if (a != 0)
                    a = -1 * a;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal resul;
                float a = c1.getValor();
                if (a != 0)
                    a = -1 * a;
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(a);
                almacenReciclables.putCeldaReal(c1);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: cambioSigno " + aux1.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op cambioSigno.";
        }
    }

    private void castInteger() {
        if (pila.size() >= 1) {
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaInt resul;
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(c1.getValor());
                almacenReciclables.putCeldaInt(c1);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaInt resul;
                int a;
                a = (int) c1.getValor();
                if (almacenReciclables.sizeInt() > 0)
                    resul = almacenReciclables.getCeldaInt();
                else
                    resul = new CeldaInt();
                resul.setValor(a);
                almacenReciclables.putCeldaReal(c1);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: castInteger " + aux1.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op castInteger.";
        }
    }

    private void castReal() {
        if (pila.size() >= 1) {
            Celda aux1 = pila.pop();

            if (aux1.getTipo().equalsIgnoreCase("Integer")) {
                CeldaInt c1 = (CeldaInt) aux1;
                CeldaReal resul;
                float a;
                a = (float) c1.getValor();
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(a);
                almacenReciclables.putCeldaInt(c1);
                pila.push(resul);
            } else if (aux1.getTipo().equalsIgnoreCase("Real")) {
                CeldaReal c1 = (CeldaReal) aux1;
                CeldaReal resul;
                if (almacenReciclables.sizeReal() > 0)
                    resul = almacenReciclables.getCeldaReal();
                else
                    resul = new CeldaReal();
                resul.setValor(c1.getValor());
                almacenReciclables.putCeldaReal(c1);
                pila.push(resul);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: castReal " + aux1.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: op castR.";
        }
    }

    /**
     * la funci�n in lee un dato de la entrada estandar. El tipo del dato viene dado por el parametro de entrada.
     * @param tipo
     */
    private void in(String tipo) {
        Scanner scan = new Scanner(System.in);
        String linea;
        boolean datoCorrecto = false;
        Celda aux1;
        if (tipo.equals("Integer"))
            aux1 = new CeldaInt();
        else
            aux1 = new CeldaReal();
        while (!datoCorrecto && !error) {
            if (aux1.getTipo().equalsIgnoreCase("Integer")) {
                System.out.println();
                System.out.print("Maquina-P.in()/> Introduce un Entero/>");
                linea = scan.nextLine();
                if (esEntero(linea)) {
                    CeldaInt c1 = (CeldaInt) aux1;
                    datoCorrecto = true;
                    CeldaInt resul;
                    if (almacenReciclables.sizeInt() > 0)
                        resul = almacenReciclables.getCeldaInt();
                    else
                        resul = new CeldaInt();
                    resul.setStringValor(linea);
                    almacenReciclables.putCeldaInt(c1);
                    //datos.set(dir, resul);
                    pila.push(resul);
                } else {
                    System.out.println("Maquina-P/> Dato introducido incompatible. Introduzca un Entero.");
                }
            } else if (aux1.getTipo().equalsIgnoreCase("Real")) {
                System.out.println();
                System.out.print("Maquina-P.in()/> Introduce un Real/>");
                linea = scan.nextLine();
                if (esReal(linea)) {
                    CeldaReal c1 = (CeldaReal) aux1;
                    datoCorrecto = true;
                    CeldaReal resul;
                    if (almacenReciclables.sizeReal() > 0)
                        resul = almacenReciclables.getCeldaReal();
                    else
                        resul = new CeldaReal();
                    resul.setStringValor(linea);
                    almacenReciclables.putCeldaReal(c1);
                    //datos.set(dir, resul);
                    pila.push(resul);
                } else {
                    System.out.println("Maquina-P/> Dato introducido incompatible. Introduzca un Real.");
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: in " + aux1.getTipo() + ".";
            }
        }
    }

    /**
     * Muestra la cima de la pila por la salida estandar.
     */
    private void out() {
        if (!pila.empty()) {
            Celda c1 = pila.peek();
            System.out.println();
            System.out.println("Maquina-P.out()/>" + c1.getStringValor());
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: out.";
        }
    }

    /**
     * Desapila el valor de la cima v y la subcima d, interpreta d
     * como un n�mero de celda en la memoria, y almacena v en dicha celda.
     * Mem[Pila[ST-1]] <- Pila[ST]
     * ST<-ST-2  (la pila pierde dos elementos)
     * PC <-PC+1
     */
    public void desapila_ind() {
        if (pila.size() < 2) {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: desapila_ind";
        } else {
            Celda cima = pila.pop();
            Celda subcima = pila.pop();
            if (subcima.getTipo().equalsIgnoreCase("Integer")) {
                int dir_mem = Integer.parseInt(subcima.getStringValor());
                // controlamos que no nos salimos de la memoria
                if (dir_mem < 0 || dir_mem >= datos.size()) {
                    error = true;
                    System.out.println("Maquina-P/> Memory access violation! Posicion de memoria inv�lida.");
                    System.out.println("Maquina-P/> Instrucci�n: " + (pc));
                    System.out.println("Maquina-P/> Posici�n referenciada: " + dir_mem);
                    System.out.println("Maquina-P/> Operaci�n que produjo el error: desapila_ind ");
                } else {
                    datos.set(dir_mem, cima);
                }
                almacenReciclables.putCeldaInt((CeldaInt) subcima);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: desapila_ind " + subcima.getTipo() + ".";
            }
        }
    }

    /**
     * Interpreta el valor d en la cima de la pila como un n�mero
     * de celda en la memoria, y sustituye dicho valor por el almacenado en la celda.
     * Pila[ST] <- Mem[Pila[ST]]
     * PC <- PC+1
     */
    public void apila_ind() {
        if (pila.isEmpty()) {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: apila_ind.";
        } else {
            //tomamos el valor de la cima como la direcci�n
            Celda cima = pila.pop();
            Celda valor = null;
            if (cima.getTipo().equalsIgnoreCase("Integer")) {
                int dir_mem = Integer.parseInt(cima.getStringValor());
                // controlamos que no nos salimos de la memoria
                if (dir_mem < 0 || dir_mem >= datos.size()) {
                    error = true;
                    System.out.println("Maquina-P/> Memory access violation! Posicion de memoria inv�lida.");
                    System.out.println("Maquina-P/> Instrucci�n: " + (pc));
                    System.out.println("Maquina-P/> Posici�n referenciada: " + dir_mem);
                    System.out.println("Maquina-P/> Operaci�n que produjo el error: apila_ind ");
                } else {
                    valor = datos.get(dir_mem);
                    valor = (Celda) valor.clone();
                    pila.push(valor);
                    almacenReciclables.putCeldaInt((CeldaInt) cima);
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: apila_ind " + cima.getTipo() + ".";
            }
        }
    }

    /**
     * Instrucci�n que encuentra en la cima la direcci�n origen o y en la
     * subcima la direcci�n destino d, y realiza el movimiento de num_celdas desde o a d.
     * para i<-0 hasta s-1 hacer
     * Mem[Pila[ST-1]+i] <- Mem[Pila[ST]+i]
     * ST<-ST-2
     * PC <- PC+1
     *
     * @param num_celdasn Numero de celdas a desplazar
     */
    public void mueve(int num_celdas) {
        if (pila.size() < 2) {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: mueve.";
        } else {
            Celda cima = pila.pop();
            Celda subcima = pila.pop();
            if (subcima.getTipo().equalsIgnoreCase("Integer") && cima.getTipo().equalsIgnoreCase("Integer")) {
                int origen = Integer.parseInt(cima.getStringValor());
                int destino = Integer.parseInt(subcima.getStringValor());
                if (origen == -1 || destino == -1) {
                    error = true;
                    errorMensaje = "ERROR Direcci�n no v�lida: mueve de " + cima.getStringValor() + " a " + subcima.getStringValor() + ".";
                }
                Celda c;
                for (int i = 0; i < num_celdas; i++) {
                    if (origen + i >= 0 && origen + i < datos.size() &&
                            destino + i >= 0 && destino + i < datos.size()) {
                        c = (Celda) datos.get(origen + i).clone();
                        datos.set(destino + i, c);
                    } else {
                        error = true;
                        errorMensaje = "ERROR Acceso a direcci�n de memoria inv�lida";
                    }
                }
                almacenReciclables.putCeldaInt((CeldaInt) cima);
                almacenReciclables.putCeldaInt((CeldaInt) subcima);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: mueve " + cima.getTipo() + " " + subcima.getTipo() + ".";
            }
        }
    }

    /**
     * Reserva espacio en el heap para t celdas consecutivas y apila
     * en la cima de la pila la direcci�n de comienzo.
     * Direccion de comienzo:
     *
     * 		|base|...|elem1|elem2| ....
     * 					^inicio
     *
     * @param nceldas    numero de celdas
     */
    public void alloc(int nceldas) {
        int dirIni = findPosLong(nceldas);
        if (almacenReciclables.sizeInt() > 0) {
            CeldaInt c = almacenReciclables.getCeldaInt();
            c.setValor(dirIni);
            pila.push(c);
        } else
            pila.push(new CeldaInt(dirIni));
        try {
            if (dirIni < datos.size() - 1)
                for (int i = 0; i < nceldas; i++) {
                    datos.set(dirIni + i, new CeldaInt(-1));
                }
            else
                for (int i = 0; i < nceldas; i++) {
                    datos.add(new CeldaInt(-1));
                }
        } catch (Exception e) {
            error = true;
            errorMensaje = "ERROR Acceso del heap corrupto: alloc.";
        }
    }

    // funci�n que maneja el vector de memoria para hacer el heap
    private int findPosLong(int nceldas) {
        int posLibresContiguas = 0;
        int pos = TAM_MEMORIA;
        while (pos < datos.size() && posLibresContiguas < nceldas) {
            if (datos.get(pos).getTipo().equals("Default")) //Celda no usada/no reservada
                posLibresContiguas++;
            else
                posLibresContiguas = 0;
            pos++;
        }
        if (pos != datos.size() - 1)    //Hemos encontrado un hueco lo suficientemente grande?
            pos = pos - posLibresContiguas;
        return pos;
    }

    /**
     * Desapila una direcci�n de comienzo d de la cima de la pila, y
     * libera en el heap t celdas consecutivas a partir de d.
     *
     * @param nceldas numero de celdas
     */
    public void free(int nceldas) {
        if (pila.isEmpty()) {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: free.";
        } else {
            Celda cima = pila.pop();
            if (cima.getTipo().equalsIgnoreCase("Integer")) {
                int dir = Integer.parseInt(cima.getStringValor());
                if (dir >= TAM_MEMORIA && dir + nceldas < datos.size()) {
                    try {
                        if (dir + nceldas == datos.size() - 1)
                            for (int i = nceldas - 1; i >= 0; i--) {
                                datos.remove(dir + i);
                            }
                        else
                            for (int i = nceldas - 1; i >= 0; i--) {
                                datos.set(dir + i, new CeldaDefault());
                            }
                    } catch (Exception e) {
                        error = true;
                        errorMensaje = "ERROR Acceso del heap corrupto: free.";
                    }
                    almacenReciclables.putCeldaInt((CeldaInt) cima);
                } else {
                    error = true;
                    errorMensaje = "ERROR Acceso a memoria incorrecto fuera de ambito del heap: free.";
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: free " + cima.getTipo() + ".";
            }
        }
    }

    /**
     * Salto incondicional a la instrucci�n i.
     *
     * @param etq el nuevo valor del contador de instrucciones
     */
    public void ir_a(int etq) {
        pc = etq;
    }

    /**
     * Desapila el valor de la cima de la pila. Si es 0
     * salta a la instrucci�n i. En otro caso, sigue la
     * ejecuci�n en secuencia.
     *
     * @param etq nuevo contador de instrucciones
     */
    public void ir_f(int etq) {
        if (etq > 0) {
            Celda cima = pila.pop();
            if (cima.getTipo().equalsIgnoreCase("Integer")) {
                if (((CeldaInt) cima).getValor() == 0) {
                    pc = etq;
                }
                almacenReciclables.putCeldaInt((CeldaInt) cima);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: ir_f " + cima.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR Direcci�n de salto" + etq + "no v�lida ir_f.";
        }
    }

    /**
     * Desapila el valor de la cima de la pila. Si es distinto de 0, salta a la instrucci�n i.
     * En otro caso, sigue la ejecuci�n en secuencia.
     *
     * @param ci        nuevo contador de instrucciones
     */
    public void ir_v(int etq) {
        if (etq > 0) {
            Celda cima = pila.pop();
            if (cima.getTipo().equalsIgnoreCase("Integer")) {
                if (((CeldaInt) cima).getValor() != 0) {
                    pc = etq;
                }
                almacenReciclables.putCeldaInt((CeldaInt) cima);
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: ir_v " + cima.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR Direcci�n de salto" + etq + "no v�lida ir_v.";
        }
    }

    /**
     * Esta funci�n se encarga de realizar un salto a la direcci�n que apunta la cima de la pila
     * consumi�ndo la cima.
     */
    public void ir_ind() {
        if (!pila.isEmpty()) {
            Celda cima = pila.pop();
            if (cima.getTipo().equalsIgnoreCase("Integer")) {
                int dir = Integer.valueOf(cima.getStringValor()).intValue();
                if (dir == -1) {
                    error = true;
                    errorMensaje = "ERROR Direcci�n no v�lida: ir_ind " + cima.getStringValor() + ".";
                } else {
                    pc = dir;
                    almacenReciclables.putCeldaInt((CeldaInt) cima);
                }
            } else {
                error = true;
                errorMensaje = "ERROR No coinciden los tipos: ir_ind " + cima.getTipo() + ".";
            }
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: ir_ind.";
        }
    }

    /**
     * Copia el valor de la cima
     */
    public void copia() {
        if (!pila.isEmpty()) {
            // ojo que no crea una celda nueva sino referencia a la antigua
            Celda c1 = pila.pop();
            Celda c2 = (Celda) c1.clone();
            pila.push(c1);
            pila.push(c2);
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: copia.";
        }
    }

    private void desapila() {
        if (!pila.isEmpty()) {
            Celda c = pila.pop();
            if (c.getTipo().equalsIgnoreCase("Integer"))
                almacenReciclables.putCeldaInt((CeldaInt) c);
            else if (c.getTipo().equalsIgnoreCase("Integer"))
                almacenReciclables.putCeldaReal((CeldaReal) c);
        } else {
            error = true;
            errorMensaje = "ERROR No quedan suficientes elementos en la pila: desapila.";
        }
    }

    private void stop() {
        finEjec = true;
    }

    /**********************************************************************
     * FUNCIONES AUXILIARES:											  *
     **********************************************************************/
    private boolean esEntero(String s) {
        try {
            int a = Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esReal(String s) {
        try {
            Float f = Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setPath(String p) {
        path = p;
    }

    public void setTrazaModo(boolean d) {
        trazaModo = d;
    }

    /**
     * Inicializamos la memoria para poder usar las variables declaradas. tam viene de una constante arriba en la clase definida
     * @param tam
     */
    public void inicializarMemoria(int tam) {
        for (int i = 0; i < tam; i++) {
            CeldaInt aux = new CeldaInt(Integer.MIN_VALUE);
            datos.add(i, aux);
        }
    }

    /**
     * Tras ejecutar, la pila puede no estar vac�a, por lo que la vaciamos antes de cerrar.
     */
    public void borrarPila() {
        while (!pila.isEmpty()) {
            pila.pop();
        }
    }

    /**
     * Metodo que muestra por pantalla los dos elementos que est�n en la cima de la pila. Adem�s muestra
     * las direcciones de memoria que tienen almacenado un dato y el PC para ver la instruccion que nos
     * encontramos
     */
    public void printEstadoMaquina() {
        Celda auxC;
        //Iterator<Celda> it = datos.iterator();

        System.out.println("==========================================");
        System.out.println("STACK: ");
        if (pila.empty())
            System.out.println("Vacia.");
        else
            System.out.println("Cima: " + pila.peek().toString());

        if (pila.size() > 1) {
            Celda aux = pila.pop();
            System.out.println("Sig:  " + pila.peek().toString());
            pila.push(aux);
        }

        System.out.println("DATA:");
        int posMem = 0;
        //Primero los datos est�ticos usados
        auxC = datos.get(posMem);
        if (!auxC.getStringValor().equals(String.valueOf(Integer.MIN_VALUE)))
            System.out.println("Pos: " + Integer.toString(posMem) + "   " + auxC.toString());
        int staticData = ((CeldaInt) auxC).getValor();
        posMem++;
        for (int i = 1; i <= staticData; i++) {
            auxC = datos.get(posMem);
            if (!auxC.getStringValor().equals(String.valueOf(Integer.MIN_VALUE)))
                System.out.println("Pos: " + Integer.toString(posMem) + "   " + auxC.toString());
            posMem++;
        }
        //resto de memoria
        while (posMem < datos.size()) {
            auxC = datos.get(posMem);
            if (!auxC.getTipo().equals("Default") && !((auxC.getStringValor().equals("-1") || auxC.getStringValor().equals(String.valueOf(Integer.MIN_VALUE))) && posMem < TAM_MEMORIA))
                System.out.println("Pos: " + Integer.toString(posMem) + "   " + auxC.toString());
            posMem++;
        }
        System.out.println("PC: " + Long.toString(pc - 1));
        System.out.println("==========================================");
    }

    /**
     * MAIN //////////////////////////////////////////////////////////////////////////////////////////////////////////
     */


    /**
     * Mirar en la memoria como usar MaquinaP
     */
    public static void main(String[] args) {
        String entrada = "";
        boolean traza = false;
        if (args.length > 0) {
            if (args[0].equals("-h"))
                System.out.println("Uso: maquinaP.jar fichero_fuente [-t]");
            else {
                entrada = args[0];
                if (args.length == 2) {
                    traza = args[1].equals("-t");
                }
            }
        } else {
            Scanner scanEntrada = new Scanner(System.in);
            do {
                System.out.print("Maquina-P/> Modo traza? si/no : ");
                entrada = scanEntrada.nextLine();
            } while (entrada.toLowerCase().compareTo("si") != 0 && entrada.toLowerCase().compareTo("no") != 0);
            if (entrada.toLowerCase().compareTo("si") == 0) {
                traza = true;
            }
            System.out.print("Maquina-P/> Path del bytecode: ");
            entrada = scanEntrada.nextLine();
        }
        if (!(entrada == "")) {
            File fich = new File(entrada);
            if (fich.exists()) {
                MaquinaP maquina = new MaquinaP();
                maquina.setPath(entrada);
                maquina.inicializarMemoria(TAM_MEMORIA);
                maquina.setTrazaModo(traza);
                maquina.ejecuta_programa(entrada);
            } else
                System.out.println("Error File not Found: No se encontr� el fichero");
        }
    }

}
