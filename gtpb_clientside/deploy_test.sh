#!/bin/bash
git pull origin master
ant -f deploy_templates_ubuntu.xml deploy
