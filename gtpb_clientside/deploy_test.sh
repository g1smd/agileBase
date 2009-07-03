#!/bin/bash
cvs update -d
ant -f deploy_templates_ubuntu.xml deploy
