var oemInfo = [];
var isVmware = false;

$(function() {
	$('#mainAddHostContainer').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllOemInfo.html', null, fnGetAllOemInfoSuccess, null);
	/*
	 * Function to check validation as input loose focus.
	 */

	$('#MainContent_tbHostName').blur(function() {
		fnTestValidation('MainContent_tbHostName',normalReg);
	});
	$('#MainContent_tbHostPort').blur(function() {
		if (isVmware == 'false') {
			$('.portNoError').each(function() {
				$(this).remove();
			});
			if (!regPortNo.test($('#MainContent_tbHostPort').val())) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv portNoError" style="float:none">only Numeric values are allowed.</span>');
				return false;
			}
			if ($('#MainContent_tbHostPort').val().length > 4 ) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv portNoError" style="float:none">Port NO length should not be greater 4.</span>');
				return false;
			}
		}
	});
	$('#MainContent_tbHostIP').blur(function() {
		if (isVmware == 'false') {
			fnTestValidation('MainContent_tbHostIP',regIPAddress);
		}
	});
	$('#MainContent_tbVCenterAddress').blur(function() {
		if (isVmware != 'false') {
			fnTestValidation('MainContent_tbVCenterAddress',regVcenterAddress);
		}
	});
	$('#MainContent_tbVCenterLoginId').blur(function() {
		if (isVmware != 'false') {
			fnTestValidation('MainContent_tbVCenterLoginId',new RegExp());
		}
	});
	$('#MainContent_tbVCenterPass').blur(function() {
		if (isVmware != 'false') {
			fnTestValidation('MainContent_tbVCenterPass',new RegExp());
		}
	});
});

function fnGetAllOemInfoSuccess(responseJSON) {
	if (responseJSON.result) {
		oemInfo = responseJSON.oemInfo;
		var options = "";
		for ( var str in oemInfo) {
			options +='<option value="'+str+'">'+str+'</option>';
		}
		$('#MainContent_ddlOEM').html(options);
		sendJSONAjaxRequest(false, 'getData/getOSVMMInfo.html', null, fnGetOSVMMInfoSuccess, null);
		fnChangeOEMVender();
	}else {
		$('#disabledDiv').remove();
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
	}
}


function fnGetOSVMMInfoSuccess(responsJSON) {
	if (responsJSON.result) {
		var options = "";
		for ( var str in responsJSON.osInfo) {
			options +='<option isvmware="'+responsJSON.osInfo[str]+'" value="'+str+'">'+str+'</option>';
		}
		$('#MainContent_LstVmm').html(options);
		$('#MainContent_LstVmm option:eq(0)').attr('selected', 'selected');
		$('#MainContent_LstVmm').trigger('change');
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responsJSON.message)+'</div>');
	}
	
	if (isAddHostPage) {
		$('#disabledDiv').remove();
	}else {
		sendJSONAjaxRequest(false, 'getData/getInfoForHostID.html',"hostName="+selectedHostID, fnFillAddHostPageDataForEdit, null);
	}
}

function fnChangeOEMVender() {
	var selected = $('#MainContent_ddlOEM').val();
	for ( var oem in oemInfo) {
		if (oem == selected) {
			var options = "";
			for ( var name in oemInfo[oem]) {
				for ( var val in oemInfo[oem][name]) {
					options +='<option biosName="'+val+'" biosVer="'+oemInfo[oem][name][val]+'">'+val+' '+oemInfo[oem][name][val]+'</option>';
				}
			}
			$('#MainContent_LstBIOS').html(options);
		}
	}
}

function SetRequired(element) {
	var selected = $(element).val();
	
	$(element).find('option').each(function() {
		if ($(this).text() == selected) {
			isVmware = $(this).attr('isvmware'); 
		}
	});
	$('.requiredOne').each(function() {
		$(this).remove();
	});
	var reqStr = '<span id="requiredFiled" class="requiredOne" style="color:red;">*  </span>';
	if (isVmware == 'false') {
		$('#hostPortDisplayDiv').show();
		$('#MainContent_tbHostIP').parent().append(reqStr);
		$('#MainContent_tbHostPort').parent().append(reqStr);
		$('#vcenterStringElement').find('input').each(function() {
			$(this).parent().find('.validationErrorDiv').remove();
		});
		$('#vcenterStringElement').hide();
	}else {
		$('#vcenterStringElement').find('input').each(function() {
			$(this).parent().append(reqStr);
		});
		$('#vcenterStringElement').show();
		/*$('#MainContent_tbVCenterAddress').parent().append(reqStr);
		$('#MainContent_tbVCenterLoginId').parent().append(reqStr);
		$('#MainContent_tbVCenterPass').parent().append(reqStr);*/
		$('#MainContent_tbHostIP').parent().find('.validationErrorDiv').remove();
		$('#MainContent_tbHostPort').parent().find('.validationErrorDiv').remove();
		$('#hostPortDisplayDiv').hide();
	}
}

function hostDataVoObbject(hostId,hostName,hostIPAddress,hostPort,hostDescription,biosName,biosBuildNo,vmmName,vmmBuildNo,updatedOn,emailAddress,location,oemName,vCenterDetails) {
	this.hostId = hostId;
	this.hostName = hostName;
	this.hostIPAddress = hostIPAddress;
	this.hostPort = hostPort;
	this.hostDescription = hostDescription;
	this.biosName = biosName;
	this.biosBuildNo = biosBuildNo;
	this.vmmName = vmmName;
	this.vmmBuildNo = vmmBuildNo;
	this.updatedOn =updatedOn;
	this.emailAddress =emailAddress;
	this.location =location;
	this.oemName =oemName;
	this.vCenterDetails =vCenterDetails;
}

