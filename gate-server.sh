#!/bin/bash

if [ "$GATE_HOME" == "" ]; then 
  GATE_HOME=.
fi

java -cp "$GATE_HOME/bin/gate.jar:$GATE_HOME/lib/*:gate-server.jar" gate.server.Main $1
