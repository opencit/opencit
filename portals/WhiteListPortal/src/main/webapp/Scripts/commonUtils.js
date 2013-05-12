var disabledDiv = '<div id="disabledDiv" class="disabledDiv"><img class="loadingImageClass" src="images/loading.gif" /></div>';
var validationDiv = '<div class="errorMessage validationErrorDiv"> Value Can\'t be Empty.</div>';
var validationSpecialDiv = '<div class="errorMessage validationErrorDiv"> Special Characters are not allowed.</div>';
var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);
var manifestReg = new RegExp(/^[a-fA-F0-9]+$/);
var selectedPageNo = 1;
var moduleAttestationDisplayString = 'PCR + Module';


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



/**
 * Function to send request to server for getting JSON Data.
 * 
 * @param isGet (pass true For GET Request for POST pass false)
 * @param url
 * @param requestData
 * @param callbackSuccessFunction (function name, which has to be called after successful response return)
 * @param callbackErrorFunction
 */

function sendJSONAjaxRequest(isGet, url, requestData, callbackSuccessFunction, callbackErrorFunction){
        //alert("sendJSONAjaxRequest: isGet="+isGet+"  url="+url+"  requestData="+requestData+"    sucesssFn="+callbackSuccessFunction+"  errorFn="+callbackErrorFunction);
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
                                //alert("sendJSONAjaxRequest: getResponseHeader(loginPage)==true");
				fnSessionExpireLoginAgain();
				return false;
			}
			if(responseJSON == null){
                                //alert("sendJSONAjaxRequest: responseJSON==true");
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
                                //alert("sendJSONAjaxRequest: error: callbackErrorFunction==null");
				fnSessionExpireLoginAgain();
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
                                //alert("sendHTMLAjaxRequest: getResponseHeader(loginPage)==true");
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
                                //alert("sendHTMLAjaxRequest: response==empty");
				fnSessionExpireLoginAgain();
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
                                //alert("sendHTMLAjaxRequest: error: callbackErrorFunction==null");
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
function setLoadImage(panel) {
	$('#'+panel).append('<div style="padding:50px">'+disabledDiv+'</div>');
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
	for ( var i = 17; i < 21; i++) {
		var str = '<tr><td><span>'+i+'</span></td><td><input type="checkbox" onclick="fnToggelRegisterValue(checked,\'MainContent_tb'+i+'\')" id="MainContent_check'+i+'"/></td><td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb'+i+'"></td>';
		if (i==17) {
			str+='<td><form class="uploadForm" method="post" enctype="multipart/form-data"><input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" /><input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()"><input type="image" src="images/helpicon.png" onclick="showDialogUploadFile();return false;" style="float:right;"></form></td><td></td>';
		}else if (i==18) {
			str+='<td><div id="successMessage"></div></td><td></td>';
		}else {
			str+='<td></td><td></td>';
		}
		str+='</tr>';
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
	for ( var i = 0; i < 6; i++) {
		var str = '<tr><td><span>'+i+'</span></td><td><input type="checkbox" onclick="fnToggelRegisterValue(checked,\'MainContent_tb'+i+'\')" id="MainContent_check'+i+'"/></td><td><input type="text" class="textBox_Border" disabled="disabled" title="Please enter the good known manifest value in HEX format. Ex:BFC3FFD7940E9281A3EBFDFA4E0412869A3F55D8" id="MainContent_tb'+i+'"></td>';
		if (i==0) {
			str+='<td><form class="uploadForm" method="post" enctype="multipart/form-data"><input id="fileToUpload" class="uploadButton" type="file" name="file" size="50" /><input type="button" class="uploadButton" value="Upload" onclick="fnUploadManifestFile()"><input type="image" src="images/helpicon.png" onclick="showDialogUploadFile();return false;" style="float:right;"></form></td><td></td>';
		}else if (i==1) {
			str+='<td><div id="successMessage"></div></td><td></td>';
		}else {
			str+='<td></td><td></td>';
		}
		str+='</tr>';
		row.append(str);
	}
	
	$('#MainContent_ddlMLEName').blur(function() {
		fnTestValidation('MainContent_ddlMLEName',normalReg);
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