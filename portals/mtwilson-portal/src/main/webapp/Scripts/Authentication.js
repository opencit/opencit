
$(function() {
    $.ajax({url: "v2proxy/authentication-status.json", success: function(data){
           // alert(JSON.stringify(result));
        //$("#authentication-Status").html(result);
        
        var result = JSON.stringify(data);
        document.getElementById('authentication_Status').value = getEscapesmessage(result);
    }});
});
  
 function getEscapesmessage(result){
     var str = result;
	str =str.replace(/\{/g, " ");
	str =str.replace(/\}/g, " ");
	str =str.replace(/\"/g, " ");
	
	return str;
 }

