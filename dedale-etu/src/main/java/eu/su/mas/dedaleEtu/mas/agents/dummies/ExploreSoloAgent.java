package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.SendBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.HuntBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 *
 */

public class ExploreSoloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		try {
		
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();

			sd.setType("explo");
			sd.setName(getLocalName());
			dfd.addServices(sd);
			
			DFService.register(this, dfd);
			
			Thread.sleep(2000);
			
			DFAgentDescription dfd1 = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("explo");
			dfd1.addServices(sd1);
			DFAgentDescription[] agentsList = DFService.search(this, dfd1);
		
			List<Behaviour> lb=new ArrayList<Behaviour>();
			
			ExploSoloBehaviour explo = new ExploSoloBehaviour(this,this.myMap);
			lb.add(explo);
			lb.add(new SendBehaviour(this,explo,agentsList));
			lb.add(new ReceiveBehaviour(this,explo));
			
			// Première version chasser le Golem avec un fsm et HuntBehaviour mais possible sans fsm

			/*
			 * FSMBehaviour fsm = new FSMBehaviour(this) { public int onEnd() {
			 * System.out.println("FSM behaviour terminé"); myAgent.doDelete(); return
			 * super.onEnd(); } };
			 * 
			 * fsm.registerFirstState(explo, "Exploration"); fsm.registerState(new
			 * HuntBehaviour(this,this.myMap,explo), "Hunt");
			 * fsm.registerDefaultTransition("Exploration","Hunt");
			 * fsm.registerTransition("Exploration", "Hunt", 1);
			 * fsm.registerTransition("Exploration", "Exploration", 0);
			 * fsm.registerTransition("Hunt", "Exploration", 1);
			 * fsm.registerTransition("Hunt", "Hunt", 0);
			 * 
			 * lb.add(fsm);
			 */
			 
			
			addBehaviour(new startMyBehaviours(this,lb));

			
			System.out.println("the  agent "+this.getLocalName()+ " is started");
		
		} catch (FIPAException fe) {
			fe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		

	}
	
	
	
}
