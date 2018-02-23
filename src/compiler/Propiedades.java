package compiler;

import compiler.Traductor.Modo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa el registro de propiedades asociado a un id en la tabla de s�mbolos.
 *
 * @author javieku & Vlad & Others
 */
public class Propiedades {

    public static enum Clase {tipo, fun, var, pvar}

    ;

    /**
     * Direcci�n en la memoria de datos.
     */
    public int dir;

    /**
     * Tipo de la variable correspondiente.
     */
    public Tipo tipo;

    /**
     * Clase de identificador
     */
    public Clase clase;

    /**
     * Profundidad del bloque que ha sido declarada
     */
    public int nivel;

    /**
     * Direcci�n de comienzo de la funci�n
     */
    //public int inicio;

    /**
     * Consructora de un registro sin saber su nivel(m�s adelante se a�ade).
     *
     * @param tipo
     * @param dir
     * @param clase
     */
    public Propiedades(Tipo tipo, int dir, Clase clase) {
        this.dir = dir;
        this.tipo = tipo;
        this.clase = clase;
        this.nivel = 0;  //por defecto.
        //this.inicio = -1;
    }

    /**
     * Constructora de un registro sabiendo tambi�n el nivel
     *
     * @param tipo
     * @param dir
     * @param clase
     * @param nivel
     */
    public Propiedades(Tipo tipo, int dir, Clase clase, int nivel) {
        this.dir = dir;
        this.tipo = tipo;
        this.clase = clase;
        this.nivel = nivel;
        //this.inicio = -1;
    }

    /**
     * Constructora de una funci�n sabiendo el comienzo de su secci�n de acciones
     *
     * @param tipo
     * @param dir
     * @param clase
     * @param nivel
     * @param params
     */
	/*
	public Propiedades(Tipo tipo, int dir, Clase clase, int nivel, int inicio) {
		this.dir = dir;
		this.tipo = tipo;
		this.clase = clase;
		this.nivel = nivel;
		this.inicio = inicio;
	}*/
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public void setInicio(int dir) {
        this.dir = dir;
    }
}

/*************************************************************************************/

/**
 * Clase que representa tipo b�sico (int, real) de la que heredan lo dem�s tipos construidos
 */
class Tipo {

    // El nombre del tipo. E.D. reg, punt, array, ref.
    public String t;

    // El tama�o del tipo(cuantas celdas en memoria ocupa)
    public int tam;

    public Tipo(String tipo, int tam) {
        this.t = tipo;
        this.tam = tam;
    }
}
/*************************************************************************************/

/**
 * Clase para tipos puntero.
 */
class TipoPunt extends Tipo {

    //Este tipo base puede no contener el tama�o correcto si es que todav�a no se ha declarado
    public Tipo tBase;

    public TipoPunt(Tipo tBase) {
        super("punt", 1); // un puntero siempre tiene tama�o 1
        this.tBase = tBase;
    }

}

/*************************************************************************************/

/**
 * Clase para tipo referencia
 */
class TipoRef extends Tipo {

    //Nombre del id al que hace referencia
    public String nombre;

    public TipoRef(int tam, String nombre) {
        super("ref", tam);
        this.nombre = nombre;
    }
}

/*************************************************************************************/

/**
 * Clase para tipo Registro
 */
class TipoReg extends Tipo {

    public List<Campo> campos;

    //Crear la clase sin los campos y luego a�adirlos con addCampo
    public TipoReg(int tam) {
        super("reg", tam);
        campos = new ArrayList<Campo>();
    }

    //Crear la clase con la lista de campos ya rellenada
    public TipoReg(int tam, List<Campo> campos) {
        super("reg", tam);
        this.campos = campos;
    }

    // Por si se necesita introducir campos m�s adelante
    public void addCampo(String nombre, Tipo tipo, int despl) {
        if (despl == 0) //por si desconocemos el desplazamiento, lo calculamos con el �ltimo campo
            despl = campos.get(campos.size()).despl + campos.get(campos.size()).tipo.tam;
        campos.add(new Campo(nombre, tipo, despl));
        tam = tam + (tipo.tam);
    }
}

/*************************************************************************************/

/**
 * Clase que representa un campo de un registro.
 */
class Campo {

    public String nombre;

    public Tipo tipo;

    public int despl;

    public Campo(String nombre, Tipo tipo, int despl) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.despl = despl;
    }
}

/*************************************************************************************/

/**
 * Clase para los arrays
 */
class TipoArray extends Tipo {

    //N�mero de elementos de tipo tBase que tiene el array
    public int nElem;

    public Tipo tBase;

    public TipoArray(int tam, int nElem, Tipo tBase) {
        super("array", tam);
        this.nElem = nElem;
        this.tBase = tBase;
    }
}

	/* Javi:Cambiada
	 * class TipoFunc extends Tipo{
		
		//Lista de par�metros que tiene la funci�n
		public List<Param> params;
		
		//El tipo y tama�o son los correspondientes a lo que devuelve la funci�n
		public TipoFunc(String tipoRet, int tamRet, List<Param> params) {
			super(tipoRet, tamRet);
			this.params = params;
		}
	}*/
/*************************************************************************************/

/**
 * Clase para las funciones
 */
class TipoFunc extends Tipo {

    //Lista de par�metros que tiene la funci�n
    public List<Param> params;

    public Tipo tipoRet;

    //El tipo y tama�o son los correspondientes a lo que devuelve la funci�n
    public TipoFunc(Tipo tipoRet, int tamRet, List<Param> params) {
        super(tipoRet.t, tamRet);
        this.params = params;
        this.tipoRet = tipoRet;
    }
}

/*************************************************************************************/

/**
 * Clase que representa un par�metro de una funci�n
 */
class Param {

    public Modo modo;
    public Tipo tipo;
    public int dir;        //dir para cuando se realice la llamada

    public Param(Modo modo, Tipo tipo, int dir) {
        this.modo = modo;
        this.tipo = tipo;
        this.dir = dir;
    }
}

