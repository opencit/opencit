/*
ajax.js - wrapper for ajax-based APIs using JSON serialization and REST style resources

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
Requires: prototype.js 1.6.1 or later

 Dependencies:
 prototype.js
 log.js
merge.js
findx.js


The library currently assumes the following resource style:

GET /resources  (plural name) to search the resource collection
POST /resources to create new resources in the collection (must post an array even if only sending one resource)
PUT /resources/{id}  to update an existing resource  (must send the entire resource)
DELETE /resources/{id}  to delete an existing resource


Events are fired (using Prototype's Event class) after a successful request.

ajax:httpPostSuccess - fired after successful HTTP POST. 
ajax:httpGetSuccess - fired after successful HTTP GET.
ajax:httpPutSuccess - fired after successful HTTP PUT.
ajax:httpDeleteSuccess - fired after successful HTTP DELETE.

For all events the memo looks like this: 
{ 
    resource: {name:'pets',uri:'/pets',datapath:'global.data.pets',idkey:'uuid', ... }, 
    content:[ {name:'Sparky', age:2, color:'brown'} ],
    params: { ... },    // any URL parameters
}

The HTTP GET event notification does not include the 'content' field, but typically might the only one
to include the 'params' field for URL parameters (as an object).

The '...' in the 'resource' field value in the example memo above represents any other options
that you passed into the AJAX call. Options 'uri', 'datapath', and 'idkey' override the resource
defaults and affect the http request. Any other options are kept for your use in the event handler.

 *   */

/* globals: $, $$, Ajax, Object, TypeError, XMLHttpRequest, document, fakeresource, log, ajax */
/*jslint white: true */

/*
if( typeof _log === 'undefined' ) {
    var _log = {
        debug: function (message) { alert("DEBUG: "+message); },
        error: function (message) { alert("ERROR: "+message); }
    };
}
*/

var ajax = {};
ajax.resources = {}; // keys are the resource plural names and values are { uri:'/resources', datapath:'resources', idkey:'id' }  and in the future could be uri templates etc.
ajax.data = {}; // you can set ajax.data = your_own_object to have it automatically populated with responses, or you can let this one be populated and navigate it yourself later, or simply point UI frameworks at this one
ajax.status = ""; // you can read the current status of the ajax requests here
ajax.view = {
    'update': function (data) {
        Log.debug("No view to update");
    },
    'sync': function () {
        Log.debug("No view to sync");
    }    
};
ajax.options = {};
ajax.options.baseurl = "";  // optional, prepends a url like "/api/v2" to all resource names

ajax.requests = []; // active requests; new requests are pushed here, completed requests are removed


// draft to encapsulate api styles... keys are style names, values are functions that return resource definition given some input (currently the resource name is the only input)
ajax.apistyles = {
    'resourceCollectionWithId': function(resourceName) { return { uri:'/'+resourceName, datapath:resourceName, idkey:'id' } }
};

// define   create(post), search(get), delete(delete),  update(put)

// the usefulness of this module will come from automatically updating the data model with the server's response
// ... we're assuming a certain style of JSON api...  maybe support multiple styles in the future... e.g. jsonapi id and url styles.
// as well a servers returning a link to created object (so go there, fetch it, and then update the model) vs servesr
// returning the complete object after its created


/*
?foo=bar&baz=The%20first%20line.%0AThe%20second%20line. 
 */

ajax.event = {
    fire: function(eventName, eventInfo) {
        if( Prototype && Event ) {
            document.fire("ajax:"+eventName, eventInfo);
        }
    }
};

/*
Content-Type: text/plain

foo=bar
baz=The first line.
The second line. 
 */
ajax.text = {
    'enctype': 'text/plain'
};

