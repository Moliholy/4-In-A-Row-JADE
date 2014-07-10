/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.Concept;

/**
 *
 * @author pedroj
 */
public class Tablero implements Concept {
    
    private int numFilas;
    private int numColumnas;
    
    public Tablero () {
        
    }

    public Tablero(int numFilas, int numColumnas) {
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
    }

    public void setNumFilas(int numFilas) {
        this.numFilas = numFilas;
    }

    public int getNumFilas() {
        return numFilas;
    }
    
    public void setNumColumnas(int numColumnas) {
        this.numColumnas = numColumnas;
    }

    public int getNumColumnas() {
        return numColumnas;
    }
    
    @Override
    public String toString() {
        return "Tablero con: " + numFilas + " filas y " + numColumnas + " columnas";
    }
}
