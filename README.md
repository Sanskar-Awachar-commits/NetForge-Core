# NetForge-Core

> A deterministic, discrete-event network traffic simulator written in modern Java.

---

## Overview

**NetForge-Core** simulates network traffic dynamics, queueing policies, bandwidth shaping algorithms, dynamic control-plane routing, real-world link pathologies, and in-band telemetry across deterministic simulation ticks.

---

## Documentation

- **[System Architecture](docs/ARCHITECTURE.md)**: Discrete-event engine mechanics, packet lifecycle, and interface contracts.
- **[Component Catalog](docs/COMPONENTS.md)**: Detailed breakdown of all Queue Policies, Traffic Shapers, Routing Engines, and Pathologies.
- **[Getting Started & Benchmarks](docs/GETTING_STARTED.md)**: Compilation guide and simulation benchmarks.

---

## Key Features

- **Queueing Engine**: Basic FIFO, Strict Priority, Random Drop, Weighted Fair Queueing (WFQ), Deficit Round Robin (DRR), Weighted Random Early Detection (WRED), and Low-Latency Queueing (LLQ).
- **Traffic Shaping & Policing**: Token Bucket, Leaky Bucket, Hierarchical Token Bucket (HTB), RFC 2698 Two-Rate Three-Color Marker (trTCM), DSCP DiffServ classification, and Firewall filtering.
- **Routing & Control Planes**: Round-Robin load balancing, Longest Prefix Matching (LPM), Dynamic Routing Tables, Distance Vector (RIP) broadcasting, Bellman-Ford metric processing, and Link-State Advertisement (LSA) flooding.
- **Network Pathologies**: Configurable propagation delays, latency jitter, packet duplication, and out-of-order delivery.
- **Observability & Telemetry**: Passive SPAN/TAP port mirroring ([`TapNode`](src/TapNode.java)), In-Band Network Telemetry ([`IntTelemetryShaper`](src/IntTelemetryShaper.java)), high-resolution percentile histograms (P50/P95/P99 via [`LatencyHistogramCollector`](src/LatencyHistogramCollector.java)), and dynamic link cost monitoring.

---

## Quickstart

### Compile
```powershell
javac -d out (Get-ChildItem -Path src -Filter *.java | ForEach-Object { $_.FullName })
```

### Run QoS Benchmark
```bash
java -cp out MainQoSTest
```