package compiler;
/**
 *
 */

import compiler.Propiedades.Clase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Clase que implementa un traductor predictivo recursivo utilizando como base una especificaci�n
 * previamente acondicionada a los problemas inherentes a este tipo de herramientas.
 *
 * @Author javieku
 * @Updates: Vlad, Green, David
 */

public class Traductor {

    //Los dos modos disponibles para una expresi�n
    public static enum Modo {
        VAR, VAL
    }

    /*--------------------------------------- Atributos Globales ----------------------------------------------*/

    /**
     * Toma como entrada un programa fuente y genera a partir de este una secuencia de tokens
     * que se procesando uno a uno.
     */
    private AnalizadorLexico al;

    /**
     * Siguiente Token en la secuencia.
     */
    private AnalizadorLexico.Token tkAct;

    //Tabla de s�mbolos global
    public TablaDeSimbolos ts;

    //C�digo del programa
    private List<Instruccion> cod;

    //Posici�n de la siguiente instrucci�n
    private int etq;

    //Direcci�n de la siguiente posici�n de memoria
    private int dir;

    //Nivel de anidamiento
    private int n;

    //Nivel de anidamiento m�ximo
    private int nMax;

    //Error en el programa
    private boolean err;

    //Modo de la expresi�n actual
    private Modo modo;

    static final int LONGINICIO = 4;
    static final int LONGAPILARET = 5;
    static final int LONGPROLOGO = 14;
    static final int LONGEPILOGO = 15;
    static final int LONGINICIOPASO = 3;
    static final int LONGFINPASO = 1;
    static final int LONGDIRECCIONPARFORMAL = 2;
    static final int LONGPASOPARAMETRO = 1;
    static final int LONGCODENT = 3;
    static final int LONGCODASIG = 2;
    static final int LONGACCESOPUNTERO = 1;
    static final int LONGACCESOREG = 2;
    static final int LONGACCESOARRAY = 3;
    static final int LONGCODLIBERA = 2;
    static final int LONGCODRESERVA = 2;


    /*--------------------------------------- Constructora ----------------------------------------------*/

    /**
     * Crea el objecto traductor
     *
     * @param al    Analizador l�xico.
     * @param path2
     * @param path
     * @return Traductor inicializado.
     */
    public Traductor(AnalizadorLexico al) {
        this.al = al;
    }
    /*---------------------------------------- Funciones ------------------------------------------------*/

    /**
     * M�todo que da comienzo a el proceso de traducci�n y genera el archivo de instrucciones.
     */
    public List<Instruccion> INICIO() throws IOException {
        tkAct = al.sigToken();
        TProg prog = new TProg();
        prog.traduce();
        match(AnalizadorLexico.CategoriaLexica.EOF);
        return cod;
    }

    /**
     * M�todo que realiza el consumo de un token del analizador l�xico
     *
     * @param Categor�a l�xica a procesar.
     */
    private void match(AnalizadorLexico.CategoriaLexica cat) throws IOException {
        if (tkAct.cat == cat) tkAct = al.sigToken();
        else {
            Aplicacion.inhibeGenCod();
            ManejadorDeErrores.tokenNoEsperado(cat, tkAct);
        }
    }

    /*---------------------------------------- Clases ------------------------------------------------*/


    private class TProg {
        public int etqs1, etqs2, etqs3;
        public String idh;

        public TProg traduce() throws IOException {
            ts = FuncionesSemanticas.creaTs();
            n = 0;
            nMax = 0;
            dir = 0;
            err = false;
            cod = new ArrayList<Instruccion>();
            cod = FuncionesSemanticas.inicio(cod); //m�s tarde se parchea con n y dir de TDECS
            etqs1 = 0;
            etqs2 = 2;
            etq = LONGINICIO;
            cod = FuncionesSemanticas.emite("ir_a ", cod);  // esta tambi�n se va a parchear con etq de TDECS
            etqs3 = etq;
            etq = etq + 1;
            TDECS decs = new TDECS().traduce();
            cod = FuncionesSemanticas.parchea(etqs1, nMax + 2, cod);
            cod = FuncionesSemanticas.parchea(etqs2, dir + nMax + 2, cod);
            cod = FuncionesSemanticas.parchea(etqs3, etq, cod);
            idh = "Main";
            TACCS accs = new TACCS().traduce(idh);
            cod = FuncionesSemanticas.emite("stop", cod);
            etq = etq + 1;
            err = err || !decs.pend.isEmpty();
            return this;
        }
    }

    /*-------------------------Secci�n de Declaraciones--------------------------*/

    private class TDECS {
        public Vector<String> pend;

        public TDECS traduce() throws IOException {
            pend = new Vector<String>();
            err = false;
            TRDECS rdecs = new TRDECS().traduce(pend);
            pend = rdecs.pend;
            //vlad dice que nada de nivel
            return this;
        }
    }

    private class TRDECS {
        public Vector<String> pend;

