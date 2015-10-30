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

function open_in_new_tab(url )
{
  var win=window.open(url, '_blank');
  win.focus();
}

function fnforRootCACertificate(){	
	var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    //alert("baseurl = " + base_url+"");
    //window.open(base_url + "ManagementService/resources/ca/certificate/rootca/current",'_blank');
    //open_in_new_tab(base_url + "mtwilson/v1/ManagementService/resources/ca/certificate/rootca/current/mtwilson-ca.pem");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/root");
	//window.location.replace(base_url + "ManagementService/resources/ca/certificate/rootca/current"); 
	//sendJSONAjaxRequest (false, 'getData/getRootCACertificate.html', null, fnRootCADownloadSuccess, null);
}

function fnforPrivacyCACertificate(){	
  var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    //alert("baseurl = " + base_url+"");
    //window.open(base_url + "ManagementService/resources/ca/certificate/privacyca/current",'_blank');
    //open_in_new_tab(base_url + "mtwilson/v1/ManagementService/resources/ca/certificate/privacyca/current/mtwilson-privacyca.pem");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/privacy");
	//window.location.replace(base_url + "ManagementService/resources/ca/certificate/privacyca/current"); 
  //sendJSONAjaxRequest (false, 'getData/getPrivacyCACertificate.html', null, fnPrivacyCADownloadSuccess, null);
}

function fnforPrivacyCACertificateList(){	
	var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    //alert("baseurl = " + base_url+"");
    //open_in_new_tab(base_url + "mtwilson/v1/ManagementService/resources/ca/certificate/privacyca/current/mtwilson-privacyca.pem");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/privacy");
    // window.open(base_url + "ManagementService/resources/ca/certificate/privacyca/current",'_blank');
	//window.location.replace(base_url + "ManagementService/resources/ca/certificate/privacyca/current"); 
	//sendJSONAjaxRequest (false, 'getData/getPrivacyCACertificateList.html', null, fnPrivacyCAListDownloadSuccess, null);
}



function fnforSAMLCertificate(){	
	var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    //alert("baseurl = " + base_url+"");
    //open_in_new_tab(base_url + "mtwilson/v1/ManagementService/resources/saml/certificate/mtwilson-saml.crt");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/saml");
    
    //window.open(base_url + "ManagementService/resources/saml/certificate",'_blank');
	//window.location.replace(base_url + "ManagementService/resources/saml/certificate");
	//sendJSONAjaxRequest (false, 'getData/getSamlCertificate.html', null, fnRootCADownloadSuccess, null);
}


function fnforTLSCertificate(){	
	var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    //alert("baseurl = " + base_url+"");
    //open_in_new_tab(base_url + "mtwilson/v1/ManagementService/resources/ca/certificate/tls/current/mtwilson-tls.crt");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/tls");
	//window.location.replace(base_url + "ManagementService/resources/ca/certificate/tls/current");
	//sendJSONAjaxRequest (false, 'getData/getTLSCertificate.html', null, fnTLSDownloadSuccess, null);
}

function fnforConfigurationDataBundle() {
	var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/configuration/databundle");    
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
    	        var oWin = window.open("PrivacyCA.list.pem", "_blank");
	            oWin.document.write(responseJSON.Certificate);
	            oWin.document.close();
                    var fileName = "/PrivacyCA.list.pem";
	            var success = oWin.document.execCommand('SaveAs','null',fileName);
	            oWin.close();
	            /* End For IE below code is for saving contents in a file , file name and type needs to be specified by the user  */
           
	            if (!success)
	            {   
	            	/* below code is for saving contents in a file but file name will be the name of the contents itself  , this will work for mozilla and chrome */
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
        alert($("#alert_no_root_ca").text());
	}
}
