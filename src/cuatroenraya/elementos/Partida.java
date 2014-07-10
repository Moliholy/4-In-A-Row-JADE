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
public class Partida implements Concept {
    
    private String id;
    private Juego juego;
    
    public Partida () {
        
    }

    public Partida(String id, Juego juego) {
        this.id = id;
        this.juego = juego;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public Juego getJuego() {
        return juego;
    }
}
