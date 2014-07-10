/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;

/**
 *
 * @author pedroj
 */
public class Jugar implements AgentAction {
    
    private Partida partida;
    private Tablero tablero;
    private Jugador oponente;
    
    public Jugar () {
        
    }

    public Jugar(Partida partida, Tablero tablero) {
        this.partida = partida;
        this.tablero = tablero;
    }

    public Jugar(Partida partida, Tablero tablero, Jugador oponente) {
        this.partida = partida;
        this.tablero = tablero;
        this.oponente = oponente;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    @Slot(mandatory=false)
    public Jugador getOponente() {
        return oponente;
    }

    public void setOponente(Jugador oponente) {
        this.oponente = oponente;
    }
}
