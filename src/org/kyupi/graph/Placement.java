package org.kyupi.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.StringFilter;

public class Placement {

	private static Logger log = Logger.getLogger(Placement.class);

	private Graph graph;

	HashMap<Node, Integer> placeX = new HashMap<>();
	HashMap<Node, Integer> placeY = new HashMap<>();
	HashSet<Integer> distinctX = new HashSet<>();
	HashSet<Integer> distinctY = new HashSet<>();

	public Placement(Graph g) {
		graph = g;
	}

	public void parseDefFile(String def_file_name, StringFilter name_filter) throws IOException {

		int minx = Integer.MAX_VALUE, maxx = 0, miny = Integer.MAX_VALUE, maxy = 0;

		FileReader fr = new FileReader(def_file_name);
		log.info("Loading " + def_file_name);
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		while ((s = br.readLine()) != null) {
			if (s.contains("+ PLACED (")) {
				String c[] = s.split("[ ]+");
				int placed = 5;
				for (int i = 0; i < c.length; i++) {
					if ("PLACED".equals(c[i])) {
						placed = i;
						break;
					}
				}
				String name = c[2];
				if (name_filter != null) {
					name = name_filter.filter(name);
				}

				Integer posx = Integer.decode(c[placed + 2]);
				Integer posy = Integer.decode(c[placed + 3]);

				Node n = graph.searchNode(name);
				if (n == null) {
					log.info("Node not found: " + name);
				} else {
					placeX.put(n, posx);
					placeY.put(n, posy);
					distinctX.add(posx);
					distinctY.add(posy);

					minx = Math.min(minx, posx);
					miny = Math.min(minx, posy);
					maxx = Math.max(maxx, posx);
					maxy = Math.max(maxy, posy);
				}
				// log.info("POS " + name + " " + posx + " " + posy);
			}
		}
		br.close();

		log.info("CellPositions " + placeX.size());
		log.info("PositionBoundary (" + minx + "," + miny + ") (" + maxx + "," + maxy + ")");
		log.info("DistinctX " + distinctX.size());
		log.info("DistinctY " + distinctY.size());

		for (Node n : graph.accessNodes()) {
			if (n == null || n.isPseudo())
				continue;
			if (!placeX.containsKey(n)) {
				log.info("Missing place annotation for " + n.queryName());
			}
		}
	}
	
	public int getX(Node n) {
		Integer x = placeX.get(n);
		if (x == null)
			throw new NoSuchElementException(n.toString());
		return x;
	}
	
	public int getY(Node n) {
		Integer y = placeY.get(n);
		if (y == null)
			throw new NoSuchElementException(n.toString());
		return y;
	}

	public boolean containsNode(Node n) {
		return placeX.containsKey(n);
	}
	
	
}
