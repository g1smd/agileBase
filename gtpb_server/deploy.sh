#!/bin/bash
cvs update -d
ant -f build_redhat.xml deploy