        public TRDECS traduce(Vector<String> pendh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER ||
                    (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID
                            && FuncionesSemanticas.existeID(ts, tkAct.lex, tkAct.linea, tkAct.columna) //Comprobamos antes que est� sino, acceso a null
                            && ts.getPropiedades(tkAct.lex).clase == Clase.tipo) ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REG ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.PUNTERO ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.DECTIPO ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.FUNCION) {

                TDEC dec = new TDEC().traduce();

                err = err || (FuncionesSemanticas.existeID(ts, dec.id, dec.linea, dec.col)
                        && FuncionesSemanticas.damePropiedades(ts, dec.id, tkAct.linea, tkAct.columna).nivel == n);

                ts = FuncionesSemanticas.anadeID(ts, dec.id, dec.props, dec.linea, dec.col);
                dir = dir + dec.tam;

                pendh.addAll(dec.pend); // concatenamos ambas listas (RDECS0Pendh || DECPend)
                if (dec.props.clase == Clase.tipo && pendh.contains(dec.id))
                    pendh.remove(dec.id);

                pend = pendh;
                TRDECS rdecs = new TRDECS().traduce(pend);
                //RDECS0Pend=RDECS1Pend <-- No faltaria algo asi??

            } else
                pend = pendh;
            return this;
        }
    }

    private class TDEC {
        public Propiedades props;
        public int tam;
        public String id;
        public Vector<String> pend;
        public int linea;
        public int col;

        public TDEC traduce() throws IOException {
            linea = tkAct.linea;
            col = tkAct.columna;
            // DECVAR
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ID ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REG ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.PUNTERO) {

                TDECVAR decvar = new TDECVAR().traduce();
                id = decvar.id;
                tam = decvar.tam;
                props = decvar.props;
                pend = decvar.pend;
            }
            //DECTIPO
            else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.DECTIPO) {
                TDECTIPO dectipo = new TDECTIPO().traduce();
                tam = 0;
                id = dectipo.id;
                props = dectipo.props;
                pend = dectipo.pend;
            }
            // DECFUNC
            else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.FUNCION) {
                TDECFUNC decfunc = new TDECFUNC().traduce();
                tam = 0;
                id = decfunc.id;
                props = decfunc.props;
                pend = new Vector<String>();
            } else //quizas error
                ;

            return this;
        }
    }

    private class TDECVAR {
        public Propiedades props;
        public int tam;
        public String id;
        public Vector<String> pend;

        public TDECVAR traduce() throws IOException {
            TDTIPO dtipo = new TDTIPO().traduce();
            id = tkAct.lex;
            match(AnalizadorLexico.CategoriaLexica.ID);
            match(AnalizadorLexico.CategoriaLexica.PYC);
            tam = dtipo.tipo.tam;
            props = new Propiedades(dtipo.tipo, dir, Clase.var, n);

            err = dtipo.tipo.t.equals("err") || FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna)
                    || (dtipo.tipo.t.equals("ref") && !FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna));
            //Aqui no faltaria algo asi como comprobar el nivel, porke puede estar declarada en otro nivel
            pend = dtipo.pend;

            return this;
        }
    }

    private class TDECTIPO {
        public String id;
        public Propiedades props;
        public Vector<String> pend;

        public TDECTIPO traduce() throws IOException {
            match(AnalizadorLexico.CategoriaLexica.DECTIPO);
            TDTIPO dtipo = new TDTIPO().traduce();
            id = tkAct.lex;
            match(AnalizadorLexico.CategoriaLexica.ID);
            match(AnalizadorLexico.CategoriaLexica.PYC);
            props = new Propiedades(dtipo.tipo, -1, Clase.tipo, n);
            err = err || FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna) ||
                    (dtipo.tipo.t.equals("ref") && !FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna));
            //idem
            pend = dtipo.pend;

            return this;
        }
    }

    private class TDECFUNC {
        public String id;
        public Vector<String> pend;
        public Propiedades props;

        public TDECFUNC traduce() throws IOException {
            int dirAnt;
            String id0;

            match(AnalizadorLexico.CategoriaLexica.FUNCION);

            n++;
            nMax = Math.max(n, nMax);
            ts = FuncionesSemanticas.creaTs(ts);
            dirAnt = dir;
            id0 = tkAct.lex;

            match(AnalizadorLexico.CategoriaLexica.ID);
            match(AnalizadorLexico.CategoriaLexica.PAB);

            TFPARAMS fparams = new TFPARAMS().traduce();

            match(AnalizadorLexico.CategoriaLexica.PCE);

            TTIPORET tiporet = new TTIPORET().traduce();

            Tipo t = new TipoFunc(tiporet.tipo, tiporet.tipo.tam, fparams.params);
            props = new Propiedades(t,/*Dir a parchear*/-1, Propiedades.Clase.fun, n);
            ts.anadeTS(id0, props);

            TBLOQUE bloque = new TBLOQUE().traduce(id0);
            match(AnalizadorLexico.CategoriaLexica.FFUNCION);
            id = tkAct.lex;
            match(AnalizadorLexico.CategoriaLexica.ID);
            match(AnalizadorLexico.CategoriaLexica.PYC);
            n--;
            ts = FuncionesSemanticas.getAnterior(ts);
            dir = dirAnt;
            err = err || (FuncionesSemanticas.existeID(ts, id0, tkAct.linea, tkAct.columna) && ts.getPropiedades(id0).clase != Clase.fun)
                    || id0 != id;
            t = new TipoFunc(tiporet.tipo, tiporet.tipo.tam, fparams.params);
            props = new Propiedades(t,/*Dir inicio*/bloque.bloqueInicio, Propiedades.Clase.fun, n);

            return this;
        }
    }

    private class TDTIPO {
        public Tipo tipo;
        public Vector<String> pend;
        public String id;
        // quiz�s deber�a haber un err aqu�, y �ste no se global, porque lo ponemos a false directamente un poco m�s abajo

        public TDTIPO traduce() throws IOException {
            //TTIPO
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL) {

                TTIPO tipo = new TTIPO().traduce();
                err = false;
                TRDTIPO rdtipo = new TRDTIPO().traduce(new Vector<String>(), tipo.tipo);
                this.tipo = rdtipo.tipo;
                this.pend = rdtipo.pend;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                id = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);

                if (FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna))
                    err = ts.getPropiedades(id).clase != Clase.tipo;
                else
                    err = false;

                // usamos pend como variable auxiliar
                pend = new Vector<String>();
                if (!FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna))
                    pend.add(id);

                int tam; // tama�o auxiliar
                if (FuncionesSemanticas.existeID(ts, id, tkAct.linea, tkAct.columna))
                    tam = ts.getPropiedades(id).tipo.tam;
                else
                    tam = -1;
                // usamos tipo como variable auxiliar
                // se pueden fusionar los 3 if's
                tipo = new TipoRef(tam, id);

                TRDTIPO rdtipo = new TRDTIPO().traduce(pend, tipo);
                tipo = rdtipo.tipo;
                pend = rdtipo.pend;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.REG) {
                match(AnalizadorLexico.CategoriaLexica.REG);
                TDCAMPOS dcampos = new TDCAMPOS().traduce();
                match(AnalizadorLexico.CategoriaLexica.FREG);
                TRDTIPO rdtipo = new TRDTIPO().traduce(dcampos.pend, new TipoReg(dcampos.tam, dcampos.campos));
                tipo = rdtipo.tipo;
                pend = rdtipo.pend;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.PUNTERO) {
                match(AnalizadorLexico.CategoriaLexica.PUNTERO);
                TDTIPO dtipo = new TDTIPO().traduce();
                TRDTIPO rdtipo = new TRDTIPO().traduce(dtipo.pend, new TipoPunt(dtipo.tipo));
                tipo = rdtipo.tipo;
                pend = rdtipo.pend;
            } else    // quiz�s error
                ;

            return this;
        }
    }

    private class TRDTIPO {
        public Tipo tipo;
        public Vector<String> pend;

        public TRDTIPO traduce(Vector<String> pendh, Tipo tipoh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.CORAB) {
                match(AnalizadorLexico.CategoriaLexica.CORAB);
                int nlex = FuncionesSemanticas.valorDeN(tkAct.lex); // variable auxiliar para guardar el valor del n�mero
                match(AnalizadorLexico.CategoriaLexica.LN);
                match(AnalizadorLexico.CategoriaLexica.CORCE);

                // Javi: A�ado comprobaci�n de casting.
                // est� mal algo ( lo del casting
                // no s� exactamente c�mo va,  y lo de linea
                // y columna no puede funcionar, porque aqu� no est� el id
                boolean errAux = false;
                if (tipoh instanceof TipoRef)
                    errAux = (tipoh.t.equals("ref") && !FuncionesSemanticas.existeID(ts, ((TipoRef) tipoh).nombre, tkAct.linea, tkAct.columna));
                err = err || errAux;

                TRDTIPO rdtipo = new TRDTIPO().traduce(pendh, new TipoArray(tipoh.tam * nlex, nlex, tipoh));

                pend = rdtipo.pend;
                tipo = rdtipo.tipo;
            } else {
                tipo = tipoh;
                pend = pendh;
            }

            return this;
        }
    }

    private class TTIPO {
        public Tipo tipo;

        public TTIPO traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL) {
                match(AnalizadorLexico.CategoriaLexica.REAL);
                tipo = new Tipo("real", 1);
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER) {
                match(AnalizadorLexico.CategoriaLexica.INTEGER);
                tipo = new Tipo("integer", 1);
            }
            return this;
        }
    }

    private class TDCAMPOS {
        public List<Campo> campos;
        public int tam;
        public Vector<String> pend;

        public TDCAMPOS traduce() throws IOException {
            TDCAMPO dcampo = new TDCAMPO().traduce(0);
            List<Campo> campo = new ArrayList<Campo>();
            campo.add(dcampo.campo);
            TRDCAMPOS rdcampos = new TRDCAMPOS().traduce(dcampo.pend, campo, dcampo.tam);
            campos = rdcampos.campos;
            tam = rdcampos.tam;
            pend = rdcampos.pend;

            return this;
        }
    }

    private class TRDCAMPOS {
        public List<Campo> campos;
        public int tam;
        public Vector<String> pend;

        public TRDCAMPOS traduce(Vector<String> pendh, List<Campo> camposh, int tamh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ID ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REG ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.PUNTERO) {

                TDCAMPO dcampo = new TDCAMPO().traduce(tamh);
                err = err || FuncionesSemanticas.existeCampo(camposh, dcampo.campo.nombre);
                pendh.addAll(dcampo.pend);
                camposh.add(dcampo.campo);
                TRDCAMPOS rdcampos = new TRDCAMPOS().traduce(pendh, camposh, tamh + dcampo.tam);
                campos = rdcampos.campos;
                tam = rdcampos.tam;
                pend = rdcampos.pend;
            } else {    // producci�n lambda
                campos = camposh;
                tam = tamh;
                pend = pendh;
            }

            return this;
        }
    }

    private class TDCAMPO {
        public Campo campo;
        public int tam;
        public Vector<String> pend;

        public TDCAMPO traduce(int desph) throws IOException {
            TDTIPO dtipo = new TDTIPO().traduce();

            campo = new Campo(tkAct.lex, dtipo.tipo, desph);
            tam = dtipo.tipo.tam;
            pend = dtipo.pend;

            match(AnalizadorLexico.CategoriaLexica.ID);
            match(AnalizadorLexico.CategoriaLexica.PYC);

            return this;
        }
    }

    private class TBLOQUE {

        public int bloqueInicio;

        public TBLOQUE traduce(String bloqueIdh) throws IOException {
            TDECS decs = new TDECS().traduce();
            bloqueInicio = etq;
            //Parcheamos la dir de inicio de la funci�n por si se produce llamada recursiva
            FuncionesSemanticas.parcheaDirInicioFunc(ts, bloqueIdh, bloqueInicio, tkAct.linea, tkAct.columna);
            FuncionesSemanticas.prologo(n, dir, cod);
            etq = etq + LONGPROLOGO;
            TACS acs = new TACS().traduce(bloqueIdh);
            etq = etq + LONGEPILOGO;
            FuncionesSemanticas.epilogo(n, dir, cod);
            // falta emite (ir_ind)
            return this;
        }
    }

    private class TFPARAMS {
        public List<Param> params;

        public TFPARAMS traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID
                    || tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER
                    || tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL) {
                TLFPARAMS lfparams = new TLFPARAMS().traduce();
                params = lfparams.params;
            } else    // producci�n lambda
                params = new ArrayList<Param>();

            return this;
        }
    }

    private class TLFPARAMS {
        public List<Param> params;

        public TLFPARAMS traduce() throws IOException {
            dir = 0;
            TFPARAM fparam = new TFPARAM().traduce();
            dir = fparam.tam;
            fparam.props.dir = 0;
            FuncionesSemanticas.anadeID(ts, fparam.id, fparam.props, tkAct.linea, tkAct.columna);
            List<Param> param = new ArrayList<Param>();
            param.add(fparam.param);
            TRLFPARAMS rlfparams = new TRLFPARAMS().traduce(param);
            params = rlfparams.params;

            return this;
        }
    }

    private class TRLFPARAMS {
        public List<Param> params;

        public TRLFPARAMS traduce(List<Param> paramsh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.COMA) {
                match(AnalizadorLexico.CategoriaLexica.COMA);
                TFPARAM fparam = new TFPARAM().traduce();
                fparam.props.dir = dir;
                dir = dir + fparam.tam;
                err = err || (FuncionesSemanticas.existeID(ts, fparam.id, tkAct.linea, tkAct.columna) && ts.getPropiedades(fparam.id).nivel == n);
                FuncionesSemanticas.anadeID(ts, fparam.id, fparam.props, tkAct.linea, tkAct.columna);
                paramsh.add(fparam.param);
                TRLFPARAMS rlfparams = new TRLFPARAMS().traduce(paramsh);
                params = rlfparams.params;
            } else    // producci�n lambda
                params = paramsh;

            return this;
        }
    }

    private class TFPARAM {
        public Param param;
        public String id;
        public int tam;
        public Propiedades props;

        public TFPARAM traduce() throws IOException {
            TDTIPO dtipo = new TDTIPO().traduce();
            TRFPARAM rfparam = new TRFPARAM().traduce(dtipo.tipo);
            id = rfparam.id;
            props = rfparam.props;
            param = rfparam.param;
            tam = dtipo.tipo.tam;

            return this;
        }
    }

    private class TRFPARAM {
        public String id;
        public Param param;
        public Propiedades props;

        public TRFPARAM traduce(Tipo tipoh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                id = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                props = new Propiedades(tipoh, dir, Clase.var, n);
                param = new Param(Modo.VAL, tipoh, dir);
            } else {
                match(AnalizadorLexico.CategoriaLexica.AMP);
                id = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                props = new Propiedades(tipoh, dir, Clase.pvar, n);
                param = new Param(Modo.VAR, tipoh, dir);
            }
            return this;
        }
    }

    private class TTIPORET {
        public Tipo tipo;
        public boolean err;

        public TTIPORET traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.DEV) {
                match(AnalizadorLexico.CategoriaLexica.DEV);
                TDTIPO dtipo = new TDTIPO().traduce();
                tipo = dtipo.tipo;
                err = err || (dtipo.tipo.t != "punt" && dtipo.tipo.t != "integer" && dtipo.tipo.t != "real");
            } else { // producci�n lambda
                tipo = new Tipo("integer", 1); // suponemos tipo entero cuando no se indica expl�citamente
                // err = false;
            }
            return this;
        }
    }

    /*-------------------------Secci�n de Acciones--------------------------*/

    private class TACCS {

        public TACCS traduce(String idh) throws IOException {
            TAC ac = new TAC().traduce(idh);
            match(AnalizadorLexico.CategoriaLexica.PYC);
            TRACCS raccs = new TRACCS().traduce(idh);
            return this;
        }
    }


    private class TRACCS {

        public TRACCS traduce(String idh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID || tkAct.cat == AnalizadorLexico.CategoriaLexica.LN ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LR || tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT || tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.SI || tkAct.cat == AnalizadorLexico.CategoriaLexica.MIENTRAS ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.RESERVA || tkAct.cat == AnalizadorLexico.CategoriaLexica.LIBERA ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.RETORNO || tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO ||
                    FuncionesAux.hayOp5(tkAct.cat)) {
                TAC ac = new TAC().traduce(idh);
                match(AnalizadorLexico.CategoriaLexica.PYC);
                TRACCS raccs = new TRACCS().traduce(idh);
            } else {
                err = false;
            }
            return this;
        }
    }


    private class TAC {
        public boolean parh;

        public TAC traduce(String idh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID || tkAct.cat == AnalizadorLexico.CategoriaLexica.LN ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LR || tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT || tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO || FuncionesAux.hayOp5(tkAct.cat)) {
                parh = true;//segun el doc es false
                TE0 e0 = new TE0().traduce(parh);
                err = e0.tipo.t.equalsIgnoreCase("err");
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SI) {
                TSENTIF sentif = new TSENTIF().traduce(idh);
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MIENTRAS) {
                TSENTWHILE sentwhile = new TSENTWHILE().traduce(idh);
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.RESERVA) {
                match(AnalizadorLexico.CategoriaLexica.RESERVA);
                Tipo rmemtipoh;
                if (FuncionesSemanticas.existeID(ts, tkAct.lex, tkAct.linea, tkAct.columna) && (ts.getPropiedades(tkAct.lex).clase == Propiedades.Clase.var || ts.getPropiedades(tkAct.lex).clase == Propiedades.Clase.pvar))
                    rmemtipoh = FuncionesSemanticas.ref_(ts.getPropiedades(tkAct.lex).tipo, ts, tkAct.linea, tkAct.columna);
                else
                    rmemtipoh = new Tipo("err", -1);
                etq = etq + FuncionesSemanticas.longAccesoVar(ts.getPropiedades(tkAct.lex));
                cod = FuncionesSemanticas.accesoVar(ts.getPropiedades(tkAct.lex), cod);
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRMEM rmem = new TRMEM().traduce(rmemtipoh);
                err = !rmem.tipo.t.equals("punt");
                cod = FuncionesSemanticas.genCodReserva(rmem.tipo, ts, cod, tkAct.linea, tkAct.columna);
                etq = etq + LONGCODRESERVA;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.LIBERA) {
                match(AnalizadorLexico.CategoriaLexica.LIBERA);
                Tipo rmemtipoh;
                if (FuncionesSemanticas.existeID(ts, tkAct.lex, tkAct.linea, tkAct.columna) && (ts.getPropiedades(tkAct.lex).clase == Propiedades.Clase.var || ts.getPropiedades(tkAct.lex).clase == Propiedades.Clase.pvar))
                    rmemtipoh = FuncionesSemanticas.ref_(ts.getPropiedades(tkAct.lex).tipo, ts, tkAct.linea, tkAct.columna);
                else
                    rmemtipoh = new Tipo("err", -1);
                etq = etq + FuncionesSemanticas.longAccesoVar(ts.getPropiedades(tkAct.lex));
                cod = FuncionesSemanticas.accesoVar(ts.getPropiedades(tkAct.lex), cod);
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRMEM rmem = new TRMEM().traduce(rmemtipoh);
                err = !rmem.tipo.t.equals("punt");
                cod = FuncionesSemanticas.genCodLibera(rmem.tipo, ts, cod, tkAct.linea, tkAct.columna);
                etq = etq + LONGCODLIBERA;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.RETORNO) {
                match(AnalizadorLexico.CategoriaLexica.RETORNO);
                parh = false;
                TE0 e0 = new TE0().traduce(parh);
                err = !FuncionesSemanticas.tipoCompatible(e0.tipo, ts.getPropiedades(idh).tipo, ts) || idh == "Main";
            }
            return this;
        }
    }

    private class TSENTIF {

        public TSENTIF traduce(String idh) throws IOException {
            int inst;
            match(AnalizadorLexico.CategoriaLexica.SI);
            TCASOS casos = new TCASOS().traduce(idh);
            FuncionesSemanticas.emite("ir_a ", cod);
            inst = etq;
            etq = etq + 1;
            TSENTELSE sentelse = new TSENTELSE().traduce(idh);
            match(AnalizadorLexico.CategoriaLexica.FSI);
            FuncionesSemanticas.parchea(inst, etq, cod);
            return this;
        }
    }

    private class TSENTELSE {

        public TSENTELSE traduce(String idh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SINO) {
                match(AnalizadorLexico.CategoriaLexica.SINO);
                TACS acs = new TACS().traduce(idh);
            } else
                err = false;
            return this;
        }
    }

    private class TCASOS {

        public TCASOS traduce(String idh) throws IOException {
            TCASO caso = new TCASO().traduce(idh);
            FuncionesSemanticas.emite("ir_a ", cod);
            int inst = etq;
            etq = etq + 1;
            TRCASOS rcaso = new TRCASOS().traduce(idh);
            FuncionesSemanticas.parchea(inst, etq, cod);
            return this;
        }
    }

    private class TRCASOS {

        public TRCASOS traduce(String idh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SINOSI) {
                match(AnalizadorLexico.CategoriaLexica.SINOSI);
                TCASO caso = new TCASO().traduce(idh);
                FuncionesSemanticas.emite("ir_a ", cod);
                int inst = etq;
                etq = etq + 1;
                TRCASOS rcaso = new TRCASOS().traduce(idh);
                FuncionesSemanticas.parchea(inst, etq, cod);
            }
            return this;
        }
    }

    private class TCASO {
        public boolean parh;

        public TCASO traduce(String idh) throws IOException {
            parh = false;
            int inst;
            TE0 e0 = new TE0().traduce(parh);
            match(AnalizadorLexico.CategoriaLexica.ENTONCES);
            FuncionesSemanticas.emite("ir_f ", cod);
            inst = etq;
            etq = etq + 1;
            TACS acs = new TACS().traduce(idh);
            err = !e0.tipo.t.equals("integer") || err;
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SINO) {
                FuncionesSemanticas.parchea(inst, etq + 2, cod); //tener en cuenta el else
            } else
                FuncionesSemanticas.parchea(inst, etq + 1, cod);

            return this;
        }
    }

    private class TSENTWHILE {
        public boolean parh;

        public TSENTWHILE traduce(String idh) throws IOException {
            parh = false;
            int inst, sentwhileetqh;
            match(AnalizadorLexico.CategoriaLexica.MIENTRAS);
            sentwhileetqh = etq;
            TE0 e0 = new TE0().traduce(parh);
            match(AnalizadorLexico.CategoriaLexica.HACER);
            inst = etq;
            FuncionesSemanticas.emite("ir_f ", cod);
            etq = etq + 1;
            TACS acs = new TACS().traduce(idh);
            match(AnalizadorLexico.CategoriaLexica.FMIENTRAS);
            err = !e0.tipo.t.equals("integer") || err;
            FuncionesSemanticas.parchea(inst, etq + 1, cod);
            FuncionesSemanticas.emite("ir_a " + sentwhileetqh, cod);
            etq = etq + 1;
            return this;
        }
    }

    private class TACS {

        public TACS traduce(String idh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID || tkAct.cat == AnalizadorLexico.CategoriaLexica.LN ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LR || tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT || tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.SI || tkAct.cat == AnalizadorLexico.CategoriaLexica.MIENTRAS ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.RESERVA || tkAct.cat == AnalizadorLexico.CategoriaLexica.LIBERA ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.RETORNO || tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO ||
                    FuncionesAux.hayOp5(tkAct.cat)) {
                TACCS accs = new TACCS().traduce(idh);
            } else {
                if (((TipoFunc) FuncionesSemanticas.damePropiedades(ts, idh, tkAct.linea, tkAct.columna).tipo).tipoRet.t.equals("integer"))
                    FuncionesSemanticas.emite("apila 0", cod);
                else
                    FuncionesSemanticas.emite("apilaNulo", cod);
                err = false;
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel 0-------------------------------------------------*/

    private class TE0 {
        public Tipo tipo;

        public TE0 traduce(boolean parh) throws IOException {
            Tipo rmemtipoh;
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT) {
                match(AnalizadorLexico.CategoriaLexica.ENT);
                String lex = tkAct.lex;
                int linea = tkAct.linea; //sobra
                int columna = tkAct.columna; //sobra
                match(AnalizadorLexico.CategoriaLexica.ID);
                etq = etq + FuncionesSemanticas.longAccesoVar(FuncionesSemanticas.damePropiedades(ts, lex, tkAct.linea, tkAct.columna));
                cod = FuncionesSemanticas.accesoVar(FuncionesSemanticas.damePropiedades(ts, lex, tkAct.linea, tkAct.columna), cod);
                if (FuncionesSemanticas.existeID(ts, lex, tkAct.linea, tkAct.columna) && (ts.getPropiedades(lex).clase == Propiedades.Clase.var || ts.getPropiedades(lex).clase == Propiedades.Clase.pvar))
                    rmemtipoh = FuncionesSemanticas.ref_(FuncionesSemanticas.damePropiedades(ts, lex, tkAct.linea, tkAct.columna).tipo, ts, tkAct.linea, tkAct.columna);
                else
                    rmemtipoh = new Tipo("err", -1);

                TRMEM rmem = new TRMEM().traduce(rmemtipoh);
                FuncionesSemanticas.emite("copia", cod);
                etq++; // segun doc no esta
                tipo = rmem.tipo/*FuncionesSemanticas.tipoDeEnt(ts, lex, linea, columna)*/;
                modo = Modo.VAL;
                cod = FuncionesSemanticas.genCodEnt(ts, rmem.tipo, cod);
                etq = etq + LONGCODENT;
                // NUEVOOOOOOOOOOOO
                TRE2 re2 = new TRE2().traduce(tipo);
                tipo = re2.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL) {
                match(AnalizadorLexico.CategoriaLexica.SAL);
                //parh = false;	// m... de momento, para que nos ponga el apila_ind si es un designador
                TE1 e1 = new TE1().traduce(parh);
                tipo = FuncionesSemanticas.tipoDeSal(ts, e1.tipo, tkAct.linea, tkAct.columna);
                modo = Modo.VAL;
                if (FuncionesSemanticas.tipoBasico(ts, tipo, tkAct.linea, tkAct.columna) && !parh) {
                    FuncionesSemanticas.emite("apila_ind", cod);
                    etq = etq + 1;
                }//segun el doc sobra todo este if
                cod = FuncionesSemanticas.genCodSal(cod, e1.desig);
                etq = etq + FuncionesSemanticas.longCodSal(e1.desig);
            } else {
                TE1 e1 = new TE1().traduce(parh);
                tipo = e1.tipo;
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel 1-------------------------------------------------*/

    private class TE1 {
        public Tipo tipo;
        public boolean desig;

        public TE1 traduce(boolean parh) throws IOException {

            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lexh = tkAct.lex;
                int lineah = tkAct.linea;
                int columnah = tkAct.columna;    // variables locales que conservan el lexema, fil y col tras consumir ID
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE1 re1 = new TRE1().traduce(lexh, parh, lineah, columnah);
                tipo = re1.tipo;
                desig = re1.desig;
            } else {
                TE5s e5s = new TE5s().traduce(parh);
                TRE4 re4 = new TRE4().traduce(e5s.tipo);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                TRE2 re2 = new TRE2().traduce(re3.tipo);
                tipo = re2.tipo;
                desig = false;
            }
            return this;
        }
    }

    private class TRE1 {
        public Tipo tipo;
        public boolean desig;
        public Tipo rmemtipoh, re4tipoh;

        public TRE1 traduce(String lexh, boolean parh, int lineah, int columnah) throws IOException {

            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB) {
                match(AnalizadorLexico.CategoriaLexica.PAB);
                FuncionesSemanticas.apilaRet(-1, cod);
                int inst = etq;
                etq = etq + LONGAPILARET;
                TARGS args = new TARGS().traduce(((TipoFunc) FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo).params);
                match(AnalizadorLexico.CategoriaLexica.PCE);
                FuncionesSemanticas.emite("ir_a " + (FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).dir/*inicio*/), cod);
                etq = etq + 1;
                FuncionesSemanticas.parchea(inst + 3, etq, cod);
                if (!FuncionesSemanticas.existeID(ts, lexh, lineah, columnah) ||
                        FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase != Propiedades.Clase.fun || err)
                    re4tipoh = new Tipo("err", -1);
                else
                    re4tipoh = FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo;
                TRE4 re4 = new TRE4().traduce(re4tipoh);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                TRE2 re2 = new TRE2().traduce(re3.tipo);
                tipo = re2.tipo;
                modo = Modo.VAL;
                desig = false;
            } else {
                if (FuncionesSemanticas.existeID(ts, lexh, lineah, columnah) &&
                        (FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase == Clase.var ||
                                FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase == Clase.pvar))
                    rmemtipoh = FuncionesSemanticas.ref_(ts.getPropiedades(lexh).tipo, ts, lineah, columnah);
                else
                    rmemtipoh = new Tipo("err", -1);
                etq = etq + FuncionesSemanticas.longAccesoVar(FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna));
                cod = FuncionesSemanticas.accesoVar(FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna), cod);
                TRMEM rmem = new TRMEM().traduce(rmemtipoh);
                if ((FuncionesSemanticas.tipoBasico(ts, rmem.tipo, lineah, columnah) && !parh)/* || (FuncionesSemanticas.damePropiedades(ts, lexh,lineah,columnah).clase == Clase.pvar)*/) {
                    FuncionesSemanticas.emite("apila_ind", cod);
                    etq = etq + 1;
                }
                modo = Modo.VAR;
                TRE1o re1o = new TRE1o().traduce(rmem.tipo, lexh, lineah, columnah);
                tipo = re1o.tipo;
                desig = re1o.desig;
            }
            return this;
        }
    }

    private class TRE1o {
        public Tipo tipo;
        public boolean desig;

        public TRE1o traduce(Tipo tipoh, String lexh, int lineah, int columnah) throws IOException {
            boolean parh = false;
            tipo = tipoh;
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ASIG) {
                // copiamos la direcci�n en la que tendremos que asignar
                FuncionesSemanticas.emite("copia", cod);
                etq++;
                match(AnalizadorLexico.CategoriaLexica.ASIG);
                TE1 e1 = new TE1().traduce(parh);
                tipo = FuncionesSemanticas.tipoDeAsig(ts, tipoh, e1.tipo, lineah, columnah);
                modo = Modo.VAL;
                cod = FuncionesSemanticas.genCodAsig(ts, lexh, cod, tipo, lineah, columnah);
                etq = etq + LONGCODASIG /*+ FuncionesSemanticas.longAccesoVar(ts.getPropiedades(lexh))*/ + FuncionesSemanticas.longCastOp(FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo, tipo);
                desig = false;
            } else {
                if (FuncionesAux.hayOp2(tkAct.cat) || FuncionesAux.hayOp3(tkAct.cat) || FuncionesAux.hayOp4(tkAct.cat)) {
				/*	if(FuncionesSemanticas.existeID(ts, lexh, lineah, columnah))
						tipoh = FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo;  //para ser m�s puritano esto se har�a con un variable fresca
				    else 															 //los param heredados no se les puede dar valor, solo se usan.
				        tipoh = new Tipo("err",-1);*/
                    TRE4 re4 = new TRE4().traduce(tipoh);
                    TRE3 re3 = new TRE3().traduce(re4.tipo);
                    TRE2 re2 = new TRE2().traduce(re3.tipo);
                    tipo = re2.tipo;
                } else
                    desig = true;// segun el doc esto estaria dentro y fuera del if
            }
            return this;
        }
    }
    /*-----------------------------------------Nivel 2---------------------------------------------*/

    private class TRE2 {
        public Tipo tipo;
        public boolean parh;

        public TRE2 traduce(Tipo tipoh) throws IOException {
            if (FuncionesAux.hayOp2(tkAct.cat)) {
                parh = false;
                int fil = tkAct.linea;
                int col = tkAct.columna;
                int etqOp1 = etq;  //Nos sirve para hacer el casting al primer operando
                TOP2 op2 = new TOP2().traduce();
                TRE2o re2o = new TRE2o().traduce(parh);
                modo = Modo.VAL;
                etq = etq + 1;
                etq = etq + FuncionesSemanticas.longCastOp(tipoh, re2o.tipo);
                cod = FuncionesSemanticas.genCodOp(op2.op, cod, tipoh, etqOp1, re2o.tipo);
                tipo = FuncionesSemanticas.tipoDeB(op2.op, tipoh, re2o.tipo, ts, fil, col);
            } else {
                tipo = tipoh;
            }
            return this;
        }
    }

    private class TRE2o {
        public Tipo tipo;

        public TRE2o traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lexh = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE6 re6 = new TRE6().traduce(lexh, parh);
                TRE4 re4 = new TRE4().traduce(re6.tipo);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                tipo = re3.tipo;
            } else {
                TE5s e5s = new TE5s().traduce(parh);
                TRE4 re4 = new TRE4().traduce(e5s.tipo);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                tipo = re3.tipo;
            }
            return this;
        }
    }

    private class TOP2 {
        public String op;

        public TOP2 traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MENOR) {
                match(AnalizadorLexico.CategoriaLexica.MENOR);
                op = "op_Menor";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MAYOR) {
                match(AnalizadorLexico.CategoriaLexica.MAYOR);
                op = "op_Mayor";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MEOIG) {
                match(AnalizadorLexico.CategoriaLexica.MEOIG);
                op = "op_Meoig";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MAOIG) {
                match(AnalizadorLexico.CategoriaLexica.MAOIG);
                op = "op_Maoig";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.IGUAL) {
                match(AnalizadorLexico.CategoriaLexica.IGUAL);
                op = "op_Igual";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.NOIGUAL) {
                match(AnalizadorLexico.CategoriaLexica.NOIGUAL);
                op = "op_NoIgual";
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel 3---------------------------------------------*/

    private class TRE3 {
        public Tipo tipo;
        public boolean parh;

        public TRE3 traduce(Tipo tipoh) throws IOException {
            if (FuncionesAux.hayOp3(tkAct.cat)) {
                int fil = tkAct.linea;
                int col = tkAct.columna;
                int etqOp1 = etq;  //Nos sirve para hacer el casting al primer operando
                TOP3 op3 = new TOP3().traduce();
                parh = false;
                TRE3o re3o = new TRE3o().traduce(parh);
                modo = Modo.VAL;
                etq = etq + 1;
                etq = etq + FuncionesSemanticas.longCastOp(tipoh, re3o.tipo);  //??
                cod = FuncionesSemanticas.genCodOp(op3.op, cod, tipoh, etqOp1, re3o.tipo);
                tipo = FuncionesSemanticas.tipoDeB(op3.op, tipoh, re3o.tipo, ts, fil, col);
            } else {
                tipo = tipoh;
            }
            return this;
        }
    }

    private class TRE3o {
        public Tipo tipo;

        public TRE3o traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lexh = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE6 re6 = new TRE6().traduce(lexh, parh);
                TRE4 re4 = new TRE4().traduce(re6.tipo);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                tipo = re3.tipo;
            } else {
                TE5s e5s = new TE5s().traduce(parh);
                TRE4 re4 = new TRE4().traduce(e5s.tipo);
                TRE3 re3 = new TRE3().traduce(re4.tipo);
                tipo = re3.tipo;
            }
            return this;
        }
    }

    private class TOP3 {
        public String op;

        public TOP3 traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.SUMA) {
                match(AnalizadorLexico.CategoriaLexica.SUMA);
                op = "op_Suma";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.RESTA) {
                match(AnalizadorLexico.CategoriaLexica.RESTA);
                op = "op_Resta";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.O) {
                match(AnalizadorLexico.CategoriaLexica.O);
                op = "op_O";
            }
            return this;
        }
    }
    /*-----------------------------------------Nivel 4---------------------------------------------*/

    private class TRE4 {
        public Tipo tipo;
        public boolean parh;

        public TRE4 traduce(Tipo tipoh) throws IOException {
            if (FuncionesAux.hayOp4(tkAct.cat)) {
                int fil = tkAct.linea;
                int col = tkAct.columna;
                int etqOp1 = etq;  //Nos sirve para hacer el casting al primer operando
                TOP4 op4 = new TOP4().traduce();
                parh = false;
                TRE4o re4o = new TRE4o().traduce(parh);
                modo = Modo.VAL;
                etq = etq + 1;
                etq = etq + FuncionesSemanticas.longCastOp(tipoh, re4o.tipo);
                cod = FuncionesSemanticas.genCodOp(op4.op, cod, tipoh, etqOp1, re4o.tipo);
                tipo = FuncionesSemanticas.tipoDeB(op4.op, tipoh, re4o.tipo, ts, fil, col);
            } else {
                tipo = tipoh;
            }
            return this;
        }
    }

    private class TRE4o {
        public Tipo tipo;

        public TRE4o traduce(boolean parh) throws IOException {

            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lex = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE6 re6 = new TRE6().traduce(lex, parh);
                TRE4 re4 = new TRE4().traduce(re6.tipo);
                tipo = re4.tipo;
            } else {
                TE5s e5s = new TE5s().traduce(parh);
                TRE4 re4 = new TRE4().traduce(e5s.tipo);
                tipo = re4.tipo;
            }
            return this;
        }
    }

    private class TOP4 {
        public String op;

        public TOP4 traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.POR) {
                match(AnalizadorLexico.CategoriaLexica.POR);
                op = "op_Por";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.DIV) {
                match(AnalizadorLexico.CategoriaLexica.DIV);
                op = "op_Div";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.Y) {
                match(AnalizadorLexico.CategoriaLexica.Y);
                op = "op_Y";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.MOD) {
                match(AnalizadorLexico.CategoriaLexica.MOD);
                op = "op_Mod";
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel 5---------------------------------------------*/

    private class TE5s {
        public Tipo tipo;

        public TE5s traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.LN ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LR ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO) {
                TE6s e6s = new TE6s().traduce(parh);
                tipo = e6s.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.RESTA ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NO) {
                int linea = tkAct.linea;
                int col = tkAct.columna;
                TOP5 op5 = new TOP5().traduce();
                parh = false;
                TE5o e5o = new TE5o().traduce(parh);
                tipo = FuncionesSemanticas.tipoDeU(op5.op, e5o.tipo, linea, col);
                cod = FuncionesSemanticas.genCodOp(op5.op, cod);
                etq = etq + 1;
                // falta modo = Modo.VAL segun el doc
            } else {
                match(AnalizadorLexico.CategoriaLexica.PAB);
                TRE5s re5s = new TRE5s().traduce(parh);
                tipo = re5s.tipo;
            }
            return this;
        }
    }

    private class TE5o {
        public Tipo tipo;

        public TE5o traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lex = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE6 re6 = new TRE6().traduce(lex, parh);
                tipo = re6.tipo;
            } else {
                TE5s e5s = new TE5s().traduce(parh);
                tipo = e5s.tipo;
            }
            return this;
        }
    }

    private class TRE5s {
        public Tipo tipo;

        public TRE5s traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL) {
                int linea = tkAct.linea;
                int col = tkAct.columna;
                TRCAS rcas = new TRCAS().traduce();
                TRE5o re5o = new TRE5o().traduce(parh);
                modo = Modo.VAL;
                tipo = FuncionesSemanticas.tipoDeU(rcas.op, re5o.tipo, linea, col);
                cod = FuncionesSemanticas.genCodOp(rcas.op, cod);
                etq = etq + 1;
            } else {
                TE0 e0 = new TE0().traduce(parh);
                match(AnalizadorLexico.CategoriaLexica.PCE);
                tipo = e0.tipo;
            }
            return this;
        }
    }

    private class TRE5o {
        public Tipo tipo;

        public TRE5o traduce(boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ID) {
                String lex = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                TRE6 re6 = new TRE6().traduce(lex, parh);
                tipo = re6.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB) {
                match(AnalizadorLexico.CategoriaLexica.PAB);
                TE0 e0 = new TE0().traduce(parh);
                match(AnalizadorLexico.CategoriaLexica.PCE);
                tipo = e0.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.LN ||
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LR) {
                TE6s e6s = new TE6s().traduce(parh);
                tipo = e6s.tipo;
            }
            return this;
        }
    }


    private class TRCAS {
        public String op;

        public TRCAS traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.INTEGER) {
                match(AnalizadorLexico.CategoriaLexica.INTEGER);
                op = "op_CasInt";
                match(AnalizadorLexico.CategoriaLexica.PCE);
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.REAL) {
                match(AnalizadorLexico.CategoriaLexica.REAL);
                op = "op_CasReal";
                match(AnalizadorLexico.CategoriaLexica.PCE);
            }
            return this;
        }
    }


    private class TOP5 {
        public String op;

        public TOP5 traduce() throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.RESTA) {
                match(AnalizadorLexico.CategoriaLexica.RESTA);
                op = "op_Neg";
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.NO) {
                match(AnalizadorLexico.CategoriaLexica.NO);
                op = "op_No";
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel 6---------------------------------------------*/

    private class TE6s {
        public Tipo tipo;

        public TE6s traduce(boolean parh) throws IOException {    //posiblemente sobre parh!! mirar en el doc tmb
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.LN) {
                tipo = new Tipo("integer", 1);                                                                //seguro que es Integer? y no Int //La verdad no lo se ahora mismo
                modo = Modo.VAL;
                FuncionesSemanticas.emite("apilaEntero " + FuncionesSemanticas.valorDeN(tkAct.lex), cod);
                match(AnalizadorLexico.CategoriaLexica.LN);
                etq = etq + 1;
            } else if ((tkAct.cat == AnalizadorLexico.CategoriaLexica.LR)) {
                tipo = new Tipo("real", 1);
                modo = Modo.VAL;
                FuncionesSemanticas.emite("apilaReal " + FuncionesSemanticas.valorDeR(tkAct.lex), cod);
                match(AnalizadorLexico.CategoriaLexica.LR);
                etq = etq + 1;
            } else if ((tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO)) {
                tipo = new Tipo("nulo", 1);
                modo = Modo.VAL;
                FuncionesSemanticas.emite("apilaNulo", cod);
                match(AnalizadorLexico.CategoriaLexica.NULO);
                etq = etq + 1;
            }
            //else: imposible
            return this;
        }
    }

    private class TRE6 {
        public Tipo tipo;
        private int etqs1;  //instrucci�n a parchear

        public TRE6 traduce(String lexh, boolean parh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB) {
                int linea = tkAct.linea;
                int col = tkAct.columna;
                match(AnalizadorLexico.CategoriaLexica.PAB);
                List<Param> argsFParamh = ((TipoFunc) FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo).params;
                FuncionesSemanticas.apilaRet(dir, cod);
                etq = etq + LONGAPILARET;
                etqs1 = etq - 2; //para luego parchear la direcci�n de retorno una vez sepamos cual es.
                TARGS args = new TARGS().traduce(argsFParamh);
                match(AnalizadorLexico.CategoriaLexica.PCE);
                if (args.err || !FuncionesSemanticas.existeID(ts, lexh, linea, col) || FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase != Clase.fun)
                    tipo = new Tipo("err", -1);
                else
                    tipo = FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).tipo;
                modo = Modo.VAL;
                FuncionesSemanticas.parchea(etqs1, etq, cod);
                FuncionesSemanticas.emite("ir_a " + FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).dir/*inicio*/, cod);
                etq = etq + 1;
            } else {
                Tipo rMemTipoh;
                if (FuncionesSemanticas.existeID(ts, lexh, tkAct.linea, tkAct.columna) && FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase == Clase.var)
                    rMemTipoh = FuncionesSemanticas.ref_(ts.getPropiedades(lexh).tipo, ts, tkAct.linea, tkAct.columna);
                else
                    rMemTipoh = new Tipo("err", -1);

                FuncionesSemanticas.accesoVar(FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna), cod);
                etq = etq + FuncionesSemanticas.longAccesoVar(FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna));
                TRMEM rmem = new TRMEM().traduce(rMemTipoh);
                tipo = rmem.tipo;
                modo = Modo.VAR;
                if ((FuncionesSemanticas.tipoBasico(ts, tipo, tkAct.linea, tkAct.columna) && !parh)/* || (FuncionesSemanticas.damePropiedades(ts, lexh, tkAct.linea, tkAct.columna).clase == Clase.pvar)*/) {
                    FuncionesSemanticas.emite("apila_ind", cod);
                    etq = etq + 1;
                }
            }
            return this;
        }
    }

    /*-----------------------------------------Nivel Superior---------------------------------------------*/

    private class TRMEM {
        public Tipo tipo;

        public TRMEM traduce(Tipo tipoh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ACC) {
                match(AnalizadorLexico.CategoriaLexica.ACC);
                String lex = tkAct.lex;
                match(AnalizadorLexico.CategoriaLexica.ID);
                Tipo rMem1tipoh;
                if (FuncionesSemanticas.ref_(tipoh, ts, tkAct.linea, tkAct.columna).t.equals("reg") && FuncionesSemanticas.campoDe(tipoh, ts, lex, tkAct.linea, tkAct.columna))
                    rMem1tipoh = FuncionesSemanticas.tipoDeCampo(tipoh, ts, lex, tkAct.linea, tkAct.columna);
                else
                    rMem1tipoh = new Tipo("err", -1);
                cod = FuncionesSemanticas.genCodDesig("Registro", cod, tipoh, ts, lex, tkAct.linea, tkAct.columna);
                etq = etq + LONGACCESOREG;  //2 instrucciones para hacer un acceder a un campo de un registro (despl + suma)
                TRMEM rmem = new TRMEM().traduce(rMem1tipoh);
                tipo = rmem.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.IND) {
                match(AnalizadorLexico.CategoriaLexica.IND);
                Tipo rMem1tipoh;
                if (FuncionesSemanticas.ref_(tipoh, ts, tkAct.linea, tkAct.columna).t.equals("punt"))
                    rMem1tipoh = ((TipoPunt) FuncionesSemanticas.ref_(tipoh, ts, tkAct.linea, tkAct.columna)).tBase;
                else
                    rMem1tipoh = new Tipo("err", -1);
                cod = FuncionesSemanticas.genCodDesig("Indireccion", cod, rMem1tipoh, ts, "", tkAct.linea, tkAct.columna);
                etq = etq + LONGACCESOPUNTERO;  //1 instrucciones para hacer un acceder a un puntero. desapilaInd
                TRMEM rmem = new TRMEM().traduce(rMem1tipoh);
                tipo = rmem.tipo;
            } else if (tkAct.cat == AnalizadorLexico.CategoriaLexica.CORAB) {
                match(AnalizadorLexico.CategoriaLexica.CORAB);
                boolean e0parh = false;  //no queremos una direcci�n sino un valor num�rico
                TE0 e0 = new TE0().traduce(e0parh);
                match(AnalizadorLexico.CategoriaLexica.CORCE);
                Tipo rMem1tipoh;
                if (FuncionesSemanticas.ref_(tipoh, ts, tkAct.linea, tkAct.columna).t.equals("array") && FuncionesSemanticas.ref_(e0.tipo, ts, tkAct.linea, tkAct.columna).t == "integer")
                    rMem1tipoh = ((TipoArray) FuncionesSemanticas.ref_(tipoh, ts, tkAct.linea, tkAct.columna)).tBase;
                else
                    rMem1tipoh = new Tipo("err", -1);
                cod = FuncionesSemanticas.genCodDesig("Array", cod, tipoh, ts, "", tkAct.linea, tkAct.columna);
                etq = etq + LONGACCESOARRAY; //3 instrucciones para acceder a un elemento de un array.
                TRMEM rmem = new TRMEM().traduce(rMem1tipoh);
                tipo = rmem.tipo;
            } else {
                tipo = tipoh;
            }

            return this;
        }
    }

    private class TARGS {
        public boolean err;

        public TARGS traduce(List<Param> fParamh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT || tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL || //respecto a E0
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ID || tkAct.cat == AnalizadorLexico.CategoriaLexica.RESTA ||  //respecto a E1 y E5s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NO || tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB ||    //respecto a E5s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LN || tkAct.cat == AnalizadorLexico.CategoriaLexica.LR ||    //respecto a E6s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO) {    //respecto a E6s

                cod = FuncionesSemanticas.iniciaPaso(cod);
                etq = etq + LONGINICIOPASO;
                TLARGS largs = new TLARGS().traduce(fParamh);
                err = largs.err || fParamh.size() != largs.nParam;
                cod = FuncionesSemanticas.finPaso(cod);
                etq = etq + LONGFINPASO;
            } else {
                err = fParamh.size() > 0;
            }

            return this;
        }
    }

    private class TLARGS {
        public boolean err;
        public int nParam;

        public TLARGS traduce(List<Param> fParamh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.ENT || tkAct.cat == AnalizadorLexico.CategoriaLexica.SAL || //respecto a E0
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.ID || tkAct.cat == AnalizadorLexico.CategoriaLexica.RESTA ||  //respecto a E1 y E5s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NO || tkAct.cat == AnalizadorLexico.CategoriaLexica.PAB ||    //respecto a E5s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.LN || tkAct.cat == AnalizadorLexico.CategoriaLexica.LR ||    //respecto a E6s
                    tkAct.cat == AnalizadorLexico.CategoriaLexica.NULO) {    //respecto a E6s
                boolean e0parh = true;
                cod = FuncionesSemanticas.emite("copia", cod);
                etq = etq + 1;
                TE0 e0 = new TE0().traduce(e0parh);
                err = fParamh.size() == 0 || (fParamh.get(0).modo == Modo.VAR && modo == Modo.VAL) || !FuncionesSemanticas.tipoCompatible(fParamh.get(0).tipo, e0.tipo, ts);
                int nParamh = 1;
                cod = FuncionesSemanticas.pasoParametro(modo, fParamh.get(0), cod);
                etq = etq /*+ 1*/ + FuncionesSemanticas.LONGPASOPARAMETRO(modo, fParamh.get(0));
                TRLARGS rlargs = new TRLARGS().traduce(nParamh, fParamh);
                nParam = rlargs.nParam;
            }
            return this;
        }
    }

    private class TRLARGS {
        public boolean err;
        public int nParam;

        public TRLARGS traduce(int nParamh, List<Param> fParamh) throws IOException {
            if (tkAct.cat == AnalizadorLexico.CategoriaLexica.COMA) {    //respecto a E6s
                match(AnalizadorLexico.CategoriaLexica.COMA);
                boolean e0parh = fParamh.get(nParamh).modo == Modo.VAR;
                cod = FuncionesSemanticas.emite("copia", cod);
                cod = FuncionesSemanticas.direccionParFormal(fParamh.get(nParamh), cod);
                etq = etq + 1 + LONGDIRECCIONPARFORMAL;
                TE0 e0 = new TE0().traduce(e0parh);
                err = err || fParamh.size() < nParam || (fParamh.get(nParamh).modo == Modo.VAR && modo == Modo.VAL) || !FuncionesSemanticas.tipoCompatible(fParamh.get(nParamh).tipo, e0.tipo, ts);
                int rLargsNParamh = nParamh + 1;
                cod = FuncionesSemanticas.pasoParametro(modo, fParamh.get(nParamh), cod);
                etq = etq /*+ 1*/ + +FuncionesSemanticas.LONGPASOPARAMETRO(modo, fParamh.get(0));
                ;
                TRLARGS rlargs = new TRLARGS().traduce(rLargsNParamh, fParamh);
                nParam = rlargs.nParam;
            } else {
                nParam = nParamh;
            }

            return this;
        }
    }
}
	


	
