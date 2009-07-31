#!/bin/bash
cvs update -d
ant -f build_redhat.xml deploy
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/resources/ -name '*.js' -print -exec java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/resources/ -name '*.css' -print -exec java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/styles/ -name '*.css' -print -exec java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
