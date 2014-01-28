/*
merge.js - useful combinations of objects and arrays

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
Author: jbuhacoff
Version: 0.1
Requires: prototype.js 1.6.1 or later

Dependencies:
prototype.js  (for isArray)

The array merge function copies only elements of its argument that are not present in this object. It does not preserve order.

The object merge function copies all elements of its argument, inserting new properties and replacing existing properties.

The object mergeInsert function copies only properties of its argument that are not present in this object.

The object mergeUpdate function copies properties of its argument, inserting new properties and updating (deep) existing properties.

The argument must match the type of the subject (can only merge arrays into arrays, and objects into objects).

*/

/* globals: $, $$, Object, TypeError */
/*jslint white: true */

(function() { 
    var defineFunction = function(obj, key, fn) {
        Object.defineProperty(obj, key, {
           enumerable: false,
           configurable: false,
           writable: false,
           value: fn
       });       
    };

    defineFunction(Array.prototype, "contains", function (val) {
        var i = this.length;
        while(i--) {
            if (this[i] === val) {
                return true;
            }
        }
        return false;    
    });

    defineFunction(Array.prototype, "merge", function (other) {
        if( other === undefined || other === null ) { return this; } // cannot merge undefined or null but instead of error we just ignore it for programmer's convenience
        if (!(other instanceof Array)) {
            throw new TypeError("Cannot merge '"+ (typeof other)+"' into Array");
        }
        var i = other.length;
        while(i--) {
            if (other[i] !== null && !this.contains(other[i])) {
                this.push(other[i]);
            }
        }
        return this;
    });


    defineFunction(Object.prototype, "mergeInsert", function (other) {
        if( other === undefined || other === null ) { return this; } // cannot merge undefined or null but instead of error we just ignore it for programmer's convenience
        if (typeof other !== 'object' ) {
            throw new TypeError("Cannot merge '" + (typeof other) + "' into object");
        }
        var p;
        for (p in other) {
            if( this[p] === undefined || this[p] === null ) {
                this[p] = other[p];
            }
        }
        return this;
    });

    defineFunction(Object.prototype, "mergeUpdate", function (other) {
       if( other === undefined || other === null ) { return this; } // cannot merge undefined or null but instead of error we just ignore it for programmer's convenience
       if (typeof other !== 'object' ) {
            throw new TypeError("Cannot merge '" + (typeof other) + "' into object");
        }
        var p;
        for (p in other) {
            if( this[p] === undefined || this[p] === null ) {
                this[p] = other[p];
            }
            else {
                if( typeof this[p] === 'object' ) {
                    if( Object.isArray(this[p]) && Object.isArray(other[p])) {
                        this[p].merge(other[p]);
                    }
                    else if( typeof other[p] === 'object' ) {
                        this[p].mergeUpdate(other[p]);
                    }
                    else {
                        this[p] = other[p]; // the other property is a string or number or something else that cannot have a deep copy
                    }
                }
                else {
                    this[p] = other[p]; // integers, strings, etc. just get replaced 
                }
            }
        }
        return this;
    });

    defineFunction(Object.prototype, "merge", function (other) {
        if( other === undefined || other === null ) { return this; } // cannot merge undefined or null but instead of error we just ignore it for programmer's convenience
        if (typeof other !== 'object' ) {
            throw new TypeError("Cannot merge '" + (typeof other) + "' into object");
        }
        var p;
        for (p in other) {
            this[p] = other[p];
        }
        return this;
    });


    function indexOfRefInStack(itemRef, stack) {
        var i = stack.length;
        while(i--) {
            if( stack[i].ref === itemRef ) {
                return i;
            }
        }
        return -1;
    }
    
    // if stack is not empty the return value will always start with "."  because the first element in the stack
    // is the root object whose key is empty string ""
    function stackPath(stack, index) {
//        Log.debug("stack length: "+stack.length);
        if( stack.length === 0 ) { return ""; }
        var i = index+1 || stack.length;
        var path = ""; //stack[i-1].key;
        while(i--) {
            if( path.length ) {
                path = stack[i].key + "." + path;
            }
            else {
                path = stack[i].key;
            }
        }
        return path;
    }
    
    function isAssignable(object) {
        if( object === undefined ) { return true; } // or typeof object === 'undefined'
        if( object === null ) { return true; } 
        if( typeof object === 'boolean' ) { return true; } 
        if( typeof object === 'string' ) { return true; } 
        if( typeof object === 'number' ) { return true; } 
        if( typeof object === 'function' ) { return true; } 
        return false;
    }
    
    function cloneArray(array, stack) {
        var result = [], len = array.length, i;
        for(i=0; i<len; i++) {
//            Log.debug("array index "+i+": "+array[i]);
            result.push( deepClone(array[i], stack, i) );
        }
        return result;
    }
    
    function cloneObject(object, stack) {
        var result = {}, p, s;
        for (p in object) {
            if( object.hasOwnProperty(p) ) {
//                Log.debug("property: "+p+" ("+(typeof object[p])+") = "+object[p]);
                result[p] = deepClone(object[p], stack, p);
            }
        }        
        return result;
    }
    
// each item in stack is { ref: object, key: "segment" }
    function deepClone(object, stack, key) {
//        stack.push({ref:object, key:key});
//        Log.debug("deepclone( "+object+" , stack: "+stackPath(stack)+" size "+stack.length+" , key "+key+" )");
        if( isAssignable(object) ) { return object; }
        else {
            var s = indexOfRefInStack(object, stack);
            if( s > -1 ) {
//                Log.debug("Circular reference from key: "+key+" to stack index "+s+": "+stackPath(stack, s));
                if( stack.keepCircularReferences ) {
                    return stack[s].ref;
                }
                else {
                    return "#"+stackPath(stack, s);
                }
            }
            else {
                var result;
                if( object instanceof Array ) { 
                    stack.push({ref:object, key:key});
                    result = cloneArray(object, stack); 
                    stack.pop();
                }
                else {
                    stack.push({ref:object, key:key});
                    result = cloneObject(object, stack); 
                    stack.pop();
                }
                return result;
            }
        }
    }

    defineFunction(Object.prototype, "clone", function () {
        var stack = [];
        stack.keepCircularReferences = true;
        return deepClone(this, stack, "");
//        return {};
    });

    // a convenience method for serializing objects with circular references using toJSON... call this method
    // first to make a clone of the object without the circular references (they are replaced with strings)
    defineFunction(Object.prototype, "cloneJSON", function() {
        var stack = [];
        stack.keepCircularReferences = false;
        return deepClone(this, stack, "");
    });

})();


