/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.Predicate;

/**
 *
 * @author pedroj
 */
public class Ganador implements Predicate {
    
    private Partida partida;
    private Jugador jugador;
    
    public Ganador () {
        
    }

    public Ganador(Partida partida, Jugador jugador) {
        this.partida = partida;
        this.jugador = jugador;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }
}
