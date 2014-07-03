var hostNameList = [];
var isVMM = true;

$(function() {
	if (!isEditMle) {
		$('#MainContent_tbVersion').blur(function() {
			fnTestValidation('MainContent_tbVersion',normalReg);
		});
		fnChangeleType($('#MainContent_ddlMLEType'));
                // We do not want to display the host name associated with the MLE in Add MLE page.
                // It will be displayed only during the EDIT LE page.
                $('#mleSourceHost').hide();
                $('#updateMleButton').hide();
	}else {
		$('.requiredField').each(function() {
			$(this).remove();
		});
                $('#addMleButton').hide();
                $('#mleSourceHost').show();
	}
});


function fnChangeleType(element) {
	$('#successMessage').html('');
	$('#mleMessage').html('');
	$('.validationErrorDiv').remove();
	$('#mainDataTableMle').prepend(disabledDiv);
	$('#MainContent_ddlHostOs').html('');
	if ($(element).val() == 'VMM') {
		isVMM = true;
		updateMlePageForVMM();
		sendJSONAjaxRequest(false, 'getData/getHostOSForVMM.html', null, fnUpdateAddMleForVMM, null);
	}
	if ($(element).val() == 'BIOS') {
		isVMM = false;
		updateMlePageForBIOS();
		//sending request to server to get all list of Host
		sendJSONAjaxRequest(false, 'getData/getHostOSForBios.html', null, fnUpdateAddMleForBIOS, null);
	}
}


function fnToggelRegisterValue(status,inputID) {
	$('#'+inputID).attr('disabled',!status);
}

function fnUpdateAddMleForVMM(responseJson){
	$('#disabledDiv').remove();
	if (responseJson.result) {
		hostNameList = responseJson.HostList;
		var str ='';
		for ( var name in hostNameList) {
			str+='<option>'+hostNameList[name].hostOS+' '+hostNameList[name].hostVersion +'</option>';
		}
		$('#MainContent_ddlHostOs').html(str);
		if (isVMM) {
			fnOnChangeVmmName($('#MainContent_ddlHostOs'));
		}
	}else{
		$('#mleMessage').html('<div class="errorMessage">'+"* "+responseJson.message+'</div>');
	}
}

function fnUpdateAddMleForBIOS(responseJson){
	$('#disabledDiv').remove();
	if (responseJson.result) {
		var str ='';
		for ( var name in responseJson.HostList) {
			str+='<option>'+responseJson.HostList[name].oemName+'</option>';
		}
		$('#MainContent_ddlHostOs').html(str);
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+"* "+responseJson.message+'</div>');
	}
}

function fnOnChangeVmmName(element) {
	$('#mainfestGKVSCheck').find('input:checkbox').removeAttr("disabled","disabled");
	if (isVMM) {
		var value = $(element).val();
		var str ='';
		for ( var name in hostNameList) {
			if (value == hostNameList[name].hostOS+' '+hostNameList[name].hostVersion) {
				for ( var vmmNames in hostNameList[name].vmmNames) {
					str+='<option>'+hostNameList[name].vmmNames[vmmNames]+'</option>';
				}
                                                // In the UI for Module attestation type, we want to show the user that Module attestation, both PCR and Modules
                                                // will be verified. For that we will just update the UI for that
                                                var displayAttestationTypeValue = hostNameList[name].attestationType;
                                                if (hostNameList[name].attestationType == "Module" || hostNameList[name].attestationType == "MODULE") {
                                                    displayAttestationTypeValue = moduleAttestationDisplayString;
                                                }
				$('#MainContent_ddlAttestationType').html('<option>'+displayAttestationTypeValue+'</option>');
				
				if (hostNameList[name].attestationType == "Module" || hostNameList[name].attestationType == "MODULE") {
					//$('#mleVmmLableInfo').hide();
					$('#mainfestGKVSCheck').show();
					$('#manifestListDiv').hide();
					$('#mainfestGKVSCheck').find('input:checkbox').attr("checked","checked");
					fnToggelManifestList(true);
					$('#mainfestGKVSCheck').find('input:checkbox').attr("disabled","disabled");
					/*Start Below message is changed by Soni  as per email on * Fri 9/14/2012 10:21 AM Issue - Message is changed */ 
					$('#moduleTypeAttestationMez').html('<div style="width: 644px;font-weight: bold;">Please use the Management Console portal or API Client library to configure the white list values. </div>');
					/*End  Below message is changed by Soni  as per email on * Fri 9/14/2012 10:21 AM*/ 
				}else {
					//$('#mleVmmLableInfo').show();
					$('#mainfestGKVSCheck').show();
					$('#manifestListDiv').show();
					fnToggelManifestList(false);
					$('#mainfestGKVSCheck').find('input:checkbox').attr("checked",false);
					$('#moduleTypeAttestationMez').html('');
				}
			}
		}
		$('#MainContent_ddlMLEName').html(str);
	}
}

