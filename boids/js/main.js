import p5 from 'p5'
import 'scalajs:main.js'

// The friendly error system checks the arguments of every p5 call, too costly for the hundreds of calls per frame
p5.disableFriendlyErrors = true
// for the sketches in instance mode and their geometries, as the import does not make p5 global
window.p5 = p5

// The simulation steps in its own thread, so that the page only draws; Main picks it up in setup
window.simulationWorker = new Worker(new URL('./worker.js', import.meta.url), { type: 'module' })
