@ECHO OFF

IF "%GATE_HOME%" == "" SET GATE_HOME=.

java -Xmx8g -Xms512m -cp "%GATE_HOME%/bin/gate.jar;%GATE_HOME%/lib/*;gate-server.jar" gate.server.Main
