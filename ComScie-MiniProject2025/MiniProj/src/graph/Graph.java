/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package graph;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * Converts an image to a graph where:
 * - Each pixel is a node (Point)
 * - Adjacent pixels are connected with edges
 * - Supports 4-directional or 8-directional connectivity
 */
public class Graph implements IGraph<Point> {
    public final int width, height;
    private final Map<Point, Node> nodes ;
    private final boolean[][] walkable;
    private int edgeCount;
    private final int connectivity;
    
    private final Map<Point, ZoneType> zoneClassification;
    private final Map<ZoneType, Color> zoneColors;
    
    // Add these constants to the existing fields
    private static final Color EXIT_COLOR = Color.rgb(14, 111, 34);
    private static final Color DANGER_COLOR = Color.rgb(243, 25, 41);
    private static final Color WALL_COLOR = Color.BLACK;
    
    // 4-directional movements (up, down, left, right)
    private static final int[][] FOUR_DIRECTIONS = {{0,1}, {1,0}, {0,-1}, {-1,0}};
    // 8-directional movements (includes diagonals)
    private static final int[][] EIGHT_DIRECTIONS = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};

    public Graph(PixelReader reader, int width, int height, boolean use8Directions) {
        this.width = width;
        this.height = height;
        this.nodes = new HashMap<>();
        this.walkable = new boolean[width][height];
        this.connectivity = use8Directions ? 8 : 4;
        
        // Create nodes for all pixels
        createAllNodes(reader);
        
        // Connect adjacent pixels
        connectAdjacentNodes();
        
        //Image Classification
        this.zoneClassification = new HashMap<>();
        this.zoneColors = new HashMap<>();
        initializeZoneColors();
        classifyZones(reader);
    }

    /**Image Classification **/
    private void initializeZoneColors() {
        zoneColors.put(ZoneType.EXIT, EXIT_COLOR);
        zoneColors.put(ZoneType.DANGER, DANGER_COLOR);
        zoneColors.put(ZoneType.WALL, WALL_COLOR);
        zoneColors.put(ZoneType.WALKABLE, Color.WHITE);
    }
    
    private void classifyZones(PixelReader reader) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = reader.getColor(x, y);
                Point p = new Point(x, y);
                
                // Use color proximity instead of exact match
                if (colorDistance(color, EXIT_COLOR) < 0.2) {
                    zoneClassification.put(p, ZoneType.EXIT);
                    walkable[x][y] = true;
                } 
                else if (colorDistance(color, DANGER_COLOR) < 0.2) {
                    zoneClassification.put(p, ZoneType.DANGER);
                    walkable[x][y] = false; // Make danger zones unwalkable
                }
                else if (colorDistance(color, WALL_COLOR) < 0.2) {
                    zoneClassification.put(p, ZoneType.WALL);
                    walkable[x][y] = false;
                }
                else {
                    zoneClassification.put(p, ZoneType.WALKABLE);
                    walkable[x][y] = isWalkable(color);
                }
            }
        }
    }
    
    private double colorDistance(Color c1, Color c2) {
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        double b = c1.getBlue() - c2.getBlue();
        return Math.sqrt(r*r + g*g + b*b);
    }
    
    // zone analysis
    public ZoneType getZoneType(Point point) {
        return zoneClassification.getOrDefault(point, ZoneType.WALL);
    }
    
    public List<Point> getAllZonesOfType(ZoneType type) {
        List<Point> zones = new ArrayList<>();
        for (Map.Entry<Point, ZoneType> entry : zoneClassification.entrySet()) {
            if (entry.getValue() == type) {
                zones.add(entry.getKey());
            }
        }
        return zones;
    }
    
    
    
    public List<Point> findSafeEvacuationPath(Point start) {
        // Get all exit points
        List<Point> exits = getAllZonesOfType(ZoneType.EXIT);
        if (exits.isEmpty()) return null;
        
        // Find nearest exit
        Point nearestExit = findNearestExit(start, exits);
        if (nearestExit == null) return null;
        
        // Use A* with danger avoidance
        return aStar(start, nearestExit);
    }
    
    
    private Point findNearestExit(Point start, List<Point> exits) {
        Point nearest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Point exit : exits) {
            int dist = heuristic(start, exit);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = exit;
            }
        }
        
        return nearest;
    }
    /**End of Image Classification**/
    
    
    
    private void createAllNodes(PixelReader reader) {
    	 // Creates nodes for walkable areas 
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = reader.getColor(x, y);
                walkable[x][y] = color.getBrightness() > 0.7;
                if (walkable[x][y]) {
                    Point p = new Point(x, y);
                    nodes.put(p, new Node(p));
                }
            }
        }
    }

    private void connectAdjacentNodes() {
        int[][] directions = (connectivity == 8) ? EIGHT_DIRECTIONS : FOUR_DIRECTIONS;
        
        for (Node node : nodes.values()) {
            Point pos = node.position;
            
            for (int[] dir : directions) {
                int nx = pos.x + dir[0];
                int ny = pos.y + dir[1];
                
                if (isValidPosition(nx, ny)) {
                    Point neighborPos = new Point(nx, ny);
                    if (walkable[nx][ny] && walkable[pos.x][pos.y]) {
                        node.edges.add(new Edge(node, nodes.get(neighborPos), 1));
                        edgeCount++;
                    }
                }
            }
        }
        
        // Since edges are bidirectional, we've double-counted them
        edgeCount /= 2;
    }

    private boolean isWalkable(Color color) {
        // Customize this based on your image characteristics
        // Example: Treat non-white pixels as walkable
        return color.getBrightness() >0.3;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // GraphADT implementation
    @Override
    public void addVertex(Point vertex) {
        if (!nodes.containsKey(vertex)) {
            nodes.put(vertex, new Node(vertex));
            walkable[vertex.x][vertex.y] = true;
        }
    }

    @Override
    public void addEdge(Point source, Point destination) {
        addEdge(source, destination, 1);
    }

    @Override
    public void addEdge(Point source, Point destination, int weight) {
        if (!nodes.containsKey(source) || !nodes.containsKey(destination)) {
            throw new IllegalArgumentException("Points not in graph");
        }
        
        Node src = nodes.get(source);
        Node dest = nodes.get(destination);
        
        if (!hasEdge(source, destination)) {
            src.edges.add(new Edge(src, dest, weight));
            dest.edges.add(new Edge(dest, src, weight));
            edgeCount++;
        }
    }

    @Override
    public void removeVertex(Point vertex) {
        if (!nodes.containsKey(vertex)) return;
        
        Node node = nodes.get(vertex);
        edgeCount -= node.edges.size();
        
        // Remove all edges pointing to this node
        for (Edge edge : node.edges) {
            Node neighbor = edge.to;
            neighbor.edges.removeIf(e -> e.to.equals(node));
        }
        
        nodes.remove(vertex);
        walkable[vertex.x][vertex.y] = false;
    }

    @Override
    public void removeEdge(Point source, Point destination) {
        if (!nodes.containsKey(source) || !nodes.containsKey(destination)) return;
        
        Node src = nodes.get(source);
        Node dest = nodes.get(destination);
        
        boolean removed1 = src.edges.removeIf(e -> e.to.equals(dest));
        boolean removed2 = dest.edges.removeIf(e -> e.to.equals(src));
        
        if (removed1 && removed2) {
            edgeCount--;
        }
    }

    @Override
    public boolean hasVertex(Point vertex) {
        return nodes.containsKey(vertex);
    }

    @Override
    public boolean hasEdge(Point source, Point destination) {
        if (!nodes.containsKey(source) || !nodes.containsKey(destination)) return false;
        return nodes.get(source).edges.stream().anyMatch(e -> e.to.position.equals(destination));
    }

    @Override
    public int getVertexCount() {
        return nodes.size();
    }

    @Override
    public int getEdgeCount() {
        return edgeCount;
    }

    @Override
    public List<Point> getNeighbors(Point vertex) {
        if (!nodes.containsKey(vertex)) return Collections.emptyList();
        
        List<Point> neighbors = new ArrayList<>();
        for (Edge edge : nodes.get(vertex).edges) {
            neighbors.add(edge.to.position);
        }
        return neighbors;
    }

    
    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(nodes.keySet());
    }

    public List<Point> aStar(Point start, Point goal) {
        Node startNode = getNodeAt(start);
        Node goalNode = getNodeAt(goal);
        if (startNode == null || goalNode == null) return null;

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Integer> gScore = new HashMap<>();
        gScore.put(startNode, 0);

        Map<Node, Integer> fScore = new HashMap<>();
        fScore.put(startNode, heuristic(startNode.position, goalNode.position));

        openSet.add(new AStarNode(startNode, fScore.get(startNode)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll().node;
            if (current == goalNode) {
                return constructPath(cameFrom, current);
            }

            for (Edge edge : current.edges) {
                Node neighbor = edge.to;
                int tentativeGScore = gScore.get(current) + getEdgeCost(edge);
                
                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic(neighbor.position, goalNode.position));
                    openSet.add(new AStarNode(neighbor, fScore.get(neighbor)));
                }
            }
        }
        return null;
    }
    
    private int getEdgeCost(Edge edge) {
        Point pos = edge.to.position;
        ZoneType type = getZoneType(pos);
        
        // Higher cost for dangerous areas
        switch (type) {
            case DANGER: return Integer.MAX_VALUE/2; // Effectively impassable
            case WALKABLE: return 1;
            case EXIT: return 0;
            default: return Integer.MAX_VALUE; // Very high cost for walls
        }
    }
    /**
     * Retrieves the Node at a specific point in the graph
     * @param point The coordinates to look up
     * @return The Node at the given point, or null if no node exists there
     */
    public Node getNodeAt(Point point) {
        // First check if the point is within graph bounds
        if (point == null || 
            point.x < 0 || point.x >= width || 
            point.y < 0 || point.y >= height) {
            return null;
        }
        
        // Check if the pixel at this point is walkable
        if (!walkable[point.x][point.y]) {
            return null;
        }
        
        // Create a temporary point object for lookup
        Point lookupPoint = new Point(point.x, point.y);
        
        // Return the node from our nodes map
        return nodes.get(lookupPoint);
    }
        
	//calculates the heuristic distance between 2 points
    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

  //helper method to draw path from 2 Nodes
    private List<Point> constructPath(Map<Node, Node> cameFrom, Node current) {
        List<Point> path = new ArrayList<>();
        path.add(current.position);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current.position);
        }
        
        
        Collections.reverse(path);
        return path;
    }


    

 

    
}



