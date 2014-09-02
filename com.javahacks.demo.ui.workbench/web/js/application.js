$(document).ready(function() {	
		
	$( "[id]" ).bind("change keyup",function (event) { handle(event.type,this.id,$( this).val())});				
		
			
		
});
	
function setValue(id,value){

	$( id ).val(value);
	
}

function dropText(e) {
	    
     e.preventDefault();
    
     var text = e.dataTransfer.getData("Text");	  	 	
  	 handle('dropText',text);
	
}	
