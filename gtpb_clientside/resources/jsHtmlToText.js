/*
Copyright (C) 2006 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

HTML decoding functionality provided by: http://code.google.com/p/google-trekker/
*/
function htmlToText(html) {
	return html
		// Remove line breaks
		.replace(/(?:\n|\r\n|\r)/ig,"")
		// Turn <br>'s into single line breaks. 
		.replace(/<\s*br[^>]*>/ig,"\n") 
		// Turn </li>'s into line breaks.
 		.replace(/<\s*\/li[^>]*>/ig,"\n") 
		// Turn <p>'s into double line breaks.
 		.replace(/<\s*p[^>]*>/ig,"\n\n") 
		// Remove content in script tags.
 		.replace(/<\s*script[^>]*>[\s\S]*?<\/script>/mig,"")
		// Remove content in style tags.
 		.replace(/<\s*style[^>]*>[\s\S]*?<\/style>/mig,"")
		// Remove content in comments.
 		.replace(/<!--.*?-->/mig,"")
 		// Format anchor tags properly. 
 		// e.g.
 		// input - <a class='ahref' href='http://pinetechlabs.com/' title='asdfqwer\"><b>asdf</b></a>
 		// output - asdf (http://pinetechlabs.com/)
 		.replace(/<\s*a[^>]*href=['"](.*?)['"][^>]*>([\s\S]*?)<\/\s*a\s*>/ig, "$2 ($1)")
		// Remove all remaining tags. 
 		.replace(/(<([^>]+)>)/ig,"") 
		// Make sure there are never more than two 
		// consecutive linebreaks.
 		.replace(/\n{2,}/g,"\n\n")
		// Remove tabs. 	
 		.replace(/\t/g,"")
		// Remove newlines at the beginning of the text. 
 		.replace(/^\n+/m,"") 	
		// Replace multiple spaces with a single space.
 		.replace(/ {2,}/g," ");
		// Decode HTML entities.
 		//.replace(/&([^;]+);/g, decodeHtmlEntity );
}

