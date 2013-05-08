var disabledDiv = '<div id="disabledDiv" class="disabledDiv"><img class="loadingImageClass" src="images/loading.gif" /></div>';
var validationDiv = '<div class="errorMessage validationErrorDiv"> Value Can\'t be Empty.</div>';
var validationSpecialDiv = '<div class="errorMessage validationErrorDiv"> Special Characters are not allowed.</div>';
var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);

// 0 = TA,  1 == vmware, 2 == citrix
var isVMWare = 0;

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
                        alert(JSON.stringify(errorMessage));
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
                        alert(JSON.stringify(errorMessage));
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

function clearAllFiled(divID) {
	$('#'+divID).find('input:text').each(function() {
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

/*Method to change display div while selection of drop down for VMWare or Opensources*/
function fnChangehostType(element,isWhiteListConfigPage) {
    //alert($('#MainContent_ddlHOSTType').attr('value') );
	var type = "";
    // This code loops through all the options in the host type
    // drop down box and finds the one thats selected and saves
    // its name in type variable
    // choices are VMware, Citrix, KVM|Xen
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

function fnWhiteListConfig(biosWhiteList,vmmWhiteList,biosWLTarget,vmmWLTarget,biosPCRs,vmmPCRs,hostLocation,registerHost) {
	this.biosWhiteList = biosWhiteList;
	this.vmmWhiteList = vmmWhiteList;
	this.biosWLTarget = biosWLTarget;
	this.vmmWLTarget = vmmWLTarget;
	this.biosPCRs = biosPCRs;
	this.vmmPCRs = vmmPCRs;
	this.hostLocation = hostLocation;
	this.registerHost = registerHost;
}

//Function to check all checkbox in table.
function fnSelectAllCheckBox(status) {
	$('.registerHostTableContent table tr td').each(function() {
		$(this).find(':checkbox').attr('checked',status);
	});
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
				fnSessionExpireLoginAgain(); // XXX TODO no response from server is an ERROR not an indicator of expired session.  we should display an appropriate message, NOT kick out the user.    this needs to be fixed also in other uses of fnSessionExpireLoginAgain that do not involve the session actually expiring.
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
    if(checkIp.test(ipAddress)) {
        return true;
    }else{
        return false;
    }
    
}