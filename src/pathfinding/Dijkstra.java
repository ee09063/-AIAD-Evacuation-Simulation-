package pathfinding;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import graph.Node;
import graph.WaypointGraph;

public class Dijkstra {
	
	private WaypointGraph graph;
	
	public Dijkstra(WaypointGraph graph){
		this.graph = graph;
	}
	
	public void dijkstra(Node node) {
	      final Node source = node;
	      
	      if(source == null){
	    	  System.out.println(" A NULL NODE!");
	      }
	      
	      PriorityQueue<Node> q = new PriorityQueue<Node>(graph.nodes.size(), new Comparator<Node>() {
	          public int compare(Node n1, Node n2) {
	             if(n1.dist < n2.dist) return -1;
	             else if(n1.dist == n2.dist) return 0;
	             else if(n1.dist > n2.dist) return 1;
	             return 0;
	          }
	      });
	      
	      for (Node v : graph.nodes) {
	         v.parent = v == source ? source : null;
	         v.dist = v == source ? 0 : Double.MAX_VALUE;
	         q.add(v);
	      }
	      
	      source.dist = 0.0;
	      source.parent = null;
	 
	      dijkstra(q);
	   }
	
	private void dijkstra(PriorityQueue<Node> q){      
		Node v, u;
				
		while (!q.isEmpty()) {
			u = q.remove();
			
			for (Entry<Node, Double> a : u.neighbours.entrySet()){
				v = a.getKey();
				 
				final double alternateDist = u.dist + a.getValue();
				if (alternateDist < v.dist) {
					q.remove(v);
					v.dist = alternateDist;
					v.parent = u;
					q.add(v);
				}
			}
		}
	}
}
