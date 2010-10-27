
		function  loadFeedControl() {
			var feed  = "http://blog.agilebase.co.uk/?feed=rss2";
				var options = {
				numResults : 6,
						displayTime : 15000,
						title : "<a href='http://blog.agilebase.co.uk'>announcements</a>",
						linkTarget : google.feeds.LINK_TARGET_SELF
				};
			var fg = new GFdynamicFeedControl(feed, "announce", options);
		}

		function LoadSlideShow() {
			// test known to work: http://feed.photobucket.com/images/sunsets/feed.rss
			// flickr: http://api.flickr.com/services/feeds/photoset.gne?set=72157615624584156&nsid=36549361@N06&lang=en-us
				var screenshotfeed  = "http://picasaweb.google.com/data/feed/base/user/oliver.kohll/albumid/5315354509188522353?alt=rss&kind=photo&hl=en_US";
				var options = {
						displayTime: 17000,
						transistionTime: 500,
						scaleImages: false,
						maintainAspectRatio: true,
						linkTarget: google.feeds.LINK_TARGET_BLANK,
						thumbnailSize : GFslideShow.THUMBNAILS_LARGE,
						pauseOnHover : false
				};
				var ss = new GFslideShow(screenshotfeed, "screenshots", options);
		}

		$(document).ready(function(){
			// frame buster needed as we may be in one of the agileBase application panes after a login timeout
			if ((top.frames.length!=0) || (document.location.href.indexOf("logout") > -1)) {
				top.location="http://agilebase.co.uk/start";
			}
			// focus the 'username' box for logging in
			$("#j_username").focus();
			try {
				// start the RSS headlines from blog
				google.setOnLoadCallback(loadFeedControl);
				// start the RSS screenshots from flickr
				google.setOnLoadCallback(LoadSlideShow);
			} catch(err) {
			}
			// test for iPhone
			if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i))) {
				if(document.location.href.indexOf("mobile") == -1) {
					$('#login').html('<big>Mobile users log in at<br><a href="http://www.agilebase.co.uk/mobile">www.agilebase.co.uk/mobile</a></big>');
				}
			}
			// if enter is pressed in username field with no password, don't submit details
			$("#loginform").submit(function() {
				if($('#j_password').val() == '') {
					return false;
				} else {
					return true;
				} 
			});
			// just in time etc. tooltips
			$("#just_in a").each(function(i) {
				$(this).mouseover(function() {
					$(this).parent().find(".just_tooltip").fadeIn('normal');
				});
			});
			$(".just_tooltip").mouseout(function() {
				$(this).fadeOut();
			});
		}); 

		function showSection(sectionName) {
			$("#navigation").find("a").removeClass("current");
			$("#nav_" + sectionName).addClass("current");
			$(".detail").fadeOut("fast");
			$("#" + sectionName).fadeIn("fast");
		}

