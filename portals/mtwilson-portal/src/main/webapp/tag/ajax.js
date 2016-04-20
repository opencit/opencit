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
 date.js
 
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

var _log = log;
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
    'update': function(data) {
        _log.debug("No view to update");
    },
    'sync': function() {
        _log.debug("No view to sync");
    }
};
ajax.options = {};
ajax.options.baseurl = "";  // optional, prepends a url like "/api/v2" to all resource names
//ajax.options.transport = null; // to force specific responses for development (if the server isn't ready yet)
// for example:  ajax.options.transport = { 'GET /resource1': response1, 'POST /resource1': response2, ... }

ajax.requests = []; // active requests; new requests are pushed here, completed requests are removed

// time synchronization: the timeDelta is automatically calculated after every server response
// and can be used by the application as needed in order to synchronize requests. for example,
// if one request creates a resource that has a validity period from that instant (server generated) to
// 5 minutes into the future, and a second request searches for currently available resources, 
// the newly created resource should be found. however if the second request is searching for
// currently available resources based on the client's clock and the client is 10 seconds behind the server,
// the new resource would not be found until 10 seconds later when the client's clock catches up to the server clock time
// when the resource was created and the request is sent again.
// to overcome that issue, the client can use the timeDelta to automatically adjust the "current time" sent
// in the search request by 10 seconds, compensating for the difference in clocks and resulting in 
// a successful search.
// an application must explicitly make use of the timeDelta,  it cannot be applied automatically to
// requests because it is only useful in situations involving the idea of "now" where the client
// must send a specific time to the server to represent "now" (instead of a keyword representing "now").
// default value is zero.
ajax.timeDelta = 0; // in milliseconds, computed as timeDelta = server time - client time


// draft to encapsulate api styles... keys are style names, values are functions that return resource definition given some input (currently the resource name is the only input)
ajax.apistyles = {
    'resourceCollectionWithId': function(resourceName) {
        return {uri: '/' + resourceName, datapath: resourceName, idkey: 'id'}
    }
};

