var hostNameList = [];
var isVMM = true;

$(function() {
	if (!isEditMle) {
		$('#MainContent_tbVersion').blur(function() {
			fnTestValidation('MainContent_tbVersion',normalReg);
		});
		fnChangeleType($('#MainContent_ddlMLEType'));
	}else {
		$('.requiredField').each(function() {
			$(this).remove();
		});
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
				$('#MainContent_ddlAttestationType').html('<option>'+hostNameList[name].attestationType+'</option>');
				
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
		$('#successMessage').html('<div class="successMessage">* File is uploaded Successfully.</div>');
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
		if (confirm("Are you Sure you want to Add this MLE ?")) {
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
		$('#mleMessage').html('<div class="successMessage">* Mle has been Successfully Added.</div>');
		clearAllFiled("mainDataTableMle");
	}else{
		$('#mleMessage').html('<div class="errorMessage" style="float:left;">* MLE is not Added, Server Error. '+response.message+'</div>');
	}
}


/*method for show dialog*/

function showDialogManifestList() {
	var str = "";
	for ( var iteam in pcrHelp) {
		str+='<div class="pcrDivHelp">'+pcrHelp[iteam]+'</div>';
	}
	fnOpenDialog(str,"Manifest List Help",780,430,false);
}

function showDialogUploadFile() {
	var str = "";
	for ( var iteam in uploadFileHelp) {
		str+='<div class="pcrDivHelp">'+uploadFileHelp[iteam]+'</div>';
	}
	fnOpenDialog(str,"Upload File Help",380,230,false);
}

function showDialogManifestloadertool() {
	var str = "";
	for ( var iteam in Manifestloadertool) {
		str+='<div class="pcrDivHelp">'+Manifestloadertool[iteam]+'</div>';
	}
	fnOpenDialog(str,"Manifest Loader Tool Help",580,330,false);
}