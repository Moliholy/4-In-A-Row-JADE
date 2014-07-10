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
public class Posicion implements Concept {
    
    private int fila;
    private int columna;
    
    public Posicion () {
        
    }

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getFila() {
        return fila;
    }
    
    public void setColumna(int columna) {
        this.columna = columna;
    }

    public int getColumna() {
        return columna;
    }
    
}
