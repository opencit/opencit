/* 

input.js - special form input field behavior

Copyright (C) 2010-2013 Jonathan Buhacoff

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

License: MIT
Version: 0.2
Requires: prototype.js 1.6.1 or later

Behavior for text input boxes with the prompt displayed over them.
How to use it:

Include the script tag in your <head> element:

<script src="input.js" type="text/javascript"></script>

Then in your text input tags, just make sure they have an "id" attribute and set
the "alt" attribute to whatever you want to display by default:

<input id="register_email" type="text" name="email" size="16" alt="Email"/>

Officially, you can only use the 'alt' attribute when the input type is 'image'.
So in version 0.2, the HTML5 data-alt attribute is used instead.

<input id="register_email" type="text" name="email" size="16" data-alt="Email"/>

If you're writing HTML5 , then use the HTML5 placeholder attribute instead:

<input id="register_email" type="text" name="email" size="16" placeholder="Email"/>

The library automatically finds all <input> elements on the page that have
an "id" and an "alt" attribute. It uses their "id" and "alt" attributes to create
a display overlay that disappears when the user clicks on the input and
reappears if the input is blank when it loses focus.
*/


var net_buhacoff_inputjs = net_buhacoff_inputjs || {};

/*
You can style the default display text by defining the css class input.empty. For example:

input.empty {
    font-family: Geneva, Helvetica, Arial,  sans-serif;
    color: #aaa;
}

If you do not define an "input.empty" style the default display text will look just
like regular input. 


*/

/*
Example: 
smart_input_box('register_name','register_name_display');
Creates the behavior that when the user clicks on the prompt or the input box, the prompt disappears.
And when the input box loses focus, if the input is empty then the prompt reappears.
*/
/*
function smart_input_box(input_id, display_id) {
	$(display_id).observe('click', function() {
		$(input_id).focus();
	});
	$(input_id).observe('focus', function() {
		$(display_id).hide();		
	});
	$(input_id).observe('blur', function() {
		var value = $(input_id).getValue();
		if( value != null && value.length == 0 ) {
			$(display_id).show();
		}
	});
}

	var container = new Element('span',{id:container_id, style:"position: relative;"});
	var display = new Element('span',{id:display_id, style:"position: absolute; color: #aaa; margin-left: 5px; margin-top: 3px;"});
	input.insert({before: container});
	container.insert({top:display});
	container.insert({bottom:input.remove()});
	display.update(display_text);
	smart_input_box(input_id, display_id);

*/

/*
Example:  
create_smart_input_box('register_email', 'Email');
Turns this:
<input id="register_email" type="text" name="email" size="16"/>
Into this:
<span>
<span id="register_email_display" style="position: absolute; color: #aaa; margin-left: 5px; margin-top: 3px;">Email</span>
<input id="register_email" type="text" name="email" size="16"/>
</span>
With the behavior that when the user clicks on the prompt or the input box, the prompt disappears.
And when the input box loses focus, if the input is empty then the prompt reappears.
*/


