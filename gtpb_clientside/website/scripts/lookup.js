function onSubmit() {
  inputChange(document.getElementById("input"));
  return false;
}

function inputChange(input) {
  var user = input.value.toLowerCase();
  if (!user) {
      user = defaultUser;
      input.value = user;
  }
  newSlideShow(user);
}

function showStatus(msg) {
  var ss = document.getElementById("slideshow");
  ss.innerHTML = '<div class=\"feed-loading\">' + msg + '</div>';
}

function ssFeedLoadedCallback(result) {
  if (result.error || result.feed.entries.length <= 0) {
    showStatus('No photos could be loaded');
    // Stop the slideshow..
    result.error = true;
  }
}

function showSlideShow(url) {
  var options = {
      displayTime: 2500,
      transistionTime: 800,
      scaleImages: true,
      thumbnailTag: 'content',
      feedLoadCallback: ssFeedLoadedCallback,
      linkTarget : google.feeds.LINK_TARGET_BLANK
  };
  new GFslideShow(url, "slideshow", options);
}

function newSlideShow(user) {
  showStatus('Resolving feed for ' + user);
  var url = 'http://www.flickr.com/photos/' + user;
  google.feeds.lookupFeed(url, lookupDone);
}

function lookupDone(result) {
  if (result.error || result.url == null) {
    showStatus('Could not locate feed for user');
  } else {
    showStatus('Found Photo Feed');
    // We need to switch over atom to RSS to get Yahoo Media..
    var url = result.url.replace('format=atom', 'format=rss_200');
    showSlideShow(url);
  }
}
