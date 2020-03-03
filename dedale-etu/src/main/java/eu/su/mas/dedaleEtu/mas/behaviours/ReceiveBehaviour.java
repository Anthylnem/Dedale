package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
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
		
		final MessageTemplate pingTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage pingMsg = this.myAgent.receive(pingTemplate);
		
		if (pingMsg != null) {
			System.out.println(this.myAgent.getLocalName()+" a reçu le ping "+pingMsg.getContent());
		
			ACLMessage ackPing = new ACLMessage(ACLMessage.AGREE);
			ackPing.setContent("ackPing send map");
			((AbstractDedaleAgent)this.myAgent).sendMessage(ackPing);
			System.out.println(this.myAgent.getLocalName()+" a envoyé le ackPing.");
			
			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			
	
			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			if (msg != null) {
				try {
					System.out.println("Receive "+msg.getContentObject());
									
					ArrayList<Serializable> messageContent = (ArrayList<Serializable>) msg.getContentObject();
	
					Set<String> closedNodes = (Set<String>) messageContent.get(0);
					ArrayList<ArrayList<String>> edges = (ArrayList<ArrayList<String>>) messageContent.get(1);
					List<String> openNodes = (List<String>) messageContent.get(2);
					
					explo.majNodes(closedNodes,openNodes);
					explo.setEdges(edges);
					
					System.out.println("Agent "+this.myAgent.getLocalName()+ " a reçu le message.");
					System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContentObject());
					this.finished=true;
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

