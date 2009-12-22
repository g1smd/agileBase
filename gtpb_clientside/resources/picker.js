/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
function fPicker(){
  function fLoadContent() {
    function fDisplayContent(sText,sXML) {
      with($(oElements.picker.content)) {
    	empty();
        append(sText);
      }
    }
    
    var aPostVars=new Array();
    if (!oElements.picker.reportsList) {
      aPostVars['return']='gui/resources/picker/wiki';
    } else {
      aPostVars['return']='gui/resources/picker/report';
      aPostVars['set_custom_report']='true';
      aPostVars['reportkey']='picker_report';
      aPostVars['custominternaltablename']=oCaller.formEl.getAttribute('internalTableName');
      if(oElements.picker.reportsList.selectedIndex<0) {
    	alert('no reports available to load content for');
    	return;
      }
      aPostVars['custominternalreportname']=oElements.picker.reportsList.options[oElements.picker.reportsList.selectedIndex].value;
    }
    var oReq=new fRequest('AppController.servlet',aPostVars,fDisplayContent,0);
  }
  
  function fLoadReports() {
    function fDisplayReports(sText,sXML) {
      // would be better to parse over a list of XML elements and add them through the DOM
      // however use innerHTML for the time being because it's quicker

      /*with(oElements.picker.reportsList) {
        innerHTML=sText;
        removeAttribute('disabled');
      }*/
      with($(oElements.picker.reportsList)) {
    	empty();
        append(sText);
        oElements.picker.reportsList.removeAttribute('disabled');
      }
      fLoadContent();
    }
  
    var aPostVars=new Array();
    if (oCaller.getAttribute('isWiki')) {
      // aPostVars['return']='gui/resources/picker/reports_list';
    } else {
      aPostVars['return']='gui/resources/picker/reports_list';
      aPostVars['set_custom_field']='true';
      aPostVars['fieldkey']='picker_field';
      aPostVars['custominternaltablename']=oCaller.formEl.getAttribute('internalTableName');
      aPostVars['custominternalfieldname']=oCaller.formEl.getAttribute('internalFieldName');
      var oReq=new fRequest('AppController.servlet',aPostVars,fDisplayReports,0);
	}        
  }
       
  function fDestroy() {
    oElements.picker.parentNode.removeChild(oElements.picker);
    // hack for iPod/iPhone compatibility - see fSetOverFlowHack in relation.vm
    document.getElementById('gtpb_wrapper').style.overflow = 'visible';
  }
 
  function fHTMLElements() {
    var oPicker=document.createElement('DIV');
    $(oPicker).addClass('relation_picker');
    oPicker.setAttribute('id','relationPicker');

    var oToolbar=document.createElement('DIV');
    oToolbar.setAttribute('id','toolbar');
 
    var oCloser=document.createElement('A');
    oCloser.innerHTML='cancel';
    $(oCloser).addClass('closer');
    oCloser.setAttribute('href','#');
    $(oCloser).click(fDestroy);
    oToolbar.appendChild(oCloser);
  
    if (!oCaller.getAttribute('isWiki')) {
      var oReportsList=document.createElement('SELECT');
      oReportsList.setAttribute('disabled','true');
      oReportsList.appendChild(new Option('loading (wait)...',''));
      $(oReportsList).change(fLoadContent);
      oToolbar.appendChild(oReportsList);
      oPicker.reportsList=oReportsList;
    }      

    oPicker.appendChild(oToolbar);
  
    var oContent=document.createElement('DIV');
    oContent.setAttribute('id','content');
    //oContent.innerHTML=oCaller.formEl.getAttribute('internalFieldName')+' > loading (wait)...';
    oContent.innerHTML='loading (wait)...';
    oPicker.appendChild(oContent);

    this.picker=oPicker;
    oPicker.content=oContent;
  }
  
  // note that 'this' refers to the link as the function was attached with addEventListener
  // see http://developer.mozilla.org/en/docs/DOM:element.addEventListener
  // updated to use jQuery click() method for x-browser compatibility
  var oCaller=this;
  
  var oElements=new fHTMLElements();
  if (oElements.picker.reportsList) { // no reports list if the field is a Wiki field
    fLoadReports();
  } else {
    fLoadContent();
  }

  //oCaller.formEl.parentNode.appendChild(oElements.picker);
  document.getElementsByTagName('body')[0].appendChild(oElements.picker);
  oElements.picker.inputElement=oCaller.formEl;
  oElements.picker.destroy=fDestroy;
  return false;
}

function fPickItem(oRow) {
  /* this is called from the table inside the picker, not the div itself so 
     it's outside of the main object */
  function fFindPicker() {
    var oPicker=oRow.parentNode;
    while ((oPicker.getAttribute('id')!='relationPicker') && (oPicker.tagName!='BODY'))
      oPicker=oPicker.parentNode;
    return oPicker;
  }
  
  function fShowFieldCaption(sResponseText,sResponseXML) {
    with(oPicker) {
      inputElement.label.update(sResponseText);
      inputElement.doUpdate(inputElement.value, true);
      destroy();
    }
  }
  
  var oPicker=fFindPicker();
  with (oPicker) {
    with(inputElement) {
      value=oRow.getAttribute('keyValue');
      if (inputElement.label) { // there should always be a label
        // label.innerHTML=oRow.getAttribute('displayValue'); // no longer used
        // download the display value over XML - this can't be done directly from the template
        var aPostVars=new Array();
        aPostVars['return']='gui/resources/picker/relation_display_value';
        aPostVars['set_custom_string']='true';
        aPostVars['stringkey']='picker_value';
        aPostVars['customstringvalue']=oRow.getAttribute('displayValue'); // note that display_value will be the row_id of the related record
        var oReq=new fRequest('AppController.servlet',aPostVars,fShowFieldCaption,1); // run the request and show a wait window 
      }
      // oRow.name is the rowid for the row
      //setAttribute('gtpb_set_row_id',oRow.getAttribute('name'));
    }
  }
} 
