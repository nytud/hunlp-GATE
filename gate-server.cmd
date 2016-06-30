
IF "%GATE_HOME%" == "" SET GATE_HOME=.

java -cp "%GATE_HOME%/bin/gate.jar;%GATE_HOME%/lib/*;gate-server.jar" gate.server.Main
