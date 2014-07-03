var selectedHost = [];
$(function() {
	$('#mainConatinerForBulkPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllHostForView.html', null, populateHostBulkDetails, null);
});

function populateHostBulkDetails(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainTableForReports').show();
		populateBulkHostDataIntoTable(responseJSON.hostVo);
		applyPagination('bulkHostPaginationDiv',responseJSON.noOfPages,fngetBulkHostNextPage,1);
		fnSelectAllCheckBox(true);
	}else {
		$('#errorMessage').html('<span class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</span>');
	}
}

function populateBulkHostDataIntoTable(hostDetails) {
	var str = "";
		for ( var item in hostDetails) {
			selectedHost[hostDetails[item].hostName] = true;
			var classValue = null;
			var checked = "";
			if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
			if(selectedHost[hostDetails[item].hostName] != undefined ){checked="checked";}
			str+='<tr class="'+classValue+'">'+
			'<td class="reportViewRow1"><input type="checkbox" checked='+checked+' onclick="selectHostForUpdate(this,checked)"></td>'+
			'<td class="reportViewRow2">'+ getHTMLEscapedMessage(hostDetails[item].hostName)+'</td>'+
			'<td class="reportViewRow3">'+ getHTMLEscapedMessage(hostDetails[item].biosName)+'</td>'+
			'<td class="reportViewRow4">'+ getHTMLEscapedMessage(hostDetails[item].biosBuildNo)+'&nbsp;</td>'+
			'<td class="reportViewRow5">'+ getHTMLEscapedMessage(hostDetails[item].vmmName)+'&nbsp;</td>'+
			'<td class="reportViewRow6">'+ getHTMLEscapedMessage(hostDetails[item].vmmBuildNo)+'&nbsp;</td>'+
			'</tr>';
		}
		$('#mainBulkHostDetailsContent table').html(str);
}

function fngetBulkHostNextPage(pageNo) {
	$('#errorMessage').html('');
	$('#mainConatinerForBulkPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getHostForViewForPage.html', "pageNo="+pageNo, fnUpdateBulkTableForPage, null);
}

function fnUpdateBulkTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		populateBulkHostDataIntoTable(responseJSON.hostVo);
	}else {
		$('#errorMessage').html(getHTMLEscapedMessage(responseJSON.message));
	}
}

function fnGetBulkUpdateForHost() {
	var data = "selectedHost=";
	for ( var host in selectedHost) {
		if (selectedHost[host]) {
			data+=host+";";
		}
	}
	data=data.substring(0,data.length-1);
	$('#mainConatinerForBulkPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/updateTrustForSelected.html', data, fnUpdateTrustForSelectedSuccess, null);
}

function fnUpdateTrustForSelectedSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		getDashBoardPage();
	}else {
		$('#errorMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
	}
}

function selectHostForUpdate(element,status) {
	var hostName = $.trim($(element).parent().parent().find('td:eq(1)').text());
	if (status) {
		selectedHost[hostName] = true;
	}else {
		selectedHost[hostName] = false;
	}
}
