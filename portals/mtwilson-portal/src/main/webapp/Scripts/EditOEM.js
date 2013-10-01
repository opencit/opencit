$(function() {
	$('#mainEditOEMDisplayDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllOEMList.html', null, fnUpdateEditOEMTable, null);
});

/*method to display edit table*/
function fnUpdateEditOEMTable(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainEditTable').show();
		fuCreateEditOEMTable(responseJSON.OEMDataVo)
		applyPagination('editOEMPaginationDiv',responseJSON.noOfPages,fngetOEMNextPageForEdit,1);
	}else {
		$('#errorEditOEM').html(responseJSON.message);
	}
}

/*Method to create dynamic edit table for MLE*/
function fuCreateEditOEMTable(oemData) {
	var str = "";
	$('#editOEMContentDiv table tbody').html("");
	for ( var items in oemData) {
		var classValue = null; 
		if(items % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
		str+='<tr class="'+classValue+'">'+
		'<td class="row1"><a href="javascript:;" onclick="fnEditOEMInfo(this)"> Edit </a><span> | </span><a href="javascript:;" onclick="fnDeleteOemInfo(this)"> Delete </a></td>'+
		'<td class="row2" name="'+oemData[items].oemName+'" value="'+oemData[items].oemName+'" id="oemName">'+oemData[items].oemName+'</td>';
		var val1 = oemData[items].oemDescription == undefined ? ' ' : oemData[items].oemDescription;
		str+='<td class="row4" name="'+val1+'" value="'+val1+'" id="oemDec">'+val1+'&nbsp;</td>';
	}
	$('#editOEMContentDiv table tbody').html(str);
}

/*function to get the value for give page no*/
function fngetOEMNextPageForEdit(pageNo) {
	$('#messageSpace').html('');
	$('#mainEditTable').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewOEMForPageNo.html', "pageNo="+pageNo, fnUpdateEditOEMTableForPage, null);
}

function fnUpdateEditOEMTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		fuCreateEditOEMTable(responseJSON.OEMDataVo);
	}else {
		$('#errorEditOEM').html(responseJSON.message);
	}
}

