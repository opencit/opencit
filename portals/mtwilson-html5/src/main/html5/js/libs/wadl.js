/* 
wadl.js - loads and parses application.wadl files for use with mtwilson api

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

*/

// requires prototype.js, wgxpath.install.js, and log.js

var WADL = {};

(function (WADL, undefined) { // start module definition

    // base url according to browser
    var uri = new URI(document.location.toString());
    
    var event = {
        fire: function(eventName, eventInfo) {
            if( Prototype && Event ) {
                document.fire("wadl:"+eventName, eventInfo);
            }
        }
    };
    
    Log.debug("initialized WADL with uri "+uri);
    
    WADL.loadWADL = function(url) {
        if( !url ) {
            url = "application.wadl";
        }
        var request = new Ajax.Request(url, {
            method: 'get',
            onSuccess: function (transport) {
                if( transport.responseXML ) {
                    Log.debug("Got WADL from "+url+":\n"+transport.responseXML);
                    WADL.processWADL(transport.responseXML);
                }
                else {
                    Log.debug("Did not get XML:\n"+transport.responseText);
                }
            }
        });
        
    };
    
    var default_xmlns; // will be initialized by processWADL, typically to "http://wadl.dev.java.net/2009/02" for jersey-generated application.wadl
    var resolver = function() {
        return default_xmlns;
    };
    
    // Automatically installs the wgxpath implementation of document.evaluate in 
    // browsers that don't have it.
    // Firefox, Chrome, Safari, Opera are known to implement document.evaluate
    // Internet Explorer does not
    // 
    var xpath = function(xpathexpr, xmldoc) {
        if(!document.evaluate) { wgxpath.install(); }
        var result = document.evaluate(xpathexpr, xmldoc, resolver, XPathResult.ANY_TYPE, null);
        // type constants: NUMBER_TYPE, STRING_TYPE, BOOLEAN_TYPE, ANY_TYPE, UNORDERED_NODE_ITERATOR_TYPE, ref: https://developer.mozilla.org/en-US/docs/Introduction_to_using_XPath_in_JavaScript#XPathResult_Defined_Constants
        switch(result.resultType) {
            case XPathResult.NUMBER_TYPE:
                return result.numberValue;
            case XPathResult.STRING_TYPE:
                return result.stringValue;
            case XPathResult.BOOLEAN_TYPE:
                return result.booleanValue;
            case XPathResult.UNORDERED_NODE_ITERATOR_TYPE:
            case XPathResult.ORDERED_NODE_ITERATOR_TYPE:
            case XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:
            case XPathResult.ORDERED_NODE_SNAPSHOT_TYPE:
                var array = [];                    
                for(var item = result.iterateNext(); item; item = result.iterateNext()) {
                    array.push(item);
                }
                return array;
            case XPathResult.ANY_UNORDERED_NODE_TYPE:
            case XPathResult.FIRST_ORDERED_NODE_TYPE:
                var item = result.singleNodeValue;
                return item;
            default:
                Log.error("Unsupported XPathResult type "+result.resultType);
        }
    };
    
    /**
     * Same as xpath but always returns a single value. If the result from
     * xpath is a number, string, boolean, or single element it is returned.
     * If the result from xpath is an array then only the first element is
     * returned. If the result from xpath is an array but it is empty then
     * null is returned.
     * @param {type} xpathexpr
     * @param {type} xmldoc
     * @returns one number, string, boolean, element, or null
     */
    var xpath1 = function(xpathexpr, xmldoc) {
        var result = xpath(xpathexpr, xmldoc);
        if( result instanceof Array ) {
            if( result.length ) {
                return result[0];
            }
            return null;
        }
        return result;
    };
    
    /**
     * <application xmlns="http://wadl.dev.java.net/2009/02">
     * <resources base="http://localhost:8080/v2/">
     * ...
     * </resources>
     * </application>
     * 
     * @param {type} xmldoc
     */
    WADL.processWADL = function(xmldoc) {
//        Log.debug("root element name: "+xmldoc.nodeName); // "#document"
//        Log.debug("first child element name: "+xmldoc.firstChild.nodeName); // "application"
//        Log.debug("xmlns = "+xmldoc.firstChild.getAttribute("xmlns")); // xmlns="http://wadl.dev.java.net/2009/02"
        default_xmlns = xmldoc.firstChild.getAttribute("xmlns");
        
        var baseurl = xpath("string(//wadl:resources[1]/@base)", xmldoc.firstChild); 
//        Log.debug("baseurL: "+baseurl);
        
        var resourceElementArray = xpath("//wadl:resources[1]/wadl:resource[@path]", xmldoc.firstChild);
//        Log.debug("resources; "+resourceElements);
        var resources = WADL.processResources(resourceElementArray, baseurl);
        //Log.debug("Processed resources: "+Object.toJSON(resources));
        event.fire("ready", { resources:resources });
    };
    
    /**
     * 
     * @param {type} resourceElementArray of XML elements representing resource tags like <resource path="/files">...</resource>
     * @param {type} parentPath
     */
    WADL.processResources = function(resourceElementArray, parentPath) {
        var resources = [];
        for(var i=0; i<resourceElementArray.length; i++) {
            var resource = WADL.processResource(resourceElementArray[i], parentPath);
            resources.push(resource);
        }
        return resources;
    };
    
    /**
     * <resource path="/files">
     * ...
     * </resource>
     * 
     * @param {type} resourceElement
     * @param {type} parentPath
     */
    WADL.processResource = function(resourceElement, parentPath) {
        var relativePath = resourceElement.getAttribute("path");
        var path = parentPath+relativePath; // for example, http://localhost:8080/v2 + /mle-modules  
//        Log.debug("resource: "+path); 
        // look for url parameters
        var params = WADL.processResourceParams(resourceElement);
         // look for methods  GET, POST, etc.
        var methods = WADL.processResourceMethods(resourceElement);

        // a resource could have nested resources
        var childResourceElements = xpath("wadl:resource[@path]", resourceElement);
        var subresources = WADL.processResources(childResourceElements, path); // if there are no child resources the array will be empty [] so this is safe
        
        var resource = {
            path: path,
            relativePath: relativePath,
            params: params,
            methods: methods,
            resources: subresources
        };
        
        return resource;
    };
    
    /**
     * <resource path="/{id}">
     * <param xmlns:xs="http://www.w3.org/2001/XMLSchema" name="id" style="template" type="xs:string"/>
     * ...
     * </resource>
     * 
     * @param {type} resourceElement
     */
    WADL.processResourceParams = function(resourceElement) {
        var paramElements = xpath("wadl:param", resourceElement);
        var params = [];
        for(var i=0; i<paramElements.length; i++) {
            var paramElement = paramElements[i];
//            Log.debug("Param name: "+paramElement.getAttribute("name")+" style: "+paramElement.getAttribute("style")+"  type: "+paramElement.getAttribute("type"));
            var param = {
                name: paramElement.getAttribute("name"),
                style: paramElement.getAttribute("style"),
                type: paramElement.getAttribute("type")
            };
            params.push(param);
        }
        return params;
    };
    
    /**
     * <resource path="/files">
     * <method id="searchCollection" name="GET">...</method>
     * <method id="createOne" name="POST">...</method>
     * ...
     * </resource>
     * @param {type} resourceElement
     */
    WADL.processResourceMethods = function(resourceElement) {
        var methodElements = xpath("wadl:method", resourceElement);
        var methods = [];
        for(var i=0; i<methodElements.length; i++) {
            var method = WADL.processMethod(methodElements[i]);
            methods.push(method);
        }
        return methods;
    };
    
    /**
     * <method id="createOne" name="POST">
     * <request>
     * <representation mediaType="application/json"/>
     * <representation mediaType="application/xml"/>
     * </request>
     * <response>
     * <representation mediaType="application/json"/>
     * <representation mediaType="application/xml"/>
     * </response>
     * 
     * or
     * 
     * <method id="searchCollection" name="GET">
     * <request>
     * <param xmlns:xs="http://www.w3.org/2001/XMLSchema" type="xs:string"/>
     * </request>
     * <response>
     * <representation mediaType="application/json"/>
     * <representation mediaType="application/xml"/>
     * </response>
     * 
     * @param {type} methodElement
     */
    WADL.processMethod = function(methodElement) {
//        Log.debug("Method name: "+methodElement.getAttribute("name")+" id: "+methodElement.getAttribute("id"));
        var method = {
            httpMethod: methodElement.getAttribute("name"), // like GET, POST, PUT, DELETE
            name: methodElement.getAttribute("id"), // the name of the method on the server like getWadl, createOne, createCollection, etc.
            request: { params:false, representations:[] },
            response: { representations:[] }
        };
        var requestElement = xpath1("wadl:request[1]", methodElement);
        if( requestElement) {
            // does it take a query string?
            var paramElements = xpath("wadl:param[@type]", requestElement);
            for(var i=0; i<paramElements.length; i++) {
//                Log.debug("Request param: "+paramElements[i].getAttribute("type")); // tends to be something useless like "xs:string" which doesn't describe the real available query parameters
                method.request.params = true;
            }
            // does it take a request body of a certain content type? (could be multiple choices)
            var representationElements = xpath("wadl:representation[@mediaType]", requestElement);
            for(var i=0; i<representationElements.length; i++) {
//                Log.debug("Request representation: "+representationElements[i].getAttribute("mediaType")); 
                method.request.representations.push(representationElements[i].getAttribute("mediaType")); // like  application/json  
            }
        }
        var responseElement = xpath1("wadl:response[1]", methodElement);
        if( responseElement ) {
            // what are the response content types? (could be multiple choices)
            var representationElements = xpath("wadl:representation[@mediaType]", responseElement);
            for(var i=0; i<representationElements.length; i++) {
//                Log.debug("Request representation: "+representationElements[i].getAttribute("mediaType"));
                method.response.representations.push(representationElements[i].getAttribute("mediaType"));
            }
        }
        return method;
    };
    
})(WADL);  // end module definition

/*
document.observe("dom:loaded", function() {
   WADL.initialize();
});
*/