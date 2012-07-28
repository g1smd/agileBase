#!/bin/bash
git fetch
git log --remotes -n 1 > lastcommit.txt
ant -f build_linode.xml deploy
