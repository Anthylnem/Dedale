package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;

/**
 * It receives a message tagged with an 
 * 
 * @author Cédric Herpson
 *
 */
public class ReceiveBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished=false;

	private ExploSoloBehaviour explo;
	/**
	 * 
	 * 
	 * @param myagent
	 */
	public ReceiveBehaviour(final Agent myagent, ExploSoloBehaviour explo) {
		super(myagent);
		this.explo = explo;

	}


	@SuppressWarnings("unchecked")
	public void action() {
		
		MessageTemplate pingTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage pingMsg = this.myAgent.receive(pingTemplate);
		
		if (pingMsg != null) {
			AID sender = pingMsg.getSender();
			String name = sender.getLocalName();
			System.out.println(this.myAgent.getLocalName()+" a reçu le ping ");
		
			ACLMessage ackPing = new ACLMessage(ACLMessage.AGREE);
			ackPing.setSender(this.myAgent.getAID());
			ackPing.setContent("ackPing send map");
			ackPing.addReceiver(new AID(name,AID.ISLOCALNAME));
			((AbstractDedaleAgent)this.myAgent).sendMessage(ackPing);
			System.out.println(this.myAgent.getLocalName()+" a envoyé le ackPing.");			
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			
			ACLMessage msg = this.myAgent.receive(msgTemplate);
			
			if (msg != null) {
				try {
					//System.out.println("Receive "+msg.getContentObject());
									
					ArrayList<Serializable> messageContent = (ArrayList<Serializable>) msg.getContentObject();
	
					Set<String> closedNodes = (Set<String>) messageContent.get(0);
					ArrayList<ArrayList<String>> edges = (ArrayList<ArrayList<String>>) messageContent.get(1);
					List<String> openNodes = (List<String>) messageContent.get(2);
					LinkedList<Couple<Integer,List<String>>> lastObsOther = (LinkedList<Couple<Integer,List<String>>>) messageContent.get(3);
					
					explo.majNodes(closedNodes,openNodes);
					explo.setEdges(edges);
					explo.setLastObs(lastObsOther);
					
					System.out.println("Agent "+this.myAgent.getLocalName()+ " a reçu le message.");
					//System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContentObject());
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
			}
		}
	}

	public boolean done() {
		return finished;
	}

}

