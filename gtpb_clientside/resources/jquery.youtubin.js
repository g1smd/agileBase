/**
 * jquery.youtubin.js
 * Copyright (c) 2009 Jon Raasch (http://jonraasch.com/)
 * Licensed under the Free BSD License (see http://dev.jonraasch.com/youtubin/documentation#licensing)
 * 
 * @author Jon Raasch
 *
 * @projectDescription    jQuery plugin to allow simple and unobtrusive embedding of youtube videos with a variety of options
 * 
 * @documentation http://dev.jonraasch.com/youtubin/documentation
 *
 * @version 0.1.0
 * 
 * @requires jquery.js (tested with v 1.3.2)
 * 
 * @optional SwfObject 2
 * 
 * NOT AFFILIATED WITH YOUTUBE
 */


( function( $ ) {    
    var youtubinCount = 0;
    var youtubinMode  = 0;
    
    $.youtubin = function( options, box ) {
        // if first time
        if ( !youtubinMode ) {
            if ( typeof( swfobject ) == 'undefined' ) youtubinMode = 'noScript';
            else youtubinMode = '2.1';
        
            if ( typeof( box ) == 'undefined' || !box ) {
                $('a[href^=http://www.youtube.com/watch?v=]').youtubin(options);
                
                return false;
            }
        }
        
        // define options
        var options = options || {};
        
        options.swfWidth  = options.swfWidth || "425";
        options.swfHeight = options.swfHeight || "344";
        options.flashVersion = options.flashVersion || "9.0.0";
        options.expressInstall = options.expressInstall || "";
        
        options.flashvars = options.flashvars || {};
        options.params    = options.params || {
            menu : "false",
            loop : "false"
        };
        
        options.srcOptions = options.srcOptions || '&hl=en&fs=1&';
        options.method     = options.method || 'href';
        
        options.replaceTime = options.replaceTime || 'auto';
        options.wrapper = options.wrapper || '<div></div>';
        
        
        var $box = $(box);
        
        // depending on replaceTime trigger replacement or attach click event
        if ( options.replaceTime == 'auto' ) replaceIt();
        else if ( options.replaceTime == 'click' ) $box.click( function(ev) { ev.preventDefault(); replaceIt(); } );
        
        function replaceIt() {
            var src = $box.attr(options.method);
    
            // build swf url from youtube link
            if ( src.substr(0,31) == 'http://www.youtube.com/watch?v=' ) src = 'http://www.youtube.com/v/' + src.substr(31) + options.srcOptions;
        
            // give replacement area an id if it doesnt have one
            if ( $box.attr('id').length ) var boxId = $box.attr('id');
            else {
                var boxId = 'youtubin-' + youtubinCount;
                youtubinCount++;
                
                $box.attr('id', boxId);
            }
            
            if ( options.wrapper ) $box.wrap(options.wrapper);
        
            // embed the swf according to youtubinMode
            switch( youtubinMode ) {
                case '2.1' : 
                    swfobject.embedSWF(src, boxId, options.swfWidth, options.swfHeight, options.flashVersion, options.expressInstall, options.flashvars, options.params);
                break;
                
                default : 
                    $box.html('<object width="' + options.swfWidth + '" height="' + options.swfHeight + '"><param name="movie" value="' + src + '"></param><param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param><embed src="' + src + '" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="' + options.swfWidth + '" height="' + options.swfHeight + '"></embed></object>');
                break;
            }
        }
        
    };
    
    $.fn.youtubin = function( options ) {
        this.each( function() 
            {
                new $.youtubin( options, this );
            }
        );
        
        return this;
    };
})( jQuery );