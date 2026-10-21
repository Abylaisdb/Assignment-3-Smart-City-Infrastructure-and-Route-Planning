# Experimental results (auto-generated)

Averages over 5 runs after 20 warm-up runs, seed base 42, weights uniform in [1,100].

### MST: Kruskal vs Prim (connected undirected graph)

| Size | V | E | Algorithm | Avg time (ms) | MST weight | Connected | Comparisons | Edge examinations | Structure ops (DSU find+union / PQ push+pop) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Small | 10 | 30 | Kruskal | 0.0239 | 177 | true | 107 | 15 | 45 |
| Small | 10 | 30 | Prim | 0.0365 | 177 | true | 196 | 60 | 60 |
| Medium | 100 | 500 | Kruskal | 0.1135 | 1182 | true | 3845 | 367 | 1101 |
| Medium | 100 | 500 | Prim | 0.3605 | 1182 | true | 7609 | 1000 | 1000 |
| Large | 1000 | 5000 | Kruskal | 1.6666 | 11984 | true | 50323 | 3378 | 10134 |
| Large | 1000 | 5000 | Prim | 1.1386 | 11984 | true | 111453 | 10000 | 10000 |


### DSU optimisations inside Kruskal (same graph, same MST weight)

| Size | Variant | Avg time (ms) | DSU find+union calls | DSU pointer steps |
| --- | --- | --- | --- | --- |
| Small | no optimisation | 0.0136 | 45 | 29 |
| Small | path compression only | 0.0139 | 45 | 25 |
| Small | union by rank only | 0.0174 | 45 | 21 |
| Small | path compression + rank | 0.0135 | 45 | 21 |
| Medium | no optimisation | 0.1243 | 1101 | 7750 |
| Medium | path compression only | 0.0929 | 1101 | 1275 |
| Medium | union by rank only | 0.1660 | 1101 | 1185 |
| Medium | path compression + rank | 0.1626 | 1101 | 755 |
| Large | no optimisation | 1.5786 | 10134 | 569462 |
| Large | path compression only | 0.7846 | 10134 | 17976 |
| Large | union by rank only | 0.7419 | 10134 | 10417 |
| Large | path compression + rank | 0.7017 | 10134 | 6902 |


### DSU stress test: chain union(i,i+1) for all i, then find(i) for all i

| n | Variant | Avg time (ms) | Pointer steps |
| --- | --- | --- | --- |
| 10 | no optimisation | 0.0009 | 45 |
| 10 | path compression only | 0.0010 | 25 |
| 10 | union by rank only | 0.0009 | 17 |
| 10 | path compression + rank | 0.0012 | 17 |
| 100 | no optimisation | 0.0122 | 4950 |
| 100 | path compression only | 0.0015 | 295 |
| 100 | union by rank only | 0.0021 | 197 |
| 100 | path compression + rank | 0.0020 | 197 |
| 1000 | no optimisation | 0.7027 | 499500 |
| 1000 | path compression only | 0.0133 | 2995 |
| 1000 | union by rank only | 0.0059 | 1997 |
| 1000 | path compression + rank | 0.0064 | 1997 |


### Topological ordering (Kahn)

| Size | V | E | Graph type | Avg time (ms) | Result | Operations |
| --- | --- | --- | --- | --- | --- | --- |
| Small | 10 | 30 | DAG | 0.0156 | valid order of 10 vertices | 80 |
| Small | 10 | 30 | cyclic | 0.0031 | cycle detected | 40 |
| Medium | 100 | 500 | DAG | 0.1142 | valid order of 100 vertices | 1200 |
| Medium | 100 | 500 | cyclic | 0.0376 | cycle detected | 600 |
| Large | 1000 | 5000 | DAG | 0.1701 | valid order of 1000 vertices | 12000 |
| Large | 1000 | 5000 | cyclic | 0.0383 | cycle detected | 6054 |


### SCC (Tarjan)

| Size | V | E | Graph type | Avg time (ms) | #SCCs | Largest SCC | Operations |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Small | 10 | 30 | random cyclic | 0.0187 | 1 | 10 | 40 |
| Small | 10 | 30 | DAG | 0.0094 | 10 | 1 | 40 |
| Medium | 100 | 500 | random cyclic | 0.0216 | 2 | 99 | 600 |
| Medium | 100 | 500 | DAG | 0.0271 | 100 | 1 | 600 |
| Large | 1000 | 5000 | random cyclic | 0.0660 | 17 | 984 | 6000 |
| Large | 1000 | 5000 | DAG | 0.1248 | 1000 | 1 | 6000 |


### Dijkstra (source = 0, connected undirected graph)

| Size | V | E | Avg time (ms) | Reachable | Edge examinations | Successful relaxations | PQ push+pop | Comparisons |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Small | 10 | 30 | 0.0149 | 10 | 60 | 14 | 30 | 66 |
| Medium | 100 | 500 | 0.2998 | 100 | 1000 | 184 | 370 | 2116 |
| Large | 1000 | 5000 | 0.5031 | 1000 | 10000 | 2006 | 4014 | 37110 |


