// requires:  jQuery, i18next

$( document ).ready(function() {
    // reference: http://i18next.com/pages/doc_init.html
    i18nInit();
});

function i18nInit() {
    i18n.init(
            {
                'resGetPath': 'locales/__ns__-__lng__.json', // resource files will be stored in locales/translation-en-US.json  for example
                'detectLngQS': 'lang', // optional query string parameter to set language, for example  ?lang=en-US 
                'cookieName': 'lang', // optional cookie name to set language
                'preload': ['en-US'], // optimization, 
                'fallbackLng': 'en-US', // if a key is not available in current language, use the corresponding key from this fallback language, 
                'fallbackToDefaultNS': false, // if a namespace is used and a key is missing, do not look in the default namespace for it;  false happens to also be the default option for this setting
                'useLocalStorage': false,  
                'localStorageExpirationTime': 86400000, // in ms, default 1 week
                'fallbackOnNull': true, // if a key is set to null then treat it as missing and use the fallback language
                'debug': true 
            },
            function(t) {
                // this function is called with translation function t after the language has been loaded;  use this to automatically translate parts of the UI
                // find all elements on the page that have data-i18n attribute and automatically translate them
                $("[data-i18n]").not("[translate='no']").each(function() {
                     $(this).i18n(); // translate it using built-in i18next rules
                });
            }
    );
}
