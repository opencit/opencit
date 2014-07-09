/*
jsonapi.js - generic implementation of specification at jsonapi.org with additions

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
Requires: prototype.js 1.6.1 or later,  merge.js

 Dependencies:
 prototype.js
 log.js
 form.js
 */

/* globals: $, $$, Ajax, Object, TypeError, URI, XMLHttpRequest, document, fakeresource, log, jsonapi, rivets */
/*jslint white: true */

/*
 * 
 * chrome.exe --allow-file-access-from-files
 */

var jsonapi = {};

(function (jsonapi, undefined) { // start module definition

    // base url
    var uri = new URI(document.location.toString());

    // DATA
    jsonapi.options = {
        'baseurl': (uri.is("url") ? (uri.protocol() + "://"+ uri.authority() + uri.directory()) : uri.toString()) // '/' // uri // (uri.scheme() == 'http' || uri.scheme() == 'https') ? '' : '#' // or http://localhost:8080
    };
    
    jsonapi.data = {}; // by plural collection name,  so every key is a collection name and its value is an array of records 
    jsonapi.resources = {}; // resource definitions loaded from resources.json
    jsonapi.forms = {}; // forms corresponding to resources; each key is a resource/model id (probably the plural name) same as in resources object
    jsonapi.requests = []; // currently active ajax requests
	
	// draft to encapsulate api styles... keys are style names, values are functions that return resource definition given some input (currently the resource name is the only input)
	var apistyles = {
		'resourceCollectionWithId': function(resourceName) { return { uri:'/'+resourceName, datapath:resourceName, idkey:'id' } }
	};
    
	jsonapi.event = {
		fire: function(eventName, eventInfo) {
			if( Prototype && Event ) {
				document.fire("jsonapi:"+eventName, eventInfo);
			}
		}
	};
	
    jsonapi.initialize = function() {
        Log.debug("Initialized JSON API with options: "+Object.toJSON(jsonapi.options));
//        jsonapi.view = view; // after every ajax call the view will be updated ???
        
		/*
        // find all forms, and automatically create a data object for each one to use in binding
        var forms = $$('form[id]');
        log.debug("Found "+forms.length+" forms to bind");
        var i = forms.length, formId;
        while(i--) {
            formId = forms[i].id;
            log.debug("Creating form data area: "+formId);
            // create a data object for the form
            jsonapi.rivets.forms[ formId ] = { 'global': client.data, 'input': {} }; // give every form a link to global data (not included when the form is submitted) and an input area (for data to submit)
            // use rivets to bind the object to the form
            jsonapi.rivets.views[ formId ] = rivets.bind(forms[i], jsonapi.rivets.forms[formId]);
        }
*/
/*
        var dataviews = $$('.dataview');
        log.debug("Found "+dataviews.length+" dataviews to bind");
        var elementId;
        i = dataviews.length;
        while(i--) {
            elementId = dataviews[i].id; 
            log.debug("Creating view data area: "+elementId);
            jsonapi.rivets.views[ elementId ] = rivets.bind(dataviews[i], client.data);        
        }

//        client.view.sync();
*/        
        // fire an event to let the application know that we're done initializing
        //document.fire("jsonapi:initialized", {});


        /*
         * VIEWS maps name (tags, requests, certificates, rdf-triples) to view objects.
         * Each view object should have an update(data) function which will be called
         * whenever our data is updated.
         */
//VIEWS = parameters.views || {};
    };
    
//    jsonapi.initialize();
    
    jsonapi.configure = function(options) {
        jsonapi.options.merge(options);
        jsonapi.event.fire("configured", jsonapi.options);
    };

	// end initialize method

    // given any resource ( for example  jsonapi.data.hosts[5] ) , if it has a "links" attribute each one of the links is followed to other documents, and if they are already loaded they are added to a dynamic "linked" attribute on that object and if they are not already loaded then we initiate a load
    jsonapi.followLinks = function(item) {
        item.linked = {};
        if( item.links && typeof item.links === 'object' ) { // "links" is an object where each key is a resource collection name like "forms" or "hosts" and its value is an array of document id's in that collection that are linked from this item ;  the value could also be a single item id instead of a collection
            for(var rel in item.links) {
                item.linked[rel] = [];
                var linkRefs = item.links[rel] || [];
                if( !Object.isArray(linkRefs) ) {
                    linkRefs = [ linkRefs ];
                }
                for(var i=0; i<linkRefs.length; i++) {
                    var found = jsonapi.findById(jsonapi.data[rel], linkRefs[i]);
                    if( found ) {
                        item.linked[rel].push(found);
                    }
                }
            }
        }
    };

    // standard processing for application/vnd.api+json documents ; 
    // this document type is ALWAYS a collection. the collection itself does NOT have an id attribute but each individual resource in it will have an id attribute. The collection and also each individual resource MAY have "meta" and "links" attributes. ONLY the collection MAY have a "linked" attribute for side-loaded documents. 
    // in this implementation we create a "linked" attribute on each resource to hold the references to documents mentioned in its "links" attribute. this way the "links" attribute is not modified yet when rendering the actual references will be available under "linked" so a renderer does not need to have the entire data context or know how to navigate it.
    jsonapi.processApplicationVndApiJson = function (json, options) {
        Log.debug("processApplicationVndApiJson");
        // Store documents and any linked documents in the data context (replacing any existing documents of the same id - not merging unless we get specific instructions to merge in the meta section indicating it's a partial update)
        var collectionName = options.collection;// where we will store the data 
        if( !collectionName && json.meta && json.meta.data ) { // if not specifeid as an option we accept a hint from the document itself, assuming  that we should store it using the same collection name the resource itself uses
            collectionName = json.meta.data;
        }
        // first store our primary documents
        var jsonArray = [];
        if( json.meta.data ) { jsonArray = json.getx(json.meta.data); }
        else { } 
        if( jsonapi.data[collectionName] && Object.isArray(jsonapi.data[collectionName]) ) {
            jsonapi.data[collectionName].merge(jsonArray);
        }
        else {
            jsonapi.data[collectionName] = jsonArray; // note that if jsonapi.data[collectionName] exists but is not an array we're replacing it here
        }
        // second store any linked documents.  these are in the "linked" section which is a map in which each key is a collection name and each value is an array of records.
        if( json.linked ) {
            for(var linkedCollectionName in json.linked ) {
                if( jsonapi.data[linkedCollectionName] && Object.isArray(jsonapi.data[linkedCollectionName]) ) {
                    jsonapi.data[linkedCollectionName].merge(json.linked[linkedCollectionName]);
                }
                else {
                    jsonapi.data[linkedCollectionName] = json.linked[linkedCollectionName];
                }
            }
        }
        // third create a "linked" section in each resource (primary and linked) where we follow the links so the references are more easily available
        for(var i=0; i<jsonapi.data[collectionName].length; i++) {
            jsonapi.followLinks(jsonapi.data[collectionName][i]);
        }
        if( json.linked ) {
            for(var linkedCollectionName in json.linked ) {
                for(var i=0; i<jsonapi.data[linkedCollectionName].length; i++) {
                    jsonapi.followLinks(jsonapi.data[linkedCollectionName][i]);
                }
            }
        }
        Log.debug("processApplicationVndApiJson, jsonapi.data = "+Object.toJSON(jsonapi.data));        
    };
    
    /**
     * 
     * @param {type} json returned from the server, specificaly from Ajax transport.responseJSON provided by prototype.js framework
     * @param {type} options can contain a 'datapath' attribute for finding the array of records in the json, and a 'collection' attribute for indicating where the data should be stored in our client data store
     */
    jsonapi.processApplicationJson = function (json, options) {
        Log.debug("Processing JSON "+Object.toJSON(json));
        // if it's application/vnd.api+json , then delegate processing to that handler  (we get here if server reports application/json but the content itself reports the more specific application/vnd.api+json in its metadata section)
        if( json.meta && json.meta.content_type === "application/vnd.api+json" ) {
            return jsonapi.processApplicationVndApiJson(json, options);
        }
        options = options || {};
        var datapath = options.datapath;
        var collection = options.collection;
        // for all other json documents, some standard processing:
        if( typeof json === 'object' ) {
            var ptr;
            if( Object.isArray(json) ) {
                // document is an array, so probably everything inside is a record
                ptr = json;
            }
            else {
                // document is an object, so look for a 'data' attribute with the records
                if( json.data  && Object.isArray(json.data) ) {
                    ptr = json.data;
                }
                else if( datapath && json[datapath] && Object.isArray(json[datapath]) ) {
                    ptr = json.getx(datapath);
                }
                // process array of records
                if( jsonapi.data[collection] && Object.isArray(jsonapi.data[collection]) ) { // must be an array
                    if( options.remove ) { jsonapi.data[collection].removeAll(null); } // automated data binding tends to break for null objects
                    jsonapi.data[collection].merge(ptr);
                }
                else {
                    jsonapi.data[collection] = ptr;
                } 
                //jsonapi.data.setx(keyPath, existingData);  
                
            }
        }
        if( typeof json === 'string' ) {
        }
        if( typeof json === 'number' ) {
        }
    };

    jsonapi.post = function (resourceName, postObject, opt) {
        var info = jsonapi.resources[resourceName] || {};
        // if( jsonapi.resources[resourceName] === undefined ) { use jsonapi.apistyles.resourceCollectionWithId(resourceName) to create a default }
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
                jsonapi.processApplicationJson(json, my); 
                jsonapi.event.fire("httpPostSuccess", { resource:my, content:postObject });
            }    
        });
        jsonapi.requests.push(request);
    };	
	
    jsonapi.get = function (resourceName, params, opt) {
        Log.debug("jsonapi.resouces = "+jsonapi.resources);
        var info = jsonapi.resources[resourceName] || {};
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
                Log.debug("GET JSON = "+Object.toJSON(json));
                my.replace = true; // indicates that data from this get should replace any previous data in the store (should be set to true for 'search' queries, and false for any 'add more data' queries)
                jsonapi.processApplicationJson(json, my); 
                jsonapi.event.fire("httpGetSuccess", { resource:my, params:params });
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
        jsonapi.requests.push(request);
    };
    jsonapi.put = function (resourceName, putObject, opt) {
        Log.debug("AJAX PUT resourceName: "+resourceName);
        Log.debug("AJAX PUT object: "+Object.toJSON(putObject));
        Log.debug("AJAX PUT opt1: "+(typeof opt));
        Log.debug("AJAX PUT opt2: "+opt);
        Log.debug("AJAX PUT opt3: "+Object.toJSON(opt));
//        Log.debug("AJAX PUT "+resourceName+": "+Object.toJSON(putObject)+" WITH OPTIONS: "+Object.toJSON(opt));
        Log.debug("AJAX PUT config: "+Object.toJSON(jsonapi.resources[resourceName]));
        var info = jsonapi.resources[resourceName] || {};
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
                jsonapi.processApplicationJson(json, my); 
                jsonapi.event.fire("httpPutSuccess", { resource:my, content:putObject });
            }    
        });
        jsonapi.requests.push(request);
    };
    jsonapi['delete'] = function (resourceName, deleteObject, opt) {
        var info = jsonapi.resources[resourceName] || {};
        var my = info.clone().merge(opt).merge({name:resourceName}); // make a copy of the resource config and override it with passed-in options
//        var keyPath = my.datapath;
        var request = new Ajax.Request(my.uri+'/'+deleteObject[my.idkey], {
            method: 'delete',
            onSuccess: function (transport) {
                var response = transport.responseText || "no response text";
                Log.debug("Success! \n\n" + response);
                if( transport.responseText ) {
                    var json = transport.responseJSON;
                    jsonapi.processApplicationJson(json, my); 
                }
                jsonapi.event.fire("httpDeleteSuccess", { resource:my, content:deleteObject });
            }    
        });
        jsonapi.requests.push(request);
    };	
	
    // linkObject: { href, type }
    // options can have anything that can be present in the link object (they get merged in)
    // so for example if the linkObject only has href, options can specify type. 
    // or if the linkObject itself is a string (Just the URL) options can have the other attributes.
    // options can also have the callback function in case it the results should not be handled normally.
    // if an attribute is specified in both linkObject and options, the options override the linkObject.
    jsonapi.loadResourcesFromLink = function(linkObject, options) {
        var spec = {};
        if( typeof linkObject === "string" ) {
            spec.href = linkObject;
            Log.debug("loadResourcesFromLink(string)");
        }
        if( typeof linkObject === "object" ) {
            spec.merge(linkObject);
            Log.debug("loadResourcesFromLink(object)");
        }
        spec.merge(options);
        jsonapi.get(spec.model, null, {uri:spec.href,datapath:spec.model}); // spec.model like "resources" 
    };
    
	// must be called after DOM is loaded
	jsonapi.loadResourceDefinitions = function() {
	Log.debug("load resource dfs");
		   // look for a "resources" link to automatically load available server resources; 
		   // if not provided then the application should have configured resources via javacript
		   var links = $(document.head).select('link');
		   var i;
		   if( links ) {
			   // look for rel=resources
			   for(i=0; i<links.length; i++) {
				   if( links[i].rel === "resources" ) {
					   Log.debug("Found resource links at "+links[i].href+" with type "+links[i].type);
                       jsonapi.loadResourcesFromLink(links[i].href, {type:"application/vnd.api+json",model:"resources"});
//					   jsonapi.get("resources", null, {uri:links[i].href,datapath:"resources"});
				   }
			   }
		   }
	};
	
