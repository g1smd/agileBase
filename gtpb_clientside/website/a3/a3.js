  jQuery(document).ready(function() {
	windowResize();
	
	if (jQuery("#loginform").size() == 0) {
	  jQuery("#a3_report").load("AppController.servlet",
	    {
		  'return':'gui/customisations/common/a3/a3_report_content',
		  'set_table':'true', // a3 reports
		  'settablename': 'a3 reports',
		  'set_report':'dbvcalc_a3_reports' // dbvcalc_a3_reports
		  //'set_report_filter_value': 'true',
		},
	    function() {
		  jQuery("#a3_report").fadeIn("normal");
		  windowResize();
		  setTimeout("fontResize(" + gtpb_currentFontSize + ", 0);", 2000);
		}
	  );
	} else {
	  loadLoginA3("why_a3");
	}

	jQuery(window).resize(function() {
	  windowResize();
	});
	
	jQuery(".editable").live('keyup', function() {
	  if (jQuery("#loginform").size() == 0) {
	    oBuffer.writeBuffer(this);
	  }
	  fontResize(gtpb_currentFontSize, 0);
	});
	
	jQuery("#new_report").click(function() {
	  jQuery("#a3_report").fadeOut("normal");
	  jQuery("#a3_report").load("AppController.servlet",
	    {'return':'gui/customisations/common/a3/a3_report_content', 'save_new_record':'true'},
	    function() {
		  jQuery("#a3_report").fadeIn("normal");
		  fieldDisplayResize();
		  gtpb_currentFontSize = gtpb_maxFontSize;
		  fontResize(gtpb_currentFontSize, 0);
	    });
	});
	
	jQuery("#next_report").click(function() {
	  next_report('next');
	});
	
	jQuery("#previous_report").click(function() {
	  next_report('previous');
	});
	
	/*
	jQuery(".actions_area").mouseenter(function() {
	  jQuery(this).stop(true,false).animate({
	    opacity: 0.8
	  }, 500);
	}).mouseleave(function() {
	  jQuery(this).stop(true,false).animate({
	    opacity: 0.4
	  }, 2000);
	});
*/
	jQuery("#search").keyup(function() {
	  alert('coming soon...');
	});
	
	jQuery("#print").click(function() {
	  alert("Print format in development...");
	  window.print();
	});
	
	jQuery("#share").click(function() {
	  alert('coming soon...');
	});
	
	jQuery("#manage").click(function() {
	  document.location = "AppController.servlet?return=gui/display_application&set_report=a3 reports";
	});
	
	jQuery("#delete").click(function() {
	  jQuery("#delete_dialog").dialog("open");
	});
	
	// Initialise the dialog object
	jQuery("#delete_dialog").dialog({
      autoOpen: false,
      modal: true,
	  buttons : {
        "Delete" : function() {
          jQuery("#a3_report").hide().load("AppController.servlet",
	        {'return':'gui/customisations/common/a3/a3_report_content', 'remove_record':'true'},
	        function() {
		      jQuery("#a3_report").fadeIn("normal");
	        }
		  );
		  $(this).dialog("close");
        },
        "Cancel" : function() {
          $(this).dialog("close");
        }
      }
    });
	
    /* Login page functions */
	
	jQuery("#why_a3").click(function() {
		  loadLoginA3("why_a3");
	});

	jQuery("#scenario").click(function() {
		  loadLoginA3("scenario");
	});

	jQuery("#tryout").click(function() {
		  loadLoginA3("tryout");
	});
	
	jQuery("#email_input").focus(function() {
	  jQuery("#email_input").val("");
	});
	
	jQuery('#signup_form').submit(function() { 
        if (jQuery("#email_input").val().indexOf("@") == -1) {
        	alert("Please enter an email address");
        	return false;
        } else {
        	return true;
        }
    });

  });  
  
/* Helper functions, outside of document.ready */

function loadLoginA3(reportName) {
	  jQuery("#a3_report").hide();
	  jQuery("#a3_report").load("/agileBase/website/a3/" + reportName + ".htm", function() {
		  jQuery("#a3_report").fadeIn("normal");
		  windowResize();
		  setTimeout("fontResize(" + gtpb_currentFontSize + ", 0);", 2000);
		  if(reportName == "tryout") {
			  jQuery("#why_are_we").focus();
		  }
	  });
}
  
