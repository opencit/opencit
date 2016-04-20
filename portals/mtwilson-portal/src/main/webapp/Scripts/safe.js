/* *
 * safe.js - Helper methods to validate and Escape the response data to avoid any XSS issues 
 *
 * Rules and details @ https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet
 *
 * Copyright (C) 2014-2015 Intel Corporation
 *
 * Author: Naresh K Gadepalli 
 * */


/* *
 * RULE #1 - HTML Escape Before Inserting Untrusted Data into HTML Element Content
 * 
 * Function to change error message into html escaped String.
 *
 * & --> &amp;
 * < --> &lt;
 * > --> &gt;
 * " --> &quot;
 * ' --> &#x27;     &apos; not recommended because its not in the HTML spec (See: section 24.4.1) &apos; is in the XML and XHTML specs.
 * / --> &#x2F;     forward slash is included as it helps end an HTML entity
 *
 * */
function getHTMLEscapedMessage(message) {
	var str = message;
	str =str.replace(/\</g, "&lt;");
	str =str.replace(/\>/g, "&gt;");
	str =str.replace(/\&/g, "&amp;");
	str =str.replace(/\"/g, "&quot;");
	str =str.replace(/\'/g, "&#x27;");
	str =str.replace(/\//g, "&#x2f;");
	return str;
}

/* *
 *
 * HtmlEscape in JavaScript, which is compatible with utf-8
 * @author Ulrich Jensen, http://www.htmlescape.net 
 * Feel free to get inspired, use or steal this code and use it in your
 * own projects. 
 * License: 
 * You have the right to use this code in your own project or publish it
 * on your own website. 
 * If you are going to use this code, please include the author lines.
 * Use this code at your own risk. The author does not warrent or assume any
 * legal liability or responsibility for the accuracy, completeness or usefullness of
 * this program code. 
 *
 * */
var hex=new Array('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f')

function escapeChar(original)
{
	var found=true;
	var thechar=original.charCodeAt(0);
	switch(thechar) {
		case 34:return "&quot;"; break; //"
		case 38:return "&amp;"; break;
		case 60:return "&lt;"; break; //<
		case 62:return "&gt;"; break; //>
		case 198:return "&AElig;"; break;
		case 193:return "&Aacute;"; break;
		case 194:return "&Acirc;"; break; 
		case 192:return "&Agrave;"; break; 
		case 197:return "&Aring;"; break; 
		case 195:return "&Atilde;"; break; 
		case 196:return "&Auml;"; break; 
		case 199:return "&Ccedil;"; break; 
		case 208:return "&ETH;"; break;
		case 201:return "&Eacute;"; break; 
		case 202:return "&Ecirc;"; break; 
		case 200:return "&Egrave;"; break; 
		case 203:return "&Euml;"; break;
		case 205:return "&Iacute;"; break;
		case 206:return "&Icirc;"; break; 
		case 204:return "&Igrave;"; break; 
		case 207:return "&Iuml;"; break;
		case 209:return "&Ntilde;"; break; 
		case 211:return "&Oacute;"; break;
		case 212:return "&Ocirc;"; break; 
		case 210:return "&Ograve;"; break; 
		case 216:return "&Oslash;"; break; 
		case 213:return "&Otilde;"; break; 
		case 214:return "&Ouml;"; break;
		case 222:return "&THORN;"; break; 
		case 218:return "&Uacute;"; break; 
		case 219:return "&Ucirc;"; break; 
		case 217:return "&Ugrave;"; break; 
		case 220:return "&Uuml;"; break; 
		case 221:return "&Yacute;"; break;
		case 225:return "&aacute;"; break; 
		case 226:return "&acirc;"; break; 
		case 230:return "&aelig;"; break; 
		case 224:return "&agrave;"; break; 
		case 229:return "&aring;"; break; 
		case 227:return "&atilde;"; break; 
		case 228:return "&auml;"; break; 
		case 231:return "&ccedil;"; break; 
		case 233:return "&eacute;"; break;
		case 234:return "&ecirc;"; break; 
		case 232:return "&egrave;"; break; 
		case 240:return "&eth;"; break; 
		case 235:return "&euml;"; break; 
		case 237:return "&iacute;"; break; 
		case 238:return "&icirc;"; break; 
		case 236:return "&igrave;"; break; 
		case 239:return "&iuml;"; break; 
		case 241:return "&ntilde;"; break; 
		case 243:return "&oacute;"; break;
		case 244:return "&ocirc;"; break; 
		case 242:return "&ograve;"; break; 
		case 248:return "&oslash;"; break; 
		case 245:return "&otilde;"; break;
		case 246:return "&ouml;"; break; 
		case 223:return "&szlig;"; break; 
		case 254:return "&thorn;"; break; 
		case 250:return "&uacute;"; break; 
		case 251:return "&ucirc;"; break; 
		case 249:return "&ugrave;"; break; 
		case 252:return "&uuml;"; break; 
		case 253:return "&yacute;"; break; 
		case 255:return "&yuml;"; break;
		case 162:return "&cent;"; break; 
		default:
			 found=false;
			 break;
	}
	if(!found)
	{
		if(thechar>127) {
			var c=thechar;
			var a4=c%16;
			c=Math.floor(c/16); 
			var a3=c%16;
			c=Math.floor(c/16);
			var a2=c%16;
			c=Math.floor(c/16);
			var a1=c%16;
			//	alert(a1);
			return "&#x"+hex[a1]+hex[a2]+hex[a3]+hex[a4]+";";		
		}
		else
		{
			return original;
		}
	}	
}

/* *
 * RULE #2 - Attribute Escape Before Inserting Untrusted Data into HTML Common Attributes
 * 
 * Method to escape values to prevent HTML attribute based XSS
 * */
function escapeForHTMLAttributes(input) {
	var escapedStr = "";
	if(input == undefined) {
		return escapedStr;
	}
	// Loop through the input string and escape all the characters
	for(var loop = 0; loop < input.length; loop++) {
		escapedStr += escapeChar(input.charAt(loop));
	}
	return escapedStr;
}

/* *
 * RULE #5 - URL Escape Before Inserting Untrusted Data into HTML URL Parameter Values
 * 
 * Method that does a standard percent encoding of the given input string. 
 * */
function escapeForURL(input) {
	return encodeURIComponent(input);
}
