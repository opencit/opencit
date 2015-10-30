var oemInfo = [];

// 0 = TA,  1 == vmware, 2 == citrix
var isVmware = 0;

$(function() {
	$('#mainAddHostContainer').prepend(disabledDiv);
        $('#updateHostButton').hide();
	sendJSONAjaxRequest(false, 'getData/getAllOemInfo.html', null, fnGetAllOemInfoSuccess, null);
	/*
	 * Function to check validation as input loose focus.
	 */

	$('#MainContent_tbHostName').blur(function() {
		fnMWValidateIpAddressOrHostName('MainContent_tbHostName',false);
	});
	$('#MainContent_tbHostPort').blur(function() {
                        // Port has to be validated for both Open Source and Citrix
		if ((isVmware == 0)  || (isVmware ==2)){
			/*$('.portNoError').each(function() {
				$(this).remove();
			});
			if (!regPortNo.test($('#MainContent_tbHostPort').val())) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv portNoError" style="float:none">Only numeric values are allowed.</span>');
				return false;
			}
			if ($('#MainContent_tbHostPort').val().length > 4 ) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv portNoError" style="float:none">Port NO length should not be greater 4.</span>');
				return false;
			}*/
                                    fnMWValidatePort('MainContent_tbHostPort', true);
		}
	});
    
	//$('#MainContent_tbHostIP').blur(function() {
                        // For both open source and citrix we have to validate IP address since it is mandatory. For VMware we validate only if the user has entered value
	//	if ((isVmware == 0) || (isVmware == 2)){
			//fnTestValidation('MainContent_tbHostIP',regIPAddress);
    //                                fnMWValidateIpAddress('MainContent_tbHostIP', true);
	//	} else if ((isVmware == 1)) {
    //                               // fnTestValidation('MainContent_tbHostIP',regIPAddress);
    //                                fnMWValidateIpAddress('MainContent_tbHostIP', false);                                   
    //       }
	//});
    
	$('#MainContent_tbVCenterAddress').blur(function() {
		if (isVmware == 1) {
			fnMWValidateIpAddressOrHostName('MainContent_tbVCenterAddress',true);
		}
	});
	$('#MainContent_tbVCenterLoginId').blur(function() {
		if (isVmware == 1) {
			fnMWValidateField('MainContent_tbVCenterLoginId',true);
		}
	});
	$('#MainContent_tbVCenterPass').blur(function() {
		if (isVmware == 1) {
			fnMWValidateField('MainContent_tbVCenterPass',true);
		}
	});
});

function fnGetAllOemInfoSuccess(responseJSON) {
	if (responseJSON.result) {
		oemInfo = responseJSON.oemInfo;
		var options = "";
		for ( var str in oemInfo) {
			options +='<option value="' + escapeForHTMLAttributes(str) + '">' + getHTMLEscapedMessage(str) + '</option>';
		}
		$('#MainContent_ddlOEM').html(options);
		sendJSONAjaxRequest(false, 'getData/getOSVMMInfo.html', null, fnGetOSVMMInfoSuccess, null);
		fnChangeOEMVender();
	}else {
		$('#disabledDiv').remove();
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
               	$('#addHostButton').attr('disabled','false');

	}
}


