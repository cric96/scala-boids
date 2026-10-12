import { defineConfig } from 'vite'
import scalaJSPlugin from '@scala-js/vite-plugin-scalajs'

const scalaJS = () => scalaJSPlugin({ cwd: '../..', projectID: 'boidsJS' })

export default defineConfig({
  base: './',
  plugins: [scalaJS()],
  // the build bundles the worker on its own, with plugins of its own; ES modules keep the chunk it shares with the page
  worker: { format: 'es', plugins: () => [scalaJS()], rolldownOptions: { checks: { bundlerTimings: false } } },
  build: {
    // p5 alone is ~1 MB minified, and the sbt link step always dominates the build time
    chunkSizeWarningLimit: 2000,
    rolldownOptions: { checks: { bundlerTimings: false } },
  },
})
