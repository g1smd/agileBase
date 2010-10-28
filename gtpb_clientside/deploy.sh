#!/bin/bash
# Get code from repository
git pull origin master
# Deploy to servlet container
ant -f deploy_templates_redhat.xml deploy
# Amalgamate JS and CSS
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tree/tree.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane1_override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane1_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane2_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane3_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tables/new_delete.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane2_amalgam.js
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.oembed.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/editBuffer_editData.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/view.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane3_amalgam.js
# Compress JS and CSS
find /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/ -name '*.js' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/ -name '*.css' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
find /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/ -name '*.css' -print -exec /usr/local/java/jdk1.6.0_02/bin/java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
