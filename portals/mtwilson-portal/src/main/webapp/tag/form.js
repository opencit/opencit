		// example: setSelectOptions('upload-enc-image-select', files, {'emptyText':"No encrypted images"});
		function setSelectOptions(select_id, options_list, opt) {
		   $(select_id).childElements().invoke('remove');
//		   alert("list length: "+options_list.length);
	//	   alert("list: "+options_list);
			var counter = 0;
		   for(var i=0; i<options_list.length; i++) {
				var value = (""+options_list[i]).strip(); // convert any object to string before trying the strip command
				if( value.length > 0 ) {
					$(select_id).insert({ bottom: "<option>"+value+"</option>" });
					counter++;
				}
				else {
					//alert("empty option? '"+options_list[i]+"' or '"+value+"'");
				}
		   }
		   if( counter == 0 && opt && opt['emptyText'] ) {
				$(select_id).insert({ bottom: "<option value=''>"+opt['emptyText']+"</option>" });
		   }
		}
		
		function selectOptionValue(select_id, value) {
			value = (""+value).strip();
			$(select_id).childElements().each( function(e) {  // or $$('#'+select_id+' options').each(...)
				var valueAttr = (""+e.readAttribute('value')).strip();
				var innerText = (""+e.innerHTML).strip();
				e.selected = (valueAttr == value) || (innerText == value); 
				} ); 
		}
		
		function getSelectOptionValue(select_id) {
			var value = "";
			$(select_id).childElements().each( function(e) {  // or $$('#'+select_id+' options').each(...)
				if( e.selected ) {
					var valueAttr = (""+e.readAttribute('value')).strip();
					var innerText = (""+e.innerHTML).strip();
					if( valueAttr ) { value = valueAttr; return; }
					if( innerText ) { value = innerText; return; }
				} } ); 
			return value;
		}
		
        function getSelectOptionText(select_id) {
            var value = "";
            $(select_id).childElements().each( function(e) {  // or $$('#'+select_id+' options').each(...)
                if( e.selected ) {
                    var valueAttr = (""+e.readAttribute('value')).strip();
                    var innerText = (""+e.innerHTML).strip();
                    value = innerText;
                } } ); 
            return value;
        }