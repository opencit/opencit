/*
findx.js - very simple expression-based object walking facility to get and set properties

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
Requires: no dependencies

Created in order to use rivets.js together with watch.js using an
adapter like this:

// watch adapter, from https://github.com/mikeric/rivets/wiki/Adapters
rivets.configure({
  adapter: {
    subscribe: function(obj, keypath, callback) {
      watch(obj, keypath, callback);
    },
    unsubscribe: function(obj, keypath, callback) {
      unwatch(obj, keypath, callback);
    },
    read: function(obj, keypath) {
	  return obj.getx(keypath);
    },
    publish: function(obj, keypath, value) {
	  obj.setx(keypath);
    }
  }
});

Rivets uses an empty-string keypath to refer to the object itself.

*/

// value is optional ; if set, the property at specified keypath will be set to the value
/*
var testobj = { "attr1":{ name:"joe",age:"bob",comments:["hello","world"] } };
testobj.setx("attr1.comments.2","goodbye");
testobj.setx("attr1.#","goodbye");  
Log.debug("testobj: "+Object.toJSON(testobj));    // output:   // output: { "attr1":{ name:"joe",age:"bob",comments:["hello","world","goodbye"],"#":"goodbye" } };
var testobj = { "attr1":{ name:"joe",age:"bob",comments:["hello","world"] } };
Log.debug("testobj: "+testobj.getx(""));   // output:   testobj
*/

(function() { 
    var defineFunction = function(obj, key, fn) {
        Object.defineProperty(obj, key, {
           enumerable: false,
           configurable: false,
           writable: false,
           value: fn
       });       
    };

var _findx = function(keypath, value) {
	var obj = this;
//	Log.debug("assign obj: "+obj+"  keypath: "+keypath+"  value: "+value);
	if( typeof keypath == 'string' && keypath == "" ) { return this; } // special case, if keypath is empty string, we return the root obj. we dont' support this when an array is passed.
	if( typeof keypath == 'string' ) { keypath = keypath.split("."); } // convert "obj.attr.nested" to [ "obj", "attr", "nested" ]
//	Log.debug("split keypath length: "+keypath.length);
   lastKeyIndex = keypath.length-1;
   for (var i = 0; i < lastKeyIndex; ++ i) {
     var key = keypath[i];
     if (!(key in obj)) {
       obj[key] = {};
	 }
     obj = obj[key];
   }
   if( typeof value != 'undefined' ) { obj[keypath[lastKeyIndex]] = value; }
   return obj[keypath[lastKeyIndex]];
};

    defineFunction(Object.prototype, "findx", _findx);
    
    defineFunction(Object.prototype, "getx", function(keypath) { return this.findx(keypath); });
    defineFunction(Object.prototype, "setx", function(keypath, value) { return this.findx(keypath, value); });
    
})();

