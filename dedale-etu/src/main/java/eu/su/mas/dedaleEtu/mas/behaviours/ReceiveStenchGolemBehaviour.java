package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;

public class ReceiveStenchGolemBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = 6681934831749207089L;

	private ExploSoloBehaviour explo;
	
	public ReceiveStenchGolemBehaviour(final Agent myagent, ExploSoloBehaviour explo) {
		super(myagent);
		this.explo = explo;
	}

	@Override
	public void action() {
		
		if(explo.finish() || explo.done()) {
			return;
		}
		
		MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);			
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		
		if (msg != null) {
			try {
				//System.out.println("Receive "+msg.getContentObject());
								
				@SuppressWarnings("unchecked")
				ArrayList<String> messageContent = (ArrayList<String>) msg.getContentObject();
				
				explo.setStench(messageContent.get(0), messageContent.get(1));
				
				//System.out.println("Agent "+this.myAgent.getLocalName()+ " a reçu le message.");
				//System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			block();// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
