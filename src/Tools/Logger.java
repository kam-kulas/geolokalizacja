package Tools;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.security.PublicKey;
import java.util.Iterator;

public class Logger {

    public Logger(){}

    public static void LogSendMessage(ACLMessage msg, Agent myAgent){
        for (Iterator iterator = msg.getAllReceiver(); iterator.hasNext();) {
            System.out.println("Sent: " + myAgent + " -> "  + iterator.next()  + "  " + msg.getContent());
        }
    }

    public static void LogReciveMessage(ACLMessage msg, Agent myAgent){
            System.out.println("Recive: " + myAgent + " <- "  + msg.getSender()  + "  " + msg.getContent());
    }
}
