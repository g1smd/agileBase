/*
 *  Copyright 2010 GT webMarque Ltd
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
function fArrayContains(aArray, vTest){	
  for (var i=0;i<aArray.length;i++)
    if (aArray[i]==vTest) return true;
  return false;
}

function fSelectChange(oEvent){ 
  var oParent=oEvent.target;
  var oChange=new fDoSelectChange(oParent);
}

function fDoSelectChange(oParent){	
  function fRepopulateList(oParent,oDependent){	
    function fReqComplete(sResponseText,sResponseXML){	
      function fCreateOptions(){	
        // clear down the list
		while (oDependent.firstChild) oDependent.removeChild(oDependent.firstChild);
		aOptions=sResponseXML.getElementsByTagName('request')[0].getElementsByTagName('option');
		for (var iOptions=0;iOptions<aOptions.length;iOptions++){
		  sDisplay=aOptions[iOptions].getElementsByTagName('display_value')[0].firstChild.nodeValue;
		  sValue=aOptions[iOptions].getElementsByTagName('internal_value')[0].firstChild.nodeValue;
		  oOption=new Option(sDisplay);
		  oOption.value=sValue;
		  oDependent.options.add(oOption);
		} 
	  }

      // if the value of the parent has changed, quit
      if(sValue!=oParent.value) return;
     	
      fCreateOptions();	
      var oChange=new fDoSelectChange(oDependent); //update any children
      oDependent.removeAttribute('disabled');		
	}
	
    function fSetPostVars(){ 
	  // create a key value array of the variables to post with the request to the server
   	  var aPostVars=new Array();
   	  if(oDependent.getAttribute('return')){
   	    aPostVars['return']='gui/resources/input/'+oDependent.getAttribute('return');
   	    aPostVars['key']='parent'+oDependent.getAttribute('parentType');
   	  }
   	  else {
   	    switch (oParent.name.replace(/.*internal/ig,'')){	
   	      case 'reportname':
   		    aPostVars['return']='gui/resources/input/xmlreturn_all_reportfields';
   		    aPostVars['key']='parentreport';
   		    break;
		  case 'tablename':
		    aPostVars['return']='gui/resources/input/xmlreturn_tablefields';
   		    aPostVars['key']='parenttable';
   		    break;
        }
   	  }

      aPostVars['returntype']='xml'; 
	  aPostVars['set_custom_string']='true';
	  aPostVars['value']=sValue;

      return aPostVars;
   	}
   	
	oDependent.setAttribute('disabled','true');
	var sValue=oParent.value;
	var oReq=new fRequest('AppController.servlet', fSetPostVars(), fReqComplete, -1);
  }

  if(!oParent.dependents) {
	return;
  }
  for (var iDependent=0;iDependent<oParent.dependents.length;iDependent++) {
    fRepopulateList(oParent,oParent.dependents[iDependent]);
  }
}

function fInitialiseDependencies(){	
  function fRegisterDependent(oChild,oParent){	
    if (!oParent.dependents) oParent.dependents=new Array;
	else // no need to do this if the dependents list is being created for the first time
	  if (fArrayContains(oParent.dependents,oChild)) return false;  // the child is already stored as a dependent
	oParent.dependents.push(oChild);
	// add a listener.  Duplicate listeners are discarded so this will only be triggered once event if it's added multiple times
	$(oParent).change(fSelectChange);
	return true;
  }

  var aInitialised=new Array();
  var aSelect=document.getElementsByTagName('SELECT');
  // for every select
  for (var iSelect=0;iSelect<aSelect.length;iSelect++){	
	var selectName = aSelect[iSelect].name;
	if (selectName.indexOf('table') > -1) {
		alert('looking at select ' + selectName);
	}  
    // see whether the child has been registered already, continue if it has
    if(aSelect[iSelect].getAttribute('registered')=='true') continue;
    // find its parents' name.  If none, continue to next select element
	var sParent=aSelect[iSelect].getAttribute('parent');
	if (!sParent) continue;
	var oForm=aSelect[iSelect].form;
	
    // only allow dependencies within the same form
	var oParent=oForm.elements[sParent];  // if there is more than one element with this name a collection will be returned
	if(!oParent.form){ // if the parent doesn't have an associated form, it's not a form element
	  if(aSelect[iSelect].getAttribute('uselastordinal')) oParent=oParent[oParent.length-1]; // is option set to use last ordinal element, use  the last element in the array
	  else continue; // otherwise continue
	}
	
	if (fRegisterDependent(aSelect[iSelect],oParent)) aSelect[iSelect].setAttribute('registered','true') ;
	var oChange=new fDoSelectChange(oParent);
  }
}

fInitialiseDependencies();