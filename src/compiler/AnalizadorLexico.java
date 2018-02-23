package compiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


public class AnalizadorLexico {

    //Categorias l�xicas reconocidas por el analizador l�xico.
    public static enum CategoriaLexica {
        ID, LN, LR, ASIG, IGUAL, NO, NOIGUAL,
        SUMA, RESTA, O, DIV, MOD, POR, PCE, PAB, PYC, ENT, SAL, Y, INTEGER, REAL, MAYOR,
        MENOR, MAOIG, MEOIG, CASI, CASR, AMP, IND, ACC, COMA, CORAB, CORCE, SI,
        ENTONCES, SINO, SINOSI, FSI, MIENTRAS, HACER, FMIENTRAS, FUNCION, FFUNCION,
        DEV, RETORNO, NULO, RESERVA, LIBERA, DECTIPO, REG, FREG, PUNTERO, EOF
    }

    ;


    /*******************/
	   /*Definicion del tipo que devuelve el analizador lexico: Token
	     Cada token posee una categor�a l�xica, un lexema, que puede ser
	     nulo dependiendo de la categor�a lexica del token, y una fila 
	     y una columna. 
	   */
    public static class Token {
        public CategoriaLexica cat;
        public String lex;
        public int linea;
        public int columna;

        public Token(CategoriaLexica cat, int linea, int columna) {
            this(cat, null, linea, columna);
        }

        public Token(CategoriaLexica cat, String lex, int linea, int columna) {
            this.cat = cat;
            this.lex = lex;
            this.linea = linea;
            this.columna = columna;
        }

        public String toString() {
            return "<cat:" + cat + ",lex:" + lex + ",(linea,columna):(" + linea + "," + columna + ")>";
        }
    }

    /*******************/

    //Estados del aut�mata, necesarios para la elaboraci�n de tokens
    private static enum Estado {
        INICIO, ID, LN, LR, ASIG, IGUAL, NO, NOIGUAL,
        SUMA, RESTA, O, DIV, MOD, POR, PCE, PAB, PYC, ENT, SAL, Y, INTEGER, REAL, MAYOR,
        MENOR, MAOIG, MEOIG, EOF, CERO, Q14, Q15, Q16, Q7, LREAL, BARRA, COM,
        AMP, IND, ACC, COMA, CORAB, CORCE
    }

    ;

    // Estado actual del analizador
    private Estado estado;

    // Tabla con las palabras reservadas del lenguaje
    private Map<String, Token> tablaReservadas;

    // lexema le�do
    private StringBuffer lex;

    // char que est� en la entrada
    private int sigCar;

    // la entrada
    private Reader input;

    // Atributos que representa la fila y columna actual en la que estamos analizando.
    private int linea;
    private int columna;

