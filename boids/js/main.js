import p5 from 'p5'
import 'scalajs:main.js'

// The friendly error system checks the arguments of every p5 call, too costly for the hundreds of calls per frame
p5.disableFriendlyErrors = true

// The simulation steps in its own thread, so that the page only draws; Main picks it up in setup
window.simulationWorker = new Worker(new URL('./worker.js', import.meta.url), { type: 'module' })