//    client.data = data; 
//    log.debug("again, data = "+Object.toJSON(client.data));
// UTILITIES

    /**
     * Uses validator.js to validate form input. Conveniently accepts form id, form element, or form child element to identify
     * the form to validate.
     *
     * @param input can be a form id (string), html form element, or any html element inside the form such as a submit button
     * @return an object { validator: Validator, input: input-model, isValid: boolean }
     */
    function validate(input) {
        var formId;
        var validator;
        var model;
        var isValid = false;
        if (typeof input === 'string') {  // the id of a form
            formId = input;
        }
        if (Object.isElement(input) && input.tagName.toLowerCase() == 'form') { // an html form, so use its id
            formId = input.id;
        }
        else if (Object.isElement(input)) { // an html element but not the form... so move up to the enclosing form to get the id
            formId = input.up('form').id;
        }
        //log.debug("getting model for form id "+formId+"  area: "+Object.toJSON(jsonapi.rivets.forms[formId]));
        //model = jsonapi.rivets.forms[ formId ].input; // only validate the input area (not the global data area)
        Log.debug("so the model is... "+Object.toJSON(model));
        validator = new Validation(formId, {
            useTitles: true,
            immediate: true,
            onSubmit: false
        });
        /*
         if( typeof input === 'object' ) {
         model = input;
         validator = null; 
         }
         */
        if (validator) {
            isValid = validator.validate();
        }
        Log.debug("returning with input/model = "+Object.toJSON(model));
        return {'validator': validator, 'input': model, 'isValid': isValid, 'formId': formId};
    }


    function apiwait(text) {
        $('ajaxstatus').addClassName("wait");
        if (!text) {
            text = "Waiting...";
        }
        if (text) {
            $('ajaxstatus').update(text);
        }
//$('ajaxstatus').show();
    }
    function apidone() {
        //$('ajaxstatus').hide();
        $('ajaxstatus').removeClassName("wait");
        $('ajaxstatus').update("");
    }

    function apiurl(resource) {
        return options.baseurl + /* '/' + */ resource;
    }


    /*
     function view(resource) {
     if( resource in VIEWS ) {
     log.debug("There is a view for resource "+resource);
     if( 'update' in VIEWS[resource] ) {
     log.debug("Found view for "+resource);
     return VIEWS[resource];
     }
     }
     return { 'update': function() { log.debug("No view for resource '"+resource+"'"); } };
     }
     */