ajax.json = {
    'enctype': 'text/plain',
    'post': function (resourceName, postObject, opt) {
        var info = ajax.resources[resourceName] || {};
        // if( ajax.resources[resourceName] === undefined ) { use ajax.apistyles.resourceCollectionWithId(resourceName) to create a default }
        var my = info.clone().merge(opt).merge({name:resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;
        var request = new Ajax.Request(my.uri, {
            method: 'post',
            contentType: 'application/json',
            postBody: Object.toJSON(postObject),
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if (typeof json === 'object') { 
                    if( json.data  ) {
                        Log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    else if( json[keyPath] ) {
                        Log.debug("Detected "+keyPath+" collection in response");
                        ptr = json[keyPath];
                    }
                }
                var existingData = ajax.data.getx(keyPath);
                if( existingData ) {
                    existingData.merge(ptr);
                }
                else {
                    existingData = ptr;
                } 
                ajax.data.setx(keyPath, existingData);
                ajax.event.fire("httpPostSuccess", { resource:my, content:postObject });
                ajax.view.sync();
            }    
        });
        ajax.requests.push(request);
    },
    'get': function (resourceName, params, opt) {
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name:resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;
        Log.debug("get resource: "+my.uri+"  into: "+keyPath);
        var request = new Ajax.Request(my.uri, {
            method: 'get',
            parameters: params || {},
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if (typeof json === 'object') { 
                    if( json.data  ) {
                        Log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    else if( json[keyPath] ) {
                        Log.debug("Detected "+keyPath+" collection in response");
                        ptr = json[keyPath];
                    }
                }
                
                Log.debug("data path: "+keyPath);
                var existingData = ajax.data.getx(keyPath);
                Log.debug("got existing data");
                if( existingData ) {
                    Log.debug("Existing data: "+Object.toJSON(existingData));
                    existingData.merge(ptr);
                    Log.debug("merge ok? " +Object.toJSON(existingData));
                }
                else {
                    Log.debug("No existing data");
                    existingData = ptr;
                }
                if( existingData instanceof Array ) { existingData.removeAll(null); } // automated data binding tends to break for null objects
                Log.debug("calling setx with keypath: "+keyPath+" and data: "+Object.toJSON(existingData));
                ajax.data.setx(keyPath, existingData);
                ajax.event.fire("httpGetSuccess", { resource:my, params:params });
                Log.debug("calling view sync");
                ajax.view.sync();
            } /*,
            onFailure: function(a,b) {
                Log.error("GET "+resourcePath+" into "+keyPath+": "+Object.toJSON(a)+": "+b);
            },
            onException: function(req,err) {
                var p;
                Log.error("Exception: GET "+resourcePath+" into "+keyPath+": "+req+": "+err);
                //for( p in req) { Log.debug("req prop: "+p+" = "+req[p]); }
                //for( p in req.parameters) { Log.debug("req param: "+p+" = "+req.parameters[p]); }
            } */
        });
        ajax.requests.push(request);
    },
    'put': function (resourceName, putObject, opt) {
        Log.debug("AJAX PUT resourceName: "+resourceName);
        Log.debug("AJAX PUT object: "+Object.toJSON(putObject));
        Log.debug("AJAX PUT opt1: "+(typeof opt));
        Log.debug("AJAX PUT opt2: "+opt);
        Log.debug("AJAX PUT opt3: "+Object.toJSON(opt));
//        Log.debug("AJAX PUT "+resourceName+": "+Object.toJSON(putObject)+" WITH OPTIONS: "+Object.toJSON(opt));
        Log.debug("AJAX PUT config: "+Object.toJSON(ajax.resources[resourceName]));
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name:resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;
        Log.debug("AJAX PUT: "+Object.toJSON(my));
        Log.debug("AJAX PUT OBJECT: "+Object.toJSON(putObject));
        Log.debug("AJAX PUT URL: "+my.uri+'/'+putObject[my.idkey]);
        var request = new Ajax.Request(my.uri+'/'+putObject[my.idkey], {
            method: 'put',
            contentType: 'application/json',
            postBody: Object.toJSON(putObject),
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if (typeof json === 'object') { 
                    if( json.data  ) {
                        Log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    else if( json[keyPath] ) {
                        Log.debug("Detected "+keyPath+" collection in response");
                        ptr = json[keyPath];
                    }
                }
                var existingData = ajax.data.getx(keyPath);
                if( existingData ) {
                    existingData.merge(ptr);
                }
                else {
                    existingData = ptr;
                } 
                Log.debug("calling setx with keypath: "+keyPath+" and data: "+Object.toJSON(existingData));
                ajax.data.setx(keyPath, existingData);
                ajax.event.fire("httpPutSuccess", { resource:my, content:putObject });
                ajax.view.sync();
            }    
        });
        ajax.requests.push(request);
    },
    'delete': function (resourceName, deleteObject, opt) {
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name:resourceName}); // make a copy of the resource config and override it with passed-in options
//        var keyPath = my.datapath;
        var request = new Ajax.Request(my.uri+'/'+deleteObject[my.idkey], {
            method: 'delete',
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                if( transport.responseText ) {
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if (typeof json === 'object') { 
                    if( json.data  ) {
                        Log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    else if( json[keyPath] ) {
                        Log.debug("Detected "+keyPath+" collection in response");
                        ptr = json[keyPath];
                    }
                }
                Log.debug("Server response for delete: "+Object.toJSON(ptr));
                }
                ajax.event.fire("httpDeleteSuccess", { resource:my, content:deleteObject });
//                ajax.view.sync();
            }    
        });
        ajax.requests.push(request);
    }
};

/*
Content-Type: application/x-www-form-urlencoded

foo=bar&baz=The+first+line.&#37;0D%0AThe+second+line.%0D%0A
 * 
 */
ajax.form = {
    'enctype': 'application/x-www-form-urlencoded'  
    /* UNSUPPORTED OPERATION  */
};
ajax.xml = {
    'enctype': 'text/plain'
    /* UNSUPPORTED OPERATION  */    
};
/*
Content-Type: multipart/form-data; boundary=---------------------------314911788813839

-----------------------------314911788813839
Content-Disposition: form-data; name="foo"

bar
-----------------------------314911788813839
Content-Disposition: form-data; name="baz"

The first line.
The second line.

-----------------------------314911788813839-- 
 */