function chechAddHostValidation() {
	$('.validationErrorDiv').each(function() {
		$(this).remove();
	});	

	//var isVmware = false;
	//var selected = $('#MainContent_LstVmm').val();
	/*$('#MainContent_LstVmm').find('option').each(function() {
		if ($(this).text() == selected) {
			isVmware = $(this).attr('isvmware'); 
		}
	});*/
	
	var valid1 = fnTestValidation('MainContent_tbHostName',normalReg);
	var valid2  = true;
	var valid3 = true;
	var valid5 = true;
	var valid6 = true;
	var valid4 = true;
	if (isVmware == 'false') {
		valid2 = fnTestValidation('MainContent_tbHostPort',normalReg);
		valid3 = fnTestValidation('MainContent_tbHostIP',normalReg);
		/* Soni_Begin_27/09/2012_for_validating IP address */
		valid4 = isValidIPAddress('MainContent_tbHostIP');
		if (!valid4){$('#MainContent_tbHostIP').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Enter Valid IP Address.</span>');
		valid =false;
		return false;}
		/* Soni_End_27/09/2012_for_validating IP address */
		/* Soni Begin_1/10/2012_for_validating descriptionnot allowing & and % Bug 436*/
		if($('#MainContent_tbDesc').val()){
			
			if(!(fnValidateDescription('MainContent_tbDesc')))
			{$('#MainContent_tbDesc').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Special charaters % and & is not allowed.</span>');
			valid5=false;
			return false;
			}
			
		}
		/* Soni End_1/10/2012_for_validating description not allowing & and % Bug 436*/
		if($('#MainContent_tbEmailAddress').val()){
			if(!(fnvalidateEmailAddress('MainContent_tbEmailAddress')))
			{$('#MainContent_tbEmailAddress').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Enter a valid email address.</span>');
			valid6 = false;
			return false;}
			
		}
		
		
	}else {
		valid3 =  fnTestValidation('MainContent_tbVCenterAddress',new RegExp());
		valid3 =  fnTestValidation('MainContent_tbVCenterLoginId',new RegExp());
		valid3 =  fnTestValidation('MainContent_tbVCenterPass',new RegExp());
	}
	
	if (valid1 && valid2 && valid3 && valid5 && valid6) {
		
		if(isVmware == 'false'){
			if (!regPortNo.test($('#MainContent_tbHostPort').val())) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">only Numeric values are allowed.</span>');
				return false;
			}
			if ($('#MainContent_tbHostPort').val().length > 4 ) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Port NO length should not be greater 4.</span>');
				return false;
			}
		}
		return true;
	}else {
		return false;
	}
}

function fnGetNewHostData() {
	var hostVo = new hostDataVoObbject();
	hostVo.hostName = $.trim($('#MainContent_tbHostName').val());
	hostVo.hostDescription = $('#MainContent_tbDesc').val();
	hostVo.biosName = $('#MainContent_LstBIOS option:selected').attr('biosname');
	hostVo.biosBuildNo = $('#MainContent_LstBIOS option:selected').attr('biosver');
	var vmm = $('#MainContent_LstVmm').val().split(':');
	hostVo.vmmName = vmm[0];
	hostVo.vmmBuildNo = vmm[1];
/*	hostVo.vmmName = vmm[0]+"-"+vmm[1].split(':')[0];
	hostVo.vmmBuildNo = vmm[1].split(':')[1];
*/	hostVo.emailAddress = $('#MainContent_tbEmailAddress').val();
	hostVo.oemName = $('#MainContent_ddlOEM').val();
	
	/*var isVmware = false;
	var selected = $('#MainContent_LstVmm').val();
	$('#MainContent_LstVmm').find('option').each(function() {
		if ($(this).text() == selected) {
			isVmware = $(this).attr('isvmware'); 
		}
	});*/
	
	hostVo.hostIPAddress = $.trim($('#MainContent_tbHostIP').val());
	if (isVmware == 'false') {
		hostVo.hostPort =$.trim($('#MainContent_tbHostPort').val());
		hostVo.vCenterDetails = "";
	}else {
	//	hostVo.hostIPAddress = "";
		hostVo.hostPort =0;
		hostVo.vCenterDetails = getVCenterServerAddress('MainContent_tbVCenterAddress')+";"+$('#MainContent_tbVCenterLoginId').val()+";"+$('#MainContent_tbVCenterPass').val();
	}
	
	//setting unwanted values to null or default
	hostVo.location = null;
	hostVo.updatedOn = null;
	return hostVo;
}

function addNewHost() {
	if (chechAddHostValidation()) {
		if (confirm("Are you Sure you want to Add this Host ?")) {
			var dataToSend = fnGetNewHostData();
			dataToSend.hostId = null;
			dataToSend = $.toJSON(dataToSend);
			$('#mainAddHostContainer').prepend(disabledDiv);
			$('#mleMessage').html('');
			sendJSONAjaxRequest(false, 'getData/saveNewHostInfo.html', "hostObject="+(dataToSend)+"&newhost=true", fnSaveNewHostInfoSuccess, null,"New Host has been successfully Added.");
		}
	}
}

function fnSaveNewHostInfoSuccess(response,messageToDisplay) {
	$('#disabledDiv').remove();
	if (response.result) {
		clearAllFiled('mainAddHostContainer');
		$('#mleMessage').html('<div class="successMessage">'+messageToDisplay+'</div>');
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(response.message)+'</div>');
	}
}


