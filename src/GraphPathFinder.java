import java.util.*;

class Aresta {
    String target;
    double weight;

    Aresta(String target, double weight) {
        this.target = target;
        this.weight = weight;
    }
}

class Path implements Comparable<Path> {
    List<String> nodes;
    double distance;

    Path(List<String> nodes, double distance) {
        this.nodes = new ArrayList<>(nodes);
        this.distance = distance;
    }

    @Override
    public int compareTo(Path other) {
        return Double.compare(this.distance, other.distance);
    }

    @Override
    public String toString() {
        return String.join(" -> ", nodes) + " (" + distance + " m)";
    }
}

public class GraphPathFinder {
    private Map<String, List<Aresta>> graph = new HashMap<>();

    public void addAresta(String a, String b, double w) {
        graph.computeIfAbsent(a, k -> new ArrayList<>()).add(new Aresta(b, w));
    }


    private Path dijkstra(String source, String target) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Path> pq = new PriorityQueue<>();

        for (String node : graph.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);
        pq.add(new Path(Arrays.asList(source), 0.0));

        while (!pq.isEmpty()) {
            Path curPath = pq.poll();
            String u = curPath.nodes.get(curPath.nodes.size() - 1);
            double d = curPath.distance;

            if (d > dist.get(u)) continue;
            if (u.equals(target)) {
                return curPath;
            }

            for (Aresta e : graph.getOrDefault(u, Collections.emptyList())) {
                double alt = d + e.weight;
                if (alt < dist.get(e.target)) {
                    dist.put(e.target, alt);
                    prev.put(e.target, u);

                    List<String> newPath = new ArrayList<>(curPath.nodes);
                    newPath.add(e.target);
                    pq.add(new Path(newPath, alt));
                }
            }
        }
        return null; 
    }

    
    public List<Path> kShortestPaths(String source, String target, int k) {
        List<Path> result = new ArrayList<>();
        PriorityQueue<Path> candidates = new PriorityQueue<>();

        Path shortest = dijkstra(source, target);
        if (shortest == null) {
            return result;
        }
        result.add(shortest);

        for (int i = 1; i < k; i++) {
            for (int j = 0; j < result.get(i - 1).nodes.size() - 1; j++) {
                String spurNode = result.get(i - 1).nodes.get(j);
                List<String> rootPath = result.get(i - 1).nodes.subList(0, j + 1);

               
                Map<String, List<Aresta>> removedArestas = new HashMap<>();
                for (Path p : result) {
                    if (p.nodes.size() > j && rootPath.equals(p.nodes.subList(0, j + 1))) {
                        String u = p.nodes.get(j);
                        String v = p.nodes.get(j + 1);
                     
                        List<Aresta> list = graph.get(u);
                        if (list != null) {
                            Aresta toRemove = null;
                            for (Aresta e : list) {
                                if (e.target.equals(v)) {
                                    toRemove = e;
                                    break;
                                }
                            }
                            if (toRemove != null) {
                                removedArestas.computeIfAbsent(u, x -> new ArrayList<>()).add(toRemove);
                                list.remove(toRemove);
                            }
                        }
                    }
                }

              
                Path spurPath = dijkstra(spurNode, target);
                if (spurPath != null) {
                    
                    List<String> total = new ArrayList<>(rootPath);
                    total.addAll(spurPath.nodes.subList(1, spurPath.nodes.size()));
                    double totalDist = 0;
                    for (int idx = 0; idx < total.size() - 1; idx++) {
                        String u = total.get(idx);
                        String v = total.get(idx + 1);
                        for (Aresta e : graph.get(u)) {
                            if (e.target.equals(v)) {
                                totalDist += e.weight;
                                break;
                            }
                        }
                    }
                    candidates.add(new Path(total, totalDist));
                }

              
                for (Map.Entry<String, List<Aresta>> entry : removedArestas.entrySet()) {
                    graph.get(entry.getKey()).addAll(entry.getValue());
                }
            }

            if (candidates.isEmpty()) break;
            result.add(candidates.poll());
        }
        return result;
    }

    public static void main(String[] args) {
        GraphPathFinder gp = new GraphPathFinder();

        String[][] data = {
            {"A","B","300"}, {"B","C","47"}, {"C","D","62"}, {"D","E","8"},
            {"E","G","230"}, {"E","F","13"}, {"C","H","141"}, {"H","I","138"},
            {"I","J","153"}, {"J","K","512"}, {"K","L","135"}, {"L","M","50"}, 
            {"L","N","187"}, {"N","O","108"}, {"O","P","82"}, {"P","Q","215"},
            {"Q","R","97"}, {"R","T","243"}, {"R","S","33"}, {"S","T","207"},
            {"S","V","38"}, {"V","U","210"}, {"T","U","22"}, {"U","X","107"},
            {"V","A","307"}, {"X","A","317"}
        };
        for (String[] e : data) {
            double w = Double.parseDouble(e[2]);
            gp.addAresta(e[0], e[1], w);
            gp.addAresta(e[1], e[0], w);
        }
        
 Scanner sc = new Scanner(System.in);
    boolean continuar = true;

    while (continuar) {
        System.out.print("\nPartida: ");
        String src = sc.nextLine().trim().toUpperCase();

        System.out.print("Chegada: ");
        String dst = sc.nextLine().trim().toUpperCase();

        List<Path> paths = gp.kShortestPaths(src, dst, 2);
        if (paths.isEmpty()) {
            System.out.println("Nenhum caminho encontrado de " + src + " até " + dst + ".");
        } else {
            System.out.println("Caminhos encontrados (até 2, ordenados por distância):");
            for (int i = 0; i < paths.size(); i++) {
                System.out.printf("%d: %s\n", i + 1, paths.get(i));
            }
        }

        System.out.print("Deseja continuar? (s/n): ");
        String resp = sc.nextLine().trim().toLowerCase();
        if (resp.equals("n")) {
            continuar = false;
        }
    }

    sc.close();
    System.out.println("Programa encerrado.");
}
}