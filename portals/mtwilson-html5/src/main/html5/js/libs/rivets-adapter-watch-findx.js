/*
rivers-adapter-watch-findx.js - a rivets binding adapter using findx

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
Requires:  watch.js, findx.js

This script does not depend on rivets.js but is intended for use with rivets.js
It exports a global variable rivets_watch_findx_adapter  which can be configured as
the rivets adapter.

Optional dependency on log.js (used if present)

// rivets configuration with prototype adapter
// http://rivetsjs.com/
// watch adapter, from https://github.com/mikeric/rivets/wiki/Adapters

*/

/**
 * Example usage:
 * rivets.configure(adapter:rivets_watch_findx_adapter);
 * 
 * To turn on logging (assuming there is a log object defined with a debug method):
 * rivets_watch_findx_adapter.isLoggingEnabled = true;
 */
var rivets_watch_findx_adapter =  {
    isLoggingEnabled: false,
    subscribe: function(obj, keypath, callback) {
//        if( typeof keypath === 'function' ) { return; }
        if( this.isLoggingEnabled && typeof log === 'object' ) {
            Log.debug("rivets subscribe keypath: "+keypath+" callback: "+callback+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")"); 
        }
        watch(obj, keypath, callback);
    },
    unsubscribe: function(obj, keypath, callback) {
//        if( typeof keypath === 'function' ) { return; }
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            Log.debug("rivets unsubscribe keypath: "+keypath+" callback: "+callback+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")"); 
        } 
        if( typeof obj !== 'string' ) {
            unwatch(obj, keypath, callback);
        } // guard against "Cannot read property '' of undefined" since if the obj is a string and it's being removed, there won't be anything to unwatch
    },
    /* read is called when the data model changes and we need to update the html element */
    read: function(obj, keypath) {
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            Log.debug("rivets read keypath: "+keypath+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        } 
        if( typeof obj === 'undefined') {
//            Log.debug("reading keypath "+keypath+" on undefined object");
            return null;
        }
        else if( typeof obj === 'string' ) {
            //Log.debug("reading keypath "+keypath+" on string: "+obj);
            return obj;
        }
        else if( typeof obj === 'object' ) {
            return obj.getx(keypath);            
        }
        else {
//            Log.debug("reading keypath "+keypath+" on type: "+(typeof obj));
            return obj.getx(keypath); //return null;
        }
    //if( obj ) {	return obj.getx(keypath);   }
    //Log.debug("tried to read undefined object with keypath: "+keypath);
    },
    /* publish is called when the html element changes and we need to update the data model */
    publish: function(obj, keypath, value) {
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            //Log.debug("rivets publish keypath: "+keypath+" value: "+value+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        } 
        if( typeof obj === 'string'  ) {
            //Log.debug("writing keypath "+keypath+" on string: "+obj+" with value: "+value); 
        }
        else if( typeof obj === 'object') {
            obj.setx(keypath, value);            
        }
        else {        
//            Log.debug("writing keypath "+keypath+" on type: "+(typeof obj)+" for value: "+value);
            obj.setx(keypath, value);            
        }
    }
};
