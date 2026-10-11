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
sbt "boids/runMain it.unibo.core.main --boids 500 --separation 1.5 --align 1.0 --cohesion 1.0 --separation-range 20 --vision 50 --width 800 --height 600 --seeds 5"
```

### 3. Reproduce from Exported JSON
```bash
sbt "boids/runMain it.unibo.core.reproduce res/<seed>"
```

---

## 🌐 Running the Web Version Manually

The web version is bundled with [Vite](https://vite.dev) through the official [`@scala-js/vite-plugin-scalajs`](https://github.com/scala-js/vite-plugin-scalajs) (requires Node.js 20.19+):

```bash
cd boids/js
npm install
npm run dev
```

The dev server links Scala.js with sbt on startup; run `sbt ~boidsJS/fastLinkJS` in another terminal to recompile on every change.

To build the static site deployed on GitHub Pages, run `npm run build` (output in `boids/js/dist`, preview it with `npm run preview`).

---

## 🧹 Code Formatting

Format all Scala sources with Scalafmt:
```bash
sbt scalafmtAll
```
