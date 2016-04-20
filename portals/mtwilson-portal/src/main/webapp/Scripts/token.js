/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

// cross-site request forgery protection
var authorizationToken = null;
function setTokenInAllForms(token) {
    $("form[method=POST]").each(function(){ 
        var form = $(this);
        // check if there is already an AuthorizationToken hidden input if we have a newer token we should replace it
        var inputs = $(form).find("input[type=hidden][name=AuthorizationToken]");
        if( inputs.length ) {
            // form already includes an AuthorizationToken field, so replace its value
            inputs.each(function() {
                var tokenInput = $(this);
                tokenInput.val(token);
            });
        }
        else {
            // form does not already include an AuthorizationToken field, so add it 
            jQuery("<input/>", {name:"AuthorizationToken",type:"hidden",value:token}).appendTo(form);
        }
    });
}
function setTokenInHttpEquiv(token) {
    var meta = $("meta[http-equiv=AuthorizationToken]");
    if( meta.length ) {
        meta.each(function() {
            $(this).attr("value", token);
        });
    }
    else {
        jQuery("<meta/>", { "http-equiv":"AuthorizationToken", value:token }).appendTo($("head"));
    }
}
function getAuthorizationToken() {
    // first look for an authorization token embedded in the current page
    var meta = $("meta[http-equiv=AuthorizationToken]");
    if( meta.length ) {
        authorizationToken = $(meta[0]).attr("value");
        //alert("got token from meta: "+authorizationToken);
        setTokenInAllForms(authorizationToken);
        return;
    }
    // if we didn't find it in the meta tags then make an ajax request to get a new token
    $.ajax({
        type: "GET",
        url: "AuthorizationToken.jsp",
        success: function(data, status, xhr) {
            authorizationToken = xhr.getResponseHeader("AuthorizationToken");
            //alert("got token from ajax: "+authorizationToken);
            setTokenInAllForms(authorizationToken);
            setTokenInHttpEquiv(authorizationToken);
        }
    });
}

$( document ).ready(function() {
    getAuthorizationToken(); // retrieve an authorization token as soon as the document loads so we're ready to provide it on the next form submit or ajax request
});

