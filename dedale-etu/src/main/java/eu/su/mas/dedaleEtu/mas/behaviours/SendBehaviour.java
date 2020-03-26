package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behaviour for send a map to agents
 *
 */
public class SendBehaviour extends TickerBehaviour{

	private static final long serialVersionUID = -2058134622078521998L;
	
	private ExploSoloBehaviour explo;
	
	private DFAgentDescription[] agentsList;

	/**
	 * An agent tries to contact its friend and to give him its map
	 * @param myagent the agent who posses the behaviour
	 *  explo the behaviour ExploSoloBehaviour of the agent
	 */
	public SendBehaviour (final Agent myagent, ExploSoloBehaviour explo, DFAgentDescription[] agentsList) {
		super(myagent, 3000);
		this.explo = explo;
		this.agentsList = agentsList;
	}

	@Override
	public void onTick() {
//		System.out.println("Send "+this.myAgent.getLocalName());
		ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
		ping.setSender(this.myAgent.getAID());
		ping.setContent("Ping send map");
		
		for(DFAgentDescription a : agentsList) {
			if(this.myAgent.getAID() != a.getName()) {
				@SuppressWarnings({ "unchecked" })
				Iterator<ServiceDescription> services = a.getAllServices();
				ServiceDescription s = services.next();
				String name = s.getName();
				
				if(!name.equals(this.myAgent.getLocalName())) {
					ping.addReceiver(new AID(name,AID.ISLOCALNAME));
				
					((AbstractDedaleAgent)this.myAgent).sendMessage(ping);
					System.out.println(this.myAgent.getLocalName()+" a envoyé le ping"+" a "+name);
				}
			}
		}

		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		MessageTemplate ackPingTemplate = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
		ACLMessage ackPingMsg = this.myAgent.receive(ackPingTemplate);
		
		if (ackPingMsg != null) {
			System.out.println(this.myAgent.getLocalName()+" a reçu le ackPing ");
			
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
			//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol("MapProtocol");
	
			if (myPosition!=""){
				//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
				try {
					Set<String> closedNodes = explo.getClosedNodes();				
					Serializable n = (Serializable) closedNodes;
					
					ArrayList<ArrayList<String>> edges = explo.getEdges();
					Serializable e = (Serializable) edges;
					
					List<String> openNodes = explo.getOpenNodes();
					Serializable o = (Serializable) openNodes;
					
					LinkedList<Couple<Integer,List<String>>> lastObs = explo.getLastObs();
					Serializable l = (Serializable) lastObs;
									
					ArrayList<Serializable> messageContent = new ArrayList<Serializable>();
					messageContent.add(n);
					messageContent.add(e);
					messageContent.add(o);
					messageContent.add(l);
					
					//System.out.println("MESSAGE CONTENT "+messageContent);
					
					Serializable s = (Serializable) messageContent;
					msg.setContentObject(s);
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
	
				for(DFAgentDescription a : agentsList) {
					if(this.myAgent.getAID() != a.getName()) {
						@SuppressWarnings({ "unchecked" })
						Iterator<ServiceDescription> services = a.getAllServices();
						ServiceDescription s = services.next();
						String name = s.getName();
						
						//System.out.println(name);
						
						msg.addReceiver(new AID(name,AID.ISLOCALNAME));
						
						((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					}
				}
	
			}
		}
	}
}