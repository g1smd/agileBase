#!/bin/bash
# Get code from repository
git pull origin master
# Deploy to servlet container
ant -f deploy_templates_ubuntu.xml deploy
# Amalgamate JS and CSS
# CSS
#pane 1
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tree/tree.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane1_override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane1_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tree/tree.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/styles/jquery.cluetip.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane1_amalgam.css
#pane 2
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane2_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/module-colours.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane2_amalgam.css
#pane 3
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/override.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane3_amalgam.css
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/styles/report.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.css /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/datePicker.css > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane3_amalgam.css
# JS
# pane 1
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tree/tree.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/module_actions.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane1_amalgam.js
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.hoverIntent.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.cluetip.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tree/tree.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane1_amalgam.js
# pane 2
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tables/new_delete.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/module_actions.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane2_amalgam.js
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.hoverIntent.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.sparkline.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane2.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane2_amalgam.js
# pane 3
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.oembed.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/editBuffer_editData.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/view.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/module_actions.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/mobile/pane3_amalgam.js
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.oembed.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/editBuffer_editData.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.autocomplete.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/highcharts.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/highcharts_exporting.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/view.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/date.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.datePicker.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/pane3_amalgam.js
# display application
cat /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/request_setFilter.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/modalFramework/modalFramework.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/tabs/tabs.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/jquery.ajaxmanager.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/wait/editBuffer_editData.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/date.js /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/jquery.datePicker.js > /usr/local/tomcat/apache-tomcat/webapps/agileBase/resources/display_application_amalgam.js