function fnUploadManifestFile() {
	$('#successMessage').html('');
	$.ajaxFileUpload({
			url:'getData/uploadManifest.html', 
			secureuri:false,
			fileElementId:'fileToUpload',
			dataType: 'jsonp',
			success: function (data, status) {
				fnuploadSuccess(data);
			},
			error: function (data, status, e){
				$('#successMessage').html('<div class="errorMessage">* File is not processed properly. Please check file format.</div>');
			}
		});
}

function fnuploadSuccess(responseHTML) {
	var validResponse =responseHTML;
	//validResponse = validResponse.lenght > 1 ? validResponse.substring(0, validResponse.length-1) : validResponse; 
	var response = $('<div>'+responseHTML+'</div>');
	if($(response).find('div').html() == null){
		if ($(response).find('pre').html() != null) {
			validResponse = $(response).find('pre').html();
		}
		validResponse=validResponse.split(':')[1];
		validResponse=validResponse.substring(0, validResponse.length-1);
	}else{
		validResponse =$.trim($(response).find('.bool').text());
	}
	
	if (validResponse == 'true') {
		$('#successMessage').html('<div class="successMessage">* File is uploaded successfully.</div>');
		sendJSONAjaxRequest(false, 'getData/getUploadedMenifestFile.html', null, updateManifestList, null);
	}else {
		$('#successMessage').html('<div class="errorMessage">* File is not processed properly. Please check file format.</div>');
	}
}

function updateManifestList(responsJson) {
	if (responsJson.result) {
		var values = responsJson.manifestValue;
		for ( var val in values) {
			for ( var pcr in values[val]) {
				fnToggelRegisterValue(true,'MainContent_tb'+pcr);
				$('#MainContent_check'+pcr).attr('checked','checked');
				$('#MainContent_tb'+pcr).attr('value',values[val][pcr]);
			}
		}
	}
}

function addNewMle() {
	var dataToSent = fnGetMleData(true);
	if (dataToSent != "") {
		if (confirm($("#alert_add_mle").text())) {
			$('#mainDataTableMle').prepend(disabledDiv);
			sendJSONAjaxRequest(false, 'getData/getAddMle.html', "mleObject="+dataToSent+"&newMle=true", addNewMleSuccess, null);
		}
		// add by soni 
		else {
			sendHTMLAjaxRequest(false, 'home.html', null, fnDisplayContent,null, 'mainContainer');
		}
	}
}

function addNewMleSuccess(response) {
	$('#disabledDiv').remove();
	
	if (response.result) {
		$('#mleMessage').html('<div class="successMessage">* Mle has been successfully added.</div>');
		clearAllFiled("mainDataTableMle");
	}else{
		$('#mleMessage').html('<div class="errorMessage" style="float:left;">* MLE is not added, Server Error. '+response.message+'</div>');
	}
}


/*method for show dialog*/

function showDialogManifestList() {
//	var str = "";
//	for ( var iteam in pcrHelp) {
//		str+='<div class="pcrDivHelp">'+pcrHelp[iteam]+'</div>';
//	}
        var str = '<div class="pcrDivHelp" data-i18n="[html]help.pcr_help"></div>';
	fnOpenDialog(str,"manifest_list_help",780,430,false);
}

function showDialogUploadFile() {
//	var str = "";
//	for ( var iteam in uploadManifestFileHelp) {
//		str+='<div class="pcrDivHelp">'+uploadManifestFileHelp[iteam]+'</div>';
//	}
        var str = '<div class="pcrDivHelp" data-i18n="[html]help.upload_manifest_file_help"></div>';
	fnOpenDialog(str,"upload_file_help",380,230,false);
}

function showDialogManifestloadertool() {
//	var str = "";
//	for ( var iteam in Manifestloadertool) {
//		str+='<div class="pcrDivHelp">'+Manifestloadertool[iteam]+'</div>';
//	}
        var str = '<div class="pcrDivHelp" data-i18n="[html]help.manifest_loader_tool"></div>';
	fnOpenDialog(str,"manifest_loader_tool_help",580,330,false);
}