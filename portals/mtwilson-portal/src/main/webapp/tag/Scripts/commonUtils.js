var disabledDiv = '<div id="disabledDiv" class="disabledDiv"><img class="loadingImageClass" src="images/loading.gif" /></div>';
var validationDiv = '<div class="errorMessage validationErrorDiv"> Value cannot be empty.</div>';
var validationSpecialDiv = '<div class="errorMessage validationErrorDiv"> Special characters are not allowed.</div>';
var validationSpan = '<span class="errorMessage validationErrorDiv" style="float:none">Value cannot be empty.</span>';
var validationSpecialSpan = '<span class="errorMessage validationErrorDiv" style="float:none">Special characters are not allowed.</span>';
var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);
var manifestReg = new RegExp(/^[a-fA-F0-9]+$/);
var selectedPageNo = 1;
var moduleAttestationDisplayString = 'PCR + Module';
var regIPAddress = new RegExp(/^[0-9_.]+$/);
var regPortNo = new RegExp(/^[0-9]+$/);
// 0 = TA,  1 == vmware, 2 == citrix
var isVMWare = 0;


// Validation functions
// stdalex_ validate ip address function

function fnMWValidateIpAddressOrHostName(elementID, isMandatory) {
    var checkIp = /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
    var checkHostname = /^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\.?$/;
    $('#'+elementID).parent().find('.errorMessage').remove();
    if ((isMandatory == true) && ($('#'+elementID).val() == '')) {
        $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value cannot be empty.</span>');
        return false;
    } else if ((isMandatory == false) && ($('#'+elementID).val() == '')) {
        // Since the value is not mandatory and the user has not specified anything, then lets return back zero
        return true;
    } else {
        // Since the user has specified a value, irrespective of whether it is mandatory or not, let us validate it.
        if(checkIp.test($('#'+elementID).val()) || checkHostname.test($('#'+elementID).val())) {
            return true;
        } else {
            $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value specified is not valid.</span>');
            return false;
        }
    }
}

function fnMWValidateIpAddress(elementID, isMandatory) {
    var checkIp = /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
    $('#'+elementID).parent().find('.errorMessage').remove();
    if ((isMandatory == true) && ($('#'+elementID).val() == '')) {
        $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value cannot be empty.</span>');
        return false;
    } else if ((isMandatory == false) && ($('#'+elementID).val() == '')) {
        // Since the value is not mandatory and the user has not specified anything, then lets return back zero
        return true;
    } else {
        // Since the user has specified a value, irrespective of whether it is mandatory or not, let us validate it.
        if(checkIp.test($('#'+elementID).val())) {
            return true;
        } else {
            $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value specified is not valid.</span>');
            return false;
        }
    }
}

function fnMWValidatePort(elementID, isMandatory) {
    var checkPort = /^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$/;
    $('#'+elementID).parent().find('.errorMessage').remove();
    if ((isMandatory == true) && ($('#'+elementID).val() == '')) {
        $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value cannot be empty.</span>');
        return false;
    } else if ((isMandatory == false) && ($('#'+elementID).val() == '')) {
        // Since the value is not mandatory and the user has not specified anything, then lets return back zero
        return true;
    } else {
        // Since the user has specified a value, irrespective of whether it is mandatory or not, let us validate it.
        if(checkPort.test($('#'+elementID).val())) {
            return true;
        } else {
            $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value specified is not valid. Please specify a value between 0 and 65535.</span>');
            return false;
        }
    }
}

// This function is basically for validating null or empty fields. There are no restrictions on the contents.
function fnMWValidateField(elementID, isMandatory) {
    $('#'+elementID).parent().find('.errorMessage').remove();
    if ((isMandatory == true) && ($('#'+elementID).val() == '')) {
        $('#'+elementID).parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Value cannot be empty.</span>');
        return false;
    } else if ((isMandatory == false) && ($('#'+elementID).val() == '')) {
        // Since the value is not mandatory and the user has not specified anything, then lets return back zero
        return true;
    } 
}

