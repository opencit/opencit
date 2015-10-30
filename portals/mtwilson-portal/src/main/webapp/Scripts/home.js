/**
 * @author Yuvraj Singh
 */

$(function() {
	createMenubar("menubarItems");
//	getApproveRequestPage();
	getDashBoardPage();
//	getViewMle();

});

/**
 * function to get different Pages from server.
 */
function getWhiteListConfigurationPage() {
	$('#mainContainer').html('<div id="WhiteListConfigurationPage"></div>');
	setLoadImage('WhiteListConfigurationPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getWhiteListConfigurationPage.html', null, fnDisplayContent, null,'WhiteListConfigurationPage');
}
function getRegisterHostPage() {
	$('#mainContainer').html('<div id="RegisterHostPage"></div>');
	setLoadImage('RegisterHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getRegisterHostPage.html', null, fnDisplayContent, null,'RegisterHostPage');
}
function getApproveRequestPage() {
	$('#mainContainer').html('<div id="ApproveRequestPage"></div>');
	setLoadImage('ApproveRequestPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getApproveRequestPage.html', null, fnDisplayContent, null,'ApproveRequestPage');
}

function getViewExpiringPage() {
	$('#mainContainer').html('<div id="viewExpiringPage"></div>');
	setLoadImage('viewExpiringPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewExpiringPage.html', null, fnDisplayContent, null,'viewExpiringPage');
}
function getDeletePendingRegistration() {
	$('#mainContainer').html('<div id="deletePendingRegistration"></div>');
	setLoadImage('deletePendingRegistration', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getDeleteRegistrationPage.html', null, fnDisplayContent, null,'deletePendingRegistration');
}
function getViewRequest() {
	$('#mainContainer').html('<div id="getViewRequest"></div>');
	setLoadImage('getViewRequest', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewRequestPage.html', null, fnDisplayContent, null,'getViewRequest');
}

function getAddHostPage() {
	isAddHostPage = true;
	$('#mainContainer').html('<div id="AddHostPage"></div>');
	setLoadImage('AddHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getAddHostPage.html', null, fnDisplayContent, null,'AddHostPage');
}

/*--Begin Added by Soni on 18/10/12 for New Screen for CA */
function getCAStatus() {
	$('#mainContainer').html('<div id="getCAStatus"></div>');
	setLoadImage('getCAStatus', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getCAStatusPage.html', null, fnDisplayContent, null,'getCAStatus');
}
/*--End Added by Soni on 18/10/12 for New Screen for CA */
/*--Begin Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function downloadSAML() {
	$('#mainContainer').html('<div id="getSAML"></div>');
	setLoadImage('getSAML', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getSAMLCertificatePage.html', null, fnDisplayContent, null,'getSAML');
}
/*--Begin Added by stdale on 1/8/13 for New Screen for view cert */
function viewCert() {
	$('#mainContainer').html('<div id="getViewCert"></div>');
	setLoadImage('getViewCert', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewCertPage.html', null, fnDisplayContent, null,'getViewCert');
}
/*--End Added by stdale on 1/8/13 for New Screen for view cert */

function fnGetEditOS() {
	$('#mainContainer').html('<div id="EditOSPage"></div>');
	setLoadImage('EditOSPage');
	sendHTMLAjaxRequest(false, 'getView/getEditOSPage.html', null, fnDisplayContent, null,'EditOSPage');
}

function getAddMLE() {
	$('#mainContainer').html('<div id="AddMLEPage"></div>');
	setLoadImage('AddMLEPage');
	isEditMle = false;
	sendHTMLAjaxRequest(false, 'getView/getAddMLEPage.html', null, fnDisplayContent, null,'AddMLEPage');
}
function getViewMle() {
	$('#mainContainer').html('<div id="ViewMle"></div>');
	setLoadImage('ViewMle');
	sendHTMLAjaxRequest(false, 'getView/getViewMle.html', null, fnDisplayContent, null,'ViewMle');
	}
function getEditMle() {
	$('#mainContainer').html('<div id="EditMle"></div>');
	setLoadImage('EditMle');
	isEditMle = true;
	sendHTMLAjaxRequest(false, 'getView/getEditMle.html', null, fnDisplayContent, null,'EditMle');
}

function fnViewAllOEM() {
	$('#mainContainer').html('<div id="ViewOEMPage"></div>');
	setLoadImage('ViewOEMPage');
	sendHTMLAjaxRequest(false, 'getView/getViewOEMPage.html', null, fnDisplayContent, null,'ViewOEMPage');
}

function fnEditOEM() {
	$('#mainContainer').html('<div id="EditOEMPage"></div>');
	setLoadImage('EditOEMPage');
	sendHTMLAjaxRequest(false, 'getView/getEditOEMPage.html', null, fnDisplayContent, null,'EditOEMPage');
}
function fnAddOEM() {
	$('#mainContainer').html('<div id="AddOEMPage"></div>');
	setLoadImage('AddOEMPage');
	sendHTMLAjaxRequest(false, 'getView/getAddOEMPage.html', null, fnDisplayContent, null,'AddOEMPage');
}
function getAboutWlm() {
	$('#mainContainer').html('<div id="AboutPage"></div>');
	setLoadImage('AboutPage');
	sendHTMLAjaxRequest(false, 'getView/getAboutPage.html', null, fnDisplayContent, null,'AboutPage');
}

function getDashBoardPage() {
	$('#mainContainer').html('<div id="DashBoardPage"></div>');
	setLoadImage('DashBoardPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getDashBoardPage.html', null, fnDisplayContent, null,"DashBoardPage");
}

function getAssetTagPage() {
    //setLoadImage('AddHostPage', '40px', '500px');
    //var serverAddy = <% out.print(com.intel.mtwilson.My.configuration().getAssetTagServerString();) %>;
    $('#mainContainer').html('<div id="AssetTagPage"></div>');
    $('#AssetTagPage').html('<iframe id="AssetTagIFrame" scrolling="no" frameborder="0" src="tag/index.html5?tab=tags" width="100%" height="1000" > </iframe>');
    //$('#AssetTagPage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/tags.html" + '" width="100%" height="1000" > </iframe>');
            //'<iframe height="410" width="100%" frameBorder="3" src="http://www.google.com.au/webhp?sourceid=navclient&ie=UTF-8/index.php">your browser does not support IFRAMEs</iframe>'    
    //window.open("https://127.0.0.1:9999/",'mywin','left=20,top=20,width=865,height=725,toolbar=1,resizable=0');        
}

function getAssetSelectionPage() {
	$('#mainContainer').html('<div id="AssetSelectionPage"></div>');
        //$('#AssetSelectionPage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/selections.html" + '" width="100%" height="1000" > </iframe>');
//        $('#AssetSelectionPage').html('<iframe  scrolling="no" frameborder="0" src="' + assetTagUrl + "/index_old.html"  + "/#selections" + '" width="100%" height="1000" > </iframe>');
        $('#AssetSelectionPage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=selections" width="100%" height="1000" > </iframe>');
    }

function getAssetCertificatePage() {
	$('#mainContainer').html('<div id="AssetCertificatePage"></div>');
        //$('#AssetCertificatePage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/certificates.html" + '" width="100%" height="2000" > </iframe>');
        $('#AssetCertificatePage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=certificates" width="100%" height="1000" > </iframe>');
}

function getTagProvisioningPage() {
        $('#mainContainer').html('<div id="TagProvisionPage"></div>');
        $('#TagProvisionPage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=provisionTag" width="100%" height="1000" > </iframe>');
}

function getAssetSettingsPage() {
	$('#mainContainer').html('<div id="AssetSettingsPage"></div>');
        //$('#AssetSettingsPage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/settings.html" + '" width="100%" height="1500" > </iframe>');
        $('#AssetSettingsPage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=configure" width="100%" height="1000" > </iframe>');
}

function getAssetLogPage() {
	$('#mainContainer').html('<div id="AssetLogPage"></div>');
        //$('#AssetLogPage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/log.html" + '" width="900" height="900" > </iframe>');
        $('#AssetLogPage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=log" width="100%" height="1000" > </iframe>');
}

function getTlsPolicyManagementPage() {
	$('#mainContainer').html('<div id="TlsPolicyManagementPage"></div>');
        //$('#AssetCertificatePage').html('<iframe scrolling="no" frameborder="0" src="' + assetTagUrl + "/certificates.html" + '" width="100%" height="2000" > </iframe>');
    //$('#TlsPolicyManagementPage').html('<iframe  scrolling="no" frameborder="0" src="tag/index.html5?tab=tls_policies" width="100%" height="1000" > </iframe>');
	sendHTMLAjaxRequest(false, 'TlsPolicyManagement.html5', null, fnDisplayContent, null,'TlsPolicyManagementPage');
}


function getViewHostPage() {
	$('#mainContainer').html('<div id="ViewHostPage"></div>');
	setLoadImage('ViewHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewHostPage.html', null, fnDisplayContent, null,'ViewHostPage');
}

function getEditHostPage() {
	$('#mainContainer').html('<div id="EditHostPage"></div>');
	setLoadImage('EditHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getEditHostPage.html', null, fnDisplayContent, null,'EditHostPage');
}
function getShowReportPage() {
	$('#mainContainer').html('<div id="showReportsPage"></div>');
	setLoadImage('showReportsPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/showReportsPage.html', null, fnDisplayContent, null,'showReportsPage');
}

function fnDisplayContent(response,inputDivID) {

	$('#'+inputDivID).html(response);
}

function fnEditOSInfo(element){
	$('#messageSpace').html('');
	editrowCon = $(element).parent().html();
    var row = $(element).parent().parent();
    row.find("td:last-child").each(function(){
        var val = $(this).attr('value');
        $(this).html('<input type="text" class="edit_textbox" value="'+val+'" name="'+val+'" />');
    });
    $(element).parent().html('<a href="javascript:;" onclick="fnUpdateOSInfo(this)">Update</a><span> | </span><a href="javascript:;" onclick="fnCancelOSInfo(this)"> Cancel </a>');
	$(".edit_textbox").focus();
}

function fnUpdateOSInfo(element) {
	var row = $(element).parent().parent();
	$('#messageSpace').html("");
	var decName = row.find("td:eq(3)").find('input').val();
	/* Soni_Begin_18/09/2012_issue_RC2:_Unable_to_remove_the_description_for_an_OS_Bug_387  */
	if ( !(decName.length== 0))
	{
		if (!(normalReg.test(decName))) {
		$('#messageSpace').html(validationSpecialDiv);
        return false;
     }
		}
	/* Soni_End_18/09/2012_issue_RC2:_Unable_to_remove_the_description_for_an_OS_Bug_387  */
	if (confirm($("#alert_update_os_info").text())) {
		var data="";
		var row = $(element).parent().parent();
		row.find("td:not(:first-child)").each(function(){
			var id=$(this).attr('id');
			data+=id+'='+$(this).attr('value')+"&";
		});
		data+="inputDec="+$.trim(row.find("td:last-child").find(':input').val());
		$('#mainEditTable').prepend(disabledDiv);
		sendJSONAjaxRequest(false, 'getData/updateOSData.html', data+"&selectedPageNo="+selectedPageNo, fnUpdatedOSValue, null,element,"OS \""+row.find("td:eq(1)").text()+"\"",true);
	}
}

function fnCancelOSInfo(element) {
	$('#messageSpace').html('');
	var row = $(element).parent().parent();
    row.find("td:not(:first-child)").each(function(){
        var val = $(this).attr('name');
        $(this).html(val);
    });
	$(element).parent().html(editrowCon);
}

function fnDeleteOSInfo(element) {
		$("#dialog-confirm").remove();
		var str = '<div id="dialog-confirm" title="Delete OS?" style="display:none;"><p>Are you sure you want to delete this OS?</p></div>';
		$('.container').append(str);
        // Define the Dialog and its properties.
        $("#dialog-confirm").dialog({
                resizable: false,
                modal: true,
                height: 150,
                width: 400,
                buttons: {
                        "Delete": function () {
                                $(this).dialog('close');
                		var data ='';
                		var row = $(element).parent().parent();
                		row.find("td:not(:first-child)").each(function(){
                        		var id=$(this).attr('id');
                        		data+=id+'='+$(this).attr('value')+"&";
                		});
                		$('#mainEditTable').prepend(disabledDiv);
                		//$('#messageSpace').html('<div >* deleteing data. Please Wait....</div>');
		                sendJSONAjaxRequest(false, 'getData/deleteOSData.html', data+"&selectedPageNo="+selectedPageNo, fnDeleteOSInfoSuccess , null,element,"OS \""+row.find("td:eq(1)").text()+"\"",true);

                                
                        },
                                "Cancel": function () {
                                $(this).dialog('close');
                        }
                }
        });	
}

function fnDeleteOSInfoSuccess(response,element,dataName,isOS) {
	$('#disabledDiv').remove();
	if (response.result) {
		//$(element).parent().parent().remove();
		if (selectedPageNo > (response.noOfPages)) {
			selectedPageNo = response.noOfPages;
		}
		if (isOS) {
			fuCreateEditOSTable(response.OSDataVo);
			applyPagination('editOSPaginationDiv',response.noOfPages,fngetOSNextPageForEdit,selectedPageNo);
		}else {
			fuCreateEditOEMTable(response.OEMDataVo);
			applyPagination('editOEMPaginationDiv',response.noOfPages,fngetOEMNextPageForEdit,selectedPageNo);
		}
		$('#messageSpace').html('<div class="successMessage">* '+dataName+' has been successfully deleted.</div>');
	}else{
		$('#messageSpace').html('<div class="errorMessage">* '+dataName+' is not deleted, '+response.message+'.</div>');
	}
}

function fnUpdatedOSValue(response,element,dataName,isOS) {
	$('#disabledDiv').remove();
	if (response.result) {
		//$(element).parent().parent().remove();
		if (isOS) {
			fuCreateEditOSTable(response.OSDataVo);
			applyPagination('editOSPaginationDiv',response.noOfPages,fngetOSNextPageForEdit,selectedPageNo);
		}else {
			fuCreateEditOEMTable(response.OEMDataVo);
			applyPagination('editOEMPaginationDiv',response.noOfPages,fngetOEMNextPageForEdit,selectedPageNo);
		}
		$('#messageSpace').html('<div class="successMessage">* '+dataName+' has been successfully Updated.</div>');
		
	}else {
		$('#messageSpace').html('<div class="errorMessage">* '+dataName+' is not Updated, '+response.message+'.</div>');
	}
}



/**
 * Method used to View All OS.....
 */

function fnViewAllOS() {
	$('#mainContainer').html('<div id="ViewOSPage"></div>');
	setLoadImage('ViewOSPage');
	sendHTMLAjaxRequest(false, 'getView/getViewOSPage.html', null, fnDisplayContent, null,'ViewOSPage');
}

function fnGetAddOS() {
	$('#mainContainer').html('<div id="getAddOSPage"></div>');
	setLoadImage('getAddOSPage');
	sendHTMLAjaxRequest(false, 'getView/getAddOSPage.html', null, fnDisplayContent, null,'getAddOSPage');
}

function fnAddNewOS(element) {
	$('#messageSpace').html('');
	var option = [];
	var data='';
	option = fnValidateOSAndOEMData(element);
	data = option.data;
	var dec =	$(element).parent().parent().parent().find('tr:eq(2)').find('td:eq(1)');
	data+=dec.find(':input(:text)').attr('name')+'='+dec.find(':input(:text)').val();
	if (option.error) {
		return false;
	}else {
		if (confirm($("#alert_add_os").text())) {
			$('#addOSDataTable').prepend(disabledDiv);
			sendJSONAjaxRequest(false, 'getData/addOSData.html', data, fnAddNewOSSuccess, null,element);
		}
	}
	
}

function fnAddNewOSSuccess(response) {
	$('#disabledDiv').remove();
	if (response.result) {
		$('#messageSpace').html('<div class="successMessage">* OS has been Successfully Added.</div>');
		resetDataTable('addOSDataTable');
	}else {
		$('#messageSpace').html('<div class="errorMessage">* OS is not Added, Server Error. '+response.message+'</div>');
	}
}

function resetDataTable(elementID) {
	$('#'+elementID).find('tr:not(:last-child)').each(function() {
		if ($(this).find(':input(:text)').html() != null) {
			$(this).find(':input(:text)').val('');
		}
	});
}


function fnValidateOSAndOEMData(element) {
	var option = [];
	var error = false;
	var data = "";
	$(element).parent().parent().parent().find('tr:not(:last-child :eq(2))').each(function() {
		if ($(this).find(':input(:text)').html() != null) {
			if ($(this).find(':input(:text)').val() == '') {
				$(this).find('td:last-child').html(validationDiv);
				error= true;
			}else{
				if (!normalReg.test($(this).find(':input(:text)').val())) {
					$(this).find('td:last-child').html(validationSpecialDiv);
					error= true;
				}else{
					$(this).find('td:last-child').html('');
					var val = $(this).find(':input(:text)').val();
					var name = $(this).find(':input(:text)').attr('name');
					data+=name+'='+val+'&';
				}
			}
			
		}
	});
	option["error"]=error;
	option["data"]=data;
	return option;
}

/*
 * function for OEM Component.
 */

function fnAddOemData(element) {
	$('#messageSpace').html('');
	var option = [];
	var data='';
	option = fnValidateOSAndOEMData(element);
	data = option.data;
	
	var dec =	$(element).parent().parent().parent().find('tr:eq(2)').find('td:eq(1)');
	data+=dec.find(':input(:text)').attr('name')+'='+dec.find(':input(:text)').val();
	if (option.error) {
		return false;
	}else {
		if (confirm($("#alert_add_oem").text())) {
			//$('#messageSpace').html('<div>* Adding OEM. Please Wait....</div>');
			$('#addOEMDataTable').prepend(disabledDiv);
			sendJSONAjaxRequest(false, 'getData/addOEMData.html', data, fnAddOemSuccess, null,element);
		}
	}
}

function fnAddOemSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#messageSpace').html('<div class="successMessage">* OEM has been Successfully Added.</div>');
		resetDataTable('addOEMDataTable');
	}else {
		$('#messageSpace').html('<div class="errorMessage">* OEM is not Added, Server Error. '+responseJSON.message+'</div>');
	}
}

function fnEditOEMInfo(element){
	$('#messageSpace').html('');
	editrowCon = $(element).parent().html();
    var row = $(element).parent().parent();
    row.find("td:last-child").each(function(){
        var val = $(this).attr('value');
        $(this).html('<input type="text" class="edit_textbox" value="'+val+'" name="'+val+'" />');
    });
    $(element).parent().html('<a href="javascript:;" onclick="fnUpdateOEMInfo(this)">Update</a><span> | </span><a href="javascript:;" onclick="fnCancelOSInfo(this)"> Cancel </a>');
	$(".edit_textbox").focus();
}

function fnUpdateOEMInfo(element) {
	if (confirm($("#alert_update_oem_info").text())) {
		var data="";
		var row = $(element).parent().parent();
		row.find("td:not(:first-child)").each(function(){
			var id=$(this).attr('id');
			data+=id+'='+$(this).attr('value')+"&";
		});
		data+="inputDec="+$.trim(row.find("td:last-child").find(':input').val());
		$('#mainEditTable').prepend(disabledDiv);
		sendJSONAjaxRequest(false, 'getData/updateOEMData.html', data+"&selectedPageNo="+selectedPageNo, fnUpdatedOSValue, null,element,"OEM \""+row.find("td:eq(1)").text()+"\"",false);
	}
}

function fnDeleteOemInfo(element) {
		$("#dialog-confirm").remove();
		var str = '<div id="dialog-confirm" title="Delete OEM?" style="display:none;"><p>Are you sure you want to delete this OEM?</p></div>';
		$('.container').append(str);
        // Define the Dialog and its properties.
        $("#dialog-confirm").dialog({
                resizable: false,
                modal: true,
                height: 150,
                width: 400,
                buttons: {
                        "Delete": function () {
                                $(this).dialog('close');
                           	var data ='';
               	 		var row = $(element).parent().parent();
                		row.find("td:not(:first-child)").each(function(){
                        		var id=$(this).attr('id');
                        		data+=id+'='+$.trim($(this).attr('value'))+"&";
                		});
                		$('#mainEditTable').prepend(disabledDiv);
		                sendJSONAjaxRequest(false, 'getData/deleteOEMData.html', data+"&selectedPageNo="+selectedPageNo, fnDeleteOSInfoSuccess , null,element,"OEM \""+row.find("td:eq(1)").text()+"\"",false);
                        },
                                "Cancel": function () {
                                $(this).dialog('close');
                        }
                }
        });	
}


/*
 * Method for Edit Host Page .... !!
 */

function fnEditHostInfo(element) {
	isAddHostPage = false;
	selectedHostID = $(element).parent().parent().find('td:eq(1)').text();
	setLoadImage('mainContainer', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getAddHostPage.html', null, fnEditGetAddHostSuccess, null,element);
}

function fnEditHostPageInfo(element) {
        isAddHostPage = false;
        selectedHostID = $(element).parent().parent().find('td:eq(0)').text();
        setLoadImage('mainContainer', '40px', '500px');
        sendHTMLAjaxRequest(false, 'getView/getAddHostPage.html', null, fnEditGetAddHostSuccess, null,element);
}

function fnEditGetAddHostSuccess(response,element) {
	$('#mainContainer').html(response);
	$('#mainHeader').text("Update Host Configuration");
	//$('#addHostButton').attr('value','Update Host');
	//$('#addHostButton').attr('onclick','updateHostInfo()');
        $('#updateHostButton').show();
        $('#addHostButton').hide();
}

function fnFillAddHostPageDataForEdit(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#MainContent_tbHostName').val(responseJSON.hostData.hostName);
        $('#MainContent_tbHostName').attr('disabled','disabled');
        var value = responseJSON.hostData.hostIPAddress == 'null' || responseJSON.hostData.hostIPAddress == undefined ? "" : responseJSON.hostData.hostIPAddress;
		//$('#MainContent_tbHostIP').val(value);
		value = responseJSON.hostData.hostPort == 'null' || responseJSON.hostData.hostPort == undefined ? "" : responseJSON.hostData.hostPort;
		$('#MainContent_tbHostPort').val(value);
		value = responseJSON.hostData.hostDescription == 'null' || responseJSON.hostData.hostDescription == undefined ? "" : responseJSON.hostData.hostDescription;
		$('#MainContent_tbDesc').val(value);
		
		$('#MainContent_ddlOEM option').each(function() {
			if ($(this).text() === responseJSON.hostData.oemName) {
				$(this).attr('selected','selected');
			};
		});
		$('#MainContent_ddlOEM').trigger('change');
		$('#MainContent_LstBIOS option').each(function() {
			if ($(this).text() === responseJSON.hostData.biosName+" "+responseJSON.hostData.biosBuildNo) {
				$(this).attr('selected','selected');
			};
		});
		
		$('#MainContent_LstVmm option').each(function() {
			if ($(this).text() === (responseJSON.hostData.vmmName+":"+responseJSON.hostData.vmmBuildNo)) {
				$(this).attr('selected','selected');
			};
		});
		$('#MainContent_LstVmm').trigger('change');
		if (responseJSON.hostData.vCenterDetails != 'null' && responseJSON.hostData.vCenterDetails!=undefined) {
			$('#MainContent_tbVCenterAddress').val(getVCeterHostIpAddress(responseJSON.hostData.vCenterDetails.split(";")[0]));
			$('#MainContent_tbVCenterLoginId').val(responseJSON.hostData.vCenterDetails.split(";")[1]);
			$('#MainContent_tbVCenterPass').val(responseJSON.hostData.vCenterDetails.split(";")[2]);
		}
		value = responseJSON.hostData.emailAddress == 'null' || responseJSON.hostData.emailAddress == undefined ? "" : responseJSON.hostData.emailAddress;
		$('#MainContent_tbEmailAddress').val(value);
        
        if( responseJSON.hostData.tlsPolicyId ) {
            // tls_policy_select is populated by tls_policy.js so we also store the selected value in a property in case the options haven't been populated yet
            $('#tls_policy_select').prop('data-selected', responseJSON.hostData.tlsPolicyId);
            $('#tls_policy_select').val(responseJSON.hostData.tlsPolicyId);
        }
        if( responseJSON.hostData.tlsPolicyType ) {
            $('#tls_policy_select').prop('data-selected', "private-"+responseJSON.hostData.tlsPolicyType);
            $('#tls_policy_select').val("private-"+responseJSON.hostData.tlsPolicyType);
            if( responseJSON.hostData.tlsPolicyData ) {
                if( responseJSON.hostData.tlsPolicyType == "certificate" ) {
                    $('#tls_policy_data_certificate').val(responseJSON.hostData.tlsPolicyData);
                }
                if( responseJSON.hostData.tlsPolicyType == "certificate-digest" ) {
                    $('#tls_policy_data_certificate_digest').val(responseJSON.hostData.tlsPolicyData);
                }
                if( responseJSON.hostData.tlsPolicyType == "public-key" ) {
                    $('#tls_policy_data_public_key').val(responseJSON.hostData.tlsPolicyData);
                }
                if( responseJSON.hostData.tlsPolicyType == "public-key-digest" ) {
                    $('#tls_policy_data_public_key_digest').val(responseJSON.hostData.tlsPolicyData);
                }
                $('#tls_policy_select').change();
            }
        }
        
        
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
	}
}
 
function updateHostInfo() {
    var ipValid = true;
        if (isVmware == 0 || isVmware == 2) { // intel and citrix hosts
         //if(!fnValidateIpAddress($('#MainContent_tbHostIP').val())) {
         //    ipValid=false;
         //}   
        }else{ // vmware host
          if(!fnValidateIpAddress($('#MainContent_tbVCenterAddress').val())) {
             ipValid=false;
         }    
        }

        if(ipValid == true) {
            if (chechAddHostValidation()) {
                        // Bug: 727
		//if (confirm("Are you sure you want to update this Host ?")) {
			var dataToSend = fnGetNewHostData();
			dataToSend.hostId = selectedHostID;
			dataToSend = $.toJSON(dataToSend);
			$('#mainAddHostContainer').prepend(disabledDiv);
			$('#mleMessage').html('');
			sendJSONAjaxRequest(false, 'getData/saveNewHostInfo.html', "hostObject="+dataToSend+"&newhost=false", fnSaveNewHostInfoSuccess, null,"Host has been successfully updated.");
		//}
            }
        }else{
            alert($("#alert_valid_ip").text());
        }
}


/*
* Function for delete host in Edit Host page.
*/

function fnDeleteHostInfo(element) {
	$("#dialog-confirm").remove();
	var str = '<div id="dialog-confirm" title="Delete Host?" style="display:none;"><p>Are you sure you want to delete this host?</p></div>';
	$('.container').append(str);
        // Define the Dialog and its properties.
        $("#dialog-confirm").dialog({
                resizable: false,
                modal: true,
                height: 150,
                width: 400,
                buttons: {
                        "Delete": function () {
                                $(this).dialog('close');
				var selectedHost = $(element).parent().attr('hostID');
				var hostName = $(element).parent().parent().find('td:eq(0)').text();
                		$('#mainAddHostContainer').prepend(disabledDiv);
                		$('#mleMessage').html('');
                		sendJSONAjaxRequest(false, 'getData/deleteHostDetails.html', "hostID="+selectedHost+"&hostName="+hostName+"&selectedPageNo="+selectedPageNo, fnDeleteHostInfoSuccess,null,element);
                        },
                                "Cancel": function () {
                                $(this).dialog('close');
                        }
                }
        });	
}

function fnDeleteHostInfoSuccess(response,element) {
	$('#disabledDiv').remove();
	if (response.result) {
		if (selectedPageNo > (response.noOfPages)) {
			selectedPageNo = response.noOfPages;
		}
		populateEditHostDataIntoTable(response.hostVo);
		applyPagination('editHostPaginationDiv',response.noOfPages,fngetEditHostNextPage,selectedPageNo);
		//$(element).parent().parent().remove();
		$('#mleMessage').html('<div class="successMessage">Host has been successfully deleted.</div>');
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(response.message)+'</div>');
	}
}

/*function updateDBForMle() {
	$('#mainContainer').html('<div id="updateDBLoading"></div>');
	setLoadImage('updateDBLoading', '40px', '500px');
	$('#mainContainer').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/updateDB.html', null, updateDBSuccess, null);
}*/

/*function updateDBSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		//getDashBoardPage();
		fnOpenDialog("Database is Updated Successfully. Click OK to close this Dialog Box.","Success",300,190,false);
	}else {
		fnOpenDialog('<span class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</span>',"Error",300,190,false);
		//$('#mainContainer').html('<div style="margin-top:40px;margin-left:40px;float:left;width:100%;" class="errorMessage">'++'</div>');
	}
}*/

function bulktrustUpdate() {
	$('#mainContainer').html('<div id="bulktrustUpdatePage"></div>');
	setLoadImage('bulktrustUpdatePage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/showbulktrustUpdatePage.html', null, fnDisplayContent, null,'bulktrustUpdatePage');
}


/*--Begin Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function fnDisplayContent(response,elementIDToBePublised) {
	$('#'+elementIDToBePublised).html(response);
}
/*--End Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function logoutUser() {
	setLoadImage('mainContainer', '40px', '510px');
	sendHTMLAjaxRequest(false, 'getData/logOutUser.html', null, displayLogingPage, null);
}

function displayLogingPage() { //responseHTML) {
        //$('#mainContainer').parent().html(responseHTML);
        window.location.replace("Login.jsp");
}

function openPreferences() {
    $('#mainContainer').html('<div id="PreferencesPage"></div>');
    setLoadImage('PreferencesPage', '40px', '500px');
    sendHTMLAjaxRequest(false, 'getData/openPreferences.html',"username="+$('#sessionUser').text(), fnDisplayContent, null, 'PreferencesPage');
}

//function displayPreferencesPage(responseHTML) {
//    $('#mainContainer').parent().html(responseHTML);
//}
if (typeof console == "undefined") {
    window.console = {
        log: function () {}
    };
}

