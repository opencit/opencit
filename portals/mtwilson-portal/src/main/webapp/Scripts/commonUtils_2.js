var disabledDiv = '<div id="disabledDiv" class="disabledDiv"><img class="loadingImageClass" src="images/loading.gif" /></div>';
var validationDiv = '<span class="errorMessage validationErrorDiv" style="float:none;"> Value can\'t be empty.</span>';
var validationSpecialDiv = '<span class="errorMessage validationErrorDiv" style="float:none;"> Special characters are not allowed.</span>';
var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);
var regIPAddress = new RegExp();
var regVcenterAddress = new RegExp();
var regEmail = new RegExp();
var regPortNo = new RegExp(/^[0-9]+$/);

var isAddHostPage = false;

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

//global variable page no.
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
				fnSessionExpireLoginAgain();
				//alert("Response from Server is null.");
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
			    //Call to success function by passing response and other extra parameter.
				callbackSuccessFunction.apply(null,args);
			}else{
				fnSessionExpireLoginAgain();
				//alert("Response from Server is null.");
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

//Function to show dialog box to show message to user if session is expired or request return Login Page.
function fnSessionExpireLoginAgain() {
	var str = 'Your login session has expired. <a href="login.htm">Click here</a> to login again.';
	fnOpenDialog(str, "Error", 300, 150,true);
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

//function to test Validation for given input ID using Regular Expression.
//function will return true if ever thing is fine.
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

/* Soni_Begin_27/09/2012_for_validating IP address */	
	function isValidIPAddress(inputID) {
		  var inputip=$('#'+inputID).val();		  
		 var CheckIP = new RegExp(/\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/);
		  if(CheckIP.test(inputip))
			 return true;
		  else 
			  return false;
	} 
	/* Soni_End_27/09/2012_for_validating IP address */
	
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
 * This Method is used to crate pagination on a JSP page using elementID and noOfPages parameter passed to it.
 * The file used to create pagination is "jquery.paginate.js".
 * For more details about pagination plugin please chack following address
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
	string = string.substring(8);
	return string.split("/")[0];
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