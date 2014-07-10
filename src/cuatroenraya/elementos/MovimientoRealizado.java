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
public class MovimientoRealizado implements Predicate {

    private Jugador jugador;
    private Movimiento movimiento;
    
    public MovimientoRealizado () {
        
    }

    public MovimientoRealizado(Jugador jugador, Movimiento movimiento) {
        this.jugador = jugador;
        this.movimiento = movimiento;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Movimiento getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(Movimiento movimiento) {
        this.movimiento = movimiento;
    }
}
