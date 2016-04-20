/* 
handlebars.util.js - utility functions for the Handlebars templating library 

Copyright (c) 2014, Intel Corporation
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
Requires: handlebars.js , prototype.js 1.6.1 or later, log.js

 */

var HandlebarUtil = {};

(function (util, undefined) { // start module definition

    util.cache = {};

    util.get = function(templateId) {
        if( !util.cache[templateId] ) {
            var src = $(templateId).innerHTML;
            util.cache[templateId] = Handlebars.compile(src);
        }
        return util.cache[templateId];        
    };
    
    /**
     * Replaces the contents of element elementId with the
     * result of template templateId evaluated with dataObject.
     * 
     * @param {type} elementId
     * @param {type} templateId
     * @param {type} dataObject
     */
    util.replace = function(elementId, templateId, dataObject) {
        var template = util.get(templateId);
        var content = template(dataObject); 
        $(elementId).childElements().invoke('remove'); // clear any previous contents
        $(elementId).insert({bottom: content});
    };

    /**
     * Automatically compiles templates that are embedded in the HTML page
     * using script tags like this: 
     * <script id="resource-views-template" type="text/x-handlebars-template">
     * ...
     * </script>
     */
    util.compileTemplates = function() {
        var sourceElements = $$("script[type='text/x-handlebars-template']");
        for(var i=0; i<sourceElements.length; i++) {
            var sourceElement = sourceElements[i];
//            var templateName = sourceElement.id;
            var src = sourceElement.innerHTML; // $('wadl-resource-template').innerHTML;
//            var template = Handlebars.compile(src);
            util.cache[sourceElement.id] = Handlebars.compile(src);
        }
    };
    
    
    /*
     * Support for the flowing style. Example:
     * 
     * Replace contents of <div id="elementId"></div> with the template
     * <script id="templateId" type="text/x-handlebars-template">...</script>
     * evaluated using dataObject  { sampleArray: [1,2,3], sampleName: "sparky" }
     * 
     * util.template(templateId).data(dataObject).replace(elementId);
     * 
     * Get just the evaluated template and do whatever you want with it:
     * 
     * var content = util.template(templateId).data(dataObject).content;
     * 
     */
    util.template = function(templateId) {
        var t = util.get(templateId);
        return {
            data: function(dataObject) {
                var content = t(dataObject); 
                return {
                    content: content,
                    replace: function(elementId) {
                        $(elementId).childElements().invoke('remove'); // clear any previous contents
                        $(elementId).insert({bottom: content});
                    }
                };
            }
        };
    };
    
    /*
                var src = $('wadl-resource-template').innerHTML;
                var template = Handlebars.compile(src);
                var wadl = template(event.memo); // looks like { resources: [ ... ] }
                $('wadl-resources').childElements().invoke('remove'); // clear any previous entries
                $('wadl-resources').insert({bottom: wadl});
     * 
     */

})(HandlebarUtil);  // end module definition


document.observe("dom:loaded", function() {
   HandlebarUtil.compileTemplates();
});
