/*
Copyright 2013 Intel Corporation. All rights reserved.

Dependencies:
prototype.js
log.js
form.js
rivets.js
 */


 
var mtwilson = mtwilson || {};
mtwilson.atag = mtwilson.atag || {};
(function() { // start mt wilson asset tag module definition

    // CONFIGURATION
    var MTWILSON_ATAG_PROVSVC_URL = "http://localhost:8080"; // redirects to http://localhost:8080
		
    // DATA
    var data;
    var view;
    
    
    mtwilson.atag.initialize = function(parameters) {
        data = parameters.data || { 'tags':[], 'certificateRequests':[], 'certificates':[], 'rdfTriples':[] };
        view = parameters.view || { 'update': function(data) { log.debug("No view to update"); } };
        /*
         * VIEWS maps name (tags, requests, certificates, rdf-triples) to view objects.
         * Each view object should have an update(data) function which will be called
         * whenever our data is updated.
         */
        //VIEWS = parameters.views || {}; 
    };
    
    // UTILITIES
    
    function apiwait(text) {
        $('ajaxstatus').addClassName("wait");
        if( !text ) {
            text = "Waiting...";
        }
        if( text ) {
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
        return MTWILSON_ATAG_PROVSVC_URL+'/'+resource;
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
		
    function apiget(resource) {		
        apiwait("Loading...");
        new Ajax.Request(apiurl(resource), {
            method:'get',
            onSuccess: function(transport) {
                var response = transport.responseText || "no response text";
                log.debug("Success! \n\n" + response);
                apidone();
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
    }

    function apipost(resource) {		
        apiwait("Saving...");
        new Ajax.Request(apiurl(resource), {
            method:'post',
            onSuccess: function(transport) {
                var response = transport.responseText || "no response text";
                log.debug("Success! \n\n" + response);
                apidone(); 
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
    }


    function apidelete(resource) {		
        apiwait("Deleting...");
        new Ajax.Request(apiurl(resource), {
            method:'delete',
            onSuccess: function(transport) {
                var response = transport.responseText || "no response text";
                log.debug("Success! \n\n" + response);
                apidone(); 
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
    }


    function apiput(resource) {		
        apiwait("Saving...");
        new Ajax.Request(resource, {
            method:'put',
            onSuccess: function(transport) {
                var response = transport.responseText || "no response text";
                log.debug("Success! \n\n" + response);
                apidone(); 
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
    }
    
    // SERVER API
    var createTag = function(tagInfo) {
        apiwait("Saving...");
        new Ajax.Request(apiurl('/tags'), {
            method:'post',
            //parameters: $('tag-create-form').serialize(true), // send www form submission
            postBody: Object.toJSON(tagInfo), // send JSON object
            onSuccess: function(transport) {
                var response = transport.responseText || "no response text";
                log.debug("Success! \n\n" + response);
                apidone(); 
                if( response.responseJSON ) { // XXX TODO and response is an array...
                    for(var i=0; i<response.responseJSON.length; i++) {
                        data.tags.unshift(response.responseJSON[i]); // add each tag in the response (which is typically 1:1 of what we sent but filled in with UUIDs, URLs, etc)
                    }
                }
                view.update(data);
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
    };

    // VIEW API

    mtwilson.atag.createTag = function(tagObject) {
        // must create a clone of the object to avoid having our data altered by accident if the caller re-uses it
        var clone = Object.toJSON(tagObject).evalJSON();
        // XXX make a real json call... update view with the result (server will say "created", then redirect us to complete object... grab that and unshift it
        data.tags.unshift(clone);
        view.update(data);
    };
    mtwilson.atag.createRdfTriple = function(rdfTripleObject) {
        // must create a clone of the object to avoid having our data altered by accident if the caller re-uses it
        var clone = Object.toJSON(rdfTripleObject).evalJSON();
        // XXX make a real json call... update view with the result (server will say "created", then redirect us to complete object... grab that and unshift it
        data.rdfTriples.unshift(clone);
        view.update(data);
    };
    
    
    mtwilson.atag.updateTags = function(tags) {
        data.tags = tags;
    }

    function searchTags() {
        apiwait("Searching tags...");
        new Ajax.Request(apiurl(resource), {
            method:'get',
            onSuccess: function(transport) {
                apidone();
                log.debug("Response: " + (transport.responseText || "no response text"));
                if( transport.responseJSON ) {
                    updateTags(transport.responseJSON); // XXX TODO if it's an array, send it to updateTags as-is, if it's an object, look for a 'tags' field and send that.
                }
            },
            onFailure: function() {
                log.error('Something went wrong...');
                apidone();
            }
        });
	
    }    
    /*
    function storeConfigForm() {
        if( !('form' in MHConfig) ) { MHConfig['form'] = {}; }
        var textInputs = $$('form#config-form input[type="text"]');
        var passwordInputs = $$('form#config-form input[type="password"]');
        for(var i=0; i<textInputs.length; i++) {
            MHConfig['form'][ textInputs[i].id ] = textInputs[i].value;
        }
        for(var i=0; i<passwordInputs.length; i++) {
            MHConfig['form'][ passwordInputs[i].id ] = passwordInputs[i].value;
        }
    }
		
    function fillConfigForm() {
        if( !('form' in MHConfig) ) { return; }			
        for(var inputId in MHConfig['form']) {
            try {
                var input = $(inputId);
                if( input.type == "text" || input.type == "password" ) {
                    input.value = MHConfig['form'][inputId];
                }
            }
            catch(e) {
                log.warning("Missing configuration input in form: "+e.name+": "+e.message);
            }
        }
    }
		
    function updateKeyTableView() {
        for(var keyname in MHConfig['dek']) {
            var record = MHConfig['dek'][keyname];
            $$('table#key-table-view tbody')[0].insert({bottom:'<tr><td>'+keyname+'</td><td>AES</td><td>128</td><td>unknown</td><td></td></tr>'});
        }
    }
    */






})();  // end mt wilson asset tag module definition
 
 
