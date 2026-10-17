package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        this.n = n;
        this.directed = directed;
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed) adj.get(v).add(new Edge(v, u, w));
    }

    public int V() { return n; }
    public int E() { return edges.size(); }
    public boolean isDirected() { return directed; }

    public List<Edge> neighbors(int u) {
        checkVertex(u);
        return Collections.unmodifiableList(adj.get(u));
    }

    public List<Edge> edges() { return Collections.unmodifiableList(edges); }

    public int connectedComponents() {
        if (directed) throw new IllegalStateException("Use SCC for directed graphs");
        boolean[] seen = new boolean[n];
        int comps = 0;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int s = 0; s < n; s++) {
            if (seen[s]) continue;
            comps++;
            seen[s] = true;
            queue.add(s);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Edge e : adj.get(u)) {
                    if (!seen[e.to()]) {
                        seen[e.to()] = true;
                        queue.add(e.to());
                    }
                }
            }
        }
        return comps;
    }

    public boolean isConnected() { return n <= 1 || connectedComponents() == 1; }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("vertex out of range: " + v);
    }
}
