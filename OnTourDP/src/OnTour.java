import java.io.*;
import java.util.*;

class Node {
    int id;
    int value;
    int worth;
    List<Integer> neighbors;

    public Node(int id, int value, int worth) {
        this.id = id;
        this.value = value;
        this.worth = worth;
        this.neighbors = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Node{id=%d, value=%d, worth=%d, neighbors=%s}", id, value, worth, neighbors);
    }
}


public class OnTour {
    // read file and creat an adjacency list to store the undirected graph
    public static int[][] readGraphFromFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int n = Integer.parseInt(reader.readLine().trim());
        int[][] data = new int[n][];

        for (int i = 0; i < n; i++) {
            String[] parts = reader.readLine().trim().split("\\s+");
            data[i] = new int[parts.length];

            for (int j = 0; j < parts.length; j++) {
                data[i][j] = Integer.parseInt(parts[j]);
            }
        }

        reader.close();
        return data;
    }

    public static Node[] buildDirectedGraph(int[][] rawData) {
        int n = rawData.length;
        Node[] graph = new Node[n];

        for (int i = 0; i < n; i++) {
            int value = rawData[i][0];
            int worth = rawData[i][1];
            graph[i] = new Node(i, value, worth);
        }

        // save sides with neighbor value smaller(ingoing)
        for (int i = 0; i < n; i++) {
            int curValue = rawData[i][0];
            int neighborCount = rawData[i][2];

            for (int j = 0; j < neighborCount; j++) {
                int neighborId = rawData[i][3 + j] - 1;
                int neighborValue = rawData[neighborId][0];

                if (neighborValue < curValue) {
                    graph[i].neighbors.add(neighborId);// neighbor id
                }
            }
        }

        // big to small sort
        Arrays.sort(graph, (a, b) -> Integer.compare(b.value, a.value));

        return graph;
    }


    public static int computeMaxScore(
            //f(i) = p(i) + max(f(j)) over neighbors(j)
            int id,
            Map<Integer, Node> idToNode, // id node combine
            Map<Integer, Integer> dp, // id to f(id)
            Map<Integer, Integer> prevPath // id of i to id of j
    ) {
        if (dp.containsKey(id)) return dp.get(id);

        Node node = idToNode.get(id);
        int maxNeighborScore = 0;
        int bestNeighbor = -1;

        for (int neighborId : node.neighbors) {
            int score = computeMaxScore(neighborId, idToNode, dp, prevPath);
            if (score > maxNeighborScore) {
                maxNeighborScore = score;
                bestNeighbor = neighborId;
            }
        }

        dp.put(id, node.worth + maxNeighborScore);

        if (bestNeighbor != -1) {
            prevPath.put(id, bestNeighbor);  // id to nextId
        }

        return dp.get(id);
    }

    public static void writeResultToFile(String outputFileName, int score, List<Integer> path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        writer.write(String.valueOf(score));
        writer.newLine();

        for (int i = 0; i < path.size(); i++) {
            writer.write(path.get(i) + (i < path.size() - 1 ? " " : ""));
        }
        writer.newLine();

        writer.close();
    }




    public static void main(String[] args) throws IOException {
        String fileName = "example-input.txt";
        //String fileName = "small-input.txt";
        int[][] undirectedData = readGraphFromFile(fileName);

//        for (int i = 0; i < undirectedData.length; i++) {
//            System.out.print("Line " + (i + 1) + ": ");
//            for (int val : undirectedData[i]) {
//                System.out.print(val + " ");
//            }
//            System.out.println();
//        }

        Node[] graph = buildDirectedGraph(undirectedData);
//        // print graph
//        for (int i = 0; i < graph.length; i++) {
//            System.out.printf("Node #%d: %s%n", i + 1, graph[i]);
//        }


        Map<Integer, Node> idToNode = new HashMap<>();
        Map<Integer, Integer> dp = new HashMap<>();
        Map<Integer, Integer> prevPath = new HashMap<>();
        for (Node node : graph) {
            idToNode.put(node.id, node);
        }

        int maxScore = 0;
        int startId = -1;

        for (Node node : graph) {
            int score = computeMaxScore(node.id, idToNode, dp, prevPath);
            if (score > maxScore) {
                maxScore = score;
                startId = node.id;
            }
        }
//        System.out.println("Max score: " + maxScore);
//        System.out.print("Path: ");

        List<Integer> path = new ArrayList<>();
        for (int at = startId; at != -1; at = prevPath.getOrDefault(at, -1)) {
            path.add(idToNode.get(at).value); // id to value
        }
        Collections.reverse(path); //reverse to small to big

//        for (int i = 0; i < path.size(); i++) {
//            System.out.print(path.get(i));
//            if (i < path.size() - 1) System.out.print(" → ");
//        }
//        System.out.println();

        String outputFile = "output.txt";
        writeResultToFile(outputFile, maxScore, path);
        System.out.println("written successfully");
    }
}