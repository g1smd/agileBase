$(document).ready(function() {
  // Show initial form radio buttons
  $("#report_selection input:checked").each(function() {
    addRemoveForm(this);
  });

  // Add/remove forms from form selector on click
  $(".report_selection input").change(function() {
    var jqCheckbox = $(this);
    addRemoveForm(this);
    var internalTableName = jqCheckbox.attr("internaltablename");
    if (jqCheckbox.is(":checked")) {
  	  var addFormOptions = {
          'return': 'blank',
          'add_form_table': 'true',
          'internaltablename': internalTableName
  	  }
      $.post("AppController.servlet", {
      	abCache: new Date().getTime()
      }, addFormOptions);
    } else {
  	  var removeFormOptions = {
          'return': 'blank',
          'remove_form_table': 'true',
          'internaltablename': internalTableName
  	  }
      $.post("AppController.servlet", {
      	abCache: new Date().getTime()
      }, removeFormOptions);
    }
  });

  $(".expander").click(function() {
	if(fMobileDevice()) {
	  $(this).parent().next(".report_selection").toggle();
	} else {
	  $(this).parent().next(".report_selection").toggle('normal');
    }
	return false;
  });

  $(".report_selection_header input").change(function(event) {
	var jqRadio = $(this);
	var internalTableName = jqRadio.attr("id").replace("legend_","");
	$("#form").load("AppController.servlet", {
		"return": "gui/reports_and_tables/tabs/edit",
		set_table: internalTableName,
		abCache: new Date().getTime()
	});
	return false;
  });

});

function fMobileDevice() {
  if($("body").attr("id") == "mobile_device") {
    return true;
  }
  return false;
}

function addRemoveForm(checkboxElement) {
  var jqCheckbox = $(checkboxElement);
  var internalTableName = jqCheckbox.attr("internaltablename");
  var simpleTableName = jqCheckbox.attr("simpletablename");
  if (jqCheckbox.is(":checked")) {
	var checked = "";
	if (jqCheckbox.attr("sessiontable") == "true") {
	  checked = "checked";
	}
    var legendElement = $("<input type='radio' name='form_radio' id='legend_" + internalTableName + "' " + checked + " />");
    $("#report_selection_header").append(legendElement);
    var labelElement = $("<label for='legend_" + internalTableName + "' >" + simpleTableName + "&nbsp;</label>")
    $("#report_selection_header").append(labelElement);
  } else {
	var legendId = "legend_" + internalTableName;
	$("#" + legendId).next("label").remove();
	$("#" + legendId).remove();
  }
}
