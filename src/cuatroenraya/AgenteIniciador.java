/**
 *
 * Agente de prueba 
 * 
 */
package cuatroenraya;

import cuatroenraya.elementos.Ficha;
import cuatroenraya.elementos.Ganador;
import cuatroenraya.elementos.Juego;
import cuatroenraya.elementos.Jugador;
import cuatroenraya.elementos.Jugar;
import cuatroenraya.elementos.Partida;
import cuatroenraya.elementos.Tablero;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simularemos el agente que inicia el protocolo FIPA Contrac Net
 * para buscar jugadores que quieran jugar al Cuatro en Raya.
 * 
 * Para la prueba los agentes que participan en la prueba deben pasarse como
 * parámetros cuando se crea este agente.
 * 
 * También se simulará la tarea que indicará el jugador que ha ganado la partida.
 * @author pedroj
 */
public class AgenteIniciador extends Agent {	
    private int nResponders;
    
    private Vector receptores = new Vector();
    
    //Jugadores de la partida activa
    private Jugador []participantes = new Jugador[2]; 
    private int respuestas = 0;
    
    private Ficha fichaRoja = new Ficha(OntologiaCuatroEnRaya.FICHA_ROJA);
    private Ficha fichaAzul = new Ficha(OntologiaCuatroEnRaya.FICHA_AZUL);
        
    private ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;
	
    /**
     * Inicialización del agente donde comprobamos que tenemos conocimiento
     * de los agentes con los que podrá contactar y realizará las dos tareas 
     * que queremos probar.
     */
    @Override
    protected void setup() { 
        //Obtenemos la instancia de la ontología y registramos el lenguaje
        //y la ontología para poder completar el contenido de los mensajes
        try {
            ontology = OntologiaCuatroEnRaya.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(PruebaOntologia.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontology);
        
        //Inicializamos las fichas para los jugadores
        participantes[0] = new Jugador();
        participantes[0].setFicha(fichaRoja);
                
        participantes[1] = new Jugador();
        participantes[1].setFicha(fichaAzul);
        
  	//Buscamos a los participantes en las páginas amarillas
        try {
            //Buscamos a los agentes participantes
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType(OntologiaCuatroEnRaya.REGISTRO_JUGADOR);
            template.addServices(templateSd);
  		
            SearchConstraints sc = new SearchConstraints();
            //Buscaremos 5 como máximo
            sc.setMaxResults(new Long(5));
  		
            DFAgentDescription[] results = DFService.search(this, template, sc);
     
            if (results.length > 1) {
  		nResponders = results.length;
                for (int i = 0; i < results.length; ++i) {
  			DFAgentDescription dfd = results[i];
                        receptores.add(dfd.getName());
  		}
                
                // Creamos el mensaje a enviar en el CFP
  		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                Enumeration elm = receptores.elements();
                while (elm.hasMoreElements()) {
  			msg.addReceiver((AID) elm.nextElement());
  		} 
       
                msg.setSender(getAID());
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		// Esperamos respuesta por 10seg.
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                
                Tablero tablero = new Tablero(6,7); // prueba del tablero
                Juego juego = new Juego(OntologiaCuatroEnRaya.TIPO_JUEGO); // creamos el juego
                Partida partida = new Partida("Partida de prueba", juego); // creamos la partida        
                Jugar jugar = new Jugar(partida,tablero); // Creamos el cuerpo de la acción
                
                msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());

		//Creación de la acción Jugar que se enviará
		Action a = new Action(getAID(), jugar);
                manager.fillContent(msg, a);
                
                //Implementación del protocolo ContractNet
                System.out.println("Empezamos con la prueba ......");
                addBehaviour(new IniciarJuego(this, msg, partida));
                
            } else {
       
                System.out.println("Necesitamos dos agentes para la prueba. ");
                doDelete();
            }
         }
          catch (Exception ex) {
  		ex.printStackTrace();
         }                    
  } 
    