(function() { // start mt wilson asset tag module definition

// best effort logging. look for log.js then console.log
net_buhacoff_inputjs.log = function(message, exception) {
        if( typeof log === 'object' && 'debug' in log && typeof log.debug === 'function' ) {
            if( exception ) {
            log.debug(message+": "+exception);                
            }
            else {
            log.debug(message);
            }
        }
        else if( typeof console === 'object' && 'log' in console && typeof console.log === 'function' ) {
            if(exception) {
            console.log(message+": "+exception);                
            }
            else {
                console.log(message);
            }
        }    
};

// for html5, since there is placeholder support we don't need to swap values; only styles
net_buhacoff_inputjs.activateWithHtml5ElementAndLabel = function(input) {
    // initial style
	if( input.getValue() == null || input.getValue().length == 0 ) {
        input.addClassName("empty");
    }
    // every time the input gets focus we restyle it normal 
	input.observe('focus', function() {
        input.removeClassName("empty");
	});
    // every time the input loses focus we check if it's empty to decide if we apply the placeholder style
	input.observe('blur', function() {
		var value = input.getValue();
		if( value == null || value.length == 0 ) {
            input.addClassName("empty");
		}
	});	
}


net_buhacoff_inputjs.activateWithElementAndLabel = function(input, display_text) {	
	if( input.getValue() == null || input.getValue().length == 0 ) {
        //input.store("net_buhacoff_inputjs_src_color", input.getStyle('color'));
    	//input.setStyle({color: "#aaa"}); // , fontSize: "1.2em"
        input.addClassName("empty");
        input.setValue(display_text); 
    }
	if( input.type == "password" ) { 
        input.store("net_buhacoff_inputjs_src_type", "password"); 
        input.type = "text"; 
    }
	input.observe('focus', function() {
		var value = input.getValue();
		if( value != null && value.length != 0 && value == display_text ) {
			input.setValue("");
            //var srcColor = input.retrieve("net_buhacoff_inputjs_src_color") || '#000';
			//input.setStyle({'color': srcColor});
            input.removeClassName("empty");
			if( input.retrieve("net_buhacoff_inputjs_src_type") == "password" ) { input.type = "password"; }
		}
	});
	input.observe('blur', function() {
		var value = input.getValue();
		if( value == null || value.strip().length == 0 ) {
            //input.store("net_buhacoff_inputjs_src_color", input.getStyle('color'));
			//input.setStyle({color: "#aaa"});
            input.addClassName("empty");
			input.setValue( display_text );
			if( input.retrieve("net_buhacoff_inputjs_src_type") == "password" ) { input.type = "text"; }
		}
	});	
}

net_buhacoff_inputjs.activateWithElement = function(input) {
    try {
        var placeholder = input.getAttribute('placeholder'); // html5
        var alt = input.getAttribute('data-alt'); // placeholder functionality for html4, but has an issue because it sets the value so some scripts may mistake the placeholder for the real value
        if( placeholder ) {
            net_buhacoff_inputjs.activateWithHtml5ElementAndLabel(input);
        }
        else if( alt ) {
            net_buhacoff_inputjs.activateWithElementAndLabel(input, alt);
        }
    }
    catch(e) {
        // best-effort logging
        net_buhacoff_inputjs.log("Error while activating alternate display for element: "+input, e);
    }
}

net_buhacoff_inputjs.activateWithId = function(input_id) {
	var input = $(input_id);
    net_buhacoff_inputjs.activateWithElement(input);
}
/*
function create_smart_submit_button(input_id) {
	var input = $(input_id);
	input.setStyle({color: "#aaa", fontSize: "1.2em"});
}
*/

/**
 * Activates all qualifying input fields that are selected by the given css selector
 *  
 */
net_buhacoff_inputjs.activateWithSelector = function(css_selector) {
    var inputs = $$(css_selector);
    for(var i=0; i<inputs.length; i++) {
        // the input control must have two attributes: id and data-alt
        if( inputs[i].id ) {
            net_buhacoff_inputjs.activateWithId( inputs[i].id );
        }
//        else if( inputs[i].id && inputs[i].value ) {
//            net_buhacoff_inputjs.activateWithId( inputs[i].id, inputs[i].value );
//        }
    }
}


}());

/*
 * Automatically activate qualifying input fields when the script is loaded
 */
document.observe('dom:loaded', function() {
	// find all input controls
    net_buhacoff_inputjs.activateWithSelector('input[type=text]');
    net_buhacoff_inputjs.activateWithSelector('input[type=password]');
    net_buhacoff_inputjs.activateWithSelector('textarea');
	// find all submit controls
	/*
	var submit = $$('submit'); // $$('input.nice-input');
	for(var i=0; i<submit.length; i++) {
		// the control must have two attribute: id and data-alt
		if( submit[i].id && submit[i].title ) {
			create_smart_submit_button(submit[i].id, submit[i].title);
		}
	}
	*/
});

