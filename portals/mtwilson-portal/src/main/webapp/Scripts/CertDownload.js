//Check for the existance of the Root CA Cert. If not exists, then hide link. 
sendJSONAjaxRequest (false, 'getData/getRootCACertificate.html', null, fnLookForRootCASuccess, null);    

function fnLookForRootCASuccess(responseJSON){
    if (responseJSON.result)
    { 
        $('#fdownloadRCA').show();
	
    } else {
         
        $('#fdownloadRCA').hide();
    }
}

function fnforRootCACertificate(){	
	
	sendJSONAjaxRequest (false, 'getData/getRootCACertificate.html', null, fnRootCADownloadSuccess, null);
}

function fnforPrivacyCACertificate(){	
	
	sendJSONAjaxRequest (false, 'getData/getPrivacyCACertificate.html', null, fnPrivacyCADownloadSuccess, null);
}

function fnforPrivacyCACertificateList(){	
	
	sendJSONAjaxRequest (false, 'getData/getPrivacyCACertificateList.html', null, fnRootCAListDownloadSuccess, null);
}



function fnforSAMLCertificate(){	
	
	sendJSONAjaxRequest (false, 'getData/getSamlCertificate.html', null, fnRootCADownloadSuccess, null);
}


function fnforTLSCertificate(){	
	
	sendJSONAjaxRequest (false, 'getData/getTLSCertificate.html', null, fnRootCADownloadSuccess, null);
}

function fnTLSDownloadSuccess(responseJSON){
	if (responseJSON.result)
	{ //alert (responseJSON.SAMLcertificate);
            if (document.execCommand) {
            /* Start For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
	          //  var oWin = window.open("about:blank", "_blank");
    	        var oWin = window.open("ssl.crt.pem", "_blank");
	            oWin.document.write(responseJSON.Certificate);
	            oWin.document.close();
                    var fileName = "/ssl.crt.pem";
	            var success = oWin.document.execCommand('SaveAs','null',fileName);
	            oWin.close();
	            /* End For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
           
	            if (!success)
	            {   
	            	/* below code is for saving contents in a file but file name will be the name of the contents itself  , this will work for mozilla and chrome */
                        // TODO: we need to handle this on the server side for the next release
                        // http://stackoverflow.com/questions/8075044/save-as-dialog-box-in-firefox
                        document.location =  "data:application/octet-stream," +
                        encodeURIComponent(responseJSON.Certificate);
                    }
                        /* start below code is for saving contents in a file , this will work for mozilla and chrome */
                        /*  downloadDataURI({
                            filename: "mtwilson-saml.crt", 
                            data: "data:application/octet-stream," + encodeURI(content)
                        });*/
                        /* end below code is for saving contents in a file , this will work for mozilla and chrome */
             }
	
	
    } else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}


function fnPrivacyCAListDownloadSuccess(responseJSON){
	if (responseJSON.result)
	{ //alert (responseJSON.SAMLcertificate);
            if (document.execCommand) {
            /* Start For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
	          //  var oWin = window.open("about:blank", "_blank");
    	        var oWin = window.open("PrivacyCA.p12.pem", "_blank");
	            oWin.document.write(responseJSON.Certificate);
	            oWin.document.close();
                    var fileName = "/PrivacyCA.p12.pem";
	            var success = oWin.document.execCommand('SaveAs','null',fileName);
	            oWin.close();
	            /* End For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
           
	            if (!success)
	            {   
	            	/* below code is for saving contents in a file but file name will be the name of the contents itself  , this will work for mozilla and chrome */
                        // TODO: we need to handle this on the server side for the next release
                        // http://stackoverflow.com/questions/8075044/save-as-dialog-box-in-firefox
                        document.location =  "data:application/octet-stream," +
                        encodeURIComponent(responseJSON.Certificate);
                    }
                        /* start below code is for saving contents in a file , this will work for mozilla and chrome */
                        /*  downloadDataURI({
                            filename: "mtwilson-saml.crt", 
                            data: "data:application/octet-stream," + encodeURI(content)
                        });*/
                        /* end below code is for saving contents in a file , this will work for mozilla and chrome */
             }
	
	
    } else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}

function fnPrivacyCADownloadSuccess(responseJSON){
	if (responseJSON.result)
	{ //alert (responseJSON.SAMLcertificate);
            if (document.execCommand) {
            /* Start For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
	          //  var oWin = window.open("about:blank", "_blank");
    	        var oWin = window.open("PrivacyCA.pem", "_blank");
	            oWin.document.write(responseJSON.Certificate);
	            oWin.document.close();
                    var fileName = "/PrivacyCA.pem";
	            var success = oWin.document.execCommand('SaveAs','null',fileName);
	            oWin.close();
	            /* End For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
           
	            if (!success)
	            {   
	            	/* below code is for saving contents in a file but file name will be the name of the contents itself  , this will work for mozilla and chrome */
                        // TODO: we need to handle this on the server side for the next release
                        // http://stackoverflow.com/questions/8075044/save-as-dialog-box-in-firefox
                        document.location =  "data:application/octet-stream," +
                        encodeURIComponent(responseJSON.Certificate);
                    }
                        /* start below code is for saving contents in a file , this will work for mozilla and chrome */
                        /*  downloadDataURI({
                            filename: "mtwilson-saml.crt", 
                            data: "data:application/octet-stream," + encodeURI(content)
                        });*/
                        /* end below code is for saving contents in a file , this will work for mozilla and chrome */
             }
	
	
    } else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}

function fnRootCADownloadSuccess(responseJSON){
	if (responseJSON.result)
	{ //alert (responseJSON.SAMLcertificate);
            if (document.execCommand) {
            /* Start For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
	          //  var oWin = window.open("about:blank", "_blank");
    	        var oWin = window.open("MtWilsonRootCA.crt.pem", "_blank");
	            oWin.document.write(responseJSON.Certificate);
	            oWin.document.close();
                    var fileName = "/MtWilsonRootCA.crt.pem";
	            var success = oWin.document.execCommand('SaveAs','null',fileName);
	            oWin.close();
	            /* End For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
           
	            if (!success)
	            {   
	            	/* below code is for saving contents in a file but file name will be the name of the contents itself  , this will work for mozilla and chrome */
                        // TODO: we need to handle this on the server side for the next release
                        // http://stackoverflow.com/questions/8075044/save-as-dialog-box-in-firefox
                        document.location =  "data:application/octet-stream," +
                        encodeURIComponent(responseJSON.Certificate);
                    }
                        /* start below code is for saving contents in a file , this will work for mozilla and chrome */
                        /*  downloadDataURI({
                            filename: "mtwilson-saml.crt", 
                            data: "data:application/octet-stream," + encodeURI(content)
                        });*/
                        /* end below code is for saving contents in a file , this will work for mozilla and chrome */
             }
	
	
    } else {
        alert("This system is not currently configured with a Root CA");
	}
}
