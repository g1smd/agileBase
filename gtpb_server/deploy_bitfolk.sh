#!/bin/bash
git pull origin master
git fetch
git log -1 origin/master > lastcommit.txt
ant -f build_bitfolk.xml deploy
