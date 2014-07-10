/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.Concept;
import jade.core.AID;

/**
 *
 * @author pedroj
 */
public class Jugador implements Concept {
    
    private AID jugador;
    private Ficha ficha;
    
    public Jugador () {
        
    }

    public Jugador(AID jugador, Ficha ficha) {
        this.jugador = jugador;
        this.ficha = ficha;
    }
    
    public void setJugador(AID jugador) {
        this.jugador = jugador;
    }

    public AID getJugador() {
        return jugador;
    }

    public Ficha getFicha() {
        return ficha;
    }

    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }
    
}