ajax.util = {
    'calculateTimeDelta': function(response) {
//        log.debug("Response date: "+response.getHeader("Date"));
//        log.debug("Parsed response date: "+Date.parse(response.getHeader("Date")).toISOString());
//        log.debug("Response timestamp: "+Date.parse(response.getHeader("Date")).getTime());
//        log.debug("Current date: "+Date.today().toISOString());
//        log.debug("Current timestamp: "+Date.now());
	var date = Date.parse(response.getHeader("Date")) || Date.now();
        ajax.timeDelta = date.getTime() - Date.now(); // network delay is in here but not server processing time        
        log.debug("Time delta: "+ajax.timeDelta);
    }
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
        if (Prototype && Event) {
            document.fire("ajax:" + eventName, eventInfo);
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
    'post': function(resourceName, postObject, opt, params) {
        var info = ajax.resources[resourceName] || {};
        // if( ajax.resources[resourceName] === undefined ) { use ajax.apistyles.resourceCollectionWithId(resourceName) to create a default }
        var my = info.clone().merge(opt).merge({name: resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;
        var req_uri = my.uri;
        if (typeof params !== 'undefined') {
            var flag = 0;
            for (key in params) {
                if (flag == 0) {
                    req_uri += '?'+ key + '=' + params[key];
                    flag = 1;
                } else {
                    req_uri += '&' + key + '=' + params[key];
                }
            }
        }
        var request = new Ajax.Request(req_uri, {
            method: 'post',
            contentType: 'application/json',
            accept: 'application/json',
            headers: { "AuthorizationToken": authorizationToken, 'Accept': 'application/json' },
            requestHeaders: { 'Accept': 'application/json' },
            postBody: Object.toJSON(postObject),
            onSuccess: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Success! \n\n" + response);
                if (transport.responseText) {
                    var json = transport.responseJSON;
                    var ptr = json;
                    ajax.event.fire("httpPostSuccess", {resource: my, content: postObject, response: json});
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if ((typeof json === 'object') && json.data) {
                        _log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    /*if (keyPath !== null) {
                        var existingData = ajax.data.getx(keyPath);
                        if (existingData) {
                            existingData.merge(ptr);
                        }
                        else {
                            existingData = ptr;
                        }
                        ajax.data.setx(keyPath, existingData);
                    }*/
					
                    //ajax.event.fire("httpPostSuccess", {resource: my, content: postObject, response: json});
                }
                else {
                    ajax.event.fire("httpPostSuccess", {resource: my, content: postObject, response: null});
                }
                ajax.view.sync();
            },
            onFailure: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Failure! "+transport.status+" \n\n" + response);
                if( transport.status === 401 ) {
                    var message = transport.statusText || "Unauthorized";
                    _log.debug("ERROR: 401 "+message);
                    ajax.event.fire("httpPostFailure", {resource:my, content: postObject, response: null, message: message, unauthorized:true});
                }
                else if( transport.getHeader("Content-Type") === "text/plain" && transport.responseText ) {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: transport, message: transport.responseText});
                }
                else if (transport.responseText) {
                    var json = transport.responseJSON;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if (typeof json === 'object' && json != null) {  //check if object
                        _log.debug("ERROR: Detected json response: " + json.error_message);
                        ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: json, message: json.error_message});
                    }
                    else {
                        _log.debug("ERROR: Detected NON-json response: " + transport.statusText);
                        ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                    }
                }
                else {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                }
            }
        });
        ajax.requests.push(request);
    },
    'get': function(resourceName, params, opt) {
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name: resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;

        params = params || {};
        var d = new Date();
        params['time'] = d.getTime();
        _log.debug("get resource: " + my.uri + "  into: " + keyPath);
        console.log("get resource: " + my.uri + "  into: " + keyPath);
        var request = new Ajax.Request(my.uri, {
            method: 'get',
            cache: false,
			headers: { "AuthorizationToken": authorizationToken },
            parameters: params || {},
            onSuccess: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Success! \n\n" + response);
                if (transport.responseText) {
                    var json = transport.responseJSON;
                    var ptr = json;
                    if(my.elementsName != undefined && my.elementsName != "") {
                        ptr = json[my.elementsName];
                    }
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if ((typeof json === 'object') && json.data) {
                        _log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    log.debug("data path: " + keyPath);
                    if (keyPath !== null) {
                        var existingData = ajax.data.getx(keyPath);
                        log.debug("got existing data");
                        if (existingData) {
                            log.debug("Existing data: " + Object.toJSON(existingData));
                            existingData.merge(ptr);
                            log.debug("merge ok? " + Object.toJSON(existingData));
                        }
                        else {
                            log.debug("No existing data");
                            existingData = ptr;
                        }
                        if (existingData instanceof Array) {
                            existingData.removeAll(null);
                        } // automated data binding tends to break for null objects
                        log.debug("calling setx with keypath: " + keyPath + " and data: " + Object.toJSON(existingData));
                        ajax.data.setx(keyPath, existingData);
                    }
                    ajax.event.fire("httpGetSuccess", {resource: my, params: params, response: ptr, serverTime: transport.getResponseHeader("Date")});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(json);
                    }
                }
                else {
                    ajax.event.fire("httpGetSuccess", {resource: my, params: params, response: null});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(null);
                    }
                }
                log.debug("calling view sync");
                ajax.view.sync();
            },
            onFailure: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Failure! "+transport.status+"\n\n" + response);
                if( transport.status === 401 ) {
                    var message = transport.statusText || "Unauthorized";
                    _log.debug("ERROR: 401 "+message);
                    ajax.event.fire("httpGetFailure", {resource:my, params: params, response: null, message: message, unauthorized:true});
                }
                else if( transport.getHeader("Content-Type") === "text/plain" && transport.responseText ) {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpGetFailure", {resource: my, content: postObject, response: transport, message: transport.responseText});
                }
                else if (transport.responseText) {
                    var json = transport.responseJSON;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if (typeof json === 'object' && json != null) {  //check if object
                        _log.debug("ERROR: Detected json response: " + json.error_message);
                        ajax.event.fire("httpGetFailure", {resource: my, params: params, response: json, message: json.error_message});
                    }
                    else {
                        _log.debug("ERROR: Detected NON-json response: " + transport.statusText);
                        ajax.event.fire("httpGetFailure", {resource: my, params: params, response: transport, message: transport.statusText});
                    }
                }
                else {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpGetFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                }
            }
            /*,
             onFailure: function(a,b) {
             _log.error("GET "+resourcePath+" into "+keyPath+": "+Object.toJSON(a)+": "+b);
             },
             onException: function(req,err) {
             var p;
             _log.error("Exception: GET "+resourcePath+" into "+keyPath+": "+req+": "+err);
             //for( p in req) { _log.debug("req prop: "+p+" = "+req[p]); }
             //for( p in req.parameters) { _log.debug("req param: "+p+" = "+req.parameters[p]); }
             } */
        });
        ajax.requests.push(request);
    },
    'put': function(resourceName, putObject, opt) {
        log.debug("AJAX PUT resourceName: " + resourceName);
        log.debug("AJAX PUT object: " + Object.toJSON(putObject));
        log.debug("AJAX PUT opt1: " + (typeof opt));
        log.debug("AJAX PUT opt2: " + opt);
        log.debug("AJAX PUT opt3: " + Object.toJSON(opt));
//        log.debug("AJAX PUT "+resourceName+": "+Object.toJSON(putObject)+" WITH OPTIONS: "+Object.toJSON(opt));
        log.debug("AJAX PUT config: " + Object.toJSON(ajax.resources[resourceName]));
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name: resourceName}); // make a copy of the resource config and override it with passed-in options
        var keyPath = my.datapath;
        log.debug("AJAX PUT: " + Object.toJSON(my));
        log.debug("AJAX PUT OBJECT: " + Object.toJSON(putObject));
        log.debug("AJAX PUT URL: " + my.uri + '/' + putObject[my.idkey]);
        var request = new Ajax.Request(my.uri + '/' + putObject[my.idkey], {
            method: 'put',
			headers: { "AuthorizationToken": authorizationToken },
            contentType: 'application/json',
            postBody: Object.toJSON(putObject),
            onSuccess: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Success! \n\n" + response);
                if (transport.responseText) {
                    var json = transport.responseJSON;
                    var ptr = json;
                    //var ptrUuid = Object.toJSON(ptr).evalJSON().uuid;
                    //log.debug("ptrUuid: " + ptrUuid);

                    // some apis return metadata in an outer object and the content inside a 'data' field
                    ajax.event.fire("httpPutSuccess", {resource: my, content: putObject, response: json});
                    if ((typeof json === 'object') && json.data) {
                        _log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    if (keyPath !== null) {
                        var existingData = ajax.data.getx(keyPath);
                        if (existingData) {
                            Object.toJSON(existingData).evalJSON().forEach(function(obj) {
                                //log.debug("existingData uuid: " + obj.uuid);
                                if (Object.toJSON(ptr).evalJSON().uuid === obj.uuid) {
                                    obj.merge(ptr);
                                }
                                else {
                                    existingData.push(ptr);
                                }
                            });
                            //existingData.merge(ptr);
                        }
                        else {
                            existingData = ptr;
                        }
                        log.debug("calling setx with keypath: " + keyPath + " and data: " + Object.toJSON(existingData));
                        ajax.data.setx(keyPath, existingData);
                    }
                    //ajax.event.fire("httpPutSuccess", {resource: my, content: putObject, response: json});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(json);
                    }
                }
                else {
                    ajax.event.fire("httpPutSuccess", {resource: my, content: putObject, response: null});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(null);
                    }
                }
                ajax.view.sync();
            },
            onFailure: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Failure! "+transport.status+"\n\n" + response);
                if( transport.status === 401 ) {
                    var message = transport.statusText || "Unauthorized";
                    _log.debug("ERROR: 401 "+message);
                    ajax.event.fire("httpPutFailure", {resource:my, content: putObject, response: null, message: message, unauthorized:true});
                }
                else if( transport.getHeader("Content-Type") === "text/plain" && transport.responseText ) {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpPutFailure", {resource: my, content: postObject, response: transport, message: transport.responseText});
                }
                else if (transport.responseText) {
                    var json = transport.responseJSON;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if (typeof json === 'object' && json != null) {  //check if object
                        _log.debug("ERROR: Detected json response: " + json.error_message);
                        ajax.event.fire("httpPutFailure", {resource: my, content: putObject, response: json, message: json.error_message});
                    }
                    else {
                        _log.debug("ERROR: Detected NON-json response: " + transport.statusText);
                        ajax.event.fire("httpPutFailure", {resource: my, content: putObject, response: transport, message: transport.statusText});
                    }
                }
                else {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpPutFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                }
            }
        });
        ajax.requests.push(request);
    },
    'delete': function(resourceName, deleteObject, opt) {
        var info = ajax.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name: resourceName}); // make a copy of the resource config and override it with passed-in options
