/**
* function to edit Mle Page ... 
*/

var isVMM = true;
var listOfmanifest = [];

//called on load
$(function() {
	$('#mainEditMleDisplayDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewMle.html', null, fnUpdateEditMleTable, null);
});

/*method to display edit table*/
function fnUpdateEditMleTable(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainTableDivEditMle').show();
		fuCreateEditMleTable(responseJSON.MLEDataVo);
		applyPagination('editMlePaginationDiv',responseJSON.noOfPages,fngetMleNextPageForEdit,1);
	}else {
		$('#errorEditMle').html(getHTMLEscapedMessage(responseJSON.message));
	}
}

/*function to get the value for give page no*/
function fngetMleNextPageForEdit(pageNo) {
	$('#mainEditMleDisplayDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewMleForPageNo.html', "pageNo="+pageNo, fnUpdateEditMleTableForPage, null);
}

function fnUpdateEditMleTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		fuCreateEditMleTable(responseJSON.MLEDataVo);
	}else {
		$('#errorEditMle').html(getHTMLEscapedMessage(responseJSON.message));
	}
}


/*Method to create dynamic edit table for MLE*/
function fuCreateEditMleTable(mleData) {
	var str = "";
	$('#editMleContentDiv table tbody').html("");
	for ( var items in mleData) {
                if(items != parseInt(items)) {
                        continue;
                }
		var classValue = null; 
		if(items % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
                        // Changing the display value for Module attestation to PCR + Module since we attest both.
                        var displayAttestationTypeValue = mleData[items].attestation_Type;
                        if (displayAttestationTypeValue == "Module" || displayAttestationTypeValue == "MODULE") {
                            displayAttestationTypeValue = moduleAttestationDisplayString;
                        }
		str+='<tr class="'+classValue+'">'+
		'<td class="rowr3" style="word-wrap: break-word;max-width:170px;" name="mleName"><a href="javascript:;" onclick="fnEditMleInfo(this)" data-toggle="tooltip" title="Edit Mle">'+ getHTMLEscapedMessage(mleData[items].mleName) +'</a></td>'+
		'<td class="row2" name="mleVersion">'+getHTMLEscapedMessage(mleData[items].mleVersion) +'</td>'+
		'<td class="rowr3" name="attestation_Type">'+ getHTMLEscapedMessage(displayAttestationTypeValue) +'</td>';
		var val1 = mleData[items].manifestList == undefined ? ' ' : mleData[items].manifestList;
		
		 //str+='<td class="rowr7" name="manifestList">'+val1+'&nbsp;</td>'+
		str+='<td class="row4" name="mleType">'+ getHTMLEscapedMessage(mleData[items].mleType) +'</td>';
		val1 = mleData[items].osName == undefined ? ' ' : mleData[items].osName;
		var val2 = mleData[items].osVersion == undefined ? ' ' : mleData[items].osVersion;
		str+='<td class="rowr4" name="osName" version="'+ escapeForHTMLAttributes(val2) +'" osName="' + escapeForHTMLAttributes(val1) +'">'+ getHTMLEscapedMessage(val1) +' ' + getHTMLEscapedMessage(val2) +'&nbsp;</td>';
		val1 = mleData[items].oemName == undefined ? ' ' : mleData[items].oemName;
		str+='<td class="rowr2" name="oemName">'+ getHTMLEscapedMessage(val1) +'&nbsp;</td>';
		val1 = mleData[items].mleDescription == undefined ? ' ' : mleData[items].mleDescription;
		str+='<td class="rowr3"  style="word-wrap: break-word;max-width:170px;"name="mleDescription">'+ getHTMLEscapedMessage(val1)+'&nbsp;</td>';
		str+='<td class="row1"><a href="javascript:;" onclick="fnDeleteMleInfo(this)" data-toggle="tooltip" title="Delete Mle"><span class="glyphicon glyphicon-trash"></span></a></td>';
		str+='</tr>';
	}
	$('#editMleContentDiv table tbody').html(str);
}

function fnEditMleInfo(element) {
	$('#messageSpace').html('');
	var data = [] ;
    var row = $(element).parent().parent();
    $(row).find("td:not(:first-child)").each(function(){
        var val = $.trim($(this).text());
        var name = $.trim($(this).attr('name'));
        data[name]=val;
    });
   	data["osVersion"]=$(row).find("td:eq(5)").attr('version');
   	data["osName"]=$(row).find("td:eq(5)").attr('osName');
    setLoadImage('mainContainer');
	sendHTMLAjaxRequest(false, 'getView/getAddMLEPage.html', null, fnEditMleData, null,data);
}

function fnGetMleDataForEdit(data) {
            // Need to change back the value for the module attestation from PCR + Module to Module during DB access
            if (data.attestation_Type == moduleAttestationDisplayString) {
                data.attestation_Type = 'MODULE';
            }           
	var dataToSend = "mleName="+data.mleName+"&mleVersion="+data.mleVersion+"&mleType="+data.mleType+"&attestation_Type="+data.attestation_Type;
	if (data.mleType == "VMM") {
		isVMM = true;
		dataToSend+="&osName="+data.osName;
		dataToSend+="&osVersion="+data.osVersion;
		updateMlePageForVMM();
		$('#MainContent_ddlMLEType').html('<option value="VMM" selected="selected">VMM</option>');
	}else{
		isVMM = false;
		dataToSend+="&oemName="+data.oemName;
		$('#MainContent_ddlMLEType').html('<option value="BIOS" selected="selected">BIOS</option>');
		updateMlePageForBIOS();
	}
	return dataToSend;
}

function fnEditMleData(response,data) {
	$('#mainContainer').html(response);
        $('#mainDataTableMle').prepend(disabledDiv);
	var dataToSend = fnGetMleDataForEdit(data);
	sendJSONAjaxRequest(false, 'getData/viewSingleMLEData.html', dataToSend, fnEditMleDataSuccess , null,dataToSend);
}

function fnEditMleDataSuccess(responseJson,dataToSend) {
	if (responseJson.result) {
                var mleSourceHostName = responseJson.mleSource;
		var response = responseJson.dataVo;
		hostNameList = [];
		hostNameList[0] = response;
		$('#disabledDiv').remove();
		
		var host = isVMM ? response.osName+" "+response.osVersion : response.oemName;
		
		$('#MainContent_ddlMLEType').attr('disabled','disabled');
		$('#MainContent_ddlHostOs').html('<option value="'+ escapeForHTMLAttributes(host) +'" >'+getHTMLEscapedMessage(host) +'</option>');
		$('#MainContent_ddlHostOs').attr('disabled','disabled');
		
		$('#mleTypeNameValue').html('<input id="MainContent_ddlMLEName" type="text" class="inputs textBox_Border" disabled="disabled" value="'+ escapeForHTMLAttributes(response.mleName) +'" >');
		
		$('#MainContent_tbVersion').attr('value', escapeForHTMLAttributes(response.mleVersion));
		$('#MainContent_tbVersion').attr('disabled','disabled');
                        // In the UI for Module attestation type, we want to show the user that Module attestation, both PCR and Modules
                        // will be verified. For that we will just update the UI for that
                        var displayAttestationTypeValue = response.attestation_Type;
                        if (response.attestation_Type == "Module" || response.attestation_Type == "MODULE") {
                            displayAttestationTypeValue = moduleAttestationDisplayString;
                        }
                        
                        $('#MainContent_ddlAttestationType').html('<option>'+displayAttestationTypeValue+'</option>');
		
		// $('#MainContent_ddlAttestationType').html('<option selected="selected">'+response.attestation_Type+'</option>');
		$('#MainContent_tbDesc').val(response.mleDescription);
		$('#MainContent_tbMleSourceHost').val(mleSourceHostName);
		
		if (response.attestation_Type == "Module" || response.attestation_Type == "MODULE") {
			//$('#mainfestGKVSCheck').remove();
			//$('#manifestListDiv').html('<div style="font-size: 14px;padding-top: 55px;width: 644px;">Please use the White List Manifest loader tool to load the manifest values to database.</div>');
			//$('#mleVmmLableInfo').hide();
			$('#mainfestGKVSCheck').show();
			$('#manifestListDiv').remove();
			$('#mainfestGKVSCheck').find('input:checkbox').attr("checked","checked");
			fnToggelManifestList(true);
			$('#mainfestGKVSCheck').find('input:checkbox').attr("disabled","disabled");
                        listOfmanifest = response.manifestList;
                        // Since we do not want the module to be updated, we will disabe it.
                        $('#MainContent_check_gkvs18').attr('disabled','disabled');
                        $('#MainContent_check_gkvs19').attr('disabled','disabled');
                        $('#MainContent_check_gkvs20').attr('disabled','disabled');
			for ( var pcr in response.manifestList) {
				$('#MainContent_check_gkvs'+response.manifestList[pcr].Name).attr('checked','checked');
                                //Bug:434 - Module manifests should not be allowed to change in this screen.
                                $('#MainContent_check_gkvs'+response.manifestList[pcr].Name).attr('disabled','disabled');
                                $('#MainContent_check_gkvs'+response.manifestList[pcr].Name).attr('value', escapeForHTMLAttributes(response.manifestList[pcr].Value));
			}
                        var str= '<div class="singleDiv"><div class="labelDiv">Manifest List :</div><div class="valueDiv">'+
				'<input type="button" class="button" value="Show Manifest" onclick="getModuleTypeMleList(\''+dataToSend+'\')"/></div></div>';
                        $('#moduleTypeManifestList').html(str);
			
		}else {
			for ( var pcr in response.manifestList) {
				fnToggelRegisterValue(true,'MainContent_tb'+response.manifestList[pcr].Name);
				$('#MainContent_check'+response.manifestList[pcr].Name).attr('checked','checked');
				$('#MainContent_tb'+response.manifestList[pcr].Name).attr('value',response.manifestList[pcr].Value);
			}
		}
                
                // Bug: 565 : For some reason dynamically changing the attribute of the button to call the UpdateMLE function 
                // is not working. As a workaround, we will create 2 buttons to start with, one button for adding the MLE and 
                // the second one for updating. If the Add MLE page, we will hide the "Update MLE" button and vice versa.
                
//              $('#addMleButton').attr("value", "Update MLE");
//		$('#addMleButton').attr("onclick", "updateMleInfo()");
                
	}else {
		$('#disabledDiv').remove();
		/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
		$('#mleMessage').html('<div class="errorMessage">'+responseJson.message+'</div>');
		/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
		//$('#mleMessage').html('<div class="errorMessage">* Server Error. '+responseJson.message+'</div>');
	}
}


function getModuleTypeMleList(dataToSend){
    var str = '<div id="mainContainerForWhiteList"></div>';
    fnOpenDialog(str, "manifest_list", 950, 600,false);
    
    $('#mainContainerForWhiteList').prepend(disabledDiv);
    sendJSONAjaxRequest(false, 'getData/getWhiteListForMle.html',dataToSend , getModuleTypeMleListSuccess, null);
}

function getModuleTypeMleListSuccess(responseJSON){
    if(responseJSON.result){
        var whiteList = responseJSON.whiteList;
        var str ="";
        str+='<div style="background-color: #3A4F63; color:#FFFFFF;"><table class="manifestModuleTypeTableHeader" width="100%" cellpadding="0" cellspacing="0">'+
              '<thead><tr>'+
              '<th class="manifestModuleTypeRow1">Component Name</th>'+
              '<th class="manifestModuleTypeRow2">Digest Value</th>'+
              '<th class="manifestModuleTypeRow3">Event Name</th>'+
              '<th class="manifestModuleTypeRow4">Package Name</th>'+
              '<th class="manifestModuleTypeRow5">Package Vendor</th>'+
              '<th class="manifestModuleTypeRow6">Package Version</th>'+
              '</tr></thead></table></div>';
          
          str+='<div class="manifestModuleTypeTableContent" style="overflow: auto;">'+
              '<table width="100%" cellpadding="0" cellspacing="0"><tbody>';
          
        for(var mani in listOfmanifest){
            str+='<tr>'+
                '<td class="manifestModuleTypeRow1" name="mleName">'+listOfmanifest[mani].Name+'</td>'+
                '<td class="manifestModuleTypeRow2" name="mleName">'+listOfmanifest[mani].Value+'</td>'+
                '<td class="manifestModuleTypeRow3" name="mleName">&nbsp;</td>'+
                '<td class="manifestModuleTypeRow4" name="mleName">&nbsp;</td>'+
                '<td class="manifestModuleTypeRow5" name="mleName">&nbsp;</td>'+
                '<td class="manifestModuleTypeRow6" name="mleName">&nbsp;</td>'+
                '</tr>';
            
        }
        for(var item in whiteList){
            str+='<tr>'+
                '<td class="manifestModuleTypeRow1" name="mleName">'+whiteList[item].componentName+'</td>'+
                '<td class="manifestModuleTypeRow2" name="mleName">'+whiteList[item].digestValue+'</td>'+
                '<td class="manifestModuleTypeRow3" name="mleName">'+whiteList[item].eventName+'</td>'+
                '<td class="manifestModuleTypeRow4" name="mleName">'+whiteList[item].packageName+'</td>'+
                '<td class="manifestModuleTypeRow5" name="mleName">'+whiteList[item].packageVendor+'</td>'+
                '<td class="manifestModuleTypeRow6" name="mleName">'+whiteList[item].packageVersion+'</td>'+
                '</tr>';
        }
        str+='</tbody> </table></div>';
        $('#mainContainerForWhiteList').html('<div style="width:99%;">'+str+'</div>');
        $('#disabledDiv').remove();
    }else{
        $('#mainContainerForWhiteList').html('<div class="errorMessage">'+responseJSON.message+'</div>');
    }
}

function updateMleInfo() {
	var dataToSent = fnGetMleData(false);
	if (dataToSent != "") {
		if (confirm($("#alert_update_mle").text())) {
			$('#mainDataTableMle').prepend(disabledDiv);
			sendJSONAjaxRequest(false, 'getData/getAddMle.html', "mleObject="+dataToSent+"&newMle=false", updateMleSuccess, null);
		}
	}
}

function updateMleSuccess(response) {
$('#disabledDiv').remove();
	if (response.result) {
		$('#mleMessage').html('<div class="successMessage">MLE has been successfully updated.</div>');
	}else{
		/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
		$('#mleMessage').html('<div class="errorMessage">'+response.message+'</div>');
		/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
		//$('#mleMessage').html('<div class="errorMessage">* Server Error. '+response.message+'</div>');
	}
}


function fnDeleteMleInfo(element) {
	//$("#dialog-confirm").html($("#alert_delete_mle").text());
	$("#dialog-confirm").dialog("open");
        // Define the Dialog and its properties.
        $("#dialog-confirm").dialog({
                resizable: false,
                modal: true,
                height: 250,
                width: 400,
                buttons: {
                        "Delete": function () {
                                $(this).dialog('close');
          			$('#messageSpace').html('');
                		var data = [] ;
            			var row = $(element).parent().parent();
            			row.find("td:not(:first-child)").each(function(){
                			var val = $.trim($(this).text());
                			var name = $(this).attr('name');
                			data[name]=val;
            			});
				data["osVersion"]=row.find("td:eq(5)").attr('version');
                		data["osName"]=row.find("td:eq(5)").attr('osName');
                		var mleName = $.trim(row.find("td:eq(1)").text()); 
                		var dataToSend = fnGetMleDataForDelete(data);
                		$('#mainTableDivEditMle').prepend(disabledDiv);
                		//$('#messageSpace').html('<div >* deleteing data. Please Wait....</div>');
                		sendJSONAjaxRequest(false, 'getData/deleteMLEData.html', dataToSend+"&selectedPageNo="+selectedPageNo, fnDeleteMleInfoSuccess , null,element,mleName);  
                        },
                                "Cancel": function () {
                                $(this).dialog('close');
                        }
                }
        });	
}

function fnGetMleDataForDelete(data) {
	var dataToSend = "mleName="+data.mleName+"&mleVersion="+data.mleVersion+"&mleType="+data.mleType+"&attestation_Type="+data.attestation_Type;
	if (data.mleType == "VMM") {
		isVMM = true;
		dataToSend+="&osName="+data.osName;
		dataToSend+="&osVersion="+data.osVersion;
	}else{
		isVMM = false;
		dataToSend+="&oemName="+data.oemName;
	}
	return dataToSend;
}

function fnDeleteMleInfoSuccess(response,element,mleName) {
	$('#disabledDiv').remove();
	if (response.result) {
		//$(element).parent().parent().remove();
		fuCreateEditMleTable(response.MLEDataVo);
		if (selectedPageNo > (response.noOfPages)) {
			selectedPageNo = response.noOfPages;
		}
		applyPagination('editMlePaginationDiv',response.noOfPages,fngetMleNextPageForEdit,selectedPageNo);
		$('#messageSpace').html('<div class="successMessage">* MLE "'+mleName+'" has been successfully deleted.</div>');
	}else{
		/* Soni_Begin_17/09/2012_issue_for_consistent_Error_Message  */
		//$('#messageSpace').html('<div class="errorMessage">* MLE "'+mleName+'" is not deleted, '+response.message+'</div>');
		$('#messageSpace').html('<div class="errorMessage">'+getHTMLEscapedMessage(response.message)+'</div>');
		/* Soni_End_17/09/2012_issue_for_consistent_Error_Message  */
	}
}
