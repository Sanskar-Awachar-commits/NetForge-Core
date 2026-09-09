# NetForge-Core: Getting Started Guide

## Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher (tested with Java 21+).
- **Git**

---

## 1. Build & Compile

To compile all Java source files into the `out/` directory:

```powershell
# Windows PowerShell
javac -d out (Get-ChildItem -Path src -Filter *.java | ForEach-Object { $_.FullName })
```

```bash
# Linux / macOS Bash
javac -d out src/*.java
```

---

## 2. Running Simulations & Test Suites

NetForge-Core includes specialized simulation scenarios:

### 2.1. Basic Simulation (`Main`)
Demonstrates constant traffic generation through a single node and FIFO queue:
```bash
java -cp out Main
```

### 2.2. Burst Traffic & Token Bucket Shaper (`MainBurstTest`)
Simulates 50-packet bursts across a token-bucket shaped node and delayed network link:
```bash
java -cp out MainBurstTest
```

### 2.3. Multi-Path Load Balancing (`MainRoutingTest`)
Demonstrates strict 50/50 round-robin interleaving across parallel egress interfaces:
```bash
java -cp out MainRoutingTest
```

### 2.4. Link Pathology Stress Test (`MainPathologyTest`)
Injects latency jitter, packet duplication, and out-of-order resequencing:
```bash
java -cp out MainPathologyTest
```

### 2.5. Self-Healing Dynamic Mesh (`MainDynamicMeshTest`)
Executes shortest path routing and tests dynamic link failure and self-healing route convergence:
```bash
java -cp out MainDynamicMeshTest
```

### 2.6. QoS LLQ & trTCM Benchmark (`MainQoSTest`)
Benchmarks hybrid Low-Latency Queueing (LLQ) protecting VoIP traffic (priority 9) against heavy shaped bulk flows (priority 1):
```bash
java -cp out MainQoSTest
```

---

## 3. Example: Setting Up a TapNode with In-Band Telemetry

```java
// 1. Setup core nodes
SinkNode primarySink = new SinkNode("PrimarySink");
SinkNode analyticsSink = new SinkNode("AnalyticsSink");
NetworkNode coreRouter = new NetworkNode("CoreRouter", primarySink);

// 2. Attach In-Band Network Telemetry (INT) shaper
IntTelemetryShaper intShaper = new IntTelemetryShaper("Switch-01", 2);

// 3. Wrap ingress with a passive TapNode for port mirroring
TapNode tapNode = new TapNode(coreRouter, analyticsSink, 1024);

// 4. Send packet
Packet packet = new Packet("VOIP_100", 120, 0L, 9);
intShaper.evaluate(packet, 0L);
Packet augmented = intShaper.getLastAugmentedPacket();
tapNode.receivePacket(augmented, 0L);

// 5. Tick engine
tapNode.tick(1L);
```
