/*var userNameRegEx = new RegExp(/^[a-zA-Z0-9]$/);//{4,10}
*/var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);
$(function() {
    //$('#ddlLocales') selected option equals ajax call to get current locale
});

function fnSavePreferences() {
    sendHTMLAjaxRequest(false, 'getData/setLocale.html',"username="+$('#sessionUser').text()+"&locale="+$('#ddlLocales option:selected').text(), fnSavePreferencesSuccess, null, null);
}

function fnSavePreferencesSuccess() {
    document.cookie = "lang="+$('#ddlLocales option:selected').text();
    location.reload();
}