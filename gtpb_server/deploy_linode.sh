#!/bin/bash
git pull origin master
git log -n 1 > lastcommit.txt
ant -f build_linode.xml deploy