function windowResize() {
	// Size the report
    var windowWidth = jQuery(window).width();
    var jqA3Report = jQuery("#a3_report");
    jqA3Report.width(windowWidth - 280);
    var leftWidth = parseInt((windowWidth * .115) + 10);
    var rightWidth = parseInt((windowWidth * .115) - 10);
    jqA3Report.css('margin-left',leftWidth + 'px');
    jqA3Report.css('margin-right',rightWidth + 'px');
    // Position the navigation stickies
    var backgroundHeight = jQuery("#paper").height();
    var stickyBase = backgroundHeight / 19;
    var stickyTop = stickyBase - 55;
    jQuery("#stickies").css("top", stickyTop);
    // Position the fields vertically
    fieldDisplayResize();
    initialiseFontSize();
}

function initialiseFontSize() {
    var fontSizeString = jQuery(".field_display").css("font-size");
    if (typeof(fontSizeString != 'undefined')) {
    	gtpb_currentFontSize = parseInt(fontSizeString);
    	if (gtpb_currentFontSize < gtpb_minFontSize) {
    	  gtpb_currentFontSize = gtpb_minFontSize;
    	} else if (gtpb_currentFontSize > gtpb_maxFontSize) {
    	  gtpb_currentFontSize = gtpb_maxFontSize;
    	}
    }
}

function fieldDisplayResize() {
  var windowHeight = jQuery(window).height();
  var jqBottomFieldDisplay = jQuery(".field_display:last");
  if (jqBottomFieldDisplay.size() == 1) {
	var bottomPos = jqBottomFieldDisplay.offset().top + jqBottomFieldDisplay.height();
	if (bottomPos < (windowHeight - (windowHeight / 10))) {
	  var fieldDisplayHeight = jqBottomFieldDisplay.css("min-height");
	  if (typeof(fieldDisplayHeight) == 'undefined') {
		fieldDisplayHeight = 20;
	  } else {
	    fieldDisplayHeight = parseInt(fieldDisplayHeight) + 1;
	  }
	  jQuery(".field_display").css("min-height", fieldDisplayHeight + "px");
	  fieldDisplayResize();
	}
  }
}

var gtpb_currentFontSize = 80;
var gtpb_maxFontSize = 150;
var gtpb_minFontSize = 60;

/* targetSize: the size set the font to
 * direction: +1, 0 or -1: whether this is bigger or smaller than the last font size
 * 0 means we don't know, just set it, use this when calling the function initially
 */
function fontResize(targetSize, direction) {
  if ((targetSize > gtpb_maxFontSize) || (targetSize < gtpb_minFontSize)) {
	return;
  }
  jQuery(".field_display").css("font-size", targetSize + "px");
  gtpb_currentFontSize = targetSize;
  var windowHeight = jQuery(window).height();
  var jqReportContent = jQuery("#report_content");
  if (jqReportContent.size() == 1) {
	var bottomPos = jqReportContent.offset().top + jqReportContent.height();
	var newSize = targetSize;
	if (bottomPos > (windowHeight + 30)) {
	  newSize = newSize - 1;
	  setTimeout("fontResize(" + newSize + ", -1);", 20);
	} else if (bottomPos < (windowHeight - 10)) {
      newSize = newSize + 1;
      // Prevent flip-flopping, make sure we're continuing in the same direction
      if (direction >= 0) {
	    setTimeout("fontResize(" + newSize + ", 1);", 20);
      }
	}
  }
}

function next_report(sAction) {
	 jQuery("#a3_report").hide();
	 jQuery("#a3_report").load("AppController.servlet",
	    {'return':'gui/customisations/common/a3/a3_report_content', 'set_row_id':sAction},
	    function() {
		  jQuery("#a3_report").fadeIn("normal");
		  fieldDisplayResize();
		  initialiseFontSize();
		  setTimeout("fontResize(" + gtpb_currentFontSize + ", 0);", 2000);
	    }
	 );
}