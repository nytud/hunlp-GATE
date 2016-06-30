This sub-project creates a wrapper around the gate library to use as a web-service and avoid re-initialization.

Usage:

java -cp "<GATE_HOME>/bin/gate.jar;<GATE_HOME>/lib/*;gate-server.jar" gate.server.Main [config]

* GATE_HOME - the path to the gate installation
* config - alternative configuration file with relative path to the jar file (default: gate-server.props)

See details on the configuration in the example found in the parent directory.

Example start scripts can be found in the parent directory - gate-server.cmd OR gate-server.sh (depending on the OS)

The program logs to stdout.

API:

http://<host>:<port>/process?run=<modules>&text=<document>

Process a document (result is an XML if successful)

* host, port - defined in the configuration file the default is : localhost and 8000 repectively
* modules - comma separated list of module names to run (in order) (names are defined in the configuration)
* document - the text to process (urlencoded)

http://<host>:<port>/exit

Exit the server
