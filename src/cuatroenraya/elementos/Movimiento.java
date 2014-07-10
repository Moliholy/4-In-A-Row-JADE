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
public class Movimiento implements Concept {
    
    private Ficha ficha;
    private Posicion posicion;

    public Movimiento () {
        
    }
    
    public Movimiento(Ficha ficha, Posicion posicion) {
        this.ficha = ficha;
        this.posicion = posicion;
    }

    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }

    public Ficha getFicha() {
        return ficha;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    public Posicion getPosicion() {
        return posicion;
    }
}
