# Flocking Simulation with Boids (Scala 3)

A Scala 3 flocking simulation based on Craig Reynolds' Boids algorithm. It is cross-compiled to run both as a desktop app on the **JVM** (Java Swing) and in the **browser** (Scala.js + p5.js).

---

## ⚡ Quick Start

Use the included quick launcher script to run either version easily.

```bash
# Set execute permissions (first time only)
chmod +x launch.sh

# Run the interactive menu, or specify "jvm" or "js" directly
./launch.sh
```

---

## 💻 Running the JVM Version Manually

If you prefer to use `sbt` directly, run:

### 1. Interactive Swing Simulation
```bash
sbt "boids/runMain it.unibo.FlockingRendering"
```

### 2. Headless Simulation & JSON Export
```bash
sbt "boids/runMain it.unibo.core.main --boids 500 --separation 1.5 --align 1.0 --cohesion 1.0 --separationRange 20 --vision 50 --width 800 --height 600 --seeds 5"
```

### 3. Reproduce from Exported JSON
```bash
sbt "boids/runMain it.unibo.core.reproduce res/<seed>"
```

---

## 🌐 Running the Web Version Manually

To compile and serve the Scala.js application manually:

### 1. Build and Bundle with Webpack
```bash
NODE_OPTIONS=--openssl-legacy-provider sbt boidsJS/fastOptJS::webpack
```
*(Note: `NODE_OPTIONS=--openssl-legacy-provider` is required on Node.js 17+ to prevent OpenSSL 3 compatibility errors).*

### 2. Start a Local Server
Serve the `boids/js` directory to bypass CORS browser restrictions:

*   **Python 3**: `python3 -m http.server --directory boids/js 8000` (Open [http://localhost:8000](http://localhost:8000))
*   **Node.js**: `npx http-server boids/js` (Open [http://localhost:8080](http://localhost:8080))

---

## 🧹 Code Formatting

Format all Scala sources with Scalafmt:
```bash
sbt scalafmtAll
```
