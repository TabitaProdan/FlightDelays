package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	
	private Map<Integer,Airport> idMap;
	
	private Map <Airport, Airport> visita;
	
	public Model () {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap <Integer,Airport>();	
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//se va bene che il grafo contenga tutti i vertici va bene il seguente metodo
		//Graphs.addAllVertices(grafo, idMap.values());
		
		//aggiungo vertici filtrati
		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		for (Rotta r: dao.getRotte(idMap)) {
			if (this.grafo.containsVertex(r.getA1())&& this.grafo.containsVertex(r.getA2()) ) {
				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2());
				if (e==null) {
					//non esiste l'arco, lo aggiungo
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2());
					
				}else {
					//esisteva già un arco tra i due, ne incremento il peso 
					double pesoVecchio  = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
			
		}
		
		System.out.println(grafo.vertexSet().size());
		System.out.println(grafo.edgeSet().size());
		
		
	}

	/*public Set<Airport> getVertici() {
		// TODO Auto-generated method stub
		return this.grafo.vertexSet();
	}*/
	
	public Set<Airport> getVertici() {
		if(grafo != null)
			return grafo.vertexSet();
		
		return null;
	}
	
	public int getNVertici() {
		if(grafo != null)
			return grafo.vertexSet().size();
		
		return 0;
	}
	
	public int getNArchi() {
		if(grafo != null)
			return grafo.edgeSet().size();
		
		return 0;
	}
	
	public List<Airport> trovaPercorso (Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<Airport>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo,a1);
		
		//per salvare l'albero di vista che ottero con il prossimo metodo, uso una mappa
		//che mi dice da chi è stato scoperto un determinato aeroporto
		visita = new HashMap<>();
		//se inserisco a1, a2 significa che a1 l'ho scoperto a partire da a2;
		
		//inserisco la radice
		visita.put(a1, null); //a1 non scoperto da nessuno
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				//siccome il grafo non è orientato io non so hi ha visitato che per primo 
				//quindi devo fare questo controllo
				if (visita.containsKey(airport1)&&!visita.containsKey(airport2)) {
					//a1 è il padre di a2 e l'ho gia visitato
					visita.put(airport2, airport1);
				} else if (visita.containsKey(airport2)&&!visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		
		while (it.hasNext()) {
			it.next();
		}
		
		//dall'albero di visita a ritroso posso andare da a2 ad a1 e costruire il percorso
		//aggiungo destinazione e risalgo la mappa 
		percorso.add(a2);
		
		Airport step = a2;
		while(visita.get(step)!=null) {
			step = visita.get(step); //recupero il padre
			//lo aggiungo al percorso
			percorso.add(step);
		}
		
		
		
		return percorso;
		
		
		
		
		
		
	}
	
	
	
}
