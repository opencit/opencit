/**
 * @author Yuvraj Singh
 */

var selectedHostID = null;

//Called on load of Home.jsp
$(function() {
	//create a menu bar.
	createMenubar("NavigationMenu");
	getDashBoardPage();
});

/**
 * function to get different Pages from server.
 */
function getDashBoardPage() {
	$('#mainContainer').html('<div id="DashBoardPage"></div>');
	setLoadImage('DashBoardPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getDashBoardPage.html', null, fnDisplayContent, null,"DashBoardPage");
}

function getAddHostPage() {
	isAddHostPage = true;
	$('#mainContainer').html('<div id="AddHostPage"></div>');
	setLoadImage('AddHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getAddHostPage.html', null, fnDisplayContent, null,'AddHostPage');
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


/*
 * Method for Edit Host Page .... !!
 */

function fnEditHostInfo(element) {
	isAddHostPage = false;
	selectedHostID = $(element).parent().parent().find('td:eq(1)').text();
	setLoadImage('mainContainer', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getAddHostPage.html', null, fnEditGetAddHostSuccess, null,element);
}

function fnEditGetAddHostSuccess(response,element) {
	$('#mainContainer').html(response);
	$('#mainHeader').text("Update Host Configuration");
	$('#addHostButton').attr('value','Update Host');
	$('#addHostButton').attr('onclick','updateHostInfo()');
}

function fnFillAddHostPageDataForEdit(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#MainContent_tbHostName').val(responseJSON.hostData.hostName);
        $('#MainContent_tbHostName').attr('disabled','disabled');
        var value = responseJSON.hostData.hostIPAddress == 'null' || responseJSON.hostData.hostIPAddress == undefined ? "" : responseJSON.hostData.hostIPAddress;
		$('#MainContent_tbHostIP').val(value);
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
	}else {
		$('#mleMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
	}
}
 
function updateHostInfo() {
	if (chechAddHostValidation()) {
		if (confirm("Are you sure you want to update this Host ?")) {
			var dataToSend = fnGetNewHostData();
			dataToSend.hostId = selectedHostID;
			dataToSend = $.toJSON(dataToSend);
			$('#mainAddHostContainer').prepend(disabledDiv);
			$('#mleMessage').html('');
			sendJSONAjaxRequest(false, 'getData/saveNewHostInfo.html', "hostObject="+dataToSend+"&newhost=false", fnSaveNewHostInfoSuccess, null,"Host has been successfully updated.");
		}
	}
}


/*
* Function for delete host in Edit Host page.
*/

function fnDeleteHostInfo(element) {
	if(confirm("Are you sure you want to delete this Host ?")){
		var selectedHost = $(element).parent().attr('hostID');
		var hostName = $(element).parent().parent().find('td:eq(1)').text();
		$('#mainAddHostContainer').prepend(disabledDiv);
		$('#mleMessage').html('');
		sendJSONAjaxRequest(false, 'getData/deleteHostDetails.html', "hostID="+selectedHost+"&hostName="+hostName+"&selectedPageNo="+selectedPageNo, fnDeleteHostInfoSuccess,null,element);
	}
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

function logoutUser() {
	setLoadImage('mainContainer', '40px', '510px');
	sendHTMLAjaxRequest(false, 'getData/logOutUser.html', null, displayLogingPage, null);
}

function displayLogingPage(responseHTML) {
	$('#mainContainer').parent().html(responseHTML);
}