ajax.file = {
    'enctype': 'multipart/form-data'
    /* UNSUPPORTED OPERATION  */
}


    /*
     * Options:
     * merge: true|false     specify { merge: true } to merge the results with existing data (applies only to arrays and objects) instead of replacing them
     *
     * @resource path of the resource to get, for example /zebras?color=striped
     * @keypath an expression indicating where the data should go; for example "sidebar.zebras" translates to data.sidebar.zebras
     * @options described above
     */
    /*
    function apiget(resource, keypath, opt) {
        opt = opt || {};
        apiwait("Loading...");
        // in offline testing there will be a fakeresource object (from sampledata.js) and we get the data from there:
        if ((typeof fakeresource === 'object') && (fakeresource[resource] !== undefined)) {
            Log.debug("apiget loading fake data...");
            if (opt.merge) {
                var existingData = data.getx(keypath);
                data.setx(keypath, merge(fakeresource[resource], existingData));
            }
            else {
                data.setx(keypath, fakeresource[resource]);
            }
//        view.sync();
            view.update(data);
            apidone();
            return;
        }
        try {
new Ajax.Request("/tags", {
    method: 'get',
    onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                //var json = transport.responseJSON;
    }    ,
    onException: function(a,b) {
Log.error("exception: a:"+a+"  b: "+b);
    }
})  ;  
        }
        catch(e) {
        Log.error("cannot send request: "+e);
        }

     try {
        // in online testing we get the resource from the server:
        new Ajax.Request(apiurl(resource), {
            method: 'get',
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if ((typeof json === 'object') && ('data' in json)) {
                    Log.debug("Detected data object in response");
                    ptr = json.data;
                }
                if (opt.merge) {
                    var existingData = data.getx(keypath);
                    data.setx(keypath, merge(ptr, existingData));
                }
                else {
                    // without the merge option we simply replace the data at the keypath with what we got from the server
                    data.setx(keypath, ptr);
                }
                apidone();
            },
            onFailure: function () {
                Log.error('Something went wrong...');
                apidone();
            }
        });
        Log.debug("test?");
        }
        catch(e) {
            Log.debug("Failed to load: "+e);
        }
    }
*/

// test
/*
    function apicreate(resource, keypath, objectArray, opt) {
        if (!Object.isArray(objectArray)) {
            throw new TypeError("Object array must be an array of resources to post");
        }
        Log.debug("saving...");
        opt = opt || {};
        opt.merge = true;
        apiwait("Saving...");
        // in offline testing there will be a fakeresource object (from sampledata.js) and we post the data to there:
        if ((typeof fakeresource === 'object') && (fakeresource[resource] !== undefined)) {
            Log.debug("found fake resource");
            if (opt.merge) {
                Log.debug("merging");
                fakeresource[resource] = merge(objectArray, fakeresource[resource]);
                var existingData = data.getx(keypath);
                data.setx(keypath, merge(fakeresource[resource], existingData));
            }
            else {
                Log.debug("replacing");
                fakeresource[resource] = objectArray;
                data.setx(keypath, fakeresource[resource]);
            }
//        view.sync();
            view.update(data);
            apidone();
            return;
        }
        Log.debug("online create... to " + apiurl(resource) + " with post: " + Object.toJSON(objectArray));
        // in online testing we get the resource from the server:
        opt.merge = true; // force merging of the created resource into our data set

        try {
            var ajax = new XMLHttpRequest();
            ajax.onreadystatechange = function () {
                Log.debug("ready state: " + ajax.readyState + "  http status: " + ajax.status + "  response: " + ajax.responseText);
            };
            ajax.open("POST",apiurl(resource),  true);
            //ajax.setRequestHeader("Content-Type", "application/json");
            ajax.send(Object.toJSON(objectArray));
        }
        catch (e) {
            Log.debug("error!! " + e);
        }

        new Ajax.Request(apiurl(resource), {
            method: 'post',
            //parameters: objectArray,
            contentType: 'application/json',
            postBody:  Object.toJSON(objectArray), // if this is enabled then 'parameters' is ignored
            // Note: the onSuccess function is the same here as it is for GET because we expect
            // the server to return a possibly revised form of the same resources we posted -
            // and we need to merge those into our data model. That's why opt[merge] is set to true above.
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                var json = transport.responseJSON;
                var ptr = json;
                // some apis return metadata in an outer object and the content inside a 'data' field
                if ((typeof json === 'object') && (typeof json.data === 'object')) {
                    Log.debug("Detected data object in response");
                    ptr = json.data;
                }
                if (opt.merge) {
                    var existingData = data.getx(keypath);
                    data.setx(keypath, merge(ptr, existingData));
                }
                else {
                    // without the merge option we simply replace the data at the keypath with what we got from the server
                    data.setx(keypath, ptr);
                }
                apidone();
            },
            onFailure: function () {
                Log.error('Something went wrong...');
                apidone();
            },
            onCreate: function () {
                Log.debug("AJAX status: created request for " + apiurl(resource));
            },
            onLoading: function () {
                Log.debug("AJAX status: loading " + apiurl(resource));
            },
            onException: function (transport, e) {
                Log.debug("AJAX status: exception while processing " + apiurl(resource) + ": " + e);
            },
            onComplete: function () {
                Log.debug("AJAX status: completed " + apiurl(resource));
            }
        });
    }

*/