var JSON = JSON || {};
JSON.stringify = JSON.stringify || function (obj) {
	var t = typeof (obj);
	if (t != "object" || obj === null) {
		// simple data type
		if (t == "string") obj = '"'+obj+'"';
		return String(obj);
	}
	else {
		// recurse array or object
		var n, v, json = [], arr = (obj && obj.constructor == Array);
		for (n in obj) {
			v = obj[n]; t = typeof(v);
			if (t == "string") v = '"'+v+'"';
			else if (t == "object" && v !== null) v = JSON.stringify(v);
			json.push((arr ? "" : '"' + n + '":') + String(v));
		}
		return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
	}
};


/**
 * Method to create a menu using jquery Menubar plugin using file jquery.ui.menubar.js and jquery.ui.menu.js
 * For more clarification, check http://wiki.jqueryui.com/w/page/38666403/Menubar
 * 
 * @param divID (id of UL which contains all menu item in structural way.)
 */
function createMenubar(divID) {
	$("#"+divID).menubar({
		autoExpand: true,
		menuIcon: true,
		buttons: true
	});
}

var selectedPageNo = 1;


/**
 * Function to send request to server for getting JSON Data.
 * 
 * @param isGet (pass true For GET Request for POST pass false) 
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
		success: function (responseJSON,code,jqXHR) {
			//check for page type, if page is login page then show a pop-up for session expire.
			if (jqXHR.getResponseHeader("loginPage") != null && jqXHR.getResponseHeader("loginPage") == "true") {
				fnSessionExpireLoginAgain();
				return false;
			}
			if(responseJSON == null){
				//alert("Response from Server is null.");
				fnSessionExpireLoginAgain();
			}else{
				var args = []; 
				args.push(responseJSON);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
			    //Call to success function by passing response and other extra parameter.
				callbackSuccessFunction.apply(null,args);
			}
		},
		error: function(errorMessage){
                        //alert(JSON.stringify(errorMessage));
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

/**
 * Function to send request to server for getting HTML Data.
 * 
 * @param isGet (pass true For GET Request for POST pass false)
 * @param url
 * @param requestData
 * @param callbackSuccessFunction
 * @param callbackErrorFunction
 */
function sendHTMLAjaxRequest(isGet, url, requestData, callbackSuccessFunction, callbackErrorFunction){
	var argLength = arguments.length;
	var requestArgumets = arguments;
	$.ajax({
		type:isGet ? "GET" : "POST",
		url:url,
		data: requestData,
		dataType: "html",
		success: function (response,code,jqXHR) {
			//check for page type, if page is login page then show a pop-up for session expire.
			if (jqXHR.getResponseHeader("loginPage") != null && jqXHR.getResponseHeader("loginPage") == "true") {
				fnSessionExpireLoginAgain();
				return false;
			}
			if(response != ""){
				var args = []; 
				args.push(response);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
				callbackSuccessFunction.apply(null,args);
			}else{
				alert("Response from Server is null.");
				fnSessionExpireLoginAgain();
			}
		},
		error: function(errorMessage){
                        //alert(JSON.stringify(errorMessage));
			if(callbackErrorFunction != null){
				var args = []; 
				args.push(responseJSON);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
			    callbackErrorFunction.apply(null,args);
			}else{
				alert("Error While Serving request. Please try again later.");
				fnSessionExpireLoginAgain();
			}
		}
	});
}


/**
 * Function to show loading Image while request is processing.
 * 
 * @param panel
 * @param topPosition
 * @param leftPosition
 */
function setLoadImage(panel, topPosition, leftPosition) {
    $('#'+panel).append('<div style="padding:50px">'+disabledDiv+'</div>');
}

//Function to clear all pre-define data in inputs of JSP.
function clearAllFiled(divID) {
	$('#'+divID).find('input:text').each(function() {
		if ($(this).attr('disabled') != 'disabled') {
			$(this).val('');
		}
	});
	$('#'+divID).find('textarea').each(function() {
		if ($(this).attr('disabled') != 'disabled') {
			$(this).val('');
		}
	});
	$('#'+divID).find('input:password').each(function() {
		if ($(this).attr('disabled') != 'disabled') {
			$(this).val('');
		}
	});
}


function fnTestValidation(inputID,regExpresion) {
	$('#'+inputID).parent().find('.errorMessage').remove();
	if ($('#'+inputID).val() == '') {
		$('#'+inputID).parent().append(validationDiv);
		return false;
	}else {
            if (!regExpresion.test($('#'+inputID).val())) {
			$('#'+inputID).parent().append(validationSpecialDiv);
			return false;
		}
	}
	return true;
}