    /*Devuelve el siguiente token a tratar por un programa externo
      o para escribirlo por pantalla
    */
    public Token sigToken() throws IOException {
        //linea y columna local que se las asigna valor cada vez
        //que va a comenzar un token
        int lin, col;
        lin = linea;
        col = columna;
        //Siempre se comienza en el estado inicial del aut�mata
        estado = Estado.INICIO;
        //Se inicializa en lexema
        iniciaLex();

        while (true) {
            switch (estado) {
                //Dependiendo de lo que vaya leyendo va transitando
                //entre los diferentes estados
                case INICIO:
                    if (hayLetra()) transita(Estado.ID);
                    else if (sigCar == '0') transita(Estado.CERO);
                    else if (hayDigitoPos()) transita(Estado.LN);
                    else if (hayEof()) transita(Estado.EOF);
                    else if (sigCar == '+') transita(Estado.SUMA);
                    else if (sigCar == '-') transita(Estado.RESTA);
                    else if (sigCar == '*') transita(Estado.POR);
                    else if (sigCar == '/') transita(Estado.DIV);
                    else if (sigCar == '%') transita(Estado.MOD);
                    else if (sigCar == '(') transita(Estado.PAB);
                    else if (sigCar == ')') transita(Estado.PCE);
                    else if (sigCar == '=') transita(Estado.ASIG);
                    else if (sigCar == '|') transita(Estado.BARRA);
                    else if (sigCar == ';') transita(Estado.PYC);
                    else if (sigCar == '&') transita(Estado.AMP);
                    else if (sigCar == '@') transitaIgnorando(Estado.COM);
                    else if (sigCar == '<') transita(Estado.MENOR);
                    else if (sigCar == '>') transita(Estado.MAYOR);
                    else if (sigCar == '!') transita(Estado.NO);
                    else if (sigCar == ',') transita(Estado.COMA);
                    else if (sigCar == '.') transitaIgnorando(Estado.ACC);
                    else if (sigCar == '^') transita(Estado.IND);
                    else if (sigCar == '[') transita(Estado.CORAB);
                    else if (sigCar == ']') transita(Estado.CORCE);
                    else if (hayIgnorable()) {
                        transitaIgnorando(Estado.INICIO);
                        lin = linea;
                        col = columna;
                    } else {
                        // Entrada no esperada, Paramos la ejecuci�n y mostramos mensaje
                        Aplicacion.inhibeGenCod();
                        ManejadorDeErrores.CaracterNoReconocido(sigCar, lin, col);
                        transitaIgnorando(Estado.INICIO);
                        lin = linea; // pasamos la l�nea y columna locales a los generales
                        col = columna;
                    }
                    break;

                case ID:
                    if (hayLetra() || hayDigito()) transita(Estado.ID);
                    else return tokenId(lin, col);
                    break;

                //Si hay . transita a q14 para intentar reconocer un real siendo 0.xxx
                case CERO:
                    if (sigCar == '.') transita(Estado.Q14);
                    else return tokenNum(lin, col);
                    break;

                //Si hay . transita a q14 para intentar reconocer un real
                //Si hay e transita a q15 para intentar reconocer un exponencial
                case LN:
                    if (hayDigito()) transita(Estado.LN);
                    else if (sigCar == '.') transita(Estado.Q14);
                    else if (sigCar == 'E' || sigCar == 'e') transita(Estado.Q15);
                    else return tokenNum(lin, col);
                    break;

                //Si encuentra otro = pasa al estado Igual
                case ASIG:
                    if (sigCar == '=') transita(Estado.IGUAL);
                    else return tokenAsig(lin, col);
                    break;

                case IGUAL:
                    return tokenIgual(lin, col);

                //Si no encuentra dos barras seguidas lanza un error, ya que
                //seria algo incompleto y no reconocido por el aut�mata
                case BARRA:
                    if (sigCar == '|') transita(Estado.O);
                    else {
                        lin = linea;
                        col = columna;
                        // el error est� en que no se espera un separador
                        if (hayIgnorable()) error(lin, col - 1);
                            // el error est� en un caracter no esperado
                        else error(lin, col);
                    }
                    break;

                case O:
                    return tokenO(lin, col);

                //Si solo hay un ampersan entonces pasa al estado Ampersan, sino
                // se reconoce una y-l�gica
                case AMP:
                    if (sigCar == '&') transita(Estado.Y);
                    else return tokenAmp(lin, col);
                    break;

                case Y:
                    return tokenY(lin, col);

                //Si encuentra un @ no cuenta con ning�n car�cter hasta
                //la siguiente l�nea ya que es un comentario de l�nia
                case COM:
                    if (sigCar == '\n') {
                        transitaIgnorando(Estado.INICIO);
                        col = columna;
                        lin = linea;
                    } else transitaIgnorando(Estado.COM);
                    break;

                case MENOR:
                    if (sigCar == '=') transita(Estado.MEOIG);
                    else return tokenMenor(lin, col);
                    break;

                case MEOIG:
                    return tokenMeoig(lin, col);

                case MAYOR:
                    if (sigCar == '=') transita(Estado.MAOIG);
                    else return tokenMayor(lin, col);
                    break;

                case MAOIG:
                    return tokenMaoig(lin, col);

                case NO:
                    if (sigCar == '=') transita(Estado.NOIGUAL);
                    else return tokenNo(lin, col);
                    break;

                case NOIGUAL:
                    return tokenNoigual(lin, col);
	              
	            /*Si encuentra 0's transita a si mismo impidiendo reconocer
	              un real que tenga ceros a la derecha, si encuentra alg�n 
	              natural positivo pasara a LREAL para poder reconocer un real
	             */
                case Q14:
                    if (sigCar == '0') transita(Estado.Q14);
                    else if (hayDigitoPos()) transita(Estado.LREAL);
                    else {
                        lin = linea;
                        col = columna;
                        error(lin, col - 1);
                    }
                    break;

                //Despues de e puede reconocer un - o nada para pasar a los exponenciales
                case Q15:
                    if (sigCar == '-') transita(Estado.Q16);
                    else estado = Estado.Q16;
                    break;

                //Si hay digitos positivos transita para reconocer una exponencial
                //Si hay un 0 transita para reconocer una exponencial con 0 ejemplo 3e0
                case Q16:
                    if (hayDigitoPos()) transita(Estado.LR);
                    else if (sigCar == '0') transita(Estado.Q7);
                    else {
                        error(lin, col);
                        lin = linea;
                        col = columna;
                    }
                    break;

                //Devuelve una exponencial con 0
                case Q7:
                    return tokenLr(lin, col);
		          
		        /*Si el �ltimo car�cter leido es un digito positivo transita a si 
		          mismo para reconocer un real, si es un 0, como ya tiene ceros 
		          a la derecha transita a q14, si tiene una e pasa a poder reconocer
		          una exponencial
		        */
                case LREAL:
                    if (sigCar == '0') transita(Estado.Q14);
                    else if (hayDigito()) transita(Estado.LREAL);
                    else if (sigCar == 'E' || sigCar == 'e') transita(Estado.Q15);
                    else return tokenLr(lin, col);
                    break;

                case LR:
                    if (hayDigito()) transita(Estado.LR);
                    else return tokenLr(lin, col);
                    break;

                case SUMA:
                    return tokenSuma(lin, col);

                case RESTA:
                    return tokenResta(lin, col);

                case POR:
                    return tokenPor(lin, col);

                case DIV:
                    return tokenDiv(lin, col);

                case MOD:
                    return tokenMod(lin, col);

                case PCE:
                    return tokenPce(lin, col);

                case PYC:
                    return tokenPyc(lin, col);

                case PAB:
                    return tokenPab(lin, col);

                case EOF:
                    return tokenEof(lin, col);

                case IND:
                    return tokenInd(lin, col);

                case CORAB:
                    return tokenCorab(lin, col);

                case CORCE:
                    return tokenCorce(lin, col);

                case ACC:
                    return tokenAcc(lin, col);

                case COMA:
                    return tokenComa(lin, col);
            }
        }
    }

