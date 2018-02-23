package compiler;
/**
 *
 */

import java.io.*;
import java.util.List;
import java.util.Scanner;


/**
 * Clase principal desde donde se ejecuta el traductor.
 *
 * @author javieku
 */
public class Aplicacion {

    private static boolean generaCod = true;

    /**
     * Funci�n que traduce el archivo especificado.
     *
     * @param pathO Path del programa fuente.
     * @param pathD Path donde se genera el programa objeto.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void traduce(String pathO, String pathD) throws FileNotFoundException, IOException {

        generaCod = true;
        AnalizadorLexico al = new AnalizadorLexico(new InputStreamReader(new FileInputStream(new File(pathO))));
        Traductor t = new Traductor(al);
        List<Instruccion> l = t.INICIO();

        if (generaCod) {
            try {
                FileOutputStream fileOut = new FileOutputStream(pathD);
                ObjectOutputStream salida = new ObjectOutputStream(fileOut);
                salida.writeObject(l);
                salida.close();
            } catch (FileNotFoundException e) {
                System.err.println("No se encontr� el fichero en la ruta especificada.");
            } catch (IOException e) {
                System.err.println("Error E/S");
            }
            // fichero de texto to debug!!.
			/*PrintWriter fichSalida = null;
			try {
				 fichSalida = new PrintWriter(new FileWriter("InstruccionesGeneradas.txt"));
				 Iterator<Instruccion> it = l.iterator();
				 while(it.hasNext()){
					 fichSalida.println(it.next());
				 }
			}catch (IOException  e1) {
				System.err.println("No se encontr� el fichero en la ruta especificada.");			 	 
			} finally{
				 if (fichSalida != null)
					 	fichSalida.close();	 
			}*/
        } else
            System.exit(1);
    }

    /**
     * Funci�n inhibe la generaci�n del fichero con el c�digo fuente en caso de error
     * contextual o l�xico.
     */
    public static void inhibeGenCod() {
        generaCod = false;
    }

    public static boolean getGeneraCod() {
        return generaCod;
    }

    private static void muestraAyuda() {
        System.out.println("Uso: traductor.jar fichero_fuente fichero_destino");
    }

    /**
     * Parsea la entrada para obtener las opciones elegidas por el usuario.
     */
    public static void main(String[] args) {
        if (args.length < 2)
            System.out.println("Pruebe '-h' para obtener mas informacion.");
        else {
            if (args[0].charAt(0) != '-') {
                Aplicacion compilador = new Aplicacion();
                try {
                    compilador.traduce(args[0], args[1]);
                } catch (FileNotFoundException e) {
                    System.err.println("Error No se encontro el fichero en la ruta especificada.");
                } catch (IOException e) {
                    System.err.println("Error E/S");
                }
                System.out.print("Pulse intro para finalizar.");
                Scanner scanEntrada = new Scanner(System.in);
                scanEntrada.nextLine();
            } else {
                if (!args[0].equals("-h"))
                    System.out.println("Opci�n incorrecta");
                else
                    muestraAyuda();
            }
        }
    }

}
	