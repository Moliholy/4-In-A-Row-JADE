/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.AgentAction;

/**
 *
 * @author pedroj
 */
public class PosicionarFicha implements AgentAction {
    
    private Partida partida;
    private Movimiento anterior;
    
    public PosicionarFicha () {
        
    }

    public PosicionarFicha(Partida partida, Movimiento anterior) {
        this.partida = partida;
        this.anterior = anterior;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Movimiento getAnterior() {
        return anterior;
    }

    public void setAnterior(Movimiento anterior) {
        this.anterior = anterior;
    }
}
