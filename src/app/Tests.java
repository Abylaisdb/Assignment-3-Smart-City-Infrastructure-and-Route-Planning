package app;

import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

public final class Tests {
    private static int passed = 0, failed = 0;

    private static void check(boolean cond, String name) {
        if (cond) passed++;
        else { failed++; System.out.println("FAIL: " + name); }
    }

    private static Graph build(int n, boolean directed, int[][] edges) {
        Graph g = new Graph(n, directed);
        for (int[] e : edges) g.addEdge(e[0], e[1], e.length > 2 ? e[2] : 1);
        return g;
    }

    public static void main(String[] args) {
        testDSU();
        testSingleVertex();
        testKnownMST();
        testDisconnectedMST();
        testDuplicateWeightsMultipleMSTs();
        testRandomMSTAgreement();
        testTopologicalSort();
        testSCC();
        testDijkstra();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void testDSU() {
        for (boolean pc : new boolean[]{true, false}) {
            for (boolean ubr : new boolean[]{true, false}) {
                String tag = " [pc=" + pc + ", rank=" + ubr + "]";
                DSU d = new DSU(6, pc, ubr);
                check(d.components() == 6, "DSU initial components" + tag);
                check(d.union(0, 1), "DSU union new" + tag);
                check(!d.union(1, 0), "DSU union same set returns false" + tag);
                check(d.union(2, 3) && d.union(1, 3), "DSU union chain" + tag);
                check(d.connected(0, 2), "DSU connected transitive" + tag);
                check(!d.connected(0, 5), "DSU not connected" + tag);
                check(d.components() == 3, "DSU component count" + tag);
            }
        }
    }

    static void testSingleVertex() {
        Graph g = new Graph(1, false);
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(k.totalWeight() == 0 && k.connected() && k.edges().isEmpty(), "Kruskal single vertex");
        check(p.totalWeight() == 0 && p.connected() && p.edges().isEmpty(), "Prim single vertex");

        Graph d = new Graph(1, true);
        TopologicalSort.Result t = TopologicalSort.sort(d);
        check(t.isDag() && t.order().equals(List.of(0)), "Topo single vertex");
        check(SCC.tarjan(d).components().size() == 1, "SCC single vertex");
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check(r.dist[0] == 0 && r.reachableCount() == 1, "Dijkstra single vertex");
    }

    static void testKnownMST() {
        Graph g = build(5, false, new int[][]{
                {0, 1, 2}, {0, 3, 6}, {1, 2, 3}, {1, 3, 8}, {1, 4, 5}, {2, 4, 7}, {3, 4, 9}});
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(k.totalWeight() == 16 && p.totalWeight() == 16, "known MST weight is 16");
        check(k.connected() && p.connected(), "known MST connected");
        check(k.edges().size() == 4 && p.edges().size() == 4, "known MST has V-1 edges");
    }

    static void testDisconnectedMST() {
        Graph g = build(6, false, new int[][]{{0, 1, 1}, {1, 2, 2}, {3, 4, 3}}); // vertex 5 isolated
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(!k.connected() && !p.connected(), "disconnected graph detected");
        check(k.totalWeight() == 6 && p.totalWeight() == 6, "forest weight on disconnected graph");
        check(k.edges().size() == 3 && p.edges().size() == 3, "forest edge count");
    }

    static void testDuplicateWeightsMultipleMSTs() {
        // 4-cycle plus a diagonal, all weights equal => many valid MSTs, all of weight 3
        Graph g = build(4, false, new int[][]{{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {3, 0, 1}, {0, 2, 1}});
        check(Kruskal.run(g).totalWeight() == 3 && Prim.run(g).totalWeight() == 3, "duplicate weights MST = 3");
    }

    static void testRandomMSTAgreement() {
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.connectedUndirected(30, 90, seed, 20); // small weight range => many ties
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            check(g.isConnected(), "generator produces connected graph, seed " + seed);
            check(k.totalWeight() == p.totalWeight(), "Kruskal == Prim on connected graph, seed " + seed);
            check(k.edges().size() == 29 && p.edges().size() == 29, "MST has V-1 edges, seed " + seed);
            check(isAcyclic(30, k.edges()) && isAcyclic(30, p.edges()), "MST is acyclic, seed " + seed);
            for (boolean pc : new boolean[]{true, false})
                for (boolean ubr : new boolean[]{true, false})
                    check(Kruskal.run(g, pc, ubr).totalWeight() == k.totalWeight(), "DSU variants agree, seed " + seed);
        }
        // possibly disconnected random graphs: forests must have equal weight
        Random r = new Random(7);
        for (int t = 0; t < 100; t++) {
            Graph g = new Graph(15, false);
            for (int i = 0; i < 12; i++) {
                int u = r.nextInt(15), v = r.nextInt(15);
                if (u != v) g.addEdge(u, v, 1 + r.nextInt(10));
            }
            check(Kruskal.run(g).totalWeight() == Prim.run(g).totalWeight(), "forest weights agree, trial " + t);
            check(Kruskal.run(g).connected() == g.isConnected(), "Kruskal connectivity flag, trial " + t);
            check(Prim.run(g).connected() == g.isConnected(), "Prim connectivity flag, trial " + t);
        }
    }

    private static boolean isAcyclic(int n, List<Edge> edges) {
        DSU d = new DSU(n);
        for (Edge e : edges) if (!d.union(e.from(), e.to())) return false;
        return true;
    }

    static void testTopologicalSort() {
        Graph dag = build(6, true, new int[][]{{5, 2}, {5, 0}, {4, 0}, {4, 1}, {2, 3}, {3, 1}});
        TopologicalSort.Result r = TopologicalSort.sort(dag);
        check(r.isDag() && TopologicalSort.isValidOrder(dag, r.order()), "topo order valid on small DAG");

        Graph cyc = build(3, true, new int[][]{{0, 1}, {1, 2}, {2, 0}});
        check(!TopologicalSort.sort(cyc).isDag(), "3-cycle detected");
        Graph selfLoop = build(2, true, new int[][]{{0, 0}, {0, 1}});
        check(!TopologicalSort.sort(selfLoop).isDag(), "self-loop detected as cycle");

        for (int seed = 0; seed < 50; seed++) {
            Graph d = GraphGenerator.directed(40, 150, seed, 10, true);
            TopologicalSort.Result t = TopologicalSort.sort(d);
            check(t.isDag() && TopologicalSort.isValidOrder(d, t.order()), "topo valid on random DAG, seed " + seed);
            Graph c = GraphGenerator.directed(40, 300, seed, 10, false);
            check(!TopologicalSort.sort(c).isDag(), "cycle found in dense random digraph, seed " + seed);
        }
        Graph empty = new Graph(4, true);
        check(TopologicalSort.sort(empty).isDag(), "graph without edges is a DAG");
    }

    static void testSCC() {
        Graph g = build(7, true, new int[][]{{0, 1}, {1, 2}, {2, 0}, {2, 3}, {3, 4}, {4, 5}, {5, 3}, {6, 5}});
        SCC.Result r = SCC.tarjan(g);
        int[] sizes = r.components().stream().mapToInt(List::size).sorted().toArray();
        check(Arrays.equals(sizes, new int[]{1, 3, 3}), "SCC sizes on known graph");
        check(r.componentId()[0] == r.componentId()[1] && r.componentId()[1] == r.componentId()[2], "SCC {0,1,2}");
        check(r.componentId()[3] == r.componentId()[4] && r.componentId()[4] == r.componentId()[5], "SCC {3,4,5}");
        check(r.componentId()[6] != r.componentId()[5], "vertex 6 alone");

        Random rnd = new Random(11);
        for (int t = 0; t < 100; t++) {
            int n = 12;
            Graph h = new Graph(n, true);
            for (int i = 0; i < 18; i++) {
                int u = rnd.nextInt(n), v = rnd.nextInt(n);
                if (u != v) h.addEdge(u, v, 1);
            }
            SCC.Result s = SCC.tarjan(h);
            int total = 0;
            for (List<Integer> c : s.components()) total += c.size();
            check(total == n, "every vertex in exactly one SCC (count), trial " + t);
            boolean allAssigned = true;
            for (int c : s.componentId()) if (c < 0) allAssigned = false;
            check(allAssigned, "every vertex has a component id, trial " + t);

            boolean[][] reach = reachability(h);
            boolean ok = true;
            for (int u = 0; u < n; u++)
                for (int v = 0; v < n; v++)
                    if ((s.componentId()[u] == s.componentId()[v]) != (reach[u][v] && reach[v][u])) ok = false;
            check(ok, "SCC matches brute-force mutual reachability, trial " + t);
        }
        Graph dag = GraphGenerator.directed(30, 100, 3, 5, true);
        check(SCC.tarjan(dag).components().size() == 30, "DAG => all components singletons");
    }

    private static boolean[][] reachability(Graph g) {
        int n = g.V();
        boolean[][] r = new boolean[n][n];
        for (int i = 0; i < n; i++) r[i][i] = true;
        for (Edge e : g.edges()) r[e.from()][e.to()] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (r[i][k] && r[k][j]) r[i][j] = true;
        return r;
    }

    static void testDijkstra() {
        Graph g = build(5, false, new int[][]{{0, 1, 4}, {0, 2, 1}, {2, 1, 2}, {1, 3, 1}, {2, 3, 5}}); // vertex 4 isolated
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check(r.dist[0] == 0 && r.dist[1] == 3 && r.dist[2] == 1 && r.dist[3] == 4, "Dijkstra known distances");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "Dijkstra known path");
        check(!r.reachable(4) && r.dist[4] == Dijkstra.INF && r.pathTo(4).isEmpty(), "unreachable vertex handled");
        check(r.reachableCount() == 4, "reachable count");

        for (int seed = 0; seed < 50; seed++) {
            Graph u = GraphGenerator.connectedUndirected(40, 120, seed, 50);
            check(Arrays.equals(Dijkstra.run(u, 0).dist, bellmanFord(u, 0)), "Dijkstra == Bellman-Ford (undirected), seed " + seed);
            Graph d = GraphGenerator.directed(40, 60, seed, 50, false); // sparse => some vertices unreachable
            Dijkstra.Result dr = Dijkstra.run(d, 0);
            check(Arrays.equals(dr.dist, bellmanFord(d, 0)), "Dijkstra == Bellman-Ford (directed), seed " + seed);
            boolean pathsOk = true;
            for (int v = 0; v < d.V(); v++) {
                if (!dr.reachable(v)) continue;
                List<Integer> p = dr.pathTo(v);
                long sum = 0;
                for (int i = 0; i + 1 < p.size(); i++) sum += minEdge(d, p.get(i), p.get(i + 1));
                if (p.get(0) != 0 || p.get(p.size() - 1) != v || sum != dr.dist[v]) pathsOk = false;
            }
            check(pathsOk, "path weights equal distances, seed " + seed);
        }

        Graph neg = build(3, true, new int[][]{{0, 1, 5}, {1, 2, -3}});
        boolean threw = false;
        try { Dijkstra.run(neg, 0); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "negative weight rejected");
    }

    private static long minEdge(Graph g, int u, int v) {
        long best = Long.MAX_VALUE;
        for (Edge e : g.neighbors(u)) if (e.to() == v) best = Math.min(best, e.weight());
        return best;
    }

    private static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.V()];
        Arrays.fill(d, Dijkstra.INF);
        d[s] = 0;
        for (int i = 0; i < g.V(); i++) {
            for (Edge e : g.edges()) {
                if (d[e.from()] != Dijkstra.INF && d[e.from()] + e.weight() < d[e.to()]) d[e.to()] = d[e.from()] + e.weight();
                if (!g.isDirected() && d[e.to()] != Dijkstra.INF && d[e.to()] + e.weight() < d[e.from()])
                    d[e.from()] = d[e.to()] + e.weight();
            }
        }
        return d;
    }
}
