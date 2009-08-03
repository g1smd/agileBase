#!/bin/bash
cvs update -d
ant -f deploy_templates_redhat.xml deploy
echo "Compressing JavaScript and CSS files"
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/resources/ -name '*.js' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/resources/ -name '*.css' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/portalBase/styles/ -name '*.css' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/portalBase/yui_compressor/yuicompressor.jar -o {} {} \;
