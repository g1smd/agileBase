#!/bin/bash
git pull origin master
ant -f build_bitfolk.xml deploy