    /*****************************************************************************************************/

    // Token de n�mero entero
    private Token tokenNum(int lin, int col) {
        return new Token(CategoriaLexica.LN, lex(), lin, col);
    }

    // Token de n�mero real
    private Token tokenLr(int lin, int col) {
        return new Token(CategoriaLexica.LR, lex(), lin, col);
    }

    // Token de s�mbolo suma num�rica ( + )
    private Token tokenSuma(int lin, int col) {
        return new Token(CategoriaLexica.SUMA, lin, col);
    }

    // Token de s�mbolo resta num�rica ( - )
    private Token tokenResta(int lin, int col) {
        return new Token(CategoriaLexica.RESTA, lin, col);
    }

    // Token de s�mbolo multiplicaci�n num�rica ( * )
    private Token tokenPor(int lin, int col) {
        return new Token(CategoriaLexica.POR, lin, col);
    }

    // Token de s�mbolo divisi�n num�rica ( / )
    private Token tokenDiv(int lin, int col) {
        return new Token(CategoriaLexica.DIV, lin, col);
    }

    // Token de s�mbolo m�dulo num�rico ( % )
    private Token tokenMod(int lin, int col) {
        return new Token(CategoriaLexica.MOD, lin, col);
    }

    // Token de s�mbolo 'par�ntesis cerrado' ( ) )
    private Token tokenPce(int lin, int col) {
        return new Token(CategoriaLexica.PCE, lin, col);
    }

    // Token de s�mbolo 'par�ntesis abierto' ( ( )
    private Token tokenPab(int lin, int col) {
        return new Token(CategoriaLexica.PAB, lin, col);
    }

    // Token de s�mbolo ( > )
    private Token tokenMayor(int lin, int col) {
        return new Token(CategoriaLexica.MAYOR, lin, col);
    }

    // Token de s�mbolo menor ( < )
    private Token tokenMenor(int lin, int col) {
        return new Token(CategoriaLexica.MENOR, lin, col);
    }

    // Token de s�mbolo asignaci�n ( = )
    private Token tokenAsig(int lin, int col) {
        return new Token(CategoriaLexica.ASIG, lin, col);
    }

    // Token de s�mbolo no-l�gico ( ! )
    private Token tokenNo(int lin, int col) {
        return new Token(CategoriaLexica.NO, lin, col);
    }

