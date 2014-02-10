/**
 * @author Yuvraj Singh
 */
var editrowCon = null;
var isEditMle = true;

//function called on load of page.
$(function() {
	
	//function to crate menu bar.
	createMenubar("menubarItems");
	getViewMle();
});

/**
 * function to get different Pages from server.
 */
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
	if (confirm("Are you sure you want to update OS Info ?")) {
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
	if (confirm("Are you sure you want to delete this OS ?")) {
		var data ='';
		var row = $(element).parent().parent();
		row.find("td:not(:first-child)").each(function(){
			var id=$(this).attr('id');
			data+=id+'='+$(this).attr('value')+"&";
		});
		$('#mainEditTable').prepend(disabledDiv);
		//$('#messageSpace').html('<div >* deleteing data. Please Wait....</div>');
		sendJSONAjaxRequest(false, 'getData/deleteOSData.html', data+"&selectedPageNo="+selectedPageNo, fnDeleteOSInfoSuccess , null,element,"OS \""+row.find("td:eq(1)").text()+"\"",true);
	}
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
		if (confirm("Are you sure want to add OS ?")) {
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
		if (confirm("Are you sure want to add OEM ?")) {
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
}

function fnUpdateOEMInfo(element) {
	if (confirm("Are you sure you want to update OEM Info ?")) {
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
	if (confirm("Are you sure you want to delete this OEM ?")) {
		var data ='';
		var row = $(element).parent().parent();
		row.find("td:not(:first-child)").each(function(){
			var id=$(this).attr('id');
			data+=id+'='+$.trim($(this).attr('value'))+"&";
		});
		$('#mainEditTable').prepend(disabledDiv);
		sendJSONAjaxRequest(false, 'getData/deleteOEMData.html', data+"&selectedPageNo="+selectedPageNo, fnDeleteOSInfoSuccess , null,element,"OEM \""+row.find("td:eq(1)").text()+"\"",false);
	}
}

function logoutUser() {
	setLoadImage('mainContainer');
	sendHTMLAjaxRequest(false, 'getData/logOutUser.html', null, displayLogingPage, null);
}

function displayLogingPage(responseHTML) {
	$('#mainContainer').parent().html(responseHTML);
}