//        var keyPath = my.datapath;
        var request = new Ajax.Request(my.uri + '/' + deleteObject[my.idkey], {
            method: 'delete',
			headers: { "AuthorizationToken": authorizationToken },
            onSuccess: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Success! \n\n" + response);
                if (transport.responseText) {
                    var json = transport.responseJSON;
                    var ptr = json;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if ((typeof json === 'object') && json.data) {
                        _log.debug("Detected data object in response");
                        ptr = json.data;
                    }
                    log.debug("Server response for delete: " + Object.toJSON(ptr));
                    ajax.event.fire("httpDeleteSuccess", {resource: my, content: deleteObject, response: json});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(json);
                    }
                }
                else {
                    ajax.event.fire("httpDeleteSuccess", {resource: my, content: deleteObject, response: null});
                    if( typeof my.onSuccess === 'function' ) {
                        my.onSuccess(null);
                    }
                }
//                ajax.view.sync();
            },
            onFailure: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Failure! "+transport.status+"\n\n" + response);
                if( transport.status === 401 ) {
                    var message = transport.statusText || "Unauthorized";
                    _log.debug("ERROR: 401 "+message);
                    ajax.event.fire("httpDeleteFailure", {resource:my, content: deleteObject, response: null, message: message, unauthorized:true});
                }
                else if( transport.getHeader("Content-Type") === "text/plain" && transport.responseText ) {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpDeleteFailure", {resource: my, content: postObject, response: transport, message: transport.responseText});
                }
                else if (transport.responseText) {
                    var json = transport.responseJSON;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if (typeof json === 'object' && json != null) {  //check if object
                        _log.debug("ERROR: Detected json response: " + json.error_message);
                        ajax.event.fire("httpDeleteFailure", {resource: my, content: deleteObject, response: json, message: json.error_message});
                    }
                    else {
                        _log.debug("ERROR: Detected NON-json response: " + transport.statusText);
                        ajax.event.fire("httpDeleteFailure", {resource: my, content: deleteObject, response: transport, message: transport.statusText});
                    }
                }
                else {
                    // no content in response, so show the status "reason phrase"
                    ajax.event.fire("httpDeleteFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                }
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
};
ajax.xml = {
    'enctype': 'text/plain'
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
};

ajax.custom = {
    //'enctype': 'text/plain',
    // the postObject here must be a string with the raw data to send.
    // the opt object must include a contentType attribute to describe the postObject ,for example  'message/rfc822'
    'post': function(resourceName, postObject, opt, params) {
        var info = ajax.resources[resourceName] || {};
        // if( ajax.resources[resourceName] === undefined ) { use ajax.apistyles.resourceCollectionWithId(resourceName) to create a default }
        var my = info.clone().merge(opt).merge({name: resourceName}); // make a copy of the resource config and override it with passed-in options
        var req_uri = my.uri;
        if (typeof params !== 'undefined') {
            var flag = 0;
            for (key in params) {
                if (flag == 0) {
                    req_uri += '?'+ key + '=' + params[key];
                    flag = 1;
                } else {
                    req_uri += '&' + key + '=' + params[key];
                }
            }
        }
        var request = new Ajax.Request(req_uri, {
            method: 'post',
            contentType: my.contentType,
            accept: my.accept,
            headers: { "AuthorizationToken": authorizationToken, 'Accept': my.accept },
            requestHeaders: { 'Accept': my.accept },
            //postBody: Object.toJSON(postObject),
            postBody: postObject,
            onSuccess: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Success! \n\n" + response);
                if (transport.responseText) {
                    var custom = transport.responseText;
                    ajax.event.fire("httpPostSuccess", {resource: my, content: postObject, response: custom});
                }
                else {
                    ajax.event.fire("httpPostSuccess", {resource: my, content: postObject, response: null});
                }
                ajax.view.sync();
            },
            onFailure: function(transport) {
                ajax.util.calculateTimeDelta(transport);
                var response = transport.responseText || "no response text";
                _log.debug("Failure! "+transport.status+"\n\n" + response);
                if (transport.responseText) {
                    var json = transport.responseJSON;
                    // some apis return metadata in an outer object and the content inside a 'data' field
                    if (typeof json === 'object' && json != null) {  //check if object
                        _log.debug("ERROR: Detected json response: " + json.error_message);
                        ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: json, message: json.error_message});
                    }
                    else {
                        _log.debug("ERROR: Detected NON-json response: " + transport.statusText);
                        ajax.event.fire("httpPostFailure", {resource: my, content: postObject, response: transport, message: transport.statusText});
                    }
                }
            }
        });
        ajax.requests.push(request);
    }

};


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
 _log.debug("apiget loading fake data...");
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
 _log.debug("Success! \n\n" + response);
 //var json = transport.responseJSON;
 }    ,
 onException: function(a,b) {
 _log.error("exception: a:"+a+"  b: "+b);
 }
 })  ;  
 }
 catch(e) {
 _log.error("cannot send request: "+e);
 }
 
 try {
 // in online testing we get the resource from the server:
 new Ajax.Request(apiurl(resource), {
 method: 'get',
 onSuccess: function (transport) {
 var response = transport.responseText || "no response text";
 _log.debug("Success! \n\n" + response);
 var json = transport.responseJSON;
 var ptr = json;
 // some apis return metadata in an outer object and the content inside a 'data' field
 if ((typeof json === 'object') && ('data' in json)) {
 _log.debug("Detected data object in response");
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
 _log.error('Something went wrong...');
 apidone();
 }
 });
 _log.debug("test?");
 }
 catch(e) {
 _log.debug("Failed to load: "+e);
 }
 }
 */

