package app;

import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;


public final class Main {
    record Config(String name, int v, int e) {}
    record Timed<T>(double ms, T value) {}

    static final int RUNS = 5;          // repetitions that are averaged
    static final int WARMUP = 20;       // untimed runs so the JIT has compiled the code
    static final long SEED = 42;
    static final int MAX_W = 100;

    public static void main(String[] args) throws Exception {
        List<String> a = Arrays.asList(args);
        boolean extended = a.contains("--extended");
        String out = a.contains("--out") ? a.get(a.indexOf("--out") + 1) : "results";
        // big stack: recursive Tarjan on large graphs
        Thread t = new Thread(null, () -> {
            try { run(extended, new File(out)); } catch (Exception ex) { ex.printStackTrace(); System.exit(1); }
        }, "bench", 512L * 1024 * 1024);
        t.start();
        t.join();
    }

    static <T> Timed<T> time(Supplier<T> s) {
        for (int i = 0; i < WARMUP; i++) s.get();
        long total = 0;
        T last = null;
        for (int i = 0; i < RUNS; i++) {
            long t0 = System.nanoTime();
            last = s.get();
            total += System.nanoTime() - t0;
        }
        return new Timed<>(total / (double) RUNS / 1e6, last);
    }

    static void run(boolean extended, File outDir) throws IOException {
        List<Config> configs = new ArrayList<>(List.of(
                new Config("Small", 10, 30), new Config("Medium", 100, 500), new Config("Large", 1000, 5000)));
        if (extended) {
            configs.add(new Config("XL-1", 2000, 10000));
            configs.add(new Config("XL-2", 5000, 25000));
        }

        Table mstT = new Table("MST: Kruskal vs Prim (connected undirected graph)",
                "Size", "V", "E", "Algorithm", "Avg time (ms)", "MST weight", "Connected", "Comparisons",
                "Edge examinations", "Structure ops (DSU find+union / PQ push+pop)");
        Table dsuT = new Table("DSU optimisations inside Kruskal (same graph, same MST weight)",
                "Size", "Variant", "Avg time (ms)", "DSU find+union calls", "DSU pointer steps");
        Table dsuStress = new Table("DSU stress test: chain union(i,i+1) for all i, then find(i) for all i",
                "n", "Variant", "Avg time (ms)", "Pointer steps");
        Table topoT = new Table("Topological ordering (Kahn)",
                "Size", "V", "E", "Graph type", "Avg time (ms)", "Result", "Operations");
        Table sccT = new Table("SCC (Tarjan)",
                "Size", "V", "E", "Graph type", "Avg time (ms)", "#SCCs", "Largest SCC", "Operations");
        Table dijT = new Table("Dijkstra (source = 0, connected undirected graph)",
                "Size", "V", "E", "Avg time (ms)", "Reachable", "Edge examinations", "Successful relaxations",
                "PQ push+pop", "Comparisons");

        int k = configs.size();
        double[] xsV = new double[k], xsE = new double[k];
        Map<String, double[]> times = new LinkedHashMap<>();
        for (String s : List.of("Kruskal", "Prim", "Topological sort (DAG)", "SCC (Tarjan)", "Dijkstra"))
            times.put(s, new double[k]);

        for (int i = 0; i < k; i++) {
            Config c = configs.get(i);
            long seed = SEED + c.v();
            xsV[i] = c.v();
            xsE[i] = c.e();
            System.out.println("== " + c.name() + " (V=" + c.v() + ", E=" + c.e() + ", seed=" + seed + ")");

            Graph und = GraphGenerator.connectedUndirected(c.v(), c.e(), seed, MAX_W);
            Graph dag = GraphGenerator.directed(c.v(), c.e(), seed, MAX_W, true);
            Graph cyc = GraphGenerator.directed(c.v(), c.e(), seed, MAX_W, false);
            if (!und.isConnected()) throw new IllegalStateException("generator bug: graph not connected");

            Timed<MSTResult> kr = time(() -> Kruskal.run(und));
            Timed<MSTResult> pr = time(() -> Prim.run(und));
            if (kr.value().totalWeight() != pr.value().totalWeight())
                throw new IllegalStateException("Kruskal and Prim disagree on " + c.name());
            for (Timed<MSTResult> t : List.of(kr, pr)) {
                MSTResult r = t.value();
                mstT.add(c.name(), c.v(), c.e(), r.algorithm(), t.ms(), r.totalWeight(), r.connected(),
                        r.comparisons(), r.edgeExaminations(), r.structureOps());
            }
            times.get("Kruskal")[i] = kr.ms();
            times.get("Prim")[i] = pr.ms();

            // --- DSU ablation ---
            boolean[][] variants = {{false, false}, {true, false}, {false, true}, {true, true}};
            String[] names = {"no optimisation", "path compression only", "union by rank only", "path compression + rank"};
            for (int j = 0; j < variants.length; j++) {
                final boolean pc = variants[j][0], ubr = variants[j][1];
                Timed<MSTResult> t = time(() -> Kruskal.run(und, pc, ubr));
                dsuT.add(c.name(), names[j], t.ms(), t.value().structureOps(), t.value().extraSteps());
                final int n = c.v();
                Timed<Long> st = time(() -> dsuStress(n, pc, ubr));
                dsuStress.add(n, names[j], st.ms(), st.value());
            }

            Timed<TopologicalSort.Result> td = time(() -> TopologicalSort.sort(dag));
            Timed<TopologicalSort.Result> tc = time(() -> TopologicalSort.sort(cyc));
            if (!td.value().isDag() || !TopologicalSort.isValidOrder(dag, td.value().order()))
                throw new IllegalStateException("topological order invalid on DAG");
            topoT.add(c.name(), c.v(), c.e(), "DAG", td.ms(), "valid order of " + td.value().order().size() + " vertices",
                    td.value().operations());
            topoT.add(c.name(), c.v(), c.e(), "cyclic", tc.ms(), tc.value().isDag() ? "UNEXPECTED: DAG" : "cycle detected",
                    tc.value().operations());
            times.get("Topological sort (DAG)")[i] = td.ms();

            Timed<SCC.Result> sc = time(() -> SCC.tarjan(cyc));
            Timed<SCC.Result> sd = time(() -> SCC.tarjan(dag));
            sccT.add(c.name(), c.v(), c.e(), "random cyclic", sc.ms(), sc.value().components().size(),
                    largest(sc.value()), sc.value().operations());
            sccT.add(c.name(), c.v(), c.e(), "DAG", sd.ms(), sd.value().components().size(),
                    largest(sd.value()), sd.value().operations());
            times.get("SCC (Tarjan)")[i] = sc.ms();

            Timed<Dijkstra.Result> dj = time(() -> Dijkstra.run(und, 0));
            Dijkstra.Result d = dj.value();
            dijT.add(c.name(), c.v(), c.e(), dj.ms(), d.reachableCount(), d.edgeExaminations, d.relaxations, d.pqOps, d.comparisons);
            times.get("Dijkstra")[i] = dj.ms();
        }

        File tables = new File(outDir, "tables"), plots = new File(outDir, "plots");
        tables.mkdirs();
        plots.mkdirs();
        List<Table> all = List.of(mstT, dsuT, dsuStress, topoT, sccT, dijT);
        String[] files = {"mst", "dsu_in_kruskal", "dsu_stress", "topological_sort", "scc", "dijkstra"};
        StringBuilder md = new StringBuilder("# Experimental results (auto-generated)\n\n");
        md.append("Averages over ").append(RUNS).append(" runs after ").append(WARMUP).append(" warm-up runs, seed base ")
                .append(SEED).append(", weights uniform in [1,").append(MAX_W).append("].\n\n");
        for (int i = 0; i < all.size(); i++) {
            Files.writeString(new File(tables, files[i] + ".csv").toPath(), all.get(i).csv());
            md.append(all.get(i).markdown()).append("\n");
        }
        Files.writeString(new File(tables, "results.md").toPath(), md.toString());

        PlotWriter.logLogChart("Execution time vs number of vertices (log-log)", "Vertices V", "Average time (ms)",
                xsV, times, new File(plots, "time_vs_vertices.png"));
        PlotWriter.logLogChart("Execution time vs number of edges (log-log)", "Edges E", "Average time (ms)",
                xsE, times, new File(plots, "time_vs_edges.png"));
        System.out.println("Done. Tables: " + tables.getPath() + "  Plots: " + plots.getPath());
    }

