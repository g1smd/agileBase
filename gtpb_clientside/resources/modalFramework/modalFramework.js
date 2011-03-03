/*
 *  Copyright 2011 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
function fShowModalDialog(sTemplateLocation, sCaption, fCallbackFn, sButtons, sAttributes) {
  // callback function
  // destroy method

  // create method
  // test that it works within a frameset

  // want to load the content from a url - probably over an XML http request

  function fCreate() {							
    function fDialog() {
      function fPicker(sTable) {
        function fLoadContent() {
	      function fDisplayContent(sText,sXML) {
	        oElements.picker.content.innerHTML=sText; 
	        if (document.getElementById('_mfp_current_row')) return; // a row is picked
	        // if no row is picked, pick the first row.
	        
	        // the template will have marked the first row with id currentRow
	        var oRow=document.getElementById('currentRow');
	        try {
	          f_mdp_rowclick(oRow);
	        } catch(e) {
	          //do nothing
	          oElements.picker.rowid_store.value='';
	        }
	      }
	      
	      if(oElements.picker.reportsList.options.length<1) return;
	      
	      var aPostVars=new Array();
          aPostVars['return']='gui/resources/modalFramework/report';
	      aPostVars['set_custom_report']='true';
	      aPostVars['reportkey']='picker_report';
	      aPostVars['custominternalreportname']=oElements.picker.reportsList.options[oElements.picker.reportsList.selectedIndex].value;
	    
	      var oReq=new fRequest('AppController.servlet',aPostVars,fDisplayContent,0);
	    }
	    	       
	    function fLoadReports() {	    
	      function fDisplayReports(sText,sXML) {
	        // would be better to parse over a list of XML elements and add them through the DOM
	        // however use innerHTML for the time being because it's quicker
	        with(oElements.picker.reportsList) {
	          innerHTML=sText;
	          removeAttribute('disabled');
	        }
	        fLoadContent();
	      }
	  
	      var aPostVars=new Array();
	      if (sTable) aPostVars['set_table']=sTable; // if a table is passed in, set this as the session table, otherwise use whatever's already set
	      aPostVars['return']='gui/resources/modalFramework/reports_list';
	      var oReq=new fRequest('AppController.servlet',aPostVars,fDisplayReports,0);        
	    }
	            
	    function fHTMLElements() {
	      var oPicker=document.createElement('DIV');
	      $(oPicker).addClass('_mdp_relation_picker');
	      oPicker.setAttribute('id','_mdp_relationPicker');
	  
	      var oToolbar=document.createElement('DIV');
	      oToolbar.setAttribute('id','_mdp_toolbar');
	    
	      var oReportsList=document.createElement('SELECT');
	      oReportsList.setAttribute('disabled','true');
	      oReportsList.appendChild(new Option('loading (wait)...',''));
	      //oReportsList.addEventListener('change',fLoadContent,false);
	      $(oReportsList).change(fLoadContent);
	      oToolbar.appendChild(document.createTextNode('pick from a report: '));
	      oToolbar.appendChild(oReportsList);
	      oPicker.reportsList=oReportsList;   
	
	      oPicker.appendChild(oToolbar);
	  
	      var oContent=document.createElement('DIV');
	      oContent.setAttribute('id','_mdp_content');
	      oContent.innerHTML='loading (wait)...';
	      oPicker.appendChild(oContent);
	      
	      var oForm=document.createElement('FORM');
	      var oHidden=document.createElement('INPUT');
	      oHidden.setAttribute('type','hidden');
	      oHidden.setAttribute('name','preset_row_id');
	      oHidden.setAttribute('id','_mfp_hidden_field');
	      oHidden.setAttribute('validationMessage','Please pick a row');
	      oPicker.rowid_store=oHidden;
	      oForm.appendChild(oHidden);
	      oPicker.appendChild(oForm);

	      oPicker.content=oContent;	    
	      this.picker=oPicker;
	    }        
	    
        var oElements=new fHTMLElements();
        fLoadReports(sTable);
        oContent.appendChild(oElements.picker);
        oContent.picker=oElements.picker;
      }
    
      function fResultOK(sXML) {
  	    if(sXML.getElementsByTagName('wizardResult')[0]) {
  	      if(sXML.getElementsByTagName('wizardResult')[0].firstChild.nodeValue=='ok') return true;
  	      else {
  	        try {
  	          var sErrorMessage=(sXML.getElementsByTagName('wizardResult')[0].getElementsByTagName('errorData')[0].getElementsByTagName('message')[0].firstChild.nodeValue).replace(/\s*/,'');
  	          alert(sErrorMessage);
  	        }
  	        catch(e){
  	          alert('an error has occurred but no information about it could be found\nPlease try again');
  	        }
  	        finally {
  	          return false; // the wizard result wasn't ok
  	        }
  	      }
  	    } 
  	    else {
  	      alert('No wizardResult detected\nPlease try again\nPerhaps the returned template is not the expected XML');
  	      return false; // there was no wizard result
  	    }
  	  }
  	      
  	  function fDestroy() {
  	    oBlank.parentNode.removeChild(oBlank);	
  	    // if on a mobile device, return to the home screen
  	    // - the dialog will have loaded in a separate page
  	    if(document.location.href.indexOf('gui/mobile/module_action') > -1) {
  	    	document.location = '?return=boot_mobile';
  	    }
  	  }
  	  
  	  function fStepValidates() {
  	    // oContent.validations is set in fDisplayContent
  	    if(!oContent.getAttribute('validations')) return true; // there are no validations
  	    var aValidations=oContent.getAttribute('validations').split(';');
  	    try {
  	      for(var i=0;i<aValidations.length;i++) {
  	        var sFieldName=aValidations[i].match(/^\w+/);
  	        var rFieldName=new RegExp(sFieldName);
  	        var sValidation=aValidations[i].replace(rFieldName,'');
 
  	        var iLastObj=document.getElementsByName(sFieldName).length-1; // if there's more than one form element with the same name, it's the last one in the dom that's significant
  	        var sEvalString="document.getElementsByName('"+sFieldName+"')["+iLastObj+"].value"+sValidation;
  	      
            var sResult=eval(sEvalString);
  	        if(sResult==false) {
  	          var sValidationMessage="Can't move on";
  	          if (document.getElementsByName(sFieldName)[iLastObj].getAttribute('validationMessage')) 
  	            sValidationMessage+='\n'+document.getElementsByName(sFieldName)[iLastObj].getAttribute('validationMessage');
  	          throw(sValidationMessage);
  	        }
  	      }
  	    } catch(e) {
  	      alert(e);
  	      return false;
  	    }
  	    return true;
  	  }
  	  
  	  function fOK() {
  	    function fProcessLastStep(sResponseText,sResponseXML) {  	      
  	      if(fResultOK(sResponseXML)) { 
  	        // if the wizard returned OK
  	        fDestroy();
  		    fCallbackFn(sResponseText,sResponseXML);  // although these are passed, the callback function doesn't have to use them or declare them in the definition
  		  } // else oContent.displayContent(sResponseText,sResponseXML);   This would cause an inifite loop!! - if the result isn't OK, the wizard pops an alert in fResultOK() and stays on the same page
  	    }
  	    
  	    if(!fStepValidates()) return;
  	    oContent.sendFormData(this.getAttribute('targetTemplate'),fProcessLastStep,true);
  	    // as this is triggered by an event, 'this' refers to the event src i.e. the button in this case							
  	  }
  	  
  	  function fNext() {
  	    if(!fStepValidates()) return;
  	    oContent.sendFormData(this.getAttribute('targetTemplate'),oContent.displayContent,true);
  	    // as this is triggered by an event, 'this' refers to the event src i.e. the button in this case
  	  }
  	  
  	  function fBack() {
  	    oContent.sendFormData(this.getAttribute('targetTemplate'),oContent.displayContent,false);
  	    // as this is triggered by an event, 'this' refers to the event src i.e. the button in this case
  	  }
  		
  	  function fTitle() {
  	    function fSetCaption(sCaption) {
  	      // remove all the children nodes (probably text)
  	      while(oTitle.lastChild) oTitle.removeChild(oTitle.lastChild);
  	      oTitle.appendChild(document.createTextNode(sCaption));
  	    }
  	    
  		var oTitle=document.createElement('DIV');
  	    oTitle.setAttribute('id','_md_title');
        fSetCaption(sCaption);
        oDialog.setCaption=fSetCaption;
  		return(oTitle);
  	  }
		
  	  function fContent() {
  	    // create a div to display the content and add some methods to allow other objects to load content in
  	    
  	    function fDisplayContent(sResponseText,sResponseXML) {  	      
  	      // if the result isn't ok then quit.  The input screen will stay the same and allow a new values to be enteres
  	      function fHighlightFieldErrors(sXML) {
  	        try {
  	          var sType=sXML.getElementsByTagName('wizardResult')[0].getElementsByTagName('errorData')[0].getElementsByTagName('type')[0].firstChild.nodeValue;
  	          if(sType!='field') return;
  	          var sInternalFieldName=sXML.getElementsByTagName('wizardResult')[0].getElementsByTagName('errorData')[0].getElementsByTagName('internalFieldName')[0].firstChild.nodeValue
  	          var cFields=document.getElementsByName(sInternalFieldName);
  	          if(cFields.length!=1) return;
  	          var oTR=cFields[0];
  	          while(oTR.tagName!='TR') oTR=oTR.parentNode;
  	          while(document.getElementById('errorRow')) document.getElementById('errorRow').removeAttribute('ID');
  	          oTR.setAttribute('id','errorRow');
  	          oTR.scrollIntoView(true);
  	        }
  	        catch(e) {
  	          // do nothing
  	        }	    
  	      }
  	      
  	      if(!fResultOK(sResponseXML)) {
  	        fHighlightFieldErrors(sResponseXML);
  	        return;
  	      }
  	      
  	       oContent.removeAttribute('validations');
  	      
  	      // in the returned XML if...
  	      if(sResponseXML.getElementsByTagName('caption')[0]) oDialog.setCaption(sResponseXML.getElementsByTagName('caption')[0].firstChild.nodeValue);
  	      
  	      if(sResponseXML.getElementsByTagName('nextTemplate')[0]) {
  	        if(sResponseXML.getElementsByTagName('nextTemplate')[0].getAttribute('validations')) {
  	          oContent.setAttribute('validations',sResponseXML.getElementsByTagName('nextTemplate')[0].getAttribute('validations'));
  	        }
  	        // ...there's a next element set up the next button and turn off the OK button
  	        oToolbar.nextButton.setAttribute('targetTemplate',sResponseXML.getElementsByTagName('nextTemplate')[0].firstChild.nodeValue);
  	        oToolbar.nextButton.switchOn();
  	      } else oToolbar.nextButton.switchOff();
  	      
  	      if(sResponseXML.getElementsByTagName('backTemplate')[0]) {
  	        // ... there's a back element set up the back button
  	        oToolbar.backButton.setAttribute('targetTemplate',sResponseXML.getElementsByTagName('backTemplate')[0].firstChild.nodeValue);
  	        oToolbar.backButton.switchOn();
  	      } else oToolbar.backButton.switchOff();
  	        
  	      if(sResponseXML.getElementsByTagName('okTemplate')[0]) {
  	        // ... there's a back element set up the back button
  	        if(sResponseXML.getElementsByTagName('okTemplate')[0].getAttribute('validations')) {
  	          oContent.setAttribute('validations',sResponseXML.getElementsByTagName('okTemplate')[0].getAttribute('validations'));
  	        }  	        
  	        oToolbar.okButton.setAttribute('targetTemplate',sResponseXML.getElementsByTagName('okTemplate')[0].firstChild.nodeValue);
  	        oToolbar.okButton.switchOn();
  	      } else oToolbar.okButton.switchOff();  
  	        
  	      if (sResponseXML.getElementsByTagName('htmlContent')[0]) {
  	        //  ...there's HTML content (in a CDATA section, show it in the content window
  	        sHTML=sResponseXML.getElementsByTagName('htmlContent')[0].firstChild.nodeValue;
  	        oContent.innerHTML=sHTML;
  	        
  	        if(sResponseXML.getElementsByTagName('htmlContent')[0].getAttribute('onload')) {
  	          try {
  	            eval(sResponseXML.getElementsByTagName('htmlContent')[0].getAttribute('onload'));
  	          } catch(e) {
  	            alert(e)
  	            // don't do anything
  	          }
  	        }  	        
  	      }
  	      else {
  	        // if there's no content then it's probably an error - probably switch off some of this debugging in the gold version
  	        oContent.innerHTML=sResponseText;
  	        oToolbar.okButton.switchOff();
  	      }
  	      
  	      // need to set to auto first because if it's hidden then the scroll height is equal to the content height
  	      oContent.style.overflow='auto';
  	      if(oContent.scrollHeight < oContent.offsetHeight) oContent.style.overflow='hidden';  	      
  	    }
  	    
  	    function fSendFormData(_sTemplateLocation,fCallback, bPostFormVars) {  // use the ubiquitous fRequest object to load HTML into the content area  	      
  	      function fPassFormVars() { // if the content area contains any forms, post up the values from the fields 	        
  	        function fSetPostVars(oElement) {
  	          function fFormElementValue(oElement) { // extract the correct value for the form element type
  	            var sValue=null;
  	            var type = $(oElement).attr('type');
                //var type=oElement.getAttribute('type');
                if(!type) {
              	  type='text';
                }
                switch(oElements[e].tagName) {
  	              case 'INPUT': switch(type) {
    	            // TODO: use jQuery val() like with normal agileBase input fields
  	                case 'text':sValue=oElement.value; break;  // might need to change this to '' rather than null if we want the wizard to allow editing
  	                case 'checkbox':sValue=oElement.checked?'true':null; break;
  	                case 'radio':sValue=oElement.checked?oElement.value:null; break;
  	                default: sValue=oElement.value;
  	              } break;
  	              
  	              case 'SELECT': sValue=oElement.options[oElement.selectedIndex].value; break;
  	              
  	              case 'TEXTAREA':sValue=$(oElement).val(); break;  	              
  	            }  
  	            return sValue;	        
  	          }
  	          var jqElement = $(oElement);
  	          var sName = jqElement.attr('name');
  	          var type = jqElement.attr('type');
  	          //var sName=oElement.getAttribute('name'); 
              //var type=oElement.getAttribute('type');
  	          if(!sName) return false; // some items in the elements list don't seem to be form objects.  If it doesn't have a name, we're not interested
  	          var sValue=fFormElementValue(oElements.item(e)); 
  	          if(!sValue && (type != 'hidden')) return false; // only set visible field values in aPostVars if the element has a value to avoid sending values for unchecked boolean input types
  	          // special case: blank preset_row_id causes server error
  	          if (sValue == '' && sName == 'preset_row_id') return false;
  	          // begin to set the values...
  	          aPostVars[sName]=sValue;
  	          /* look at all the attributes that the DOM object has and pass all the 
       			 ones prefixed gtpb_ to the server in the post request.  */
    	      for (var i=0;i<oElement.attributes.length;i++)
                with(oElement.attributes.item(i))
                  if (nodeName.search(/^gtpb_/)>=0)
                    aPostVars[nodeName.replace(/^gtpb_/,'')]=nodeValue; 
                    
              return true; 	 // no need to return anything but might be useful in the future :)       
  	        }  	        

  	        var oForms=oContent.getElementsByTagName('FORM');  // look for any forms in the content area
  	        for(var f=0; f<oForms.length; f++) { // for each form, look at all the elements...
  	          var oElements=oForms[f].elements;  // ... and set the post vars from them
  	          for(var e=0; e<oElements.length; e++) fSetPostVars(oElements[e]);
  	        }  
  	      }
  		  var aPostVars=new Array();
  		  aPostVars['return']=_sTemplateLocation; // the local template location, not necessarily that passed when the dialog obj is created!
          aPostVars['returntype']='xml'; // it's always going to be XML
          if(bPostFormVars) fPassFormVars();
          var oReq=new fRequest('AppController.servlet',aPostVars,fCallback,1); // run the request and show a wait window  
  	    }
  	    
  		var oContent=document.createElement('DIV');
  		oContent.setAttribute('id','_md_content');
  		fSendFormData(sTemplateLocation,fDisplayContent,true); // load the content passed in the constructor call of the dialog
  		
  		oContent.displayContent=fDisplayContent; // make fDisplayContent available to other objects (e.g. fStep - called when the back & next buttons are pushed)
  		oContent.sendFormData=fSendFormData; // make available to other objects e.g. fStep, fOK
  		
  		return oContent;
  	  }
		
  	  function fToolbar() {  	  
  	    // function to create generic button
  		function fCreateButton(sCaption,fAction) {  		  				
  		  var oButton=document.createElement('BUTTON');
  		  oButton.setAttribute('caption',sCaption.toLowerCase()); // an expando property to allow the stylesheet to detect specific buttons
  		  /* put the caption in a span because button icons are applied as a bg image.  If a bg
  		     image is set directly on the button, its whole appearance changes.  Applying the bg
  		     image to the span solves the issue. */
  		  var oCaptionSpan=document.createElement('SPAN');
  		  oCaptionSpan.appendChild(document.createTextNode(sCaption));
  		  oButton.appendChild(oCaptionSpan);
  		  
  		  oButton.toolbar=oToolbar;
  		  $(oButton).click(fAction);
  		  
  		  // add switchon, switchoff functionality to the buttons so that they can be controlled by fDisplayContent
  		  oButton.switchOn=function() {
    	    this.removeAttribute('disabled');
  		  };
  		  oButton.switchOff=function() {
  			this.setAttribute('disabled','true');
  		  };
  		  
  		  if(!aButtons.containsValue(sCaption.toLowerCase())) oButton.style.display='none';  		  
  		  
  		  oToolbar.appendChild(oButton);
  		  
  		  // if there are more than 1 buttons specified, switch them all off 
  		  // the cancel button will be turned on later
  		  if(aButtons.length>1) oButton.switchOff();
  		  else oButton.switchOn();
  		  
  		  return oButton;  // note that oButton is the actual HTML object
  		}
			
  		var oToolbar=document.createElement('DIV');
  		oToolbar.setAttribute('id','_md_toolbar');
  		
  		var oSpacer=document.createElement('DIV');
  		oSpacer.setAttribute('id','_md_spacer');
  		
  		var aButtons=sButtons.split(' ');
  		Array.prototype.containsValue=function(sValue) {
    		  for(iItem in this) {
    	  		    if(this[iItem]==sValue) return true;
    	      }
    	  	  return false;
  		};
  		
  		// create all the buttons	
  		oToolbar.backButton=fCreateButton('Back',fBack);
  		oToolbar.nextButton=fCreateButton('Next',fNext);
  		oToolbar.appendChild(oSpacer);
  		oToolbar.okButton=fCreateButton('OK',fOK);
  		oToolbar.cancelButton=fCreateButton('Cancel',fDestroy);
  		oToolbar.cancelButton.switchOn();
  		
  		return oToolbar;
  	  }
	  
	  // center the dialog vertically in its offset parent (this should be the blanking div although it could be something else),
	  // vertical equivalent to setting left & right margins to 'auto' in a style definition
	  function fCentraliseVertically() {
	    // if a positioning attribute is passed in the contructor call then the dialog will be positioned absolutely and we don't want to
	    // automatically update it's position.
	    // If only a left or right attriubute is set, it could be that we would want to maintain the vertical position - future functionality maybe...
  	    try {
  	      if(oDialog.style.position=='absolute') return;
  	      var iParentHeight=oDialog.offsetParent.offsetHeight;
  	      var iMarginTop=Math.round((iParentHeight-oDialog.offsetHeight)/2); // this doesn't calculate properly in IE!!  Why is this?
  	      if (iMarginTop<0) iMarginTop=0; // position at the top if the dialog is taller than the available screen space  	    
  	      // Using the margin property because the left right position is set by setting the left-right margin to 'auto'
  	      oDialog.style.marginTop=iMarginTop+'px';  // use a margin to vertically position
  	    } 
  	    catch(e) {
  	      // don't do anything!
  	    }
	    oDialog.style.visibility='visible';
  	    finally {
  	      //  maintain the central position by running again if the window is resized
    	  //top.addEventListener('resize',fCentraliseVertically,false);
    	  $(top).resize(fCentraliseVertically);
  	    }
	  }
	  
	  // split up the string of attributes passed in the contructor and add them as styles to the dialog div
  	  function fCreateAttributes() {
  	    // there doesn't seem to be a built in trim function in JS :(
  	    function fTrim(sString) {
  	      while (sString.substring(0,1)==' ') sString=sString.substring(1,sString.length);
  		  while (sString.substring(sString.length-1, sString.length)==' ') sString=sString.substring(0,sString.length-1);
  		  return sString;
	    }
		
		// foreach item separated by ;
  	    var aAttributes=sAttributes.split(';');
  	    for(var i=0; i<aAttributes.length; i++) {
  	      if(aAttributes[i].match(/=/)) {
  	        aAttribute=aAttributes[i].split('=');
  	        // can't find a standards method for adding styles to an object.  Instead create a string to add the
  	        // style and evaluate it.
  		    eval('oDialog.style.'+fTrim(aAttribute[0])+'=\''+fTrim(aAttribute[1])+'\'');
  		    // if the attribute is a positioning element, need to absolutely position the dialog div (stylesheet sets it to relative by default)
  		    if (fTrim(aAttribute[0]).match(/^(top|bottom|left|right)$/i)) oDialog.style.position='absolute';
  	      }
  	    }
  	  }
  	  
  	  // create the dialog
  	  var oDialog=document.createElement('DIV');
  	  oDialog.setAttribute('id','_md_dialog'); 
  	  oDialog.style.visibility='hidden'; 	  
  	  
  	  // create the dialog components	
      oDialog.appendChild(fTitle());
  	  var oToolbar=fToolbar();
  	  oDialog.appendChild(oToolbar);
  	  var oContent=fContent();
  	  oDialog.content=oContent;
  	  oDialog.appendChild(oContent); 
  	  
  	  // give the div a centraliseVertically method
  	  oDialog.centraliseVertically=fCentraliseVertically;
  	  // set attributes passed in the constructor
  	  if(sAttributes) fCreateAttributes();	
  	  
  	  oDialog.destroy=fDestroy;
  	  
  	  // load any customisations - from external library loaded in display_application
  	  try {
  	    var oCustomisations=new _md_fCustomisations(oDialog);
  	    oDialog.customisations=oCustomisations; // allow objects on the screen to use the customisations too
  	    var bLoadedCustomisations=true;
  	  } catch(e) {
  	    bLoadedCustomisations=false;
  	  } finally {  	  
  	    return oDialog;
  	  }				
    }

    var oBlank=document.createElement('DIV');
    oBlank.setAttribute('id','_md_blank');
    var oDialog=fDialog(); // the HTML div is returned with methods added to it
    oBlank.appendChild(oDialog);
    top.document.body.appendChild(oBlank); // blank out the whole screen by adding the blanker to the top element
    oDialog.centraliseVertically();	
    return oDialog;					
  }
  
  if((sTemplateLocation=='') && fCallbackFn) fCallbackFn(null,null);
  else var oModalDialog=fCreate();
}

function f_mdp_rowclick(oRow) {
  var oHidden=document.getElementById('_mfp_hidden_field');
  sRowId=oRow.getAttribute('name');
  oHidden.value=sRowId;
  
  while(document.getElementById('_mfp_current_row')) document.getElementById('_mfp_current_row').removeAttribute('id');
  oRow.setAttribute('id','_mfp_current_row');
}
