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
function fTagCloud(jTagCloud) {
	function fTagCloudLoaded() {
	  $(this).removeClass('loading');
      fLoadResults();
    }
	  
	function fLoadResults() {
	  function fResultsLoaded() {
	    jResults.removeClass('loading');
	    // noresults is an id passed printed out of the no results template
	    if($('#noresults').length>0) fPopup(sBaseURL+'?'+jTagCloud.attr('disambiguationtemplate'), null, null, 'Results disambiguation', fPopupCallback, 'onload');
	  }
	  
	  function fPopupCallback(bSuccess,oPopup, sType) {
	     if(sType=='onload') {
	    	 $(oPopup).filter('a.disambiguation').click(function() {  // this would be better using oPopup
	    		 $(oFilter).empty();
	    		 oFilter.add(this.getAttribute('filter').replace(/\*/ig,''));
	    		 oPopup.ok();
	    	 });
	     }
	  }
	  
	  // jResults is set as the tag cloud is initialised
	  jResults.empty();
	  jResults.addClass('loading');
	  var sQueryString='?'+jResults.attr('resultstemplate');
	  if(oFilter.filterValue) var sFilterValue=oFilter.filterValue;
	  else var sFilterValue='';
	  sQueryString+='&'+jTagCloud.attr('filterparams')+'='+escape(sFilterValue);
	  
	  jResults.load(sBaseURL+sQueryString,null,fResultsLoaded)
	}
	
    //var jTagCloud=$('#tagCloud');  // this is now set in the function call
	
    // could do some detection around this and check that it's found properly
    // could also allow this to be passed in programatically
    // and/or expose so that it can be found by external functions
    // this functionality isn't needed right now, however
    var jResults=$(jTagCloud.attr('resultslocation'));
    var sBaseURL=jTagCloud.attr('baseurl'); // again, could do some testing and fail-safeing here
      
    var oSearchWrapper=document.createElement('div');
    oSearchWrapper.setAttribute('id','searchWrapper');
    $(oSearchWrapper).appendTo(jTagCloud.parent());
      
    var oFilter=document.createElement('div');
    oFilter.setAttribute('id','filter');
    
    oFilter.showDefaultText=function() {
      with ($(this)) {
        empty();
        append('<h1>filter is empty</h1>');
        append('<p>click some words in the tag cloud to start searching</p>');
      }
    }    
    oFilter.showDefaultText();
    
    oFilter.run=function () {	        
      var sFilterValue='';
      if($(this).find('a').length==0) this.showDefaultText();
      $(this).find('a').each(function() { // this is oFilter
        var bLast=$(this).is(':last-child')?true:false;  // this is an a
        sFilterValue+='*'+this.innerHTML;
        if(!bLast) sFilterValue+=' AND ';
      });
      sFilterValue=sFilterValue.replace(/&nbsp;/ig,'');
      
	  this.filterValue=sFilterValue;
        
      jTagCloud.empty();
      jTagCloud.addClass('loading');
      var sQueryString='?'+jTagCloud.attr('filterparams')+'='+escape(sFilterValue)+'&'+jTagCloud.attr('tagcloudtemplate');
	  jTagCloud.load(sBaseURL+sQueryString,null,fTagCloudLoaded);	        
	}
	
	oFilter.add=function(vFilterObj) {
	  if($(this).find('a').length==0) $(this).empty(); // clear the default text if there are no links
	  try {
	      var aSearchWords=vFilterObj.split(' ');  // split the search term by space
	      jQuery.each(aSearchWords,function() {  // add the separate words individually
	        $(oFilter).append($('<a href="#">&nbsp;'+this+'&nbsp;</a>'));
	      });		
	  } catch(e) {
		  try {			  
			  $(vFilterObj).appendTo($(this));
		  } catch(e) {
			  alert("can't add filter");
		  }
	  } finally {
		  oFilter.run();
	  }
	};
    
    $(oFilter).appendTo($(oSearchWrapper));
      
    var oSearch=document.createElement('div');
    oSearch.setAttribute('id','search');
    $(oSearch).appendTo($(oSearchWrapper));
      
    var oInput=document.createElement('input');
    $(oInput).appendTo($(oSearch));
      
    var oButton=document.createElement('button');
    $(oButton).append(document.createTextNode('search'));
    $(oButton).appendTo($(oSearch));
    $(oButton).click(function() {
      if(oInput.value.length<1) return;
      oFilter.add(oInput.value);
      oInput.value='';
    });
    
    jTagCloud[0].load=function(sExtraParams) {
	  jTagCloud.load(sBaseURL+'?'+jTagCloud.attr('tagcloudtemplate')+(sExtraParams?sExtraParams:''),null,fTagCloudLoaded);     
	}; 
    jTagCloud[0].load();
    
    // if any filter are set in the session, load them in as text (e.g. if the page has been refreshed)
    $(oFilter).load(sBaseURL+'?'+jTagCloud.attr('filterstemplate'),function() {
      if($(this).find('a').length==0) this.showDefaultText();
    });
    
    /* control an action for a click event on the tags in the cloud (note that using the live function means
       that this action is applied to any tag in the cloud - even new ones added after this is called)
     */
    $('#'+jTagCloud.attr('id')+' a').live('click',function() { // this would be better if we used jTagCloud rather than #tagCloud
      $(this).fadeOut('slow',function() {
    	oFilter.add(this);
        $(this).toggleClass('clicked');
        $(this).fadeIn('slow');	          
	    //oFilter.run();
	  });
	  $(this).toggleClass('clicked');
	  this.scrollintoview();
      return false; 
    });
      
    /* control an action for any terms in the filter box
     */
    $('#filter a').live('click',function() {  // this would be better if we used oFilter rather than #filter
      $(this).fadeOut('slow',function() {
        $(this).remove();
        $(this).toggleClass('clicked');
        oFilter.run();
      });
      $(this).toggleClass('clicked');
      return false;
    });
}  	

$(document).ready(function(){	    
	    $('.tagCloud').each(function(){
	    	fTagCloud($(this));
	    });
});