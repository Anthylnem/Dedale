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
	
	private int stenchIt = 0;
	
	private int notStenchIt = 0;
	
	private boolean hunt = false;
	
	private String lastPosition;
	
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
		if(cpt == 1)
			lastPosition = myPosition;
	
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
				//System.out.println("Exploration successfully done, behaviour removed.");
				
				nextNode = randomPosition(myPosition);

			}	
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					//System.out.println("test"+this.myMap.getShortestPath(myPosition, this.openNodes.get(0)));
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
				
				// Mauvaise solution interblocage
				/*
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite2=lobs.iterator();
				if(!lastNode.isBlank()) {
					while(!myPosition.equals(nextNode) && lastNode.equals(nextNode) && ite2.hasNext()) {
						Couple<String, List<Couple<Observation, Integer>>> next = ite2.next();
						System.out.println(this.myAgent.getLocalName()+" (avant) nextnode: "+nextNode+" lastnode:"+lastNode);
						nextNode = next.getLeft();
						System.out.println(this.myAgent.getLocalName()+" (après) nextnode: "+nextNode+" lastnode:"+lastNode);
					}
				}*/
				
				// Interblocage
				
				Random rand1 = new Random();
				int index1 = rand1.nextInt(lobs.size());
				int cpt = 0;
				//String node = "";
				
				if(!lastNode.isBlank() && lastNode.equals(nextNode)) {
					System.out.println(this.myAgent.getLocalName()+" (avant) nextnode: "+nextNode+" lastnode:"+lastNode);
					for (Iterator<Couple<String, List<Couple<Observation, Integer>>>> it = lobs.iterator(); it.hasNext(); ) {
						Couple<String, List<Couple<Observation, Integer>>> next = it.next();
						if(cpt >= index1) {
							 nextNode = next.getLeft();
							 if(nextNode.equals(myPosition))
								 nextNode = it.next().getLeft();
							 break;
						}
						nextNode = next.getLeft();
						cpt++;
				    }
					System.out.println(this.myAgent.getLocalName()+" (après) nextnode: "+nextNode+" lastnode:"+lastNode);
				}
				
				//Hunt
				
				hunt = false;
				
				if(lastObs.size() > 0) {
					//System.out.println("test "+lastObs.get(0));
					if(!lastObs.isEmpty() && myPosition !=  lastObs.get(0).getRight().get(0)) {
						//System.out.println("HELLO "+this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)));
						nextNode = this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)).get(0);
						hunt = true;
					}
				}
				
				
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite=lobs.iterator();
				
				List<Couple<String, List<Couple<Observation, Integer>>>> stench = new ArrayList<>();
				while(ite.hasNext()) {
					Couple<String, List<Couple<Observation, Integer>>> next = ite.next();
					//String nodeId = next.getLeft();
					//System.out.println("next"+next);
					if(!next.getRight().isEmpty()) {
						if((next.getRight()).get(0).getLeft().toString().equals("Stench")) {
							stench.add(next);
						}
					}
					//System.out.println(stench);
				}
				if(stench.size() > 0) {
					Random rand = new Random();
					int index = rand.nextInt(stench.size());
					nextNode = stench.get(index).getLeft();
					System.out.println(this.myAgent.getLocalName()+" va chasser au noeud "+nextNode);
					hunt = true;
				}
					
				
				
				if(!lastObs.isEmpty() && myPosition.equals(lastObs.get(0).getRight().get(0))) {
					lastObs = new LinkedList<Couple<Integer,List<String>>>();
					//System.out.println("lastObs"+lastObs);
				}
			
				//System.out.println("nextNode"+nextNode);
				
				
				// Arrêter les behaviours quand le golem est bloqué
				
				boolean myPositionStench = false;
				
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite1 = lobs.iterator();
				
				while(ite1.hasNext()) {
					Couple<String, List<Couple<Observation, Integer>>> next = ite1.next();
					if(!next.getRight().isEmpty() && next.getLeft().equals(myPosition)) {
						if((next.getRight()).get(0).getLeft().toString().equals("Stench")) {
							myPositionStench = true;
							break;
						}
					}
				}
				
				if(myPosition.equals(nextNode)) {
					if(stenchIt == 0) {
						lastPosition = myPosition;
					}
					stenchIt++;
					
				} else if(myPosition.equals(lastPosition) && myPositionStench) {
						stenchIt++;
						System.out.println(this.myAgent.getName()+" "+stenchIt);
						if(stenchIt >= 10) {
							finished = true;
							System.out.println(this.myAgent.getName()+ " A FINI");
						}
				} else if(!myPosition.equals(lastPosition)) {
						stenchIt = 0;
				}
				
				//S'éloigner quand l'agent veut chasser un golem déjà bloqué
				if(!myPositionStench && !currObs.isEmpty()) {
					if(notStenchIt == 0) {
						lastPosition = myPosition;
					}
					notStenchIt++;
					System.out.println("NotStenchIt "+notStenchIt +" "+this.myAgent.getLocalName());
					if(notStenchIt >= 10) {
						nextNode = randomPosition(myPosition);
						notStenchIt = 0;
					}
				}
				
				
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				lastNode = nextNode;
				

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
	
	public ArrayList<ArrayList<String>> getEdges() {
		return edges;
	}
	
	public void majNodes(Set<String> closedNodes,List<String> openNodes) {
		for(String s: closedNodes) {
			this.myMap.addNode(s,MapAttribute.closed);
			this.closedNodes.add(s);
			this.openNodes.remove(s);
		}
		
		for(String s: openNodes) {
			this.myMap.addNode(s,MapAttribute.open);
			if(!this.closedNodes.contains(s))
				this.openNodes.add(s);
		}
	}



	public void setEdges(ArrayList<ArrayList<String>> edges) {
		//System.out.println("edges "+myAgent.getLocalName()+" "+edges);
		
		for(ArrayList<String> e : edges) {
			//System.out.println("ALLO"+closedNodes+" "+openNodes+" "+myAgent.getLocalName());
			//System.out.println("setEdges "+myAgent.getLocalName()+" "+e.get(0)+" "+e.get(1));
			//this.myMap.addNode(e.get(0), MapAttribute.closed);
			//this.myMap.addNode(e.get(1), MapAttribute.open);

			this.myMap.addEdge(e.get(0), e.get(1));
			this.edges.add(e);
		}
	}
	
	public String randomPosition(String myPosition) {
		Random rand = new Random();
		int index = rand.nextInt(closedNodes.size());
		int cpt = 0 ;
		String node = "";
		
		for (Iterator<String> it = closedNodes.iterator(); it.hasNext(); ) {
			if(cpt >= index) {
				 node = it.next();
				 if(node.equals(myPosition))
					 node = it.next();
				 break;
			}
			node = it.next();
			cpt++;
	    }
		List<String> shortestPath = this.myMap.getShortestPath(myPosition, node);
		if(shortestPath.isEmpty()) {
			shortestPath.add(myPosition);
			return shortestPath.get(0);
		}
		
		return shortestPath.get(0);
		
	}
	
}
