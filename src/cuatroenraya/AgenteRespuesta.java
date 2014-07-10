/**
 * Agente de ejemplo
 */
package cuatroenraya;

import cuatroenraya.elementos.Ganador;
import cuatroenraya.elementos.Jugador;
import cuatroenraya.elementos.Jugar;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simularemos el agente de respuesta para el protocolo FIPA Contrac Net
 * para buscar jugadores que quieran jugar al Cuatro en Raya.
 * 
 * También se simulará la tarea que indicará el jugador que ha ganado la partida.
 * @author pedroj
 */
public class AgenteRespuesta extends Agent {
        
    private ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;

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
            
        System.out.println("El agente "+getName()+" esperando para CFP...");
        
        //Registro del agente en las páginas amarillas
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType(OntologiaCuatroEnRaya.REGISTRO_JUGADOR);
            // Agents that want to use this service need to "know" the weather-forecast-ontology
            sd.addOntologies(OntologiaCuatroEnRaya.ONTOLOGY_NAME);
            // Agents that want to use this service need to "speak" the FIPA-SL language
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            dfd.addServices(sd);
  		
            DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        
        //Simulamos la tarea que nos informa el ganador
        addBehaviour(new GanadorJuego(this)); 
	
        //Plantilla la exploración del mensaje para el protocolo ContractNet
        MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
			MessageTemplate.MatchPerformative(ACLMessage.CFP) );
	
        addBehaviour(new IniciarJuego(this, template));
    }
    
    class IniciarJuego extends  ContractNetResponder {
        
        public IniciarJuego (Agent agent, MessageTemplate template) {
            super (agent, template);
        }
        
        @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				
                    System.out.println("El agente "+getLocalName()+": CFP recibido desde "+cfp.getSender().getName()+". La acción es "+cfp.getContent());
                    //Siempre se responderá afirmativamente a la propuesta inicial
                    System.out.println("El agente "+getLocalName()+": reenvía la propuesta inicial");
                    ACLMessage propose = cfp.createReply();
                    
                    try { 
                        Action a = (Action) manager.extractContent(cfp);
                        Jugar jugar = (Jugar) a.getAction();
                        Jugador jugador = new Jugador();
                        jugador.setJugador(propose.getSender());
                        jugar.setOponente(jugador); 
                        //Rellenamos el campo oponente con el agente
                        //que acepta la propuesta de juego
                                
                        a.setAction(jugar); //La incluimos en la acción
                        manager.fillContent(propose, a); // Y en el mensaja
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    propose.setPerformative(ACLMessage.PROPOSE);
                    System.out.println(propose);
                    return propose;        
            }

			
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
                System.out.println("El agente "+getLocalName()+": Proposición aceptada");
                ACLMessage inform = accept.createReply();
                
                try {
                    Action a = (Action) manager.extractContent(accept);
                    Jugar jugar = (Jugar) a.getAction();
                    System.out.println("El oponente del agente: " + getLocalName() + " es el agente: " + jugar.getOponente().getJugador().getLocalName());                
                
                    //Informamos que estamos conformes en realizar la acción
                    Done d = new Done(jugar);
                    manager.fillContent(inform, a);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }        
                inform.setPerformative(ACLMessage.INFORM);
                System.out.println(inform);
                return inform;
            }
			
                
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                    System.out.println("El agente "+getLocalName()+": Proposición rechazada");
                    doDelete();
            }
    }
        
    class GanadorJuego extends CyclicBehaviour {

        public GanadorJuego(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE));
                if (msg != null) {
                    try {
                        System.out.println("\nPRUEBA: Simulación mensaje ganador partida");
                        System.out.println(msg);
                        Ganador ganador = (Ganador) manager.extractContent(msg);

			System.out.println("El identificador de la partida es: " + ganador.getPartida().getId());
			System.out.println("El juego es: " + ganador.getPartida().getJuego().getTipo());
                        System.out.println("El jugador ganador es: " + ganador.getJugador().getJugador().getLocalName());
                        System.out.println("El color de la ficha es: " + ganador.getJugador().getFicha());

			// Presentación de los datos

			doDelete();
                    } catch (Exception e) {
			e.printStackTrace();
                    }
		} else {
                    block();
		}
        }
        
    }
    
    @Override
    protected void takeDown() {
        System.out.println("\n" + getName() + " Terminadas las tareas ...");
        try { DFService.deregister(this); } //Eliminar el registro de las páginas amarillas
          catch (Exception e) {}
    }   
}

