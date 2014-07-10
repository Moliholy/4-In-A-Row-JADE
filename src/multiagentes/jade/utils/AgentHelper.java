/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagentes.jade.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Auxiliary class that help to do a few operations with Agents by providing
 * public static methods.
 *
 * @author Molina
 */
public class AgentHelper {

    /**
     * Register an Agent in the yellow pages indicating only a few mandatory
     * parameters
     *
     * @param a the agent to be registered.
     * @param serviceName name of the agent's service
     * @param serviceType service's type name
     * @throws FIPAException during the registration process it is possible that
     * errors could happen.
     */
    public static void registerYellowPages(Agent a, String serviceName, String serviceType) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(a.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType(serviceType);
        dfd.addServices(sd);
        DFService.register(a, dfd);
    }

    /**
     * Register an Agent in the yellow pages indicating only a few mandatory
     * parameters
     *
     * @param a the agent to be registered.
     * @param serviceName name of the agent's service
     * @param serviceType service's type name
     * @param ontologyName service's understandable ontology
     * @throws FIPAException during the registration process it is possible that
     * errors could happen.
     */
    public static void registerYellowPages(Agent a, String serviceName, String serviceType,
            String ontologyName, String languageName) throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(a.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType(serviceType);
        sd.addOntologies(ontologyName);
        sd.addLanguages(languageName);
        dfd.addServices(sd);
        DFService.register(a, dfd);
    }

    /**
     * It makes a search of avaiable agents that have the required service
     *
     * @param a Agent that makes the search
     * @param serviceType service's type name to search
     * @return an array of agents that offer the service, or an empty array if
     * there is no one avaliable
     * @throws FIPAException
     */
    public static DFAgentDescription[] lookForAvailableAgents(Agent a, String serviceType) throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(serviceType);
        template.addServices(templateSd);
        return DFService.search(a, template);
    }
    
    /**
     * It makes a search of avaiable agents that have the required service
     *
     * @param a Agent that makes the search
     * @param serviceType service's type name to search
     * @param ontologyName required ontology
     * @return an array of agents that offer the service, or an empty array if
     * there is no one avaliable
     * @throws FIPAException
     */
    public static DFAgentDescription[] lookForAvailableAgents(Agent a, String serviceType, String ontologyName) throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(serviceType);
        templateSd.addOntologies(ontologyName);
        template.addServices(templateSd);
        return DFService.search(a, template);
    }

    /**
     * It sends a message to another agent
     *
     * @param sender the agent that sends the message
     * @param provider the receiver of the message
     * @param performative the type of message to send
     * @param content body of the message
     */
    public static void sendMessage(Agent sender, AID provider, int performative, String content) {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(provider);
        msg.setSender(sender.getAID());
        msg.setContent(content);
        sender.send(msg);
    }

    /**
     * Looks for messages for the agent of the specified type.
     *
     * @param origin the agent that looks for messages.
     * @param message_type type of the message to look for.
     * @return the message that has the specified type in its mailbox.
     */
    public static ACLMessage receiveMessage(Agent origin, int message_type) {
        MessageTemplate template = MessageTemplate.MatchPerformative(message_type);
        return origin.receive(template);
    }

    /**
     * Prints a log message in order to debug the agents.
     *
     * @param agent the agent to debug.
     * @param message log message to print in cosole.
     */
    public static void log(Agent agent, String message) {
        System.out.println(agent.getAID().getName() + " - " + message);
    }
}
