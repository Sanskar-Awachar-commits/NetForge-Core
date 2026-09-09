# NetForge-Core: Component Catalog

This document details all modules and components implemented across simulation milestones up to Day 52 (TapNode).

---

## 1. Core Architecture & Simulation Infrastructure

| Component | Type | Description |
| :--- | :--- | :--- |
| [`SimulationEngine`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/SimulationEngine.java) | Engine | Central discrete-event coordinator managing synchronized execution of all registered `Tickable` components. |
| [`Packet`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/Packet.java) | Model | Immutable record representing payload chunks with identifier, byte size, creation tick, and priority. |
| [`NetworkNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/NetworkNode.java) | Node Base | Base router and switch abstraction with pluggable `QueuePolicy` and `TrafficShaper`. |
| [`SinkNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/SinkNode.java) | Egress Node | Terminal endpoint capturing arrival metrics, latency averages, duplicates, and priority breakdowns. |
| [`Telemetry`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/Telemetry.java) | Metric DTO | Value object bundling processed packet count, duplicate count, and drop statistics. |

---

## 2. Queue Policies (`QueuePolicy`)

| Policy | Description |
| :--- | :--- |
| [`BasicFIFOQueue`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/BasicFIFOQueue.java) | Standard First-In, First-Out queue bounded by buffer capacity. |
| [`PriorityQueuePolicy`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/PriorityQueuePolicy.java) | Multi-level strict priority queue prioritizing higher priority values first. |
| [`RandomDropQueue`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/RandomDropQueue.java) | Proactive congestion drop policy discarding packets probabilistically above threshold. |
| [`WeightedFairQueuePolicy`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/WeightedFairQueuePolicy.java) | Weighted fair queueing allocating bandwidth proportionally to flow weights. |
| [`DeficitRoundRobinQueue`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/DeficitRoundRobinQueue.java) | O(1) packet scheduler handling variable frame sizes via per-flow quantum deficit credits. |
| [`WredQueuePolicy`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/WredQueuePolicy.java) | Weighted Random Early Detection applying per-priority min/max thresholds and linear drop curves. |
| [`LowLatencyQueuePolicy`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/LowLatencyQueuePolicy.java) | Hybrid scheduler combining a strict expedited queue for priority ≥ 8 with fair queueing for data. |

---

## 3. Traffic Shapers & Classifiers (`TrafficShaper`)

| Shaper / Classifier | Description |
| :--- | :--- |
| [`TokenBucketShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/TokenBucketShaper.java) | Classic token bucket enforcing average sustained rate while permitting burst capacity. |
| [`LeakyBucketShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/LeakyBucketShaper.java) | Smooths out bursty ingress traffic into constant egress rate. |
| [`HierarchicalTokenBucket`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/HierarchicalTokenBucket.java) | Hierarchical token bucket supporting multi-tenant rate limits and parent token borrowing. |
| [`TwoRateThreeColorShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/TwoRateThreeColorShaper.java) | RFC 2698 trTCM dual-token metering categorizing packets into Green, Yellow, and Red. |
| [`FlowClassifier`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/FlowClassifier.java) | Ingress classifier re-tagging packet priority based on prefix rules (e.g. `VOIP_`, `BULK_`). |
| [`DiffServClassifier`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/DiffServClassifier.java) | Maps DSCP class identifiers to packet priority hierarchies. |
| [`FirewallShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/FirewallShaper.java) | Rule-based packet filtering rejecting unapproved payload prefixes. |
| [`CorruptionShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/CorruptionShaper.java) | Simulates bit errors and packet corruption based on configurable fault rates. |
| [`IntTelemetryShaper`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/IntTelemetryShaper.java) | Appends In-Band Network Telemetry (INT) node hop timestamps and delay metadata to packet IDs. |
| [`ShaperLogger`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/ShaperLogger.java) | Decorator logging pass/drop decisions of wrapped shapers. |

---

## 4. Routing & Control Plane (`RoutingTable`)

| Component | Description |
| :--- | :--- |
| [`RouterNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/RouterNode.java) | Multi-interface forwarding node directing traffic through attached routing tables. |
| [`RoundRobinBalancer`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/RoundRobinBalancer.java) | Thread-safe round-robin load balancer distributing flows evenly across egress links. |
| [`PrefixRoutingTable`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/PrefixRoutingTable.java) | Longest Prefix Match (LPM) routing table for static route rules. |
| [`DynamicRoutingTable`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/DynamicRoutingTable.java) | Mutable routing table processing control frames (priority 8) and dynamic path updates. |
| [`DistanceVectorAgent`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/DistanceVectorAgent.java) | RIP-style agent broadcasting periodic distance vector control packets to neighbors. |
| [`RoutingUpdateProcessor`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/RoutingUpdateProcessor.java) | Intercepts control frames, evaluates Bellman-Ford metrics, and updates routing tables. |
| [`LsaFloodAgent`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/LsaFloodAgent.java) | Floods Link-State Advertisements across mesh topologies with deduplication. |
| [`FailoverRouterNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/FailoverRouterNode.java) | Monitors link health and instantly failovers active flows to backup interfaces. |

---

## 5. Network Links & Pathologies

| Link Type | Description |
| :--- | :--- |
| [`NetworkLink`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/NetworkLink.java) | Standard link simulating discrete propagation delay in ticks. |
| [`JitteryNetworkLink`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/JitteryNetworkLink.java) | Introduces random latency jitter on arriving packets. |
| [`DuplicatingNetworkLink`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/DuplicatingNetworkLink.java) | Injects duplicate clone packets simulating multipath packet duplication. |
| [`OutOfOrderNetworkLink`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/OutOfOrderNetworkLink.java) | Shuffles and reorders packet streams across transit buffers. |
| [`BroadcastNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/BroadcastNode.java) | Replicates incoming packets to all connected downstream nodes. |
| [`ChaosNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/ChaosNode.java) | Stress-testing node injecting arbitrary packet drops and delays. |

---

## 6. Observability & Mirroring

| Component | Description |
| :--- | :--- |
| [`TapNode`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/TapNode.java) | Passive port mirroring tap that delivers original packets to the primary path while non-blockingly forwarding `[TAP_MIRROR]` clones to analytics sinks. |
| [`LatencyHistogramCollector`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/LatencyHistogramCollector.java) | High-resolution latency bucket collector calculating P50, P95, and P99 percentiles via frequency interpolation. |
| [`LinkCostMonitor`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/LinkCostMonitor.java) | Tracks link buffer occupancy and in-flight MTU flight bytes to calculate dynamic cost weights. |

---

## 7. Traffic Generators

| Generator | Description |
| :--- | :--- |
| [`ConstantTrafficGenerator`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/ConstantTrafficGenerator.java) | Emits fixed-size packets at uniform tick intervals. |
| [`BurstTrafficGenerator`](file:///d:/Projects/Codjing/Official/Languages/Java/NetForge-Core/NetForge-Core/src/BurstTrafficGenerator.java) | Emits bursts of packets at periodic intervals to stress queues and shapers. |
