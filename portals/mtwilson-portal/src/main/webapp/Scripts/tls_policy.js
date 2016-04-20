// requires jquery
var mtwilsonTlsPolicyModule = {};
(function($,m){
    var defineFunction = function(obj, key, fn) {
        Object.defineProperty(obj, key, {
           enumerable: false,
           configurable: false,
           writable: false,
           value: fn
       });       
    };
    
    defineFunction(m, "onGetTlsPolicies", function(data) {
        // data looks like this: {"meta":{"default":null,"allow":["certificate","public-key"],"global":null},"tls_policies":[]}
        // or like this with tls_policies filled in: {"meta":{"default":"8fc80a70-f9b7-11e3-a3ac-0800200c9a66","allow":["certificate","public-key"],"global":null},"tls_policies":[{"id":"8fc80a70-f9b7-11e3-a3ac-0800200c9a66","name":"server default","descriptor":{"policy_type":"INSECURE"},"comment":"server default","private":false},{"id":"8fddca40-f994-11e3-a3ac-0800200c9a66","name":"TRUST_FIRST_CERTIFICATE","descriptor":{"policy_type":"TRUST_FIRST_CERTIFICATE"},"comment":"hello","private":false}]}
        m.tls_policies = data.tls_policies;
        m.configuration = data.meta;
    });
    
    defineFunction(m, "getTlsPolicyChoices", function() {
        var choices = []; // each element will be { label:"...", value:"..." }
        // first, server global if it's defined will cause ALL policy choices to be disabled because the user needs to know the server will force a global policy and the user doesn't get a choice
        var disabled = false;
        if( m.configuration["global"] ) {
            disabled = true;
            choices.push({value:"",label:m.configuration["global"],policy_scope:"global",policy_type:"", isGlobal:true, disabled:false});
        }
        // second, server default if it's defined (notice we don't put real policy data here - everything is blank so when the request is submitted to server it will use the default)
        if( m.configuration["default"] ) {
            choices.push({value:"",label:m.configuration["default"],policy_scope:"default",policy_type:"", isDefault:true, disabled:disabled});
        }
        console.log("allowed policies: ", m.configuration.allow);
        // next, any shared policies
        for(var i=0; i<m.tls_policies.length; i++) {
            var choice = {};
            choice.value = m.tls_policies[i].id || "";
            choice.label = m.tls_policies[i].name;
            choice.policy_scope = (m.tls_policies[i].private ? "private" : "shared");
            choice.policy_type = m.tls_policies[i].descriptor.policy_type;
            choice.disabled = disabled;
            if( m.configuration["default"] == choice.value ) {
                choice.isDefault = true; //the UI may indicate to the user that this policy is the current server default; user can select this one specifically to retain it even when the default changes
            }
            if( m.configuration["global"] == choice.value ) {
                choice.isGlobal = true;
            }
            // we only add it to the list if the policy type is in the allowed list
            if( m.configuration.allow.indexOf(choice.policy_type) > -1 ) { 
                choices.push(choice);
            }
        }
        // automatically add "INSECURE" and "TRUST_FIRST_CERTIFICATE" to the policy list IF they are allowed
        if( m.configuration.allow.indexOf("INSECURE") > -1 ) {
            choices.push({value:"INSECURE",label:"INSECURE",policy_scope:"shared",policy_type:"INSECURE", disabled:disabled});
        }
        if( m.configuration.allow.indexOf("TRUST_FIRST_CERTIFICATE") > -1 ) {
            choices.push({value:"TRUST_FIRST_CERTIFICATE",label:"TRUST_FIRST_CERTIFICATE",policy_scope:"shared",policy_type:"TRUST_FIRST_CERTIFICATE", disabled:disabled});
        }
        return choices;
    });
    
    // element is an html "<select>" element
    // choicesArray is output of getTlsPolicyChoices
    defineFunction(m, "populateSelectOptionsWithTlsPolicyChoices", function(element,choicesArray) {
        $(element).empty();
        if(choicesArray.length===0) {
            return;
        }
        var globalChoice, defaultChoice;
        var shared = "";
        for(var i=0; i<choicesArray.length; i++) {
            if( choicesArray[i].isDefault ) { defaultChoice = choicesArray[i]; }
            if( choicesArray[i].isGlobal ) { globalChoice = choicesArray[i]; }
            if( choicesArray[i].policy_scope == "shared" ) {
                shared += "<option value='"+choicesArray[i].value+"' "+(choicesArray[i].disabled?"disabled":"")+">"+choicesArray[i].label+"</option>";
            }
        }
        if( shared.length > 0 ) {
            $(element).append("<optgroup label='Shared' data-i18n='[label]tls_policy.option.shared'>"+shared+"</optgroup>"); // data-tls-policy-scope='shared' 
        }
        if( globalChoice ) {
            $(element).append("<optgroup label='Global' data-i18n='[label]tls_policy.option.global'><option value='"+globalChoice.value+"' selected>"+globalChoice.label+"</option></optgroup>"); // data-tls-policy-scope='shared' 
        }
        if( defaultChoice ) {
            $(element).append("<optgroup label='Default' data-i18n='[label]tls_policy.option.default'><option value='"+defaultChoice.value+"' "+(defaultChoice.disabled?"disabled":"selected")+">"+defaultChoice.label+"</option></optgroup>"); // data-tls-policy-scope='shared' 
        }
    });
    defineFunction(m, "insertSelectOptionsWithPerHostTlsPolicyChoices", function(selectElement, options) {
        var perhost = "";
        var disabled = false;
        if( m.configuration["global"] ) {
            disabled = true;
        }
        if( m.configuration.allow ) { 
            if( m.configuration.allow.indexOf("certificate") > -1 ) {
                perhost += "<option value='private-certificate' data-i18n='tls_policy.option.per_host_certificate' data-tls-policy-scope='private' data-tls-policy-type='certificate' "+(disabled?"disabled":"")+">Host certificate</option>";
            }
            if( m.configuration.allow.indexOf("certificate-digest") > -1 ) {
                perhost += "<option value='private-certificate-digest' data-i18n='tls_policy.option.per_host_certificate_digest' data-tls-policy-scope='private' data-tls-policy-type='certificate-digest' "+(disabled?"disabled":"")+">Host certificate fingerprint</option>";
            }
            if( m.configuration.allow.indexOf("public-key") > -1 ) {
                perhost += "<option value='private-public-key' data-i18n='tls_policy.option.per_host_public_key' data-tls-policy-scope='private' data-tls-policy-type='public-key' "+(disabled?"disabled":"")+">Host public key</option>";
            }
            if( m.configuration.allow.indexOf("public-key-digest") > -1 ) {
                perhost += "<option value='private-public-key-digest' data-i18n='tls_policy.option.per_host_public_key_digest' data-tls-policy-scope='private' data-tls-policy-type='public-key-digest' "+(disabled?"disabled":"")+">Host public key fingerprint</option>";
            }
        }
        if( perhost.length ) {
            $(selectElement).prepend("<optgroup label='Per-host' data-i18n='[label]tls_policy.option.private'>"+perhost+"</optgroup>");
        }
        if( options ) {
            if( options.dataInputContainer  ) {
                // dataInputContainer must be an element, probably a div or form, which contains extra inputs for each policy type
                // the container is geared towards input of ONE certificate, public key, or digest value because we are implementing
                // an abbreviated form for a per-host policy. an alternative is to open a complete "edit tls policy" dialog and
                // let the user define or select any policy with that dialog, save that policy to the server and just populate the
                // policy id back to the form.
                var inputContainer = $(options.dataInputContainer);
                // check if we already installed the handler
                if( ! $(selectElement).data("tlspolicy-change-handler") ) {
                    $(selectElement).change(function() {
                        var newValue = $(selectElement).val();
                        var optionElement = $(selectElement).find("option[value='"+newValue+"']").first().get();
                        
                        var scope;
                        if( $(optionElement).attr("data-tls-policy-scope") ) { scope = $(optionElement).attr("data-tls-policy-scope"); }
                        else if( newValue.indexOf("private-") == 0 ) { scope = "private"; } // see perhost option values inserted to the select element, earlier in this function
                        else { scope = "shared"; }
                        
                        var policyType;
                        if( $(optionElement).attr("data-tls-policy-type") ) { policyType = $(optionElement).attr("data-tls-policy-type"); }
                        
                        var tlsPolicyInfo = {
                            "value": newValue,
                            "scope": scope,
                            "policy_type": policyType
                        };
                        
                        if( scope == "private" ) {
                            inputContainer.find("[class^='tlspolicy-input-']").hide(); // hide all policy-type-specific input elements
                            inputContainer.find("[class~='tlspolicy-input-"+policyType+"']").show(); // show only the elements with the selected policy type
                            inputContainer.show();
                        }
                        else {
                            inputContainer.hide();
                        }
                        
                    });
                    $(selectElement).data("tlspolicy-change-handler", true);
                }
            }
        }
        // TODO: we need to use the call back whenuser selects a per-host option because we need the page to display the inputs or existing values...
    });
    defineFunction(m, "selectDefaultTlsPolicyChoice", function(element, defaultValue) {
        // the server default choice will already have the 'selected' attribute so no javascript required for that
        // in this function, we look to see if host.js has set a host-specific tls policy id on the tls_policy_select element
        // (because it may have loaded that info before we populated the select options) and if it's there we select it
        var selectedValue = $(element).prop("data-selected");
        if( selectedValue ) {
            if( $(element).find("option[value='"+selectedValue+"']") ) {
                $(element).val( selectedValue );
            }
        }
        $(element).change();
        /*
        if( defaultValue ) {           
            $(element).val(defaultValue);
        }
        else if( m.configuration["default"] ) {
            $(element).val(m.configuration["default"]);
        }
        else {
            $(element).val('');
        }
        */
    });
    // input.tls_policy_select is the tls_policy_select 
    // input.tls_policy_data_certificate
    // input.tls_policy_data_certificate_digest
    // input.tls_policy_data_public_key
    // input.tls_policy_data_public_key_digest
    // hostDetails is from new RegisterHostVo(); from WhiteListConfig.js or AddHost.js or RegisterHost.js, which has three fields tlsPolicyId, tlsPolicyType, and tlsPolicyData
    defineFunction(m, "copyTlsPolicyChoiceToHostDetailsVO", function(input, hostDetails) {
        var tlsPolicySelect = input.tls_policy_select || "";
        if( tlsPolicySelect == "INSECURE" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "INSECURE";
            hostDetails.tlsPolicyData = null;
        }
        else if( tlsPolicySelect == "TRUST_FIRST_CERTIFICATE" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "TRUST_FIRST_CERTIFICATE";
            hostDetails.tlsPolicyData = null;
        }
        else if( tlsPolicySelect == "private-certificate" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "certificate";
            hostDetails.tlsPolicyData = input.tls_policy_data_certificate;
        }
        else if( tlsPolicySelect == "private-certificate-digest" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "certificate-digest";
            hostDetails.tlsPolicyData = input.tls_policy_data_certificate_digest;
        }
        else if( tlsPolicySelect == "private-public-key" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "public-key";
            hostDetails.tlsPolicyData = input.tls_policy_data_public_key;
        }
        else if( tlsPolicySelect == "private-public-key-digest" ) {
            hostDetails.tlsPolicyId = null;
            hostDetails.tlsPolicyType = "public-key-digest";
            hostDetails.tlsPolicyData = input.tls_policy_data_public_key_digest;
        }
        else {
            // anything else is assumed to be a shared policy id
            hostDetails.tlsPolicyId = tlsPolicySelect;
            hostDetails.tlsPolicyType = null;
            hostDetails.tlsPolicyData = null;
        }
    });
}(jQuery,mtwilsonTlsPolicyModule));