function fnGetOSVMMInfoSuccess(responsJSON) {
	if (responsJSON.result) {
		var options = "";
		for ( var str in responsJSON.osInfo) {
			options +='<option isvmware="'+ escapeForHTMLAttributes(responsJSON.osInfo[str]) + '" value="' + escapeForHTMLAttributes(str) +'">' + getHTMLEscapedMessage(str) + '</option>';
		}
                if (options != "") {
                    $('#MainContent_LstVmm').html(options);
                    $('#MainContent_LstVmm option:eq(0)').attr('selected', 'selected');
                    $('#MainContent_LstVmm').trigger('change');
                } else {
                    var errorMsg = "No VMM MLEs are configured in the system."
                    $('#mleMessage').html('<div class="errorMessage">'+ errorMsg +'</div>');
                    $('#addHostButton').attr('disabled','false');
                }
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
					options +='<option biosName="' + escapeForHTMLAttributes(val) + '" biosVer="' + escapeForHTMLAttributes(oemInfo[oem][name][val]) + '">' + getHTMLEscapedMessage(val) + ' ' + getHTMLEscapedMessage(oemInfo[oem][name][val]) + '</option>';
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
			type = $(this).attr('value');  // 'isvmware'
		}
	});
    
    if (type.indexOf("VMware") != -1) {  
		isVmware = 1;
	}else if(type.indexOf("XenServer") != -1) {
       isVmware = 2;
	}else{
        isVmware= 0;
    }
    
	$('.requiredOne').each(function() {
		$(this).remove();
	});
	var reqStr = '<span id="requiredFiled" class="requiredOne" style="color:red;">*  </span>';
	if (isVmware == 0) { // TA
        
		$('#hostPortDisplayDiv').show();
		//$('#MainContent_tbHostIP').parent().append(reqStr);
		$('#MainContent_tbHostPort').parent().append(reqStr);
		$('#vcenterStringElement').find('input').each(function() {
			$(this).parent().find('.validationErrorDiv').remove();
		});
		$('#vcenterStringElement').hide();
//                $('#openSourceStringElement').find('input').each(function() {
//                    $(this).parent().append(reqStr);
//                });
                $('#opensource_credentials').show();
                $('#openSourceStringElement').hide();
		$('#citrixStringElement').hide();
                $('#MainContent_tbHostPort').val("1443");
	}else if(isVmware == 1){  // VMWARE
        
		$('#vcenterStringElement').find('input').each(function() {
			$(this).parent().append(reqStr);
		});
		$('#vcenterStringElement').show();
		/*$('#MainContent_tbVCenterAddress').parent().append(reqStr);
		$('#MainContent_tbVCenterLoginId').parent().append(reqStr);
		$('#MainContent_tbVCenterPass').parent().append(reqStr);*/
		//$('#MainContent_tbHostIP').parent().find('.validationErrorDiv').remove();
		$('#MainContent_tbHostPort').parent().find('.validationErrorDiv').remove();
		$('#hostPortDisplayDiv').hide();
                $('#openSourceStringElement').hide();
                $('#opensource_credentials').hide();
		$('#citrixStringElement').hide();
	}else { //CITRIX
                $('#hostPortDisplayDiv').show();
                //$('#MainContent_tbHostIP').parent().append(reqStr);
		$('#MainContent_tbHostPort').parent().append(reqStr);
                $('#vcenterStringElement').find('input').each(function() {
                    $(this).parent().find('.validationErrorDiv').remove();
                });
                $('#vcenterStringElement').hide();
                
                $('#citrixStringElement').find('input').each(function() {
                    $(this).parent().append(reqStr);
                });
                $('#citrixStringElement').show();
                $('#openSourceStringElement').hide(); 
                $('#opensource_credentials').hide();
                $('#MainContent_tbHostPort').val("443");
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

function fnShowLoginCredentials() {
    str = "<a href=\"#\" onclick=\"fnShowLoginCredentials()\">";
    if ((document.getElementById('opensource_credentials').innerHTML).indexOf("Show login credentials") > 0) {
        $('#openSourceStringElement').show();
        str = str + "Hide login credentials";        
    } else {
        $('#openSourceStringElement').hide();
        str = str + "Show login credentials";
    }
    str = str + "</a>";
    document.getElementById('opensource_credentials').innerHTML = str;
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
	if (isVmware == 0) { // TA
		valid2 = fnTestValidation('MainContent_tbHostPort',normalReg);
                //valid3 =  fnTestValidation('MainContent_tbVopensourceLoginId',new RegExp());
                //valid3 =  fnTestValidation('MainContent_tbVopensourcePass',new RegExp());
		//valid3 = fnTestValidation('MainContent_tbHostIP',normalReg);
		/* Soni_Begin_27/09/2012_for_validating IP address */
		//valid4 = isValidIPAddress('MainContent_tbHostIP');
		//if (!valid4){
                //    $('#MainContent_tbHostIP').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Enter valid IP address.</span>'); 
                //     valid =false;
                //     return false;
                //}
		/* Soni_End_27/09/2012_for_validating IP address */		
		
	}else if(isVmware == 1){ //vmware
		valid3 =  fnTestValidation('MainContent_tbVCenterAddress',new RegExp());
		valid3 =  fnTestValidation('MainContent_tbVCenterLoginId',new RegExp());
		valid3 =  fnTestValidation('MainContent_tbVCenterPass',new RegExp());
	}else { // citrix
        valid2 = fnTestValidation('MainContent_tbHostPort',normalReg);
        //valid3 = fnTestValidation('MainContent_tbHostIP',normalReg);
        valid3 =  fnTestValidation('MainContent_tbVcitrixLoginId',new RegExp());
        valid3 =  fnTestValidation('MainContent_tbVcitrixPass',new RegExp());
    }
	
        // Description and email field were not getting validated for VMware hosts. So, moved it outside the if/else statement
        /* Soni Begin_1/10/2012_for_validating descriptionnot allowing & and % Bug 436*/
        if($('#MainContent_tbDesc').val()){

                if(!(fnValidateDescription('MainContent_tbDesc')))
                {$('#MainContent_tbDesc').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Special characters % and & are not allowed.</span>');
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
        
	if (valid1 && valid2 && valid3) {
		
		if(isVmware == 0){
			if (!regPortNo.test($('#MainContent_tbHostPort').val())) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Only numeric values are allowed.</span>');
				return false;
			}
			if ($('#MainContent_tbHostPort').val().length > 5 ) {
				$('#MainContent_tbHostPort').parent().append('<span class="errorMessage validationErrorDiv" style="float:none">Length should not be greater 5.</span>');
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
	
	hostVo.hostIPAddress = hostVo.hostName; //$.trim($('#MainContent_tbHostIP').val());
	if (isVmware == 0) { // TA
		hostVo.hostPort =$.trim($('#MainContent_tbHostPort').val());
		hostVo.vCenterDetails = "intel:https://"+$('#MainContent_tbHostName').val()+":"+$('#MainContent_tbHostPort').val()+
                                             "/;"+$('#MainContent_tbVopensourceLoginId').val()+";"+$('#MainContent_tbVopensourcePass').val();
	}else if(isVmware == 1) { // VMWARE
	//	hostVo.hostIPAddress = "";
		hostVo.hostPort =0;
		hostVo.vCenterDetails = getVCenterServerAddress('MainContent_tbVCenterAddress')+";"+$('#MainContent_tbVCenterLoginId').val()+";"+$('#MainContent_tbVCenterPass').val();
	}else { // CITRIX
        hostVo.hostPort =$.trim($('#MainContent_tbHostPort').val());
        hostVo.vCenterDetails = "citrix:https://"+$('#MainContent_tbHostName').val()+":"+$('#MainContent_tbHostPort').val()+
                                             "/;"+$('#MainContent_tbVcitrixLoginId').val()+";"+$('#MainContent_tbVcitrixPass').val();
        
    }

    var tlsPolicyChoice = {}
    mtwilsonTlsPolicyModule.copyTlsPolicyChoiceToHostDetailsVO({
        'tls_policy_select': $('#tls_policy_select').val(),
        'tls_policy_data_certificate': $("#tls_policy_data_certificate").val(),
        'tls_policy_data_certificate_digest': $("#tls_policy_data_certificate_digest").val(),
        'tls_policy_data_public_key': $("#tls_policy_data_public_key").val(),
        'tls_policy_data_public_key_digest': $("#tls_policy_data_public_key_digest").val()
    }, tlsPolicyChoice);
    hostVo.tlsPolicyId = tlsPolicyChoice.tlsPolicyId;
    hostVo.tlsPolicyType = tlsPolicyChoice.tlsPolicyType;
    hostVo.tlsPolicyData = encodeURIComponent(tlsPolicyChoice.tlsPolicyData); // may contain base64 data include + sign which , if not encoded, would be received by the server as a space and cause the data to be unusable
    
    
	//setting unwanted values to null or default
	hostVo.location = null;
	hostVo.updatedOn = null;
	return hostVo;
}

function addNewHost() {
        var ipValid = true;
        
        if (isVmware == 0 || isVmware == 2) {
         //if(!fnValidateIpAddress($('#MainContent_tbHostIP').val())) {
       
         //    ipValid=false;
         //}   
        }else{
          if(!fnValidateIpAddress($('#MainContent_tbVCenterAddress').val())) {

             ipValid=false;
         }    
        }

        if(ipValid == true) {
            if (chechAddHostValidation()) {
                if (confirm($("#alert_confirm_add_host").text())) {
                    var dataToSend = fnGetNewHostData();
                    dataToSend.hostId = null;
                    dataToSend = $.toJSON(dataToSend);
                    $('#mainAddHostContainer').prepend(disabledDiv);
                    $('#mleMessage').html('');
                    sendJSONAjaxRequest(false, 'getData/saveNewHostInfo.html', "hostObject="+(dataToSend)+"&newhost=true", fnSaveNewHostInfoSuccess, null,"New Host has been successfully Added.");
                }
            }
        }else{
            alert($("#alert_valid_ip").text())
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

// see also WhiteListConfig.js
$(document).ready(function() {
    $.getJSON("v2proxy/tls-policies.json", {"privateEqualTo":"false"}, function(data) {
        //console.log(data); // {"meta":{"default":null,"allow":["certificate","public-key"],"global":null},"tls_policies":[]}
	mtwilsonTlsPolicyModule.onGetTlsPolicies(data);
        var choicesArray = mtwilsonTlsPolicyModule.getTlsPolicyChoices();
           var el = $("#tls_policy_select");
  		mtwilsonTlsPolicyModule.populateSelectOptionsWithTlsPolicyChoices(el, choicesArray);
        mtwilsonTlsPolicyModule.insertSelectOptionsWithPerHostTlsPolicyChoices(el, {
            dataInputContainer: $('#tls_policy_data_container')
        });
        mtwilsonTlsPolicyModule.selectDefaultTlsPolicyChoice(el);
        $("#tls_policy_input_div").i18n();
       	$("#tls_policy_input_div").show();
    });
});
