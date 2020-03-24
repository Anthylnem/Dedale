package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class HuntBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -8629971343041586453L;
	
	private boolean finished = false;
	
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	
	private ExploSoloBehaviour explo;
	
	private List<Couple<String, List<Couple<Observation, Integer>>>> obs;
	
	public HuntBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, ExploSoloBehaviour explo) {
		super(myagent);
		this.myMap=myMap;
		this.explo = explo;
	}

	@Override
	public void action() {
		System.out.println("Je chasse des gens ;) ptdr");
		this.obs = explo.getObs();
		String nextNode = null;
		
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite=obs.iterator();
		while(ite.hasNext()) {
			Couple<String, List<Couple<Observation, Integer>>> next = ite.next();
			List<Couple<Observation, Integer>> stench = next.getRight();
			
			if(stench.size() > 0) {
				if((stench.get(0).getLeft()).toString().equals("Stench"))
					nextNode = next.getLeft();
					break;
			}
		}
		 
		System.out.println("Je vais chasser au noeud "+nextNode);
		((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
	}

	/*
	 * @Override public boolean done() { return finished; }
	 */
	
	@Override
	public int onEnd() {
		return 1;
	}

}
