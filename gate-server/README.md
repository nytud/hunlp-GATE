# gate-server

This sub-project creates a wrapper around the GATE library to use as a web-service and avoid re-initialization.

## Usage

`java -cp "<GATE_HOME>/bin/gate.jar;<GATE_HOME>/lib/*;gate-server.jar" gate.server.Main [config]`

* `GATE_HOME` - the path to the GATE installation
* `config` - alternative configuration file with relative path to the jar file. Default: `gate-server.props`

See details on the configuration in the example found in the parent directory.

Example start scripts can be found in the parent directory:

 * `gate-server.sh` for Linux (and Mac?)
 * `gate-server.cmd` for Windows

The program logs to stdout.

## API

Process a document:

`http://<host>:<port>/process?run=<modules>&text=<document>`

* `host`, `port` - defined in the configuration file. Default: `localhost` and `8000` respectively.
* `modules` - comma separated list of module names to run. Modules are run in the specified order. Module names are defined in the configuration file.
* `document` - the text to process (urlencoded)

The result is an XML (in GATE XML format) if successful.

Exit the server:

`http://<host>:<port>/exit`

Use `emw.sh` to analyze short texts having gate-server already running.

Example:

```sh
cat *.txt | emw.sh
# or
emw.sh *.txt
```

See `emw.sh -h` for details.
