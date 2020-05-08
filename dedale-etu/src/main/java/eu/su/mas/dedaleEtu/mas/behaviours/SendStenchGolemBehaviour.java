package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendStenchGolemBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -9220667988667779503L;
	
	private ExploSoloBehaviour explo;

	private DFAgentDescription[] agentsList;
	
	public SendStenchGolemBehaviour(final Agent myAgent, ExploSoloBehaviour explo, DFAgentDescription[] agentsList) {
		super(myAgent);
		this.explo = explo;
		this.agentsList = agentsList;
	}
	
	@Override
	public void action() {
		if(!explo.getHunt() || explo.done()) {
			return;
			
		} else {
			
			ACLMessage stenchGolem = new ACLMessage(ACLMessage.PROPAGATE);
			stenchGolem.setSender(this.myAgent.getAID());
			
			for(DFAgentDescription a : agentsList) {
				if(this.myAgent.getAID() != a.getName()) {
					@SuppressWarnings({ "unchecked" })
					Iterator<ServiceDescription> services = a.getAllServices();
					ServiceDescription s = services.next();
					String name = s.getName();
					
					if(!name.equals(this.myAgent.getLocalName())) {
						stenchGolem.addReceiver(new AID(name,AID.ISLOCALNAME));
						
						String stench = explo.getStench();
						Serializable se = (Serializable) stench;
						
						try {
							stenchGolem.setContentObject(se);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						((AbstractDedaleAgent)this.myAgent).sendMessage(stenchGolem);
						//System.out.println(this.myAgent.getLocalName()+" a envoyé le ping"+" a "+name);
					}
				}
			}
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
