#!/bin/bash
git fetch
git log -1 origin/master > lastcommit.txt
ant -f build_linode.xml deploy