    static int largest(SCC.Result r) {
        int m = 0;
        for (List<Integer> c : r.components()) m = Math.max(m, c.size());
        return m;
    }

    static long dsuStress(int n, boolean pc, boolean ubr) {
        DSU d = new DSU(n, pc, ubr);
        for (int i = 0; i + 1 < n; i++) d.union(i, i + 1);
        for (int i = 0; i < n; i++) d.find(i);
        return d.pointerSteps();
    }

    static final class Table {
        final String title;
        final String[] headers;
        final List<String[]> rows = new ArrayList<>();

        Table(String title, String... headers) { this.title = title; this.headers = headers; }

        void add(Object... cells) {
            String[] r = new String[cells.length];
            for (int i = 0; i < cells.length; i++)
                r[i] = cells[i] instanceof Double d ? String.format(Locale.ROOT, "%.4f", d) : String.valueOf(cells[i]);
            rows.add(r);
        }

        String csv() {
            StringBuilder sb = new StringBuilder(String.join(",", quote(headers))).append('\n');
            for (String[] r : rows) sb.append(String.join(",", quote(r))).append('\n');
            return sb.toString();
        }

        private static String[] quote(String[] a) {
            String[] q = new String[a.length];
            for (int i = 0; i < a.length; i++) q[i] = "\"" + a[i].replace("\"", "\"\"") + "\"";
            return q;
        }

        String markdown() {
            StringBuilder sb = new StringBuilder("### ").append(title).append("\n\n");
            sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
            sb.append("|").append(" --- |".repeat(headers.length)).append("\n");
            for (String[] r : rows) sb.append("| ").append(String.join(" | ", r)).append(" |\n");
            return sb.append("\n").toString();
        }
    }
}
