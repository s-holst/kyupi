package org.kyupi.circuit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.kyupi.misc.StringFilter;

public class Placement {

	private static Logger log = Logger.getLogger(Placement.class);

	private Circuit circuit;

	HashMap<Cell, Integer> placeX = new HashMap<>();
	HashMap<Cell, Integer> placeY = new HashMap<>();
	HashSet<Integer> distinctX = new HashSet<>();
	HashSet<Integer> distinctY = new HashSet<>();

	Cell[][] place = new Cell[0][0];

	int[] coordX = new int[0];
	int[] coordY = new int[0];
	HashMap<Integer, Integer> coordXmap = new HashMap<>();
	HashMap<Integer, Integer> coordYmap = new HashMap<>();

	public Placement(Circuit circuit) {
		this.circuit = circuit;
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

				Cell n = circuit.searchNode(name);
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

		int i = 0;
		coordX = new int[distinctX.size()];
		for (Integer x : distinctX)
			coordX[i++] = x;
		Arrays.sort(coordX);
		for (i = 0; i < coordX.length; i++)
			coordXmap.put(coordX[i], i);

		i = 0;
		coordY = new int[distinctY.size()];
		for (Integer y : distinctY)
			coordY[i++] = y;
		Arrays.sort(coordY);
		for (i = 0; i < coordY.length; i++)
			coordYmap.put(coordY[i], i);

		place = new Cell[coordX.length][coordY.length];

		for (Cell n : circuit.cells()) {
			if (n == null || n.isPseudo())
				continue;
			if (!placeX.containsKey(n)) {
				log.info("Missing place annotation for " + n.queryName());
			} else {
				place[coordXmap.get(placeX.get(n))][coordYmap.get(placeY.get(n))] = n;
			}
		}
		int minCellXDist = Integer.MAX_VALUE;
		for (i = 0; i < coordX.length - 1; i++) {
			minCellXDist = Math.min(minCellXDist, coordX[i + 1] - coordX[i]);
		}

		HashMap<Integer, Integer> heightHist = new HashMap<>();
		int minCellYDist = Integer.MAX_VALUE;
		for (i = 0; i < coordY.length - 1; i++) {
			int height = coordY[i + 1] - coordY[i];
			int cnt = heightHist.getOrDefault(height, 0);
			heightHist.put(height, cnt + 1);
			minCellYDist = Math.min(minCellYDist, height);
		}
		int rowHeight = 0;
		int commonCnt = -1;
		for (Integer h : heightHist.keySet()) {
			int cnt = heightHist.get(h);
			if (cnt > commonCnt) {
				rowHeight = h;
				commonCnt = cnt;
			}
		}
		log.info("MinCellXDist " + minCellXDist);
		log.info("MinCellYDist " + minCellYDist);
		log.info("RowHeight " + rowHeight);

		// for (i = 0 ; i < coordY.length; i++) {
		// int count = 0;
		// for (int j = 0; j < coordX.length; j++) {
		// if (place[j][i] != null)
		// count++;
		// }
		// log.debug("Row " + i + " contains " + count + " cells");
		// }

	}

	public int getX(Cell n) {
		Integer x = placeX.get(n);
		if (x == null)
			throw new NoSuchElementException(n.toString());
		return x;
	}

	public int getY(Cell n) {
		Integer y = placeY.get(n);
		if (y == null)
			throw new NoSuchElementException(n.toString());
		return y;
	}

	public boolean containsNode(Cell n) {
		return placeX.containsKey(n);
	}

	public HashSet<Cell> getRectangle(int x1, int y1, int x2, int y2) {

		// ensure, that x1/y1 are smaller than x2/y2
		if (x1 > x2) {
			int x = x1;
			x1 = x2;
			x1 = x;
		}
		if (y1 > y2) {
			int y = y1;
			y1 = y2;
			y1 = y;
		}

		// find start and end indices for place array
		int xi1 = 0, xi2 = 0;
		for (int i = 0; i < coordX.length; i++) {
			if (coordX[i] < x1)
				xi1 = i + 1;
			if (coordX[i] < x2)
				xi2 = i;
		}
		int yi1 = 0, yi2 = 0;
		for (int i = 0; i < coordY.length; i++) {
			if (coordY[i] < y1)
				yi1 = i + 1;
			if (coordY[i] < y2)
				yi2 = i;
		}

		HashSet<Cell> nodes = new HashSet<>();

		if (xi2 >= coordX.length || yi2 >= coordY.length) {
			log.warn("rectangle out of bounds, returning no cells.");
			return nodes;
		}
		//log.debug("Collecting cells in rectangle: (" + coordX[xi1] + "," + coordY[yi1] + ") (" + coordX[xi2] + ","
		//		+ coordY[yi2] + ")");


		for (int y = yi1; y <= yi2; y++) {
			for (int x = xi1; x <= xi2; x++) {
				Cell n = place[x][y];
				if (n != null)
					nodes.add(n);
			}
		}
		//log.debug("Found " + nodes.size() + " cells");
		return nodes;
	}

}
