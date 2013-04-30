$(function(){ 
    var hostname ="hostName="+$('#hostName').text();
    sendJSONAjaxRequest(false, "/TrustDashBoard/getData/trustVerificationDetailsXML.html", hostname, showXMLData, null);    
});

function showXMLData(response){
    if(response.result){
        var str = response.trustSamlDetails;
        str =str.replace(/\</g, "&lt;");
        str =str.replace(/\>/g, "&gt;");
        str=replceTags(str);
        $('#showXMlData').html('<pre class="prettyprint">'+str+'</pre>');
    }else{
        $('#showXMlData').html(response.message);
    }
    
    prettyPrint();
    
}

function replceTags(value) {
    if ( value == null ) return "";
    value = value.replace(/\r\n/g,"<br>&nbsp;&nbsp;&nbsp;&nbsp;");
    value = value.replace(/\r/g,"<br>&nbsp;&nbsp;");
    value = value.replace(/\n/g,"<br>&nbsp;&nbsp;");
    return value;
}


/**
 * Function to send request to server for getting JSON Data.
 * 
 * @param isGet 
 * @param url
 * @param requestData
 * @param callbackSuccessFunction
 * @param callbackErrorFunction
 */
function sendJSONAjaxRequest(isGet, url, requestData, callbackSuccessFunction, callbackErrorFunction){
	var argLength = arguments.length;
	var requestArgumets = arguments;
	$.ajax({
		type:isGet ? "GET" : "POST",
		url:url,
		data: requestData,
		dataType: "json",
		success: function (responseJSON) {
			if(responseJSON == null){
				fnSessionExpireLoginAgain();
				//alert("Response from Server is null.");
			}else{
				var args = []; 
				args.push(responseJSON);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
				callbackSuccessFunction.apply(null,args);
			}
		},
		error: function(errorMessage){
			if(callbackErrorFunction != null){
				var args = []; 
				args.push(responseJSON);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
			    callbackErrorFunction.apply(null,args);
			}else{
				fnSessionExpireLoginAgain();
				//alert("Error While Serving request. Please try again later.");
			}
		}
	});
}

function formatXml(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>$/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });
    alert(formatted);
    return formatted;
}