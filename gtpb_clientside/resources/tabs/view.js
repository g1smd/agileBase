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

/* 
 * JS for view tab is separated out because the mobile version of agileBase
 * only needs these functions, not those for the edit, manage etc. tabs
 */
function loadMap(postcode) {
	if (!postcode) {
	  return;
	}
	if (GBrowserIsCompatible()) {
		var geocoder = new GClientGeocoder();
		geocoder.getLatLng(
			postcode,
			function(point) {
				if (!point) {
				  alert("Postcode " + postcode + " not found");
				} else {
					var mapUrl = 'https://maps.google.com/staticmap?center=' + 
					  point.toUrlValue() + '&zoom=11&size=100x100&maptype=terrain&key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBSsFD5D7A41QFa4vWfOgDnykADPDxRmS3oyj7HLtk0xVDNhc4xnV0s6sg&sensor=false';
					$('#map').html('<a href="http://maps.google.co.uk/maps?f=q&hl=en&q='
					  + postcode
					  + '" target="_blank"><img border="1" src="'
					  + mapUrl + '" /></a>');
				}
		});
	}
}

function fYouTube() {
	$('a.gtpb_url').each(
		function() {
			var sHref = this.getAttribute('href');
			if ((sHref.indexOf('youtube.com') > -1)
					|| (sHref.indexOf('vimeo.com') > -1)) {
				var oContainer = document.createElement('div');
				oContainer = $(oContainer);
				oContainer.addClass('gtpb_youtube');
				$(this).replaceWith(oContainer);
				oContainer.oembed(sHref);
				setTimeout(function() {
					oContainer.find('object').css("maxWidth","100%");
					oContainer.find('embed').css("maxWidth","100%");
				}, 2000);
			}
		});
}

/* ---------- Add functions to the callFunctions list ---------- */
/* ------ These will be called every time a tab refreshes ------ */

// Don't need to call loadMap when a tab loads, just need to have the fn
// available
// for when a map does need to be loaded
// pane3Scripts.functionList.push(loadMap);
pane3Scripts.functionList.push(fYouTube);
