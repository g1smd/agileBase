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
  	    jqElement.qtip({
          content: event.title
        });
      }
    },
    eventClick: function(calEvent, jsEvent, view) {
      var eventId = calEvent.id;
      scroll(0,0); // workaround for popup showing at the top of the screen rather than the current scroll position
      fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event','fEditEventOK()','ok cancel','width=800px; height=600px');
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
});

// checkboxElement is the checkbox to select/deselect a calendar
function addRemoveCalendar(var checkboxElement) {
  var jqCheckbox = $(checkboxElement);
  var internalTableName = jqCheckbox.attr("internaltablename");
  var internalReportName = jqCheckbox.attr("internalreportname");
  var reportName = jqCheckbox.parent().text();
  var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename=" + internalTableName + "&internalreportname=" + internalReportName;
  if (jqCheckbox.is(":checked")) {
    $("#calendar").fullCalendar('addEventSource', feedUrl);
    var legendElement = $("<span class='report_" + internalReportName + "' id='legend_" + internalReportName + "'>" + reportName + "</span>");
    $("#report_selection_header").append(legendElement);
  } else {
	$("#calendar").fullCalendar('removeEventSource', feedUrl);
	var legendId = "legend_" + internalReportName;
	$("#" + legendId).remove();
  }
}
