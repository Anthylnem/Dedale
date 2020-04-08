package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploSoloBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;
	
	/**
	 * Edges known
	 */
	private ArrayList<ArrayList<String>> edges;
	
	/**
	 * Current observations
	 */
	private List<Couple<String,List<Couple<Observation,Integer>>>> lobs;
	
	/**
	 * Last node visited
	 */
	private String lastNode = "";
	
	/**
	 * Set of observations
	 */
	private LinkedList<Couple<Integer,List<String>>> lastObs;
	
	private int lastObsSize = 1;
	
	private int cpt = 0;
	
	private boolean hunt = false;

	public ExploSoloBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
		this.edges = new ArrayList<ArrayList<String>>();
		this.lastObs = new LinkedList<Couple<Integer,List<String>>>();
	}

	@Override
	public void action() {
		cpt++;
		//System.out.println(this.myAgent.getLocalName()+" "+cpt);
//		System.out.println("Explo "+this.myAgent.getLocalName());
		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			this.lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			
			List<String> currObs = new ArrayList<String>();
			
			for(Couple<String,List<Couple<Observation,Integer>>> l : lobs) {
				//System.out.println("l "+l);
				List<Couple<Observation, Integer>> l1 = l.getRight();
				for(Couple<Observation, Integer> c : l1) {
					if(c.getLeft().toString().equals("Stench"))
						currObs.add(l.getLeft());
				}
			}
			
			
			Couple<Integer,List<String>> c = new Couple<Integer,List<String>>(cpt,currObs);
			if(c.getRight().size() > 0) {
				if(lastObs.size() >= lastObsSize) {
					lastObs.removeFirst();
					lastObs.add(c);
				} else {
					lastObs.add(c);
				}
			}


			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);
						
						this.openNodes.add(nodeId);
						
						ArrayList<String> edge = new ArrayList<>();
						edge.add(myPosition);
						edge.add(nodeId);
						this.edges.add(edge);
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
						
						ArrayList<String> edge = new ArrayList<>();
						edge.add(myPosition);
						edge.add(nodeId);
						this.edges.add(edge);
					}
					if (nextNode==null) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				//Explo finished
				//finished=true;
				System.out.println("Exploration successfully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					System.out.println("test"+this.myMap.getShortestPath(myPosition, this.openNodes.get(0)));
					List<String> shortestPath = this.myMap.getShortestPath(myPosition, this.openNodes.get(0));
					if(shortestPath.isEmpty())
						shortestPath.add(myPosition);
					nextNode= shortestPath.get(0);
				}
				
				
				
				/***************************************************
				** 		ADDING the API CALL to illustrate their use **
				*****************************************************/

				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
				//System.out.println(this.myAgent.getLocalName()+" - noeuds visités:"+closedNodes);
				
				//example related to the use of the backpack for the treasure hunt
				Boolean b=false;
				for(Couple<Observation,Integer> o:lObservations){
					switch (o.getLeft()) {
					case DIAMOND:case GOLD:

						System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
						System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						System.out.println(this.myAgent.getLocalName()+" - My expertise is: "+((AbstractDedaleAgent) this.myAgent).getMyExpertise());
						System.out.println(this.myAgent.getLocalName()+" - I try to open the safe: "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
						System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
						System.out.println(this.myAgent.getLocalName()+" - The agent grabbed : "+((AbstractDedaleAgent) this.myAgent).pick());
						System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
						b=true;
						break;
					default:
						break;
					}
				}

				//If the agent picked (part of) the treasure
				if (b){
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					System.out.println(this.myAgent.getLocalName()+" - State of the observations after picking "+lobs2);
					
					//Trying to store everything in the tanker
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
					
				}
				
				//Trying to store everything in the tanker
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
				//System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Silo"));
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());


				/************************************************
				 * 				END API CALL ILUSTRATION
				 *************************************************/
				
				// Shortest path
				/*
				 * System.out.println("position "+myPosition);
				 * 
				 * List<String> path = this.myMap.getShortestPath(myPosition, "0_10");
				 * System.out.println(path);
				 */
				
				// Mauvaise solution interblocage
				/*
				 * Iterator<Couple<String, List<Couple<Observation, Integer>>>>
				 * ite2=lobs.iterator(); if(!lastNode.isBlank()) {
				 * while(lastNode.equals(nextNode) && ite2.hasNext()) { Couple<String,
				 * List<Couple<Observation, Integer>>> next = ite2.next(); nextNode =
				 * next.getLeft(); } }
				 */
				
				hunt = false;
				
				if(lastObs.size() > 0) {
					//System.out.println("test "+lastObs.get(0));
					if(!lastObs.isEmpty() && myPosition !=  lastObs.get(0).getRight().get(0)) {
						System.out.println("HELLO "+this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)));
						nextNode = this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)).get(0);
						hunt = true;
					}
				}
				
				//Hunt
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite=lobs.iterator();
				while(ite.hasNext()) {
					Couple<String, List<Couple<Observation, Integer>>> next = ite.next();
					String nodeId = next.getLeft();
					List<Couple<Observation, Integer>> stench = next.getRight();
					//System.out.println(nodeId+" "+stench);
					
					if(stench.size() > 0) {
						if((stench.get(0).getLeft()).toString().equals("Stench"))
							nextNode = next.getLeft();
							System.out.println(this.myAgent.getLocalName()+" va chasser au noeud "+nextNode);
							hunt = true;
							break;
					}
				}
				
				
				if(!lastObs.isEmpty() && myPosition.equals(lastObs.get(0).getRight().get(0))) {
					lastObs = new LinkedList<Couple<Integer,List<String>>>();
					System.out.println("lastObs"+lastObs);
				}
			
				//System.out.println("nextNode"+nextNode);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				//lastNode = nextNode;
				
				
			}

		}
		
	}
	
	public boolean getHunt() {
		return hunt;
	}
	
	public List<Couple<String, List<Couple<Observation, Integer>>>> getObs() {		
		return lobs;
	}
	
	public LinkedList<Couple<Integer,List<String>>> getLastObs() {
		return lastObs;
	}
	
	public void setLastObs(LinkedList<Couple<Integer,List<String>>> lastObsOther) {
		if(!lastObsOther.isEmpty() && !lastObs.isEmpty() && (lastObsOther.get(0).getLeft() > lastObs.get(0).getLeft())) {
			if(lastObs.size() >= lastObsSize) {
				lastObs.removeFirst();
				lastObs.add(lastObsOther.get(0));
			} else {
				lastObs.add(lastObsOther.get(0));
			}
		}
	}

	
	@Override public boolean done() { return finished; }
	 

	public Set<String> getClosedNodes() {
		return closedNodes;
	}
	
	public List<String> getOpenNodes() {
		return openNodes;
	}
	
	public void majNodes(Set<String> closedNodes,List<String> openNodes) {
		for(String s: closedNodes) {
			this.myMap.addNode(s,MapAttribute.closed,true);
			this.closedNodes.add(s);
			this.openNodes.remove(s);

		}
		
		for(String s: openNodes) {
			this.myMap.addNode(s,MapAttribute.open,true);
			this.openNodes.add(s);
		}
	}

	public ArrayList<ArrayList<String>> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<ArrayList<String>> edges) {
		//System.out.println("edges "+myAgent.getLocalName()+" "+edges);
		
		for(ArrayList<String> e : edges) {
			//System.out.println("ALLO"+closedNodes+" "+openNodes+" "+myAgent.getLocalName());
			//System.out.println("setEdges "+myAgent.getLocalName()+" "+e.get(0)+" "+e.get(1));
			//this.myMap.addNode(e.get(0), MapAttribute.closed);
			//this.myMap.addNode(e.get(1), MapAttribute.open);

			this.myMap.addEdge(e.get(0), e.get(1));
		}
	}

	
}
