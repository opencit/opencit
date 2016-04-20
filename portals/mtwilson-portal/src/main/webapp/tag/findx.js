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
log.debug("testobj: "+Object.toJSON(testobj));    // output:   // output: { "attr1":{ name:"joe",age:"bob",comments:["hello","world","goodbye"],"#":"goodbye" } };
var testobj = { "attr1":{ name:"joe",age:"bob",comments:["hello","world"] } };
log.debug("testobj: "+testobj.getx(""));   // output:   testobj
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
//	log.debug("assign obj: "+obj+"  keypath: "+keypath+"  value: "+value);
	if( typeof keypath == 'string' && keypath == "" ) { return this; } // special case, if keypath is empty string, we return the root obj. we dont' support this when an array is passed.
	if( typeof keypath == 'string' ) { keypath = keypath.split("."); } // convert "obj.attr.nested" to [ "obj", "attr", "nested" ]
//	log.debug("split keypath length: "+keypath.length);
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