// test
/*
 function apicreate(resource, keypath, objectArray, opt) {
 if (!Object.isArray(objectArray)) {
 throw new TypeError("Object array must be an array of resources to post");
 }
 _log.debug("saving...");
 opt = opt || {};
 opt.merge = true;
 apiwait("Saving...");
 // in offline testing there will be a fakeresource object (from sampledata.js) and we post the data to there:
 if ((typeof fakeresource === 'object') && (fakeresource[resource] !== undefined)) {
 _log.debug("found fake resource");
 if (opt.merge) {
 _log.debug("merging");
 fakeresource[resource] = merge(objectArray, fakeresource[resource]);
 var existingData = data.getx(keypath);
 data.setx(keypath, merge(fakeresource[resource], existingData));
 }
 else {
 _log.debug("replacing");
 fakeresource[resource] = objectArray;
 data.setx(keypath, fakeresource[resource]);
 }
 //        view.sync();
 view.update(data);
 apidone();
 return;
 }
 _log.debug("online create... to " + apiurl(resource) + " with post: " + Object.toJSON(objectArray));
 // in online testing we get the resource from the server:
 opt.merge = true; // force merging of the created resource into our data set
 
 try {
 var ajax = new XMLHttpRequest();
 ajax.onreadystatechange = function () {
 _log.debug("ready state: " + ajax.readyState + "  http status: " + ajax.status + "  response: " + ajax.responseText);
 };
 ajax.open("POST",apiurl(resource),  true);
 //ajax.setRequestHeader("Content-Type", "application/json");
 ajax.send(Object.toJSON(objectArray));
 }
 catch (e) {
 _log.debug("error!! " + e);
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
 _log.debug("Success! \n\n" + response);
 var json = transport.responseJSON;
 var ptr = json;
 // some apis return metadata in an outer object and the content inside a 'data' field
 if ((typeof json === 'object') && (typeof json.data === 'object')) {
 _log.debug("Detected data object in response");
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
 _log.error('Something went wrong...');
 apidone();
 },
 onCreate: function () {
 _log.debug("AJAX status: created request for " + apiurl(resource));
 },
 onLoading: function () {
 _log.debug("AJAX status: loading " + apiurl(resource));
 },
 onException: function (transport, e) {
 _log.debug("AJAX status: exception while processing " + apiurl(resource) + ": " + e);
 },
 onComplete: function () {
 _log.debug("AJAX status: completed " + apiurl(resource));
 }
 });
 }
 
 */
