/*
 Copyright 2013 Intel Corporation. All rights reserved.

 Dependencies:
 prototype.js
 log.js
 form.js
 rivets.js
 */

/* globals: $, $$, Ajax, Object, TypeError, URI, XMLHttpRequest, document, fakeresource, log, mtwilson, rivets */
/*jslint white: true */

if( mtwilson === undefined ) {
     var mtwilson = {};
}

//var mtwilson = mtwilson || {};
mtwilson.atag = mtwilson.atag || {};
(function () { // start mt wilson asset tag module definition

    // base url
    var uri = new URI(document.location.toString());

    // DATA
    var data;
    var view;
    var options;

    mtwilson.atag.initialize = function (parameters) {
        data = parameters.data || {
            'tags': [],
            'certificateRequests': [],
            'certificates': [],
            'rdfTriples': []
        };
        log.debug("init data to "+Object.toJSON(data));
        ajax.data = data;
        mtwilson.atag.data = data;
        view = parameters.view || {
            'update': function (data) {
                log.debug("No view to update");
            },
            'sync': function () {
                log.debug("No view to sync");
            }
        };
        ajax.view = view; // after every ajax call the view will be updated ???
        options = parameters.options || {
            'baseurl': (uri.scheme() == 'http' || uri.scheme() == 'https') ? '' : '#' // or http://localhost:8080
        };
        /*
         * VIEWS maps name (tags, requests, certificates, rdf-triples) to view objects.
         * Each view object should have an update(data) function which will be called
         * whenever our data is updated.
         */
//VIEWS = parameters.views || {};
    };


    // configure the ajax framework
    ajax.resources.tags = { uri:'/tags', datapath:'tags', idkey:'uuid' };
    ajax.resources.rdfTriples = { uri:'/rdfTriples', datapath:'rdfTriples', idkey:'uuid' };
    ajax.resources.certificates = { uri:'/certificates', datapath:'certificates', idkey:'uuid' };
    ajax.resources.certificateRequests = { uri:'/certificateRequests', datapath:'certificateRequests', idkey:'uuid' };

//    mtwilson.atag.data = data; 
//    log.debug("again, data = "+Object.toJSON(mtwilson.atag.data));
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
        model = mtwilson.rivets.forms[ formId ];
        validator = new Validation(formId, {
            useTitles: true,
            immediate: true,
            onSubmit: false
        });
        /*
         if( typeof input === 'object' ) {
         model = input;
         validator = null; // XXX TODO maybe we can use an optional second parameter to identify the form?? or maybe we shouldn'ta ccept obejcts... ??
         }
         */
        if (validator) {
            isValid = validator.validate();
        }
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



// VIEW API

    mtwilson.atag.createTag = function (input) {
        var report = validate(input);
        if (report.isValid) {
            // XXX make a real json call... update view with the result (server will say "created", then redirect us to complete object... grab that and unshift it
            var clone = Object.toJSON(report.input).evalJSON(); // or use report.input.cloneJSON() if it has circular references...
//        data.tags.unshift(clone);
//        view.sync(); //view.update(data);  // XXX TODO  need to pass the call back to ajax...
            ajax.json.post('tags', [clone]);
        }
    };
    mtwilson.atag.createRdfTriple = function (input) {
        var report = validate(input);
        if (report.isValid) {
            // XXX make a real json call... update view with the result (server will say "created", then redirect us to complete object... grab that and unshift it
            var clone = Object.toJSON(report.input).evalJSON();
//            data.rdfTriples.unshift(clone);
            ajax.json.post('rdfTriples', [clone]);
            view.sync(); //view.update(data);
        }
    };


    mtwilson.atag.removeFirstTag = function (oid) {
        var i;
        for (i = 0; i < data.tags.length; i--) {
            if (('oid' in data.tags[i]) && data.tags[i].oid == oid) {
                ajax.json.delete('tags', data.tags[i]);
                data.tags.splice(i, 1);  // removes just this element...  note it's the first one found, so if you search for something that appears many times, only the first one will be removed!
                view.sync(); //view.update(data);
                return;
            }
        }
    };

    // removes all tags with this oid
    mtwilson.atag.removeTag = function (oid) {
        var i;
        for (i = data.tags.length - 1; i >= 0; i--) {
            if (('oid' in data.tags[i]) && data.tags[i].oid == oid) {
                ajax.json.delete('tags', data.tags[i]);
                data.tags.splice(i, 1);  // removes just this element...  note it's the first one found, so if you search for something that appears many times, only the first one will be removed!
                //					return;
            }
            view.sync(); //view.update(data);
        }
    };


    mtwilson.atag.updateTags = function (tags) {
        data.tags = tags;
    };

    mtwilson.atag.searchTags = function (input) {
        var report = validate(input); // XXX TODO: add an OID validator function ; currently none of the inputs need to be validated
//    if( report.isValid ) { ... }
        // each section of the tag search form looks like "Name [equalTo|contains] [argument]" so to create the search criteria
        // we form parameters like nameEqualTo=argument  or nameContains=argument
        var fields = ['name', 'oid', 'value'];
        var i;
        for (i = 0; i < fields.length; i++) {
            $('tag-search-' + fields[i]).name = fields[i] + $F('tag-search-' + fields[i] + '-criteria'); // this.options[this.selectedIndex].value;
        }
        ajax.json.get('tags', {uri:'/tags?' + $(report.formId).serialize()}); // XXX TODO  serialize the search form controls into url parameters...
//    apiwait("Searching tags...");
    };
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

// given a form model (eg mtwilson.rivets.forms['myform']) check each attribute
// which corresponds to a text input, password input, or textarea, and if its value
// is the same as the default display text (from input.js) then clear the value
// Returns:  a copy of the model (so that when we reset the value to empty string
// it doesn't affect the UI at all
// Example:   { 'attr1':'default value', 'attr2':'user defined', 'attr3':'user defined' }
//   if the 'default value' corresponds to 'data-alt' in <input id='input1' data-bind-value='attr1' data-alt='default value'/>
//   then the returned object would be:
//            { 'attr1':'',  'attr2':'user defined', 'attr3':'user defined' }
    /*  *** NOT NEEDED WHEN YOU USE HTML5 PLACEHOLDER ATTRIBUTES... ***
     function cloneWithoutAltText(model) {
     var clone = {};
     for(var p in model) {
     var els = $$('input[data-bind-value='+p+']','password[data-bind-value='+p+']', 'textarea[data-bind-text='+p+']');
     if( els.length > 0 ) {
     for(var i=0; i<els.length; i++) {
     if( els[i].getAttribute('data-alt') && (els[i].getAttribute('data-alt') == model[p]) ) {
     clone[p] = '';
     }
     else {
     clone[p] = model[p];
     }
     }
     }
     else {
     clone[p] = model[p];
     }
     }
     return clone;
     }
     */


    /*
     * This method accepts either the id of a form or a javascript object with input.
     * If a form id is given, the form is validated and the corresponding rivets model
     * is used as input.
     * If a javascript object is given, at this time we don't validate it because the
     * validation is tied to the form and there's no convenient way to reapply the rules
     * to the object.
     */
    mtwilson.atag.updateCertificateAuthority = function (input) {
        var report = validate(input);
        if (report.isValid) {
            alert("form is valid, update ca: " + Object.toJSON(report.input));
        }
        else {
            alert("form NOT valid, update ca: " + Object.toJSON(report.input));

        }
        //alert("Update CA: "+Object.toJSON(cloneWithoutAltText(input)));
    };



})();  // end mt wilson asset tag module definition


mtwilson.rivets = {};
mtwilson.rivets.forms = {};
mtwilson.rivets.views = {};
document.observe('dom:loaded', function () {
    // find all forms, and automatically create a data object for each one to use in binding
    var forms = $$('form[id]');
    var i, formId;
    for (i = 0; i < forms.length; i++) {
        formId = forms[i].id;
        // create a data object for the form
        mtwilson.rivets.forms[ formId ] = {};
        // use rivets to bind the object to the form
        mtwilson.rivets.views[ formId ] = rivets.bind(forms[i], mtwilson.rivets.forms[formId]);
    }
});

