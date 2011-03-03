$(document).ready(function() {
  $('#calendar').fullCalendar({
    header: {
      left:   'title',
      center: 'month,agendaWeek,agendaDay',
      right:  'today prev,next'
	},
    editable: true,
    eventRender: function(event, jqElement, view) {
	  if ((view.name == 'month') || ((view.name == 'agendaWeek') && event.allDay)) {
        jqElement.height(15);
  	    //jqElement.qtip({
        //  content: event.title
        //});
      }
    },
    eventClick: function(calEvent, jsEvent, view) {
      var eventId = calEvent.id;
      scroll(0,0); // workaround for popup showing at the top of the screen rather than the current scroll position
      var mobile_device = false;
      if($("body").attr("id") == "mobile_device") {
    	  mobile_device = true;
      }
      if(mobile_device) {
          fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event',fEditEventOK,'ok cancel','width=auto; height=auto');
      } else {
          fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event',fEditEventOK,'ok cancel','width=800px; height=600px');
      }
    },
    eventDrop: function(event, dayDelta, minuteDelta, allDay, revertFunc, jsEvent, ui, view ) {
      var eventDate = event.start;
      var options = {
        'return': 'blank',
        'update_record': 'true',
        'set_table': event.internalTableName,
        'set_row_id': event.rowId
      }
      // the new event date
      options[event.dateFieldInternalName + '_years'] = eventDate.getFullYear();
      options[event.dateFieldInternalName + '_months'] = eventDate.getMonth() + 1;
      options[event.dateFieldInternalName + '_days'] = eventDate.getDate();
      if (event.allDay) {
        options[event.dateFieldInternalName + '_hours'] = 0;
        options[event.dateFieldInternalName + '_minutes'] = 0;
      } else {
        options[event.dateFieldInternalName + '_hours'] = eventDate.getHours();
        options[event.dateFieldInternalName + '_minutes'] = eventDate.getMinutes();
      }
      // and the change
      //options[event.dateFieldInternalName + '_days_delta'] = dayDelta;
      //options[event.dateFieldInternalName + '_minutes_delta'] = minuteDelta;      
      //TODO: visually change the event element while saving: add then remove a CSS class
      $.post("AppController.servlet", options);
    },
    minTime: 6
  });
  
  // Show initial calendars
  $("#report_selection input:checked").each(function() {
    addRemoveCalendar(this);
  });
  // Show report selector if no reports are initially selected
  if($("#report_selection input:checked").length == 0) {
	$("#report_selection").show("normal");
  }

  // Add/remove calendars on click
  $("#report_selection input").change(function() {
    addRemoveCalendar(this);
    var jqCheckbox = $(this);
    var internalTableName = jqCheckbox.attr("internaltablename");
    var internalReportName = jqCheckbox.attr("internalreportname");
    if (jqCheckbox.is(":checked")) {
	  var addReportOptions = {
        'return': 'blank',
        'add_operational_dashboard_report': 'true',
        'internaltablename': internalTableName,
        'internalreportname': internalReportName
	  }
      $.post("AppController.servlet", addReportOptions);
    } else {
	  var removeReportOptions = {
        'return': 'blank',
        'remove_operational_dashboard_report': 'true',
        'internaltablename': internalTableName,
        'internalreportname': internalReportName
	  }
      $.post("AppController.servlet", removeReportOptions);
    }
  });

  $("#report_selection_header").click(function() {
    $("#report_selection").toggle('normal');
  });
  
  $("#new_record").click(function() {
      var mobile_device = false;
      if($("body").attr("id") == "mobile_device") {
    	  mobile_device = true;
      }
      if(mobile_device) {
    		fShowModalDialog(
    				  'gui/calendar/new_event',
    				  'new event',
    				  fEditEventOK,
    				  'back next ok cancel',
    				  'width=auto; height=auto');
      } else {
    		fShowModalDialog(
    				  'gui/calendar/new_event',
    				  'new event',
    				  fEditEventOK,
    				  'back next ok cancel',
    				  'width=800px; height=600px');
      }
  });
});

// checkboxElement is the checkbox to select/deselect a calendar
function addRemoveCalendar(checkboxElement) {
  var jqCheckbox = $(checkboxElement);
  var internalTableName = jqCheckbox.attr("internaltablename");
  var internalReportName = jqCheckbox.attr("internalreportname");
  var reportName = jqCheckbox.parent().text();
  var reportTooltip = jqCheckbox.parent().attr("title");
  var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename=" + internalTableName + "&internalreportname=" + internalReportName;
  if (jqCheckbox.is(":checked")) {
    $("#calendar").fullCalendar('addEventSource', feedUrl);
    var legendElement = $("<span class='legend_report report_" + internalReportName + "' id='legend_" + internalReportName + "' title='" + reportTooltip + "'>" + reportName + "</span>");
    $("#report_selection_header").append(legendElement);
  } else {
	$("#calendar").fullCalendar('removeEventSource', feedUrl);
	var legendId = "legend_" + internalReportName;
	$("#" + legendId).remove();
  }
}

function fEditEventOK(sResponseText,sResponseXML) {
  $("#calendar").fullCalendar('refetchEvents');
}
