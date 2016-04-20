/*
array.js - some extensions to the javascript array object

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

License: BSD3 (for the collection; sub-components licensed under CC-BY-SA)
Version: 0.2
Requires: html5, no dependencies

**/

(function() { 
    var defineFunction = function(obj, key, fn) {
        Object.defineProperty(obj, key, {
           enumerable: false,
           configurable: false,
           writable: false,
           value: fn
       });       
    };
    
    // Source of this function is unknown, 
    var _indexOf = function (searchElement /*, fromIndex */ ) {
    'use strict';
    if (this == null) {
      throw new TypeError();
    }
    var n, k, t = Object(this),
        len = t.length >>> 0;

    if (len === 0) {
      return -1;
    }
    n = 0;
    if (arguments.length > 1) {
      n = Number(arguments[1]);
      if (n != n) { // shortcut for verifying if it's NaN
        n = 0;
      } else if (n != 0 && n != Infinity && n != -Infinity) {
        n = (n > 0 || -1) * Math.floor(Math.abs(n));
      }
    }
    if (n >= len) {
      return -1;
    }
    for (k = n >= 0 ? n : Math.max(len - Math.abs(n), 0); k < len; k++) {
      if (k in t && t[k] === searchElement) {
        return k;
      }
    }
    return -1;
  };

/**
 * Older browsers may not have Array.indexOf
 * 
 * Credits:
 * This portion adapted from
 * of https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf?redirectlocale=en-US&redirectslug=JavaScript%2FReference%2FGlobal_Objects%2FArray%2FindexOf
 * by Mozilla Contributors
 * licensed http://creativecommons.org/licenses/by-sa/3.0/
 */
if (!Array.prototype.indexOf) {
    defineFunction(Array.prototype, "indexOf", _indexOf);
}

/**
 * Older browsers may not have Array.indexOf
 * 
 * Credits:
 * This portion adapted from
 * of http://stackoverflow.com/questions/3954438/remove-item-from-array-by-value 
 * by http://stackoverflow.com/users/80860/kennebec
 * licensed http://creativecommons.org/licenses/by-sa/3.0/
 */
/*
if(!Array.prototype.indexOf) {
    Array.prototype.indexOf = function(valueToFind, startIndex) {
        startIndex = startIndex || 0;
        var i = startIndex;
        var cachedLength = this.length;
        while (i < cachedLength) {
            if(this[i] === valueToFind) { return i; }
            ++i;
        }
        return -1;
    };
}
*/

/**
 * Arguments:  one or more values to remove from the array
 * Post-condition: all elements in the array that have the same value as any one of the arguments are removed
 * Example:   ['e','b','b','c','z'].removeAll('b','c')  == ['e','z']
 * 
 * Credits:
 * This portion adapted from
 * of http://stackoverflow.com/questions/3954438/remove-item-from-array-by-value 
 * by http://stackoverflow.com/users/80860/kennebec
 * licensed http://creativecommons.org/licenses/by-sa/3.0/
 */
var _removeAll = function() {
    var valueToRemove, argIndex = arguments.length, foundAtIndex; 
    while( argIndex > 0 && this.length > -1 ) {
        valueToRemove = arguments[--argIndex];
        while( (foundAtIndex = this.indexOf(valueToRemove)) !== -1 ) {
            this.splice(foundAtIndex,1);
        }
    }
    return this;
};

    

if (!Array.prototype.removeAll) {
    defineFunction(Array.prototype, "removeAll", _removeAll);
}

if( !Array.prototype.pushArray ) {
    defineFunction(Array.prototype, "pushArray", function(array) { 
        this.push.apply(this, array);
        return this;
    });
} 

if (!Array.prototype.clear) {
    defineFunction(Array.prototype, "clear", function() {
        while (this.length > 0) {
          this.pop();
        }
        return this;
    });
}

if (!Object.prototype.keys) {
    defineFunction(Object.prototype, "keys", function() {
        var keynames = [], p;
        for(p in this) {
          if( this.hasOwnProperty(p) ) {
              keynames.push(p);
          }
        }
        return keynames;
    });
}

if (!Object.prototype.clear) {
    defineFunction(Object.prototype, "clear", function() {
        var keynames = this.keys(), i = keynames.length;
        while (i--) {
          delete this[keynames[i]];
        }
        return this;
    });
}


})();
