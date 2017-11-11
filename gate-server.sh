#!/bin/bash

if [ "$GATE_HOME" == "" ]; then 
  GATE_HOME=.
fi

java -Xmx8g -Xms512m -cp "$GATE_HOME/bin/gate.jar:$GATE_HOME/lib/*:gate-server.jar" gate.server.Main $1
