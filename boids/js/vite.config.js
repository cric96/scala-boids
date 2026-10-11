import { defineConfig } from 'vite'
import scalaJSPlugin from '@scala-js/vite-plugin-scalajs'

export default defineConfig({
  base: './',
  plugins: [scalaJSPlugin({ cwd: '../..', projectID: 'boidsJS' })],
  build: {
    // p5 alone is ~1 MB minified, and the sbt link step always dominates the build time
    chunkSizeWarningLimit: 2000,
    rolldownOptions: { checks: { bundlerTimings: false } },
  },
})
