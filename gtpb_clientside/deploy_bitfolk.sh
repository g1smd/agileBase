#!/bin/bash
# Get code from repository
git pull origin master
# Deploy to servlet container
ant -f deploy_templates_bitfolk.xml deploy
# Amalgamate JS and CSS
# CSS
#pane 1
cat /var/lib/tomcat7/webapps/agileBase/resources/tree/tree.css /var/lib/tomcat7/webapps/agileBase/resources/module-colours.css /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane1_override.css > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane1_amalgam.css
cat /var/lib/tomcat7/webapps/agileBase/resources/tree/tree.css /var/lib/tomcat7/webapps/agileBase/resources/module-colours.css /var/lib/tomcat7/webapps/agileBase/styles/jquery.cluetip.css > /var/lib/tomcat7/webapps/agileBase/resources/pane1_amalgam.css
#pane 2
cat /var/lib/tomcat7/webapps/agileBase/styles/report.css /var/lib/tomcat7/webapps/agileBase/resources/module-colours.css /var/lib/tomcat7/webapps/agileBase/resources/mobile/override.css > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane2_amalgam.css
cat /var/lib/tomcat7/webapps/agileBase/styles/report.css /var/lib/tomcat7/webapps/agileBase/resources/module-colours.css > /var/lib/tomcat7/webapps/agileBase/resources/pane2_amalgam.css
#pane 3
cat /var/lib/tomcat7/webapps/agileBase/styles/report.css /var/lib/tomcat7/webapps/agileBase/resources/tabs/tabs.css /var/lib/tomcat7/webapps/agileBase/resources/jquery.autocomplete.css /var/lib/tomcat7/webapps/agileBase/resources/datePicker.css /var/lib/tomcat7/webapps/agileBase/resources/mobile/override.css > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane3_amalgam.css
cat /var/lib/tomcat7/webapps/agileBase/styles/report.css /var/lib/tomcat7/webapps/agileBase/resources/tabs/tabs.css /var/lib/tomcat7/webapps/agileBase/resources/jquery.autocomplete.css /var/lib/tomcat7/webapps/agileBase/resources/datePicker.css /var/lib/tomcat7/webapps/agileBase/resources/jquery.tweet.css /var/lib/tomcat7/webapps/agileBase/resources/button/button.css > /var/lib/tomcat7/webapps/agileBase/resources/pane3_amalgam.css
# display application
cat /var/lib/tomcat7/webapps/agileBase/resources/display_application.css /var/lib/tomcat7/webapps/agileBase/resources/button/button.css /var/lib/tomcat7/webapps/agileBase/resources/preview/edit_nav.css > /var/lib/tomcat7/webapps/agileBase/resources/display_application_amalgam.css
# JS
# pane 1
cat /var/lib/tomcat7/webapps/agileBase/resources/tree/tree.js /var/lib/tomcat7/webapps/agileBase/resources/mobile/module_actions.js > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane1_amalgam.js
cat /var/lib/tomcat7/webapps/agileBase/resources/jquery.hoverIntent.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.cluetip.js /var/lib/tomcat7/webapps/agileBase/resources/tree/tree.js > /var/lib/tomcat7/webapps/agileBase/resources/pane1_amalgam.js
# pane 2
cat /var/lib/tomcat7/webapps/agileBase/resources/tables/new_delete.js /var/lib/tomcat7/webapps/agileBase/resources/wait/request_setFilter.js /var/lib/tomcat7/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /var/lib/tomcat7/webapps/agileBase/resources/mobile/module_actions.js > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane2_amalgam.js
cat /var/lib/tomcat7/webapps/agileBase/resources/jquery.hoverIntent.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.sparkline.js /var/lib/tomcat7/webapps/agileBase/resources/wait/request_setFilter.js /var/lib/tomcat7/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /var/lib/tomcat7/webapps/agileBase/resources/pane2.js > /var/lib/tomcat7/webapps/agileBase/resources/pane2_amalgam.js
# pane 3
cat /var/lib/tomcat7/webapps/agileBase/resources/jquery.oembed.js /var/lib/tomcat7/webapps/agileBase/resources/wait/editBuffer_editData.js /var/lib/tomcat7/webapps/agileBase/resources/wait/request_setFilter.js /var/lib/tomcat7/webapps/agileBase/resources/tabs/tabs.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.autocomplete.js /var/lib/tomcat7/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /var/lib/tomcat7/webapps/agileBase/resources/tabs/view.js /var/lib/tomcat7/webapps/agileBase/resources/date.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.datePicker.js /var/lib/tomcat7/webapps/agileBase/resources/mobile/module_actions.js /var/lib/tomcat7/webapps/agileBase/resources/picker.js > /var/lib/tomcat7/webapps/agileBase/resources/mobile/pane3_amalgam.js
cat /var/lib/tomcat7/webapps/agileBase/resources/jquery.oembed.js /var/lib/tomcat7/webapps/agileBase/resources/wait/editBuffer_editData.js /var/lib/tomcat7/webapps/agileBase/resources/wait/request_setFilter.js /var/lib/tomcat7/webapps/agileBase/resources/tabs/tabs.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.autocomplete.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.sexypost.js /var/lib/tomcat7/webapps/agileBase/resources/highcharts.js /var/lib/tomcat7/webapps/agileBase/resources/highcharts_exporting.js /var/lib/tomcat7/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /var/lib/tomcat7/webapps/agileBase/resources/tabs/view.js /var/lib/tomcat7/webapps/agileBase/resources/date.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.datePicker.js /var/lib/tomcat7/webapps/agileBase/resources/picker.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.tweet.js /var/lib/tomcat7/webapps/agileBase/resources/button/button.js > /var/lib/tomcat7/webapps/agileBase/resources/pane3_amalgam.js
# display application
cat /var/lib/tomcat7/webapps/agileBase/resources/wait/request_setFilter.js /var/lib/tomcat7/webapps/agileBase/resources/modalFramework/modalFramework.js /var/lib/tomcat7/webapps/agileBase/resources/tabs/tabs.js /var/lib/tomcat7/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /var/lib/tomcat7/webapps/agileBase/resources/wait/editBuffer_editData.js /var/lib/tomcat7/webapps/agileBase/resources/date.js /var/lib/tomcat7/webapps/agileBase/resources/jquery.datePicker.js /var/lib/tomcat7/webapps/agileBase/resources/button/button.js /var/lib/tomcat7/webapps/agileBase/resources/preview/edit_nav.js > /var/lib/tomcat7/webapps/agileBase/resources/display_application_amalgam.js
# Compress JS and CSS
#find /var/lib/tomcat7/webapps/agileBase/resources/ -name '*.js' -print -exec java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
#find /var/lib/tomcat7/webapps/agileBase/resources/ -name '*.css' -print -exec java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
#find /var/lib/tomcat7/webapps/agileBase/styles/ -name '*.css' -print -exec java -jar /usr/local/agileBase/yui_compressor/yuicompressor.jar -o {} {} \;
#echo "Compressing Javascript"
#find /var/lib/tomcat7/webapps/agileBase/resources/ -name '*.js' -print0 | xargs -0 java -jar /usr/local/agileBase/yuicompressor/yuicompressor.jar -o '.js$:.js'
#echo "Compressing CSS"
#find /var/lib/tomcat7/webapps/agileBase/resources/ -name '*.css' -print0 | xargs -0 java -jar /usr/local/agileBase/yuicompressor/yuicompressor.jar -o '.css$:.css'
#find /var/lib/tomcat7/webapps/agileBase/styles/ -name '*.css' -print0 | xargs -0 java -jar /usr/local/agileBase/yuicompressor/yuicompressor.jar -o '.css$:.css'
