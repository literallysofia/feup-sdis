#!/bin/bash 
mkdir bin
javac -d bin Messages/*.java Peer/*.java Sockets/*.java
cp tracker.public bin/Peer
cp tracker.private bin/Peer