    // Token de s�mbolo 'punto y coma' ( ; )
    private Token tokenPyc(int lin, int col) {
        return new Token(CategoriaLexica.PYC, lin, col);
    }

    // Token de s�mbolo igual-comparaci�n ( == )
    private Token tokenIgual(int lin, int col) {
        return new Token(CategoriaLexica.IGUAL, lin, col);
    }

    // Token de s�mbolo o-l�gico ( || )
    private Token tokenO(int lin, int col) {
        return new Token(CategoriaLexica.O, lin, col);
    }

    // Token de s�mbolo y-l�gico ( && )
    private Token tokenY(int lin, int col) {
        return new Token(CategoriaLexica.Y, lin, col);
    }

    // Token de s�mbolo 'menor o igual' ( <= )
    private Token tokenMeoig(int lin, int col) {
        return new Token(CategoriaLexica.MEOIG, lin, col);
    }

    // Token de s�mbolo 'menor o igual' ( >= )
    private Token tokenMaoig(int lin, int col) {
        return new Token(CategoriaLexica.MAOIG, lin, col);
    }

    // Token de s�mbolo ampersan ( & )
    private Token tokenAmp(int lin, int col) {
        return new Token(CategoriaLexica.AMP, lin, col);
    }

    // Token de s�mbolo punto ( . )
    private Token tokenAcc(int lin, int col) {
        return new Token(CategoriaLexica.ACC, lin, col);
    }

    // Token de s�mbolo coma ( , )
    private Token tokenComa(int lin, int col) {
        return new Token(CategoriaLexica.COMA, lin, col);
    }

    // Token de corchete abierto ( [ )
    private Token tokenCorab(int lin, int col) {
        return new Token(CategoriaLexica.CORAB, lin, col);
    }

    // Token de corchete cerrado ( ] )
    private Token tokenCorce(int lin, int col) {
        return new Token(CategoriaLexica.CORCE, lin, col);
    }

    // Token de s�mbolo distinto ( != )
    private Token tokenNoigual(int lin, int col) {
        return new Token(CategoriaLexica.NOIGUAL, lin, col);
    }

    // Token de �ndice ( ^ )
    private Token tokenInd(int lin, int col) {
        return new Token(CategoriaLexica.IND, lin, col);
    }

    //Mira si existe en la tabla de reservadas,
    //y si no existe devuelve un ID
    private Token tokenId(int lin, int col) {
        Token t = obtenReservada();
        if (t == null) return new Token(CategoriaLexica.ID, lex().toLowerCase(), lin, col);
        else return new Token(t.cat, lin, col);
    }

    //Token de final de fichero
    private Token tokenEof(int lin, int col) {
        return new Token(CategoriaLexica.EOF, lin, col);
    }

    //Devuelve el token asociado a un ID reservado (el lexema actual)
    //Si no se encuentra devuelve null
    private Token obtenReservada() {
        return tablaReservadas.get(lex().toLowerCase());
    }

    /*****************************************************************************************************/

    //Devuelve el lexema en string
    private String lex() {
        return lex.toString();
    }

    //Leemos de la entrada un caracter y lo a�adimos al lexema
    private void transita(Estado siguienteEstado) throws IOException {
        estado = siguienteEstado;
        columna++;
        lex.append((char) sigCar);
        sigCar = input.read();
    }

    //Leemos de la entrada un caracter y no hacemos nada con �l.
    private void transitaIgnorando(Estado siguienteEstado) throws IOException {
        estado = siguienteEstado;
        if (sigCar == '\n') {
            linea++;
            columna = 1;
        } else if (sigCar == '\t') {
            columna = columna + 9 - columna % 8;
        } else if (sigCar == ' ') {
            columna++;
        } else columna++;
        sigCar = input.read();
    }

    //Iniciamos el lexema actual borrando su contenido.
    private void iniciaLex() {
        lex.delete(0, lex.length());
    }

    //Tenemos una letra como siguiente caracter? (para reconocer IDs)
    private boolean hayLetra() {
        return sigCar >= 'a' && sigCar <= 'z' ||
                sigCar >= 'A' && sigCar <= 'Z' ||
                sigCar == '_';
    }

    //Tenemos un d�gito como siguiente caracter? (para reconocer reales o enteros)
    private boolean hayDigito() {
        return sigCar >= '0' && sigCar <= '9';
    }

