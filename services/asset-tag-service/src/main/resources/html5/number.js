/*
number.js - some useful operations on numbers

Copyright (c) 2009-2013 Jonathan Buhacoff

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/*
Reference: 
http://www.w3schools.com/jsref/jsref_obj_string.asp
http://www.w3schools.com/jsref/jsref_obj_number.asp
http://phrogz.net/JS/Classes/ExtendingJavaScriptObjectsAndClasses.html
*/

/* behavior: format a string or a number value as a US currency
*/

function addCommas(nStr)
{
	var str = '' + nStr; // convert number to string
	var x = str.split('.');
	var x1 = x[0];
	var x2 = x.length > 1 ? '.' + x[1] : ''; // decimal and after, if present
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}
function format_currency(value) {
	if( typeof(value) == "number" ) {
		return "$" + addCommas(value.toFixed(2));
	}
	if( typeof(value) == "string" ) {
		var num = parseFloat(value);
		return "$" + addCommas(num.toFixed(2));
	}
	return "Unknown";
}

/* behavior: format a string or a number as a percentage
*/
function format_percent(value) {
	if( typeof(value) == "number" ) {
		var num = value;
		return addCommas(num.toFixed(3)) + "%";
	}
	if( typeof(value) == "string" ) {
		var num = parseFloat(value);
		return addCommas(num.toFixed(3)) + "%";
	}
	return "Unknown";	
}

function format_integer(value) {
	if( typeof(value) == "number" ) {
		var num = value;
		return addCommas(num.toFixed(0));
	}
	if( typeof(value) == "string" ) {
		var num = parseFloat(value);
		return addCommas(num.toFixed(0));
	}
	return "Unknown";	
}


Number.prototype.toCurrency = function() {
	return format_currency(this.valueOf());
}

Number.prototype.toPercent = function() {
	return format_percent(this.valueOf());
}

String.prototype.toCurrency = function() {
	return format_currency(this.valueOf());
}

String.prototype.toPercent = function() {
	return format_percent(this.valueOf());
}

// remove $ or % signs and then use parseFloat
String.prototype.toFloat = function() {
	var s = this.valueOf().replace(/\$/, '').replace(/%/,'');
	return parseFloat(s);
}

// remove $ or % signs and then use parseInt
String.prototype.toInteger = function() {
	var s = this.valueOf().replace(/\$/, '').replace(/%/,'');
	return parseInt(s);
}
