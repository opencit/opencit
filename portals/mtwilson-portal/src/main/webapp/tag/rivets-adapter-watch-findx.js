/*
findx.js - very simple expression-based object walking facility to get and set properties

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
            log.debug("rivets subscribe keypath: "+keypath+" callback: "+callback+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        }
        watch(obj, keypath, callback);
    },
    unsubscribe: function(obj, keypath, callback) {
//        if( typeof keypath === 'function' ) { return; }
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            log.debug("rivets unsubscribe keypath: "+keypath+" callback: "+callback+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        } 
        if( typeof obj !== 'string' ) {
            unwatch(obj, keypath, callback);
        } // guard against "Cannot read property '' of undefined" since if the obj is a string and it's being removed, there won't be anything to unwatch
    },
    /* read is called when the data model changes and we need to update the html element */
    read: function(obj, keypath) {
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            log.debug("rivets read keypath: "+keypath+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        } 
        if( typeof obj === 'undefined') {
//            log.debug("reading keypath "+keypath+" on undefined object");
            return null;
        }
        else if( typeof obj === 'string' ) {
            //log.debug("reading keypath "+keypath+" on string: "+obj);
            return obj;
        }
        else if( typeof obj === 'object' ) {
            return obj.getx(keypath);            
        }
        else {
//            log.debug("reading keypath "+keypath+" on type: "+(typeof obj));
            return obj.getx(keypath); //return null;
        }
    //if( obj ) {	return obj.getx(keypath);   }
    //log.debug("tried to read undefined object with keypath: "+keypath);
    },
    /* publish is called when the html element changes and we need to update the data model */
    publish: function(obj, keypath, value) {
        if( this.isLoggingEnabled  && typeof log === 'object' ) {
            //log.debug("rivets publish keypath: "+keypath+" value: "+value+" on object: "+Object.toJSON(obj)+" ("+(typeof obj)+")");
        }
        if( typeof obj === 'string'  ) {

        }
        else if( typeof obj === 'object') {
            obj.setx(keypath, value);            
        }
        else {        
//            log.debug("writing keypath "+keypath+" on type: "+(typeof obj)+" for value: "+value);
            obj.setx(keypath, value);            
        }
    }
};
