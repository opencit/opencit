/*
log.js - simple logging framework for javascript

Copyright (C) 2013 Jonathan Buhacoff

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

License: MIT
Version: 0.1
Requires: prototype.js 1.6.1 or later,  datejs
*/

var log = log || {};
(function(log) { // start log module definition


var _logdata = [];  // where each entry is {timestamp, level, message}

var _logelementid; // will be initialized when user calls initlog(id) ... this is where events are fired, and also is expected to be the log window itself

log.attach = function(element_id) {
	_logelementid = $(element_id);
	_logelementid.observe('log:entry', function(event) {
		_logelementid.innerHTML += "["+event.memo.level +"] "+event.memo.message+"<br/>";
		_logelementid.scrollTop = _logelementid.scrollHeight;  
	});
};

if( !window.console ) { console = {}; }
if( !console.log ) { console.log = function(){}; }
if( !console.trace ) { console.trace = console.log; }
if( !console.debug ) { console.debug = console.log; }
if( !console.info ) { console.info = function(){}; }
if( !console.warn ) { console.warn = function(){}; }
if( !console.error ) { console.error = function(){}; }
_console = {
    'TRACE': function(message) { console.trace(message); },
    'DEBUG': function(message) { console.debug(message); },
    'INFO': function(message) { console.info(message); },
    'WARNING': function(message) { console.warn(message); },
    'ERROR': function(message) { console.error(message); }
};

function _log(level, message) {
	var newentry = {
		'timestamp': Date.today().toISOString(),
		'level': level,
		'message': message
		};
	_logdata.push(newentry);
	if( _logelementid ) {
        _logelementid.fire('log:entry', newentry);
    }
    // since we are hiding the log tab, print the message to the console instead (works on firefox and chrome)
    (_console[level])(message);
}

var _logerror = true;
var _logwarning = true;
var _loginfo = true;
var _logdebug = true;
var _logtrace = false;

log.isErrorEnabled = function() { return _logerror; };
log.isWarningEnabled = function() { return _logwarning; };
log.isInfoEnabled = function() { return _loginfo; };
log.isDebugEnabled = function() { return _logdebug; };
log.isTraceEnabled = function() { return _logtrace; };
log.error = function(text) {
		if( _logerror ) { _log('ERROR', text); }
	};
log.warning = function(text) {
		if( _logwarning ) { _log('WARNING', text); }
	};
log.warn = function(text) {
		if( _logwarning ) { _log('WARNING', text); }
	};
log.info = function(text) {
		if( _loginfo ) { _log('INFO', text); }
	};
log.debug = function(text) {
		if( _logdebug ) { _log('DEBUG', text); }
	};
log.trace = function(text) {
		if( _logtrace ) { _log('TRACE', text); }
	};

})(log);  // end log module definition