/*
 * 
 * Unit tests:
 * 

    
var a = ['a1','a2','a3'];
var b = ['b1','b2','b3'];

Log.debug("a.merge(b) -> "+Object.toJSON(a.merge(b)));

var c = ['c1','c2','c3'];
var c2 = ['c1','d2','d3']; // first element is c1 on purpose

Log.debug("c.merge(c2) -> "+Object.toJSON(c.merge(c2)));

var x = { 'int1':1, 'int2':2, 'str1':'hello', 'str2':'goodbye', 'obj1':{}, 'obj2':{'foo':'bar'} };
var y = { 'int3':3, 'int4':4 };

Log.debug("x.merge(y) -> "+Object.toJSON(x.merge(y)));

var x = { 'int1':1, 'int2':2, 'str1':'hello', 'str2':'goodbye', 'obj1':{}, 'obj2':{'foo':'bar'} };
var ys = { 'int3':3, 'int4':4, 'str1':'tick', 'str2':'tock' };

Log.debug("x.merge(ys) -> "+Object.toJSON(x.merge(ys)));

var x = { 'int1':1, 'int2':2, 'str1':'hello', 'str2':'goodbye', 'obj1':{}, 'obj2':{'foo':'bar'} };
var yo = { 'int3':3, 'str1':'tick', 'obj1':{'int5':5}, 'obj2':{'foo':'baz'} };

Log.debug("x.merge(yo) -> "+Object.toJSON(x.merge(yo)));

var z1 = { 'foo':['z1','z2','z3'] };
var z2 = { 'foo':['z1','z4','z5'] };

Log.debug("z1.merge(z2) -> "+Object.toJSON(z1.merge(z2)));

var w1 = { 'foo':{'w1':'value1','w2':'value2','w3':'value3'} };
var w2 = { 'foo':{'w1':'valuex','w4':'value4'} };

Log.debug("w1.merge(w2) -> "+Object.toJSON(w1.merge(w2)));

var x2 = { 'int1':1, 'int2':2, 'str1':'hello', 'str2':'goodbye', 'obj1':{}, 'obj2':{'foo':'bar'} };
var y2 = { 'int3':3, 'int4':4, 'str2':'tick', 'obj1':{'foo':'bar'}, 'obj2':{'foo':'baz'} };

Log.debug("x2.mergeInsert(y2) -> "+Object.toJSON(x2.mergeInsert(y2)));

var x3 = { 'int1':1, 'int2':2, 'str1':'hello', 'str2':'goodbye', 'obj1':{}, 'obj2':{'foo':'bar'} };
var y3 = { 'int3':3, 'int4':4, 'str2':'tick', 'obj1':{'foo':'bar'}, 'obj2':{'foo':'baz'} };

Log.debug("x3.mergeUpdate(y3) -> "+Object.toJSON(x3.mergeUpdate(y3)));



[DEBUG] a.merge(b) -> ["a1","a2","a3","b3","b2","b1"]
[DEBUG] c.merge(c2) -> ["c1","c2","c3","d3","d2"]
[DEBUG] x.merge(y) -> {"int1":1,"int2":2,"str1":"hello","str2":"goodbye","obj1":{},"obj2":{"foo":"bar"},"int3":3,"int4":4}
[DEBUG] x.merge(ys) -> {"int1":1,"int2":2,"str1":"tick","str2":"tock","obj1":{},"obj2":{"foo":"bar"},"int3":3,"int4":4}
[DEBUG] x.merge(yo) -> {"int1":1,"int2":2,"str1":"tick","str2":"goodbye","obj1":{"int5":5},"obj2":{"foo":"baz"},"int3":3}
[DEBUG] z1.merge(z2) -> {"foo":["z1","z4","z5"]}
[DEBUG] w1.merge(w2) -> {"foo":{"w1":"valuex","w4":"value4"}}
[DEBUG] x2.mergeInsert(y2) -> {"int1":1,"int2":2,"str1":"hello","str2":"goodbye","obj1":{},"obj2":{"foo":"bar"},"int3":3,"int4":4}
[DEBUG] x3.mergeUpdate(y3) -> {"int1":1,"int2":2,"str1":"hello","str2":"tick","obj1":{"foo":"bar"},"obj2":{"foo":"baz"},"int3":3,"int4":4} 
 * 
 */