/**
 * This method is used to open custom dialog box with specific Title and Content.
 * Custom dialog is created by calling dialog() function present in jquery.ui.dialog.js
 * for more information please visit http://docs.jquery.com/UI/Dialog
 * 
 * @param content (Message shown inside body of Dialog Box)
 * @param title (Title Message)
 * @param width (Width of dialog box)
 * @param height
 * @param removeOkButton (if true, navigate user to Login Page)
 */
function fnOpenDialog(content,title,width,height,removeOkButton) {
	$('#dialog').remove();
	var str = '<div id="dialog">'+content+'</div>';
	$('#mainContainer').append(str);
		$('#dialog').dialog({
			autoOpen: true,
			width: width,
			height: height,
			 modal: true,
			 title: title,
			dialogClass: "dialogClass",
			buttons: {
                "Ok": function() { 
                    if(removeOkButton){
                        window.location.href = "login.htm";
                    }else{
                        $(this).dialog("close"); 
                    }
                 }
			}
		});
}

//Function to show dialog box to show message to user if session is expired or request return Login Page.
function fnSessionExpireLoginAgain() {
	var str = 'Your Login Session is Expired. <a href="login.htm">Click here</a> to Login again.';
	fnOpenDialog(str, "Error", 300, 150,true);
}

function fnChangehostType(element,isWhiteListConfigPage) {
    //alert($('#MainContent_ddlHOSTType').attr('value') );
	var type = "";
    // This code loops through all the options in the host type
    // drop down box and finds the one thats selected and saves
    // its name in type variable
    // choices are VMware, Citrix, KVM|Xen
    cleanUpAllDivs();
	$(element).find('option').each(function() {
		if ($(this).attr('selected') == 'selected') {
			type = $(this).attr('value');
		}
	});

	if (type.indexOf("VMware") != -1) {       
		isVMWare = 1;
        
		$('#openSourcesHostType').hide();
        $('#citrixHostType').hide();
		$('#vmwareHostType').show();
	}else if(type.indexOf("Citrix") != -1) {
        isVMWare = 2;
       
        $('#openSourcesHostType').hide();
        $('#vmwareHostType').hide();
		$('#citrixHostType').show();
        
	}else{
        isVMWare= 0;
        
        $('#vmwareHostType').hide();
        $('#citrixHostType').hide();
		$('#openSourcesHostType').show();
		
    }
	
	if (isWhiteListConfigPage == true) {
		$('#whiteListMessage').html('');
		fnClearAllConfigFiled();
		changeRequiredPCR();
		fnDisableOrEnableUploadButton(false);
	}
}

//function to test Validation for given input ID for empty values.
//function will return true if ever thing is fine.
function fnValidateEmptyValue(elementID) {
	$('#'+elementID).parent().find('span').html('*');
	if ($.trim($('#'+elementID).val()) == "") {
		$('#'+elementID).parent().find('span').html(validationDiv);
		return false;
	}
	return true;
}

//Function to remove all error/success message from screen.
function cleanUpAllDivs() {
	$('#registerHostTableContent').html('');
	$('#registerHostTable').hide();
	$('.successMessage').remove();
	$('.errorMessage').remove();
}


/*Class for register host vo ..... used while sending data to server*/
function RegisterHostVo(hostType,hostName,hostPortNo,vCenterString,vmWareType,status,biosWLTarget,vmmWLtarget,registered) {
	this.hostType = hostType;
	this.hostName = hostName;
	this.hostPortNo = hostPortNo;
	this.vCenterString = vCenterString;
	this.vmWareType = vmWareType;
	this.status = status;
	this.biosWLTarget = biosWLTarget;
	this.vmmWLtarget = vmmWLtarget;
	this.registered = registered;
}

function RegistrationDetailsVo(name,fingerprint,requestedRoles,expires,comments) {
	this.name = name;
	this.fingerprint = fingerprint;
	this.requestedRoles = [];
	this.expires = expires;
	this.comments = comments;
}

function fnWhiteListConfig(biosWhiteList,vmmWhiteList,biosWLTarget,vmmWLTarget,biosPCRs,vmmPCRs,hostLocation,registerHost,overWriteWhiteList) {
	this.biosWhiteList = biosWhiteList;
	this.vmmWhiteList = vmmWhiteList;
	this.biosWLTarget = biosWLTarget;
	this.vmmWLTarget = vmmWLTarget;
	this.biosPCRs = biosPCRs;
	this.vmmPCRs = vmmPCRs;
	this.hostLocation = hostLocation;
	this.registerHost = registerHost;
        this.overWriteWhiteList = overWriteWhiteList;
}


/**
 * This will send a AJAX request to Server in Synchronous Mode.
 * this function is used in register host page, while click on register user button. 
 * 
 * @param isGet
 * @param url
 * @param requestData
 * @param callbackSuccessFunction
 * @param callbackErrorFunction
 */
function sendSynchronousAjaxRequest(isGet, url, requestData, callbackSuccessFunction, callbackErrorFunction){
	var argLength = arguments.length;
	var requestArgumets = arguments;
	$.ajax({
		type:isGet ? "GET" : "POST",
		url:url,
                async:false,
		data: requestData,
		dataType: "json",
		success: function (responseJSON) {
			if(responseJSON == null){
				fnSessionExpireLoginAgain(); 
			}else{
				var args = []; 
				args.push(responseJSON);
				
			    for(var i = 5; i < argLength; i++){
			        args.push(requestArgumets[i]);
			    }
			    //Call to success function by passing response and other extra parameter.
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

//function to change give date into a format "MM/DD/YYYY hh:mm:sec".  
function fnGetFormatedDate(dateObject) {
	var date = new Date(dateObject);
	var str = "";
	var month = date.getMonth()+1;
	month = month.toString().length == 1 ? "0"+month : month;
	var day = date.getDate().toString().length == 1 ? "0"+date.getDate() : date.getDate();
	var hr = date.getHours().toString().length == 1 ? "0"+date.getHours() : date.getHours();
	var min = date.getMinutes().toString().length == 1 ? "0"+date.getMinutes() : date.getMinutes();
	var sec = date.getSeconds().toString().length == 1 ? "0"+date.getSeconds() : date.getSeconds();
	str+=month+"/"+day+"/"+date.getFullYear()+" "+hr+":"+min+":"+sec;
	return str;
}

//function to create vCenterString using IPAdress and port no provided. 
function getVCenterServerAddress(inputID) {
	var preFix = "https://";
	var sufix="/sdk";
	var port = "443";
	var server = $('#'+inputID).val();
	if (server.indexOf(":") > 0) {
		return preFix+server+sufix;
	}else {
		return preFix+server+":"+port+sufix;
	}
}

//This function will create a custom dialog box without any Buttons.
//for more information check fnOpenDialog() function in same file.
function fnOpenDialogWithOutOKButtom(content,title,width,height) {
	$('#WithOutOKButtom').remove();
	var str = '<div id="WithOutOKButtom">'+content+'</div>';
	$('#mainContainer').append(str);
		$('#WithOutOKButtom').dialog({
			autoOpen: true,
			width: width,
			height: height,
			 modal: true,
			 title: title,
			dialogClass: "dialogClass"
		});
}


//function to show confirm dialog box with YES and NO button
function fnOpenDialogWithYesNOButton(content,title,width,height,functionForYES,functionForNO) {
	$('#dialog').remove();
	var str = '<div id="dialog">'+content+'</div>';
	var args = [];
	var argLength = arguments.length;
	var requestArgumets = arguments;
    for(var i = 6; i < argLength; i++){
        args.push(requestArgumets[i]);
    }
	$('#mainContainer').append(str);
		$('#dialog').dialog({
			autoOpen: true,
			width: width,
			height: height,
			 modal: true,
			 title: title,
			dialogClass: "dialogClass",
			buttons: {
                 "No": function() { 
                    functionForNO.apply(null,args);
                    $(this).dialog("close");
                 },
				"Yes": function() { 
					functionForYES.apply(null,args);
					 $(this).dialog("close");
				}
			}
		});
}
// Soni_for Validating comments in approving requests
function fnvalidateComments(inputID)
{
var comments=$('#'+inputID).val();

for(var i=0,len =comments.length; i < len; i++){
	 if(comments[i]=='<'||comments[i]=='>')
			 return false;
    }
 //var regcomments= new RegExp(/^[<>]*$/);

return true;
	
	
}

// stdalex_ validate ip address function
function fnValidateIpAddress(ipAddress) {
    var checkIp = /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
    var checkHostname = /^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$/;
    if(checkIp.test(ipAddress) || checkHostname.test(ipAddress)) {
        return true;
    }else{
        return false;
    }
    
}




/* common function to change UI design for Add Mle Page, when user change MLE-Type */

//Function to change UI of Add MLE page if user has selected VMM.
function updateMlePageForVMM() {
    $('#mleSubTypeLable').text('Host OS :');
    $('#mleTypeNameLabel').text('VMM Name :');
    $('#mleTypeNameValue').html('<select class="textBox_Border" id="MainContent_ddlMLEName"></select>');
    $('#mleTypeVerLabel').text('VMM Version :');
    var row = $('#manifestListDiv table');
    row.html('');
    for (var i = 18; i < 21; i++) {
        var str = '<tr><td><span>' + i + '</span></td><td><input type="checkbox" onclick="fnToggelRegisterValue(checked,\'MainContent_tb' + i + '\')" id="MainContent_check' + i + '"/></td><td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb' + i + '"></td>';
        //if (i==17) {
        //	str+='<td><form class="uploadForm" method="post" enctype="multipart/form-data"><input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" /><input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()"><input type="image" src="images/helpicon.png" onclick="showDialogUploadFile();return false;" style="float:right;"></form></td><td></td>';
        //}else
        if (i === 18) {
            str += '<td><div id="successMessage"></div></td><td></td>';
        } else {
            str += '<td></td><td></td>';
        }
        str += '</tr>';
        row.append(str);
    }
}

//Function to change UI of Add MLE page if user has selected BIOS.
function updateMlePageForBIOS() {
    $('#mleSubTypeLable').text('OEM Vendor :');
    $('#mleTypeNameLabel').text('BIOS Name :');
    $('#mleTypeNameValue').html('<input type="text" class="inputs textBox_Border" id="MainContent_ddlMLEName" maxlength="200" ><span class="requiredField">*</span>');
    $('#mleTypeVerLabel').text('BIOS Version :');
    $('#MainContent_ddlAttestationType').html('<option>PCR</option>');
    fnToggelManifestList(false);
    var row = $('#manifestListDiv table');
    row.html('');
    for (var i = 0; i < 6; i++) {
        var str = '<tr><td><span>' + i + '</span></td><td><input type="checkbox" onclick="fnToggelRegisterValue(checked,\'MainContent_tb' + i + '\')" id="MainContent_check' + i + '"/></td><td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb' + i + '"></td>';
        if (i == 0) {
            str += '<td><form class="uploadForm" method="post" enctype="multipart/form-data"><input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" /><input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()"><input type="image" src="images/helpicon.png" onclick="showDialogUploadFile();return false;" style="float:right;"></form></td><td></td>';
        } else if (i == 1) {
            str += '<td><div id="successMessage"></div></td><td></td>';
        } else {
            str += '<td></td><td></td>';
        }
        str += '</tr>';
        row.append(str);
    }
    
    var i = 17;
    var str = '<tr><td><span>' + i + '</span></td><td><input type="checkbox" onclick="fnToggelRegisterValue(checked,\'MainContent_tb' + i + '\')" id="MainContent_check' + i + '"/></td><td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb' + i + '"></td>';
    str += '<td><form class="uploadForm" method="post" enctype="multipart/form-data"><input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" /><input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()"><input type="image" src="images/helpicon.png" onclick="showDialogUploadFile();return false;" style="float:right;"></form></td><td></td>';
    str += '</tr>';
    row.append(str);
    
    $('#MainContent_ddlMLEName').blur(function() {
        fnTestValidation('MainContent_ddlMLEName', normalReg);
    });
}

function fnToggelManifestList(elementStatus) {
	if (elementStatus) {
		$('#uploadGkvs').show();
		$('#manifestListDiv').hide();
	}else {
		$('#manifestListDiv').show();
		$('#uploadGkvs').hide();
	}
}

/*
 * Mle Data vo objects used while sending data to server.
 */
function mleDataVoObbject(mleName,mleVersion,mleDescription,osName,osVersion,attestation_Type,oemName,mleType) {
	this.mleName = mleName;
	this.mleVersion = mleVersion;
	this.mleDescription = mleDescription;
	this.osName = osName;
	this.osVersion = osVersion;
	this.attestation_Type = attestation_Type;
	this.oemName = oemName;
	this.mleType = mleType;
	this.manifestList =[];
	
}

function manifestList(name,value) {
	this.name=name;
	this.value=value;
}

//Function to clear all pre entered data in input:text. 
function clearAllFiled(divID) {
	$('#'+divID).find('input:text').each(function() {
		if ($(this).attr('disabled') != 'disabled') {
			$(this).val('');
		}
	});
    	$('#'+divID).find('input:password').each(function() {
		if ($(this).attr('disabled') != 'disabled') {
			$(this).val('');
		}
	});
}

function fnGetMleData(isNewMle) {
	var valid1 = true;
	var valid2 = true;
	var valid3 = true;
	var valid4 = true;
	var valid5 = true;
	$('#successMessage').html('');
	$('.validationErrorDiv').each(function() {
		$(this).remove();
	});
	
	valid1 = fnTestValidation('MainContent_tbVersion',normalReg);
	valid2 =fnTestValidation('MainContent_ddlMLEName',normalReg);
	
	$('#manifestListDiv table tr').each(function() {
		if($(this).find('td:eq(1)').find('input').attr('checked') == 'checked'){
			if ($(this).find('td:eq(2) :input').val() == "") {
				valid3 = false;
			}
		}
	});
	$('#manifestListDiv table tr').each(function() {
		if($(this).find('td:eq(1)').find('input').attr('checked') == 'checked'){
			if ($(this).find('td:eq(2) :input').val() != "") {
				if(!manifestReg.test($.trim($(this).find('td:eq(2) :input').val()))){
					valid4 = false;
				}
			}
		}
	});
	$('#manifestListDiv table tr').each(function() {
		if($(this).find('td:eq(1)').find('input').attr('checked') == 'checked'){
			if ($(this).find('td:eq(2) :input').val() != "") {
				if($.trim($(this).find('td:eq(2) :input').val()).indexOf(" ") >= 0){
					valid5 = false;
				}
			}
		}
	});
	if (!valid3) {
		$('#mleMessage').html('<div class="errorMessage">Please provide value for selected Manifests.</div>');
		return "";
	}
	if (!valid5) {
		$('#mleMessage').html('<div class="errorMessage">No space is allowed between Manifest values.</div>');
		return "";
	}
	if (!valid4) {
		$('#mleMessage').html('<div class="errorMessage">Only HEX values are allowed for selected Manifests.</div>');
		return "";
	}
	var data ="";
	if (valid1 && valid2 && valid3 && valid4 && valid5) {
		var mleObj = new mleDataVoObbject();
		mleObj.mleType = $('#MainContent_ddlMLEType').val();
		if ($('#MainContent_ddlMLEType').val() == 'VMM') {
			mleObj.oemName = null;
			if (isNewMle) {
				for ( var name in hostNameList) {
					if ($('#MainContent_ddlHostOs').val() == hostNameList[name].hostOS+' '+hostNameList[name].hostVersion) {
						mleObj.osName = hostNameList[name].hostOS;
						mleObj.osVersion = hostNameList[name].hostVersion;
					}
				}
			}else {
				for ( var name in hostNameList) {
					if ($('#MainContent_ddlHostOs').val() == hostNameList[name].osName+' '+hostNameList[name].osVersion) {
						mleObj.osName = hostNameList[name].osName;
						mleObj.osVersion = hostNameList[name].osVersion;
					}
				}
			}
			
		}else {
			mleObj.oemName = $('#MainContent_ddlHostOs').val();
			mleObj.osName = null;
			mleObj.osVersion = null;
		}
		mleObj.mleName = $('#MainContent_ddlMLEName').val();
		mleObj.mleVersion = $('#MainContent_tbVersion').val();
                        // For Module Attestation types, we are displaying PCR + Module string. So, while retrieving the data we need to check if 
                        // the value of the Attestation Type is PCR + Module and change it accordingly back to Module if needed.
                        if ($('#MainContent_ddlAttestationType').val() == moduleAttestationDisplayString) {
                            mleObj.attestation_Type = 'MODULE';
                        } else {
                            mleObj.attestation_Type = $('#MainContent_ddlAttestationType').val();
                        }
		mleObj.mleDescription = $('#MainContent_tbDesc').val();
		var mani = [];
		if (mleObj.attestation_Type == "Module" || mleObj.attestation_Type == "MODULE") {
			$('#gkvs_register_checkbox input').each(function() {
				var manifestObj = new manifestList();
				if($(this).attr('checked') == 'checked'){
					manifestObj.name = $(this).attr('name');
					manifestObj.value= $(this).attr('value');
                                                            // alert("Manifest Name is: " +  manifestObj.name + " Value is: " + manifestObj.value );
					mani.push(manifestObj);
				}
			});
		}else {
			$('#manifestListDiv table tr').each(function() {
				var manifestObj = new manifestList();
				if($(this).find('td:eq(1)').find('input').attr('checked') == 'checked'){
					manifestObj.name = $(this).find('td:eq(0)').text();
					manifestObj.value=$(this).find('td:eq(2) :input').val();
					mani.push(manifestObj);
				}
			});
		}
		
		mleObj.manifestList = mani;
		data+=$.toJSON(mleObj);
	}
	return data;
}


function fnTestValidation(inputID,regExpresion) {
           // alert($('#'+inputID).val());
	$('#'+inputID).parent().find('.errorMessage').remove();
	if ($('#'+inputID).val() == '') {
		$('#'+inputID).parent().append(validationSpan);
		return false;
	}else {
		if (!regExpresion.test($('#'+inputID).val())) {
			$('#'+inputID).parent().append(validationSpecialSpan);
			return false;
		}
	}
	return true;
}

/**
 * This method is used to open custom dialog box with specific Title and Content.
 * Custom dialog is created by calling dialog() function present in jquery.ui.dialog.js
 * for more information please visit http://docs.jquery.com/UI/Dialog
 * 
 * @param content (Message shown inside body of Dialog Box)
 * @param title (Title Message)
 * @param width (Width of dialog box)
 * @param height
 * @param removeOkButton (if true, navigate user to Login Page)
 */
function fnOpenDialog(content,title,width,height,removeOkButton) {
	$('#dialog').remove();
	var str = '<div id="dialog">'+content+'</div>';
	$('#mainContainer').append(str);
		$('#dialog').dialog({
			autoOpen: true,
			width: width,
			height: height,
			 modal: true,
			 title: title,
			dialogClass: "dialogClass",
			buttons: {
	            "Ok": function() { 
	                if(removeOkButton){
	                    window.location.href = "login.htm";
	                }else{
	                    $(this).dialog("close"); 
	                }
	             }
			}
		});
}

//Function to show dialog box to show message to user if session is expired or request return Login Page.
function fnSessionExpireLoginAgain() {
	var str = 'Your Login Session is Expired. <a href="login.htm">Click here</a> to Login again.';
	fnOpenDialog(str, "Error", 300, 150,true);
}


/**
 * This Method is used to create pagination on a JSP page using elementID and noOfPages parameter passed to it.
 * The file used to create pagination is "jquery.paginate.js".
 * For more details about pagination plugin please check following address
 * http://tympanus.net/codrops/2009/11/17/jpaginate-a-fancy-jquery-pagination-plugin/
 * 
 * @param elementID (Div id on which you want to create pagination)
 * @param noOfPages (no of pages to be created)
 * @param fuToCallOnButtonClick (function to be called when user click on any page no.)
 * @param startPageNo (page no to be selected once pagination is done)
 */
function applyPagination(elementID,noOfPages,fuToCallOnButtonClick,startPageNo){
	selectedPageNo = startPageNo;
	$("#"+elementID).paginate({
		count : noOfPages,
		start : startPageNo,
		display   : 10,
		border : true,
		border_color: '#fff',
		text_color : '#fff',
		background_color	: 'black',	
		border_hover_color	: '#ccc',
		text_hover_color  	: '#000',
		background_hover_color: '#fff', 
		images	: false,
		mouse	: 'press',
		onChange	: function(page){
			selectedPageNo = page;
			var args = []; 
			args.push(page);
			fuToCallOnButtonClick.apply(null,args);
					  }
	});
}

/*Function to change error message into html escaped String.*/
function getHTMLEscapedMessage(message) {
	 var str = message;
     str =str.replace(/\</g, "&lt;");
     str =str.replace(/\>/g, "&gt;");
     return str;
}


//function to check empty input:text value. If input is empty then add validationDiv into third div of a row for which we are checking the value.
function validateValue(inputID){
	if ($.trim($('#'+inputID).val()) == "") {
		$('#'+inputID).parent().parent().find('td:eq(2)').append(validationDiv);
		return false;
	}
	return true;
}


	/* Soni_Begin_27/09/2012_for_validating IP address */
	function fnvalidateEmailAddress(inputID){
	 var regemail=  new RegExp (/^[+a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/i);
	 var emailaddressVal = $('#'+inputID).val();
	 if(!regemail.test(emailaddressVal)) 
		 { return false;
		 }else 
			 return true;
	}
	/* Soni End_27/09/2012_for_validating IP address */
	/* Soni Begin_1/10/2012_for_validating descriptionnot allowing & and % Bug 436*/
	function fnValidateDescription(inputID){
		var descinput=$('#'+inputID).val();		
		for(var i=0,len =descinput.length; i < len; i++){
			 if(descinput[i]=='%'||descinput[i]=='&')
					 return false;
	         }
		return true;
	}
	/* Soni End_1/10/2012_for_validating descriptionnot allowing & and % bug#436*/
//Function called on success of all function for getting JSP pages using sendHTMLAjaxRequest() function. 
function fnDisplayContent(response,inputDivID) {
	//Set HTML content received from Server to given DIV ID.
	$('#'+inputDivID).html(response);
}

//Function will called onclick of checkbox which is used to select all other checkbox present in table.
function fnSelectAllCheckBox(status) {
	$('.hostTableContent table tr').each(function() {
		$(this).find('td:eq(0)').find(':checkbox').attr('checked',status);
            var hostName = $.trim($(this).find('td:eq(1)').text());
            if (status) {
            	selectedHost[hostName] = status;
            }else {
                    selectedHost[hostName] = false;
            }
	});
}


/**
 * This method is used to open custom dialog box with specific Title and Content.
 * Custom dialog is created by calling dialog() function present in jquery.ui.dialog.js
 * for more information please visit http://docs.jquery.com/UI/Dialog
 * 
 * @param content (Message shown inside body of Dialog Box)
 * @param title (Title Message)
 * @param width (Width of dialog box)
 * @param height
 * @param removeOkButton (if true, navigate user to Login Page)
 */
function fnOpenDialog(content,title,width,height,removeOkButton) {
	$('#dialog').remove();
	var str = '<div id="dialog">'+content+'</div>';
	$('#mainContainer').append(str);
	$('#dialog').dialog({
		autoOpen: true,
		width: width,
		height: height,
		 modal: true,
		 title: title,
		dialogClass: "dialogClass",
		buttons: {
	        "Ok": function() { 
	            if(removeOkButton){
	                window.location.href = "login.htm";
	            }else{
	                $(this).dialog("close"); 
	            }
	         }
		}
	});
}


/*Function to change error message into html escaped string.*/
function getHTMLEscapedMessage(message) {
	 var str = message;
     str =str.replace(/\</g, "&lt;");
     str =str.replace(/\>/g, "&gt;");
     return str;
}

//function to get vCenterServer Address 
function getVCenterServerAddress(inputID) {
	var preFix = "https://";
	var sufix="/sdk";
	var port = "443";
	var server = $('#'+inputID).val();
	if (server.indexOf(":") > 0) {
		return preFix+server+sufix;
	}else {
		return preFix+server+":"+port+sufix;
	}
}

function getVCeterHostIpAddress(address){
	var string = address;
	string = string.substring(15);
            // remove the sdk part
	string = string.split("/")[0]; 
            // remove the port # if using default
            if (string.split(":")[1] == "443")
                return string.split(":")[0];
            else
                return string;
}

