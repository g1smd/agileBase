#!/bin/bash
git pull origin master
ant -f build_ubuntu.xml deploy
