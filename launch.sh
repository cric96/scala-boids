#!/usr/bin/env bash

set -e

# Function to run JVM simulation
run_jvm() {
  echo "=================================================="
  echo "🚀 Launching JVM Swing Visual Simulation..."
  echo "=================================================="
  sbt "boids/runMain it.unibo.FlockingRendering"
}

# Function to build and run JS simulation
run_js() {
  echo "=================================================="
  echo "📦 Building Scala.js bundle..."
  echo "=================================================="
  NODE_OPTIONS=--openssl-legacy-provider sbt boidsJS/fastOptJS::webpack

  echo "=================================================="
  echo "🌐 Starting local HTTP server..."
  echo "=================================================="
  if command -v python3 &>/dev/null; then
    echo "👉 Opening server at: http://localhost:8000"
    python3 -m http.server --directory boids/js 8000
  elif command -v python &>/dev/null; then
    echo "👉 Opening server at: http://localhost:8000"
    python -m SimpleHTTPServer 8000
  elif command -v npx &>/dev/null; then
    echo "👉 Opening server at: http://localhost:8080"
    npx http-server boids/js
  else
    echo "⚠️  No local HTTP server tool (python3, python, npx) found."
    echo "Please open boids/js/index.html manually or run your preferred server in that folder."
  fi
}

show_help() {
  echo "Usage: $0 [jvm|js]"
  echo "  jvm  - Run the JVM Swing GUI simulation"
  echo "  js   - Compile Scala.js and run a local web server"
  echo "If no argument is provided, an interactive menu will be shown."
}

if [ "$1" = "jvm" ]; then
  run_jvm
elif [ "$1" = "js" ]; then
  run_js
elif [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  show_help
elif [ -z "$1" ]; then
  echo "=================================================="
  echo "      Scala Boids - Quick Launch Script          "
  echo "=================================================="
  echo "1) Launch JVM Swing Visual Simulation"
  echo "2) Build & Serve JS Web Simulation (p5.js)"
  echo "3) Exit"
  echo "=================================================="
  read -p "Select an option [1-3]: " choice

  case $choice in
    1) run_jvm ;;
    2) run_js ;;
    3) exit 0 ;;
    *) echo "Invalid option"; exit 1 ;;
  esac
else
  echo "Unknown argument: $1"
  show_help
  exit 1
fi
