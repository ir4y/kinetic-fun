# kinetic-fun

This is a simple web application using clojurescript, kineticjs and websocket.
You can darg circle and it will move on all other connected clients.
Application use redis pub/sub for communication.
## Prerequisites

You will need [Leiningen][1] 2.2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running
First compile cljs

    lein cljsbuild once

Then start a web server for the application, run:

    lein run
    
## License

Copyright © 2013 FIXME
