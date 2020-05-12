package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.
 */

public class ExploSoloBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	
	private boolean finished = false;

	private boolean golemBlocked = false;

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
	 * Liste des dernières observations
	 * Integer : itération (max lastObsSize)
	 * List<String> : noeuds autour
	 */
	private LinkedList<Couple<Integer,List<String>>> lastObs;
	
	/**
	 * Taille de la LinkedList lastObs
	 */
	private int lastObsSize = 1;
	
	/**
	 *  Compteur d'itérations
	 */
	private int cpt = 0;
	
	/** 
	 * Itérations quand un golem est bloqué
	 */
	private int stenchIt = 0;
	
	/** 
	 * Itérations pour un agent qui ne bloque pas quand un golem est bloqué
	 */
	private int notStenchIt = 0;
	
	/**
	 * Mode chasse
	 * hunt = true quand l'agent suit une odeur
	 */
	private boolean hunt = false;
	
	/**
	 * Précédente position de l'agent
	 */
	private String lastPosition;
	
	//private List<Couple<String, List<Couple<Observation, Integer>>>> stench = new ArrayList<>();
	
	/**
	 * Prochain déplacement
	 */
	private String nextNode = null;
	
	/**
	 * Case ayant une odeur
	 * Reçue par un agent voisin
	 */
	private String receivedStench = "";
	
	private String nextNodeOther = "";
	
	/**
	 * Liste des noeuds où ne plus retourner (zone où un golem est bloqué)
	 */
	private List<String> blackList = new ArrayList<String>();
	
	/**
	 * Score de l'agent
	 */
	private int score = 1000;
	
	private List<Couple<String, List<Couple<Observation, Integer>>>> stench;
	
	public ExploSoloBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
		this.edges = new ArrayList<ArrayList<String>>();
		this.lastObs = new LinkedList<Couple<Integer,List<String>>>();
		this.stench = new ArrayList<>();
	}

	@Override
	public void action() {
		cpt++;
		
		//System.out.println(this.myAgent.getLocalName()+" "+nextNode);
		//System.out.println("Explo "+this.myAgent.getLocalName());
		
		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(cpt == 1)
			lastPosition = myPosition;
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			this.lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			
			// CurrObs : liste des noeuds observés avec une odeur
			List<String> currObs = new ArrayList<String>();
			
			for(Couple<String,List<Couple<Observation,Integer>>> l : lobs) {
				List<Couple<Observation, Integer>> l1 = l.getRight();
				for(Couple<Observation, Integer> c : l1) {
					if(c.getLeft().toString().equals("Stench"))
						if(!blackList.contains(l.getLeft()))
							currObs.add(l.getLeft());
				}
			}
			
			if(golemBlocked) {
				if(!myPositionHasStench(myPosition)) {
					System.out.println(this.myAgent.getLocalName()+" EST DE RETOUR "+myPosition+" "+currObs+lobs);
					stenchIt = 0;
					golemBlocked = false;
				} else {
					stenchIt++;
					if(stenchIt >= 30) {
						System.out.println(this.myAgent.getLocalName()+" A FINI SA JOURNEE ##################################################################################################################################");
						finished = true;
					}
					return;
				}
			}
			
			
			
			// Initialisation lastObs
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
			nextNode=null;
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
						if(!blackList.contains(myPosition) && !blackList.contains(nodeId))
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
				System.out.println(this.myAgent.getLocalName()+" Exploration successfully done.");
				
				nextNode = randomPosition(myPosition);

			}	
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					if(!blackList.contains(myPosition) && !blackList.contains(this.openNodes.get(0))) {
						List<String> shortestPath = this.myMap.getShortestPath(myPosition, this.openNodes.get(0));
						if(shortestPath.isEmpty())
							shortestPath.add(myPosition);
						nextNode= shortestPath.get(0);
					}
				}
				
				
				
				/***************************************************
				** 		ADDING the API CALL to illustrate their use **
				*****************************************************/

				//list of observations associated to the currentPosition
				List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
				System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
				//System.out.println(this.myAgent.getLocalName()+" - noeuds visités:"+closedNodes);
				
				/*
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
				*/

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
				
				if(!lastNode.isBlank() && lastNode.equals(nextNode) && currObs.isEmpty() && !myPosition.equals(lastNode)) {
					System.out.println(this.myAgent.getLocalName()+" (avant) nextnode: "+nextNode+" lastnode:"+lastNode);
					for (Iterator<Couple<String, List<Couple<Observation, Integer>>>> it = lobs.iterator(); it.hasNext(); ) {
						Couple<String, List<Couple<Observation, Integer>>> next = it.next();
						//System.out.println(this.myAgent.getLocalName()+" INTERBLOCAGE");
						if(cpt >= index1) {
							 nextNode = next.getLeft();
							 if(nextNode.equals(myPosition))
								 nextNode = it.next().getLeft();
							 break;
						}
						nextNode = next.getLeft();
						cpt++;
				    }
					//System.out.println(this.myAgent.getLocalName()+" (après) nextnode: "+nextNode+" lastnode:"+lastNode);
				}
				
				// Déplacement vers une case (receivedStench) avec odeur envoyée par un autre agent
				
				if(!receivedStench.isBlank() && !nextNodeOther.isBlank() && !blackList.contains(receivedStench) && (openNodes.contains(receivedStench) || closedNodes.contains(receivedStench))) {
					System.out.println("RECEIVED STENCH");
					List<String> node = new ArrayList<String>();
					if(nextNodeOther.equals(receivedStench))
						node = this.myMap.getShortestPath(myPosition,receivedStench);
					else if(!myPosition.equals(nextNodeOther)){
						removeNode(nextNodeOther);
						node = this.myMap.getShortestPath(myPosition,receivedStench);
						addNode(nextNodeOther);
					}
					if(!node.isEmpty()) {
						nextNode = node.get(0);
					}
					if(lastNode.equals(nextNode) || receivedStench.equals(myPosition))
						receivedStench = "";
				}
				
				//Hunt
				
				hunt = false;
				
				if(lastObs.size() > 0) {
					//System.out.println("test "+lastObs.get(0));
					if(!lastObs.isEmpty() && myPosition !=  lastObs.get(0).getRight().get(0)) {
						
						Couple<Integer, List<String>> a = lastObs.get(0);
						//List<String> aba = this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0));
						
						if(!blackList.contains(lastObs.get(0).getRight().get(0))) {
							if(!this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)).isEmpty())
								nextNode = this.myMap.getShortestPath(myPosition,lastObs.get(0).getRight().get(0)).get(0);
							else {
								nextNode = randomPosition(myPosition);
								System.out.println(this.myAgent.getLocalName()+" J'EXPLORE ALEATOIREMENT");
							}
						}
						
						hunt = true;
					}
				}
				
				
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite=lobs.iterator();
				stench = new ArrayList<>();
				
				while(ite.hasNext()) {
					Couple<String, List<Couple<Observation, Integer>>> next = ite.next();
					if(!next.getRight().isEmpty()) {
						if((next.getRight()).get(0).getLeft().toString().equals("Stench")) {
							if(!blackList.contains(next.getLeft()))
								stench.add(next);
						}
					}
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
				
				
				// Arrêter les behaviours quand le golem est bloqué
				
				boolean myPositionStench = myPositionHasStench(myPosition);
				
				if(myPosition.equals(nextNode) && myPositionStench) {
					if(stenchIt == 0) {
						lastPosition = myPosition;
					}
					stenchIt++;
					System.out.println(this.myAgent.getLocalName()+" "+stenchIt);
					
				} else if(myPosition.equals(lastPosition) && myPositionStench) {
						stenchIt++;
						System.out.println(this.myAgent.getLocalName()+" "+stenchIt);
						if(stenchIt >= 10) {
							golemBlocked = true;
							System.out.println(this.myAgent.getName()+ " A FINI ##########################################################################################################################################");
						}
				} else if(!myPosition.equals(lastPosition)) {
						stenchIt = 0;
				}
				
				if(myPositionHasStench(myPosition)) {
					nextNode = myPosition;
				}
				
				// S'éloigner quand l'agent veut chasser un golem déjà bloqué et
				// mettre dans la blackList les positions autour où il y a un stench
				
				boolean inBlackList = true;
				for(Couple<String, List<Couple<Observation, Integer>>> s : stench) {
					if(!blackList.contains(s.getLeft())) {
						inBlackList = false;
						break;
					}
				}
				
				
				if(!myPositionStench && !currObs.isEmpty() && !inBlackList) {
					if(notStenchIt == 0) {
						lastPosition = myPosition;
					}
					notStenchIt++;
					//System.out.println("NotStenchIt "+notStenchIt +" "+this.myAgent.getLocalName());
					//System.out.println("currObs "+currObs);
					if(notStenchIt >= 10) {
						for(Couple<String, List<Couple<Observation, Integer>>> s : stench) {
							if(!this.blackList.contains(s.getLeft()))
								blackList.add(s.getLeft());
							removeNode(s.getLeft());
							//System.out.println(s);
						}
						nextNode = randomPosition(myPosition);
						notStenchIt = 0;
					}
					
					if(!myPosition.equals(lastPosition)) {
						notStenchIt = 0;
					}
				}
				
				/* //Ancienne solution
				// Empêcher l'agent de retourner sur une position dans la blacklist
				if(blackList.contains(nextNode))
					nextNode = randomPosition(myPosition);
				 */
				
				//Random en cas de soucis
				if(nextNode == null) {
					nextNode = randomPosition(myPosition);
				}
				
				System.out.println(this.myAgent.getLocalName()+" "+nextNode);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				lastNode = nextNode;
				
				//System.out.println(this.myAgent.getLocalName()+" BLACKLIST "+blackList);

		}
		
	}
	
	public boolean myPositionHasStench(String myPosition) {
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> ite1 = lobs.iterator();
		
		while(ite1.hasNext()) {
			Couple<String, List<Couple<Observation, Integer>>> next = ite1.next();
			if(!next.getRight().isEmpty() && next.getLeft().equals(myPosition)) {
				if((next.getRight()).get(0).getLeft().toString().equals("Stench")) {
					return true;
				}
			}
		}
		return false;
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
	
	public boolean finish() { return golemBlocked; }
	
	@Override public boolean done() { return finished; }
	
	
	public void setStench(String receivedStench, String nextNodeOther) {
		this.receivedStench = receivedStench;
		this.nextNodeOther = nextNodeOther;
	}
	
	public String getStench() {
		if(!stench.isEmpty()) {
			for(Couple<String, List<Couple<Observation, Integer>>> s : stench) {
				if(!s.getLeft().equals(nextNode)) {
					return s.getLeft();
				}
			}
		}
		return nextNode;
	}
	
	public List<String> getBlackList() {
		return blackList;
	}
	
	public Set<String> getClosedNodes() {
		return closedNodes;
	}
	
	public List<String> getOpenNodes() {
		return openNodes;
	}
	
	public ArrayList<ArrayList<String>> getEdges() {
		return edges;
	}
	
	public String getNextNode() {
		return nextNode;
	}
	
	public void removeNode(String node) {
		openNodes.remove(node);
		closedNodes.remove(node);
		myMap.removeNode(node);
	}
	
	public void addNode(String node) {
		openNodes.add(node);
		closedNodes.add(node);
		myMap.addNode(node, MapAttribute.closed);
	}
	
	public void majNodes(Set<String> closedNodes, List<String> openNodes, List<String> blackList) {
		for(String n : blackList) {
			if(!this.blackList.contains(n))
				this.blackList.add(n);
			removeNode(n);
		}
		
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
			if(!blackList.contains(e.get(0)) && !blackList.contains(e.get(1)))
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
				 while(node.equals(myPosition) || blackList.contains(node))
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
