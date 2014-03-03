$(function() {
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllCAStatus.html', null, fnCAStatusSuccess, null);
});

function fnCAStatusSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#caStatusTable').show();
		var str="";
		var request = responseJSON.caStatus;
		for ( var item in request) {
			var classValue = null;
			if(item % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
			str+='<tr class="'+classValue+'">'+
					'<td class="viewRow1" title="'+request[item].fingerprint+'" name="name">'+request[item].name+'</td>'+
					'<td class="viewRow2" name="download">'+request[item].download+'</td>'+
					'<td class="viewRow3" name="status">'+request[item].status+'</td>'+					
					'<td class="viewRow4" name="expires">'+fnGetFormatedDate(request[item].expires)+'</td>';
				var comment = request[item].comments == undefined || request[item].comments == null || request[item].comments == "" ? "&nbsp;" : request[item].comments; 
					str+='<td class="viewRow5" name="expires">'+comment+'</td>'+
				'</tr>';
		}
		$('#caStatusContent').html(str);
	}else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}