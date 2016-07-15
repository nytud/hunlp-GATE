@ECHO OFF

IF "%GATE_HOME%" == "" SET GATE_HOME=.

java -Xmx1g -Xms256m -cp "%GATE_HOME%/bin/gate.jar;%GATE_HOME%/lib/*;gate-server.jar" gate.server.Main