    @Override
    protected void takeDown() {
        System.out.println("\n" + getName() + " Terminadas las tareas ...");
    }
    
    class IniciarJuego extends ContractNetInitiator {
        
        private Partida partida; //Partida que se está jugando
        
        public IniciarJuego(Agent agent, ACLMessage message, Partida partida) {
            super(agent,message);
            this.partida = partida;
        }
        
        protected void handlePropose(ACLMessage propose, Vector v) {
					
                        System.out.println("El agente "+propose.getSender().getName()+" propone "+propose.getContent());
                        if (respuestas < 2) {
                            //Los dos primeros en responder se almacenan para jugar
                            participantes[respuestas].setJugador(propose.getSender());
                            respuestas++;
                        }
                    }
				
				
                    protected void handleRefuse(ACLMessage refuse) {
					
                        System.out.println("El agente "+refuse.getSender().getName()+" reusa");
				
                    }
				
				
                    protected void handleFailure(ACLMessage failure) {
			
                        if (failure.getSender().equals(myAgent.getAMS())) {
			
                            // Fallo en el envío al no encontrar al agente participante
                            System.out.println("No se ha encontrado el agente participante");
					
                        }
			else {
                            System.out.println("El agente "+failure.getSender().getName()+" falla");
			}
                            // Immediate failure --> we will not receive a response from this agent
                            nResponders--;
                    }
				
				
                    protected void handleAllResponses(Vector responses, Vector acceptances) {
					
                        if (responses.size() < nResponders) {
                            //No todos agentes han contestado en el tiempo
                            System.out.println("Timeout agotado: perdidos "+(nResponders - responses.size())+" participantes");
			}
					
                        // Seleccionamos a los dos primeros que hayan contestado
                        int i = 0;
                        int j = 1; //Para localizar al oponente
			Enumeration e = responses.elements();
			while (e.hasMoreElements()) {
                            ACLMessage msg = (ACLMessage) e.nextElement();
                            ACLMessage reply = msg.createReply();
                            
                            if (i<2) {
                                try {
                                    Action a = (Action) manager.extractContent(msg);
                                    Jugar jugar = (Jugar) a.getAction();
                                    jugar.setOponente(participantes[j]); //Incluimos el oponente
                                
                                    a.setAction(jugar); //La incluimos en la acción
                                    manager.fillContent(reply, a); // Y en el mensaja

                                
                                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    acceptances.addElement(reply);   
                                    j--;
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            
                            } else {
                                //Rechazamos si ya tenemos a dos
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply);
                            }
                            
                            i++;
                        }
                    }				
				
                    protected void handleInform(ACLMessage inform) {
                        System.out.println("El agente "+inform.getSender().getName()+" responde afirmativamente a la realización del juego");
                        System.out.println(inform);
                    }
                    
                    protected void handleAllResultNotifications(Vector resultNotifications) {
                        //Simulación para la comuniciación del ganador
                        //Cuando todas las notificaciones hayan sido recibidas
                        Ganador ganador = new Ganador(partida,participantes[0]);
                        addBehaviour(new GanadorJuego(myAgent,ganador));
                    }
    }
    
    class GanadorJuego extends OneShotBehaviour {
        
        Ganador ganador;

	public GanadorJuego(Agent a, Ganador ganador) {
            super(a);
            this.ganador = ganador;
	}

        @Override
        public void action() {
            try {
                System.out.println("\nGANADOR: envío comunicación del ganador");

		// Crear el mensaje
		ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                
		msg.setSender(getAID());
		msg.addReceiver(participantes[0].getJugador());
                msg.addReceiver(participantes[1].getJugador());
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());

		manager.fillContent(msg, ganador);
                
                System.out.println(msg);

                send(msg);
                
                System.out.println("\nMensaje enviado....");
            } catch (Exception e) {
		e.printStackTrace();
            }
            doDelete(); //Finalizamos las tareas del agente
        }
    } 
}

