#!/bin/bash
cvs update -d
ant -f deploy_templates_redhat.xml deploy