// SERVER REPOSITORY HELPER METHODS

    jsonapi.notify = function(notice) {
        Log.debug("NOTICE: "+Object.toJSON(notice));
//        client.data.notices.push(notice); // { text:'...', clear:'auto' }  or clear:'confirm' to force user to acknowledge
 //       view.sync();
        // for now we implement it this way, but in the future it should be part of the data model (data.notices) with automatic confirmations to server when user clicks on a 'confirm' notice to acknowledge it:
//        $('notifications').insert({ bottom: notice.text });
    };

    // look through the array for an object with the given id, if found return it (assume there is only one) if not found return null
    // usage example:   var oneResource = jsonapi.findById(jsonapi.data.hosts, hostId)
    jsonapi.findById = function(array, id) {
        if( !array || !Object.isArray(array) ) { return null; }
        for(var i=0; i<array.length; i++) {
            if( typeof array[i] === 'object' && array[i].id === id ) { return array[i]; } 
        }
        return null;
    };

// VIEW API
    document.observe("jsonapi:httpPostSuccess", function(event) {
        log.debug("HTTP POST OK: "+event.memo.resource.name);
	/*
        switch(event.memo.resource.name) {
			
            default:
                log.debug("No handler for successful HTTP POST of "+event.memo.resource.name);
        };
		*/
        /*
        if( event.memo.resource.name === 'tags' ) {
            //$('tag-create-form').reset();
            // reset the form's data model, and rivets will automatically update the form GUI. 
            // you CANNOT just set forms['tag-create-form'] = { name:'', oid:'', values:[] } because that will 
            // replace the reference and will cause rivets to lose the connection between the model & the form.            
//            event.memo.resource.app.input.name = ''; //jsonapi.rivets.forms['tag-create-form'].name = ''; 
//            event.memo.resource.app.input.oid = '';//jsonapi.rivets.forms['tag-create-form'].oid = ''; 
//            event.memo.resource.app.input.values = [];//jsonapi.rivets.forms['tag-create-form'].values = []; 
//            event.memo.resource.app.validator.reset();
//            $('tag-create-name').value = '';
//            $('tag-create-oid').value = '';
//            $('tag-create-values').value = '';
        }
                */
    });

    document.observe("jsonapi:httpDeleteSuccess", function(event) {
        Log.debug("HTTP DELETE OK: "+event.memo.resource.name);
        Log.debug("httpDeleteSuccess: "+Object.toJSON(event.memo));
	/*
        switch(event.memo.resource.name) {
            default:
                log.debug("No handler for successful HTTP DELETE of "+event.memo.resource.name);
        };
		*/

    });    

    document.observe("jsonapi:httpPutSuccess", function(event) {
        Log.debug("HTTP PUT OK: "+event.memo.resource.name);
	/*
        switch(event.memo.resource.name) {
            default:
                log.debug("No handler for successful HTTP PUT of "+event.memo.resource.name);
        };
		*/

    });    
    
    document.observe("jsonapi:httpGetSuccess", function(event) {
        Log.debug("HTTP GET OK: "+event.memo.resource.name);
        switch(event.memo.resource.name) { // or should this be based on a mandatory meta.model attribute in the response? or maybe look for both but separately so orthogonal processing can apply
			case 'resources':
				Log.debug("got resources from server!!");
                Log.debug("Resources: "+Object.toJSON(jsonapi.data.resources));
                // fill in default values... 
                for(var i=0; i<jsonapi.data.resources.length; i++) {
                    if( jsonapi.data.resources[i].one && !jsonapi.data.resources[i].many ) {
                        jsonapi.data.resources[i].many = jsonapi.data.resources[i].one+"s"; // should use a pluralize method not yet defined (to catch common cases like person->people)
                    }
                    if( jsonapi.data.resources[i].one && !jsonapi.data.resources[i].label_one ) {
                        jsonapi.data.resources[i].label_one = jsonapi.data.resources[i].one.capitalize(); // capitalize is defined by prototype.js 
                    }
                    if( jsonapi.data.resources[i].many && !jsonapi.data.resources[i].label_many ) {
                        jsonapi.data.resources[i].label_many = jsonapi.data.resources[i].many.capitalize(); // capitalize is defined by prototype.js 
                    }
                    var meta = jsonapi.data.resources[i].meta || {};
                    Log.debug("Metadata: "+Object.toJSON(meta));
                    Log.debug("Links? "+jsonapi.data.resources[i].links);
                    Log.debug("Forms? "+jsonapi.data.resources[i].links.forms);
                    Log.debug("Forms array? "+Object.isArray(jsonapi.data.resources[i].links.forms));
                    if( jsonapi.data.resources[i].links && jsonapi.data.resources[i].links.forms && Object.isArray(jsonapi.data.resources[i].links.forms) ) {
                        var formRefs = jsonapi.data.resources[i].links.forms;
                        Log.debug("There are forms: "+Object.toJSON(formRefs)); // for example,  ["register_host", "search_hosts"]  these are form ids
                        for(var j=0; j<formRefs.length; j++) {
                            var formId = formRefs[j];
                            Log.debug("Looking for form with id "+formId+" in "+Object.toJSON(jsonapi.data.forms));
                            var form = jsonapi.findById(jsonapi.data.forms, formId);
                            Log.debug("Found form: "+Object.toJSON(form));
                            if( form ) {
                                /*
                                if( link.rel && !link.title ) {
                                    link.title = link.rel.capitalize();
                                }
                                */
                                if( form.href ) {
                                    Log.debug("form href: "+form.href);
                                    if( !form.href.startsWith("http") && !form.href.startsWith("#") ) {
                                        // relative links from baseurl
                                        form.href = jsonapi.options.baseurl + "/" + form.href;
                                    }
                                }
                                else {
                                    Log.error("form does not contain href: "+Object.toJSON(form));
                                }
                                // special handling of forms - we show them in a window
                                /*
                                if( link.rel === "form" && link.href ) {
                                    link.onclick = "jsonapi.displayFormWindow"; // a function name; will be called when user clicks the link and it will be passed the <a> element containing href and rel; the <a> should also have an attribute like data-model="hosts" that the display function can query to find out which resource this applies to
                                }*/
                            }
                            else {
                                Log.error("Cannot find form "+formId+" referenced by resource "+jsonapi.data.resources[i].id);
                            }
                        }
                    }
                    // link this resource definition to its data section
                    if( !jsonapi.data[ jsonapi.data.resources[i].many ] ) {
                        Log.debug("Creating data section for resource collection: "+jsonapi.data.resources[i].many);
                        jsonapi.data[ jsonapi.data.resources[i].many ] = [];
                    }
                    jsonapi.data.resources[i].data = jsonapi.data[ jsonapi.data.resources[i].many ];
                    Log.debug("Final resource: "+Object.toJSON(jsonapi.data.resources[i]));
                }
               // populate   nav ul  element   with items like                      <li class="tab"><a href="#rdf">RDF</a></li>			
                var sourceNav = $('custom-resourcenav-template').innerHTML;
                var templateNav = Handlebars.compile(sourceNav);
                var itemNav = templateNav({resources:jsonapi.data.resources, forms:jsonapi.data.forms}); // resources is an array, with each element like { "one": "notice", "many": "notices", "label_one":"Notice", "label_many":"Notices", "uri":"/notices" };
                $('resourcenavtabs').childElements().invoke('remove'); // clear any previous entries
                $('resourcenavtabs').insert({bottom: itemNav});
                // now resource views
                var sourceViews = $('resource-views-template').innerHTML;
                var templateViews = Handlebars.compile(sourceViews);
                var itemViews = templateViews({resources:jsonapi.data.resources}); // resources is an array, with each element like { "one": "notice", "many": "notices", "label_one":"Notice", "label_many":"Notices", "uri":"/notices" };
                $('resource-views').childElements().invoke('remove'); // clear any previous entries
                $('resource-views').insert({bottom: itemViews});
                /*
                jsonapi.navTabs.initialize('resourcenavtabs',{
                    activeClassName: 'current',
                    setClassOnContainer: true,
                    afterChange: function(new_container) {
//                        Log.debug("afterChange: "+new_container.innerHTML);
  //                      new_container.show();
                    }                    
                }); */
                
                break;
            default:
                Log.debug("No handler for successful HTTP GET of "+event.memo.resource.name);
        };

    });    
    
    // this could be in a utility library, it's a somewhat generic function to convert an array of objects into a table (array of arrays) with option for specific header fields to include (if not specified then data will be scanned and all attributes will be used) and option for specifying a formatting/filter function for specific attributes to be used for pre-processing data before it's applied to a view template
    // collection:  an array of objects    (for example  jsonapi.data.hosts)   
    // options:  can include a header.columns attribute to indicate which attributes to use
    jsonapi.createTable = function(collection, options) {
        if( !collection || !Object.isArray(collection) ) {
            Log.error("createTable on non-array returning empty array");
            return { "header":{}, "body":[] };
        }
        var headerColumnNames = [];
        if( options && options.header && options.header.columns && Object.isArray(options.header.columns) ) {
            headerColumnNames.pushArray(options.header.columns); // pushArray defined in array.js
        }
        else {
            // collect the list of attributes to show as table headers by scanning the data 
            for(var i=0; i<collection.length; i++) {
                headerColumnNames.pushArray(collection[i].keys());// pushArray defined in array.js
            }
            headerColumnNames = headerColumnNames.uniq(); // uniq provided by prototype.js
        }
        Log.debug("Unique set of column names: "+Object.toJSON(headerColumnNames));
        // now create the body as an array of arrays using these column names
        var bodyArray = [];
        for(var i=0,imax=collection.length; i<imax; i++) {
            var row = [];
            for(var j=0,jmax=headerColumnNames.length; j<jmax; j++) {
                var attrName = headerColumnNames[j];
                row.push(collection[i][attrName]);
            }
            Log.debug("Table row: "+Object.toJSON(row));
            bodyArray.push(row);
        }
        return { "header":{"columns":headerColumnNames}, "body":bodyArray };
    };
    
    jsonapi.refreshDataView = function(collectionName) {
        var sections = $$("section[data-model=\""+collectionName+"\"]");
        if( sections && sections.length > 0 ) {
            var dataview = sections[0].down("div.data-view");
            dataview.childElements().invoke('remove'); // clear any previous entries
            if( jsonapi.data[collectionName] && Object.isArray(jsonapi.data[collectionName]) ) {
                var table = jsonapi.createTable(jsonapi.data[collectionName]);
                var source = $('resource-data-template').innerHTML;
                var template = Handlebars.compile(source);
                var itemViews = template(table); // resources is an array, with each element like { "one": "notice", "many": "notices", "label_one":"Notice", "label_many":"Notices", "uri":"/notices" };
                dataview.insert({bottom: itemViews});
            }
        }
    };

    document.observe("jsonapi:formSubmitInsert", function(event) {
        Log.debug("FORM SUBMIT INSERT: "+event.memo.collectionName); // there'sd also event.memo.data (what got submitted) and event.memo.form (the form tag itself)
        // data was already added to jsonapi.data[collectionName], we just need to display it now
        jsonapi.refreshDataView(event.memo.collectionName);
    });

    // linkElement should look like this: <a href="http://localhost:2700/forms/hosts/create_host.html" rel="form" onclick="javascript:jsonapi.displayFormWindow(this); return false;" data-model="hosts">Register Host</a>
    jsonapi.displayFormWindow = function(linkElement, options) {
        // the "data-model" attribute is required so we know what this form applies to
        var model = linkElement.readAttribute("data-model");
        if( !jsonapi.forms[model] ) { jsonapi.forms[model] = {}; }
        // check if we already created the form window
        var rel = linkElement.readAttribute("rel");
        var href = linkElement.readAttribute("href");
        if( !jsonapi.forms[model][href] ) {
    var window_header = new Element('div',{  
        className: 'window_header'  
    });  
    var window_title = new Element('div',{  
        className: 'window_title'  
    });  
    var window_close = new Element('div',{  
        className: 'window_close'  
    });  
    var window_contents = new Element('div',{  
        className: 'window_contents'  
    });  
    var w = new Control.Window(linkElement,Object.extend({  
        className: 'window',  
        closeOnClick: window_close,  
        draggable: window_header,  
        insertRemoteContentAt: window_contents,  
        afterOpen: function(){  
            window_title.update(linkElement.readAttribute('title'));
            // look for the form element and disable submit action because we'll handle that with javascript
            var form = window_contents.down("form");
            Log.debug("Found form element? "+form);
            form.on("submit", function(event) { 
                // is this form supposed to INSERT, SEARCH(select/find), EDIT(update/replace/patch), or DELETE information?
                if( form.hasClassName("insert") ) {
                    // when we submit,  grab all the data and just add a host record...  to jsonapi.data[model]
                    var formdata = Form.serialize(form, true); // true means to serialize into a javascript object
                    if( !formdata.id ) { formdata.id = uuid.v4(); } // automatically generate a UUID if it was not specified by the user
                    Log.debug("Form data: "+Object.toJSON(formdata));
                    if( !jsonapi.data[model] ) { jsonapi.data[model] = []; }
                    jsonapi.data[model].push(formdata);
                    jsonapi.event.fire("formSubmitInsert", {data:formdata, form:form, collectionName:model});
                }
                Log.debug("Submitting form -- can intercept here and submit via ajax to the appropriate resource url");
                if(event) { event.preventDefault(); }  // officially stop the event, otherwise it will submit the form even though we return false below
                return false;  // before html5 we have to return false to stop the form submit
            });
            // look for the submit button and add javascript to it!   this works but we don't need it right now other than to maybe gray out the button while submitting...
//            var submit = window_contents.down("input[type='submit']");
//            Log.debug("Found submit button? "+submit);
//            submit.on("click",function() { alert("hello"); return false; }); //javascript:alert('submitting'); return false;";
            
        },
        afterClose: function() {
        },                
        onFailure: function(requestObject) {
            Log.error("Failed to load form "+href+": "+requestObject.request.transport.statusText);
        },
        onSuccess: function(requestObject) {
            //requestObject.request.options.method == "get" 
            // requestObject.request.transport.status == 200   requestObject.request.transport.statusText == "OK"
//            Log.debug("Loaded content for window: "+Object.toJSON(requestObject.request.transport.responseText));
        }
    },options || { method:'GET' }));  
    w.container.insert(window_header);  
    window_header.insert(window_title);  
    window_header.insert(window_close);  
    w.container.insert(window_contents);  
    //return w;  
    jsonapi.forms[model][href] = w;
        } // end create form if does not already exist

        jsonapi.forms[model][href].open();


    };

})(jsonapi);  // end module definition


document.observe("dom:loaded", function() {
   jsonapi.initialize();
   jsonapi.loadResourceDefinitions();
});
