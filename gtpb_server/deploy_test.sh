#!/bin/bash
git pull origin master
rm WEB-INF/lib/javamelody.jar
ant -f build_ubuntu.xml deploy
