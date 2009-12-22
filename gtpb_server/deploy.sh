#!/bin/bash
git pull origin master
ant -f build_redhat.xml deploy
