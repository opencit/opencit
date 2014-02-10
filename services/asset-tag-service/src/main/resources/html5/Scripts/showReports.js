var selectedHost = [];
$(function() {
	$('#mainConatinerForReportPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllHostForView.html', null, populateHostReportDetails, null);
});

function populateHostReportDetails(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainTableForReports').show();
		populateReportHostDataIntoTable(responseJSON.hostVo);
		applyPagination('ReportHostPaginationDiv',responseJSON.noOfPages,fngetReportHostNextPage,1);
		fnSelectAllCheckBox(true);
	}else {
		$('#errorMessage').html('<span class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</span>');
	}
}

function populateReportHostDataIntoTable(hostDetails) {
	var str = "";
		for ( var item in hostDetails) {
			selectedHost[hostDetails[item].hostName] = true;
			var classValue = null;
			var checked = "";
			if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
			if(selectedHost[hostDetails[item].hostName] != undefined ){checked="checked";}
			str+='<tr class="'+classValue+'">'+
			'<td class="reportViewRow1"><input type="checkbox" checked='+checked+' onclick="selectHostForUpdate(this,checked)"></td>'+
			'<td class="reportViewRow2">'+hostDetails[item].hostName+'</td>'+
			'<td class="reportViewRow3">'+hostDetails[item].biosName+'</td>'+
			'<td class="reportViewRow4">'+hostDetails[item].biosBuildNo+'&nbsp;</td>'+
			'<td class="reportViewRow5">'+hostDetails[item].vmmName+'&nbsp;</td>'+
			'<td class="reportViewRow6">'+hostDetails[item].vmmBuildNo+'&nbsp;</td>'+
			'</tr>';
		}
		$('#mainReportHostDetailsContent table').html(str);
}

function fngetReportHostNextPage(pageNo) {
	$('#errorMessage').html('');
	$('#mainConatinerForReportPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getHostForViewForPage.html', "pageNo="+pageNo, fnUpdateReportTableForPage, null);
}

function fnUpdateReportTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		populateReportHostDataIntoTable(responseJSON.hostVo);
	}else {
		$('#errorMessage').html(getHTMLEscapedMessage(responseJSON.message));
	}
}

function fnGetReportUpdateForHost() {
	var data = "";
        $('#errorMessage').html("");
	for ( var host in selectedHost) {
            if (selectedHost[host]) {
                    data+="&selectedHost="+host;
            }
	}
	$('#mainConatinerForReportPage').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getHostsReport.html', data, fnUpdateTrustForSelectedSuccess, null);
}

function fnUpdateTrustForSelectedSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
        $('#mainTableForReports').html('');
		createReportTable(responseJSON.reports);
	}else {
		$('#errorMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
	}
}

function selectHostForUpdate(element,status) {
	var hostName = $.trim($(element).parent().parent().find('td:eq(1)').text());
	if (status) {
		selectedHost[hostName] = status;
	}else {
		selectedHost[hostName] = false;
	}
}

function createReportTable(response){
    var str = '<table width="100%" cellspacing="0" cellpadding="0" class="tableDisplay"><thead>'+
               '<tr>'+
               '<th class="reportResultRow1">Host Name</th>'+
               '<th class="reportResultRow2">MLE Details</th>'+
               // Since we are not going to store the created and updated data for host in the host table, we will not
               // show this data here.
               //'<th class="reportResultRow3">Created</th>'+
               '<th class="reportResultRow4">Trust Status</th>'+
               '<th class="reportResultRow5Header">Trust Verified</th>'+
               '<tr></thead></table>';
           
           str+='<div id="ReportTableContent" class="tableContentStyle hostTableContent">'+
                '<table width="100%" cellspacing="0" cellpadding="0" class="tableDisplay"><tbody>';
            for(var item in response){
               var classValue = null;
			if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
                str+='<tr class="'+classValue+'">'+
                    '<td class="reportResultRow1">'+response[item].hostName+'</td>'+
                    '<td class="reportResultRow2">'+response[item].mleInfo+'</td>'+
                    // '<td class="reportResultRow3">'+response[item].createdOn+'</td>'+
                    '<td class="reportResultRow4">'+response[item].trustStatus+'</td>'+
                    '<td class="reportResultRow5">'+response[item].verifiedOn+'</td>'+
                    '</tr>';
            }
                    str+='</tbody></table></div>';
            
       $('#mainTableForReports').html(str);
}