    //Reconocer un d�gito mayor que 0
    private boolean hayDigitoPos() {
        return sigCar >= '1' && sigCar <= '9';
    }

    //Reconocer final de fichero
    private boolean hayEof() {
        return sigCar == -1;
    }

    //Reconocer un caracter de formato o separador sin significado para el aut�mata
    private boolean hayIgnorable() {
        return sigCar == ' ' || sigCar == '\t' || sigCar == '\n' || sigCar == '\b' ||
                sigCar == '\r';
    }

    //Si tenemos un error inhibimos la traducci�n y mostramos el error con su posici�n
    private void error(int lin, int col) {
        iniciaLex();
        estado = Estado.INICIO;
        Aplicacion.inhibeGenCod();
        ManejadorDeErrores.CaracterNoEsperado(lin, col);
    }

    //Crea la tabla de palabras reservadas que tienen significado especial
    private void construyeReservadas() {
        tablaReservadas = new HashMap<String, Token>();
        tablaReservadas.put("real", new Token(CategoriaLexica.REAL, 0, 0));
        tablaReservadas.put("int", new Token(CategoriaLexica.INTEGER, 0, 0));
        tablaReservadas.put("in", new Token(CategoriaLexica.ENT, 0, 0));
        tablaReservadas.put("out", new Token(CategoriaLexica.SAL, 0, 0));
        tablaReservadas.put("if", new Token(CategoriaLexica.SI, 0, 0));
        tablaReservadas.put("then", new Token(CategoriaLexica.ENTONCES, 0, 0));
        tablaReservadas.put("else", new Token(CategoriaLexica.SINO, 0, 0));
        tablaReservadas.put("endif", new Token(CategoriaLexica.FSI, 0, 0));
        tablaReservadas.put("fi", new Token(CategoriaLexica.FSI, 0, 0));
        tablaReservadas.put("elsif", new Token(CategoriaLexica.SINOSI, 0, 0));
        tablaReservadas.put("while", new Token(CategoriaLexica.MIENTRAS, 0, 0));
        tablaReservadas.put("do", new Token(CategoriaLexica.HACER, 0, 0));
        tablaReservadas.put("endwhile", new Token(CategoriaLexica.FMIENTRAS, 0, 0));
        tablaReservadas.put("tipo", new Token(CategoriaLexica.DECTIPO, 0, 0));
        tablaReservadas.put("rec", new Token(CategoriaLexica.REG, 0, 0));
        tablaReservadas.put("endrec", new Token(CategoriaLexica.FREG, 0, 0));
        tablaReservadas.put("pointer", new Token(CategoriaLexica.PUNTERO, 0, 0));
        tablaReservadas.put("null", new Token(CategoriaLexica.NULO, 0, 0));
        tablaReservadas.put("alloc", new Token(CategoriaLexica.RESERVA, 0, 0));
        tablaReservadas.put("free", new Token(CategoriaLexica.LIBERA, 0, 0));
        tablaReservadas.put("fun", new Token(CategoriaLexica.FUNCION, 0, 0));
        tablaReservadas.put("end", new Token(CategoriaLexica.FFUNCION, 0, 0));
        tablaReservadas.put("returns", new Token(CategoriaLexica.DEV, 0, 0));
        tablaReservadas.put("return", new Token(CategoriaLexica.RETORNO, 0, 0));
    }

    /*****************************************************************************************************/
	   
	   /*El analizador lexico por construcci�n reconoce exponenciales 
	     con exponente 0 y enteros e id's intecalados sin ningun 
	     espacio entre ellos, a parte de lo especificado por el aut�mata.
	     Por ejeplo la cadena 15ab12a ser� reconocida como:
	     15:int
	     ab:id
	     12:int
	     a:id
	     Cadenas de la forma |@
	     dar� error en el @ como car�cter no esperado y tambi�n error 
	     como car�cter no reconocido por el aut�mata desde el estado incial.
	    */
    public AnalizadorLexico(Reader input) throws IOException {
        this.input = input;
        sigCar = input.read();
        lex = new StringBuffer();
        linea = 1;
        columna = 1;
        construyeReservadas();
    }

    public static void main(String[] args) throws IOException {
        AnalizadorLexico al = new AnalizadorLexico(new InputStreamReader(System.in));
        Token t = al.sigToken();
        while (t.cat != CategoriaLexica.EOF) {
            System.out.println(t);
            t = al.sigToken();
        }
        System.out.println(t);
    }

}
