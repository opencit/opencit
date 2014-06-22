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
        // first, server global if it's defined
        var disabled = false;
        if( m.configuration["global"] ) {
            choices.push({label:"Required policy (global)",value:null});
            disabled = true;
        }
        // next, server default if it's available
        if( m.configuration["default"] ) {
            choices.push({label:"",value:null, disabled:disabled});
        }
        // next, any shared policies
        for(var i=0; i<m.tls_policies.length; i++) {
            var id = m.tls_policies[i].id;
            var name = m.tls_policies[i].name;
            if( m.configuration["default"] == id ) {
                name += " (server default)";
            }
            if( m.configuration["global"] == id ) {
                name += " (global)";
            }
            choices.push({label:name, value:id, disabled:disabled});
        }
        return choices;
    });
    
    // element is an html "<select>" element
    // choicesArray is output of getTlsPolicyChoices
    defineFunction(m, "populateSelectOptionsWithTlsPolicyChoices", function(element,choicesArray) {
        $(element).empty();
        if(choicesArray.length===0) {
            $(element).append("<option value='' disabled>None available</option>");
        }
        for(var i=0; i<choicesArray.length; i++) {
            $(element).append("<option value='"+choicesArray[i].value+"' "+(choicesArray[i].disabled?"disabled":"")+">"+choicesArray[i].label+"</option>");
        }
    });
    
}(jQuery,mtwilsonTlsPolicyModule));

