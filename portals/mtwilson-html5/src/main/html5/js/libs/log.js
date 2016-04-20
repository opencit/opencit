/*
log.js - simple logging framework for javascript
 
Copyright (c) 2013, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list 
  of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this 
  list of conditions and the following disclaimer in the documentation and/or other 
  materials provided with the distribution.
* Neither the name of Intel Corporation nor the names of its contributors may be 
  used to endorse or promote products derived from this software without specific 
  prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.


License: BSD3
Version: 0.1
Requires: prototype.js 1.6.1 or later,  datejs
 */

var Log = (function(undefined) {
    var my = {};
    'use strict';
    my.element_id = null;

    my.isErrorEnabled = true;
    my.isWarningEnabled = true;
    my.isInfoEnabled = true;
    my.isDebugEnabled = true;
    my.isTraceEnabled = true;
    //alert("test...");
    my.attach = function(element_id) {
        my.element_id = $(element_id);
        my.element_id.observe('log:entry', function(event) {
            my.element_id.innerHTML += "[" + event.memo.level + "] " + event.memo.message + "<br/>";
            my.element_id.scrollTop = my.element_id.scrollHeight; 
        });
    };

    my._logdata = [];  // where each entry is {timestamp, level, message}

    my._log = function (level, message) {
        var newentry = {
            'timestamp': Date.today().toISOString(),
            'level': level,
            'message': message
        };
        my._logdata.push(newentry);
        if (my.element_id) {
            my.element_id.fire('log:entry', newentry);
        }
    };
    //alert("test...4");

    my.error = function(text) {
        if (my.isErrorEnabled) {
            my._log('ERROR', text);
        }
    };
    my.warning = function(text) {
        if (my.isWarningEnabled) {
            my._log('WARNING', text);
        }
    };
    my.warn = function(text) {
        if (my.isWarningEnabled) {
            my._log('WARNING', text);
        }
    };
    my.info = function(text) {
        if (my.isInfoEnabled) {
            my._log('INFO', text);
        }
    };
    my.debug = function(text) {
        if (my.isDebugEnabled) {
            my._log('DEBUG', text);
        }
    };
    my.trace = function(text) {
        if (my.isTraceEnabled) {
            my._log('TRACE', text);
        }
    };

    return my;
})();
