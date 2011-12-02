#!/bin/bash
git pull origin master
ant -f build_linode.xml deploy
