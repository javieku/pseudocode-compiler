package compiler;

import compiler.AnalizadorLexico.CategoriaLexica;

/**
 * Clases que mejoran la visibilidad del c�digo para la comprobaci�n del siguiente token.
 */
public class FuncionesAux {

    public static boolean hayOp2(CategoriaLexica cat) {
        return (cat == AnalizadorLexico.CategoriaLexica.MENOR || cat == AnalizadorLexico.CategoriaLexica.MAYOR ||
                cat == AnalizadorLexico.CategoriaLexica.MEOIG || cat == AnalizadorLexico.CategoriaLexica.MAOIG ||
                cat == AnalizadorLexico.CategoriaLexica.IGUAL || cat == AnalizadorLexico.CategoriaLexica.NOIGUAL);
    }

    public static boolean hayOp3(CategoriaLexica cat) {
        return (cat == AnalizadorLexico.CategoriaLexica.SUMA || cat == AnalizadorLexico.CategoriaLexica.RESTA ||
                cat == AnalizadorLexico.CategoriaLexica.O);
    }

    public static boolean hayOp4(CategoriaLexica cat) {
        return (cat == AnalizadorLexico.CategoriaLexica.POR || cat == AnalizadorLexico.CategoriaLexica.DIV ||
                cat == AnalizadorLexico.CategoriaLexica.Y || cat == AnalizadorLexico.CategoriaLexica.MOD);
    }

    public static boolean hayOp5(CategoriaLexica cat) {
        return (cat == AnalizadorLexico.CategoriaLexica.NO || cat == AnalizadorLexico.CategoriaLexica.RESTA);
    }

}
