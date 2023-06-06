// Immediately-Invoked Function Expression mettendo la funzione tra parentesi e passandole i parametri a seguire (no parametri qui)
(function() {

	// esplicito l'hoisting
	var form;
	
	// se sono loggato voglio essere su home-page.html
	window.addEventListener('load', function() { askLogged( function(x){
                if( x.readyState == XMLHttpRequest.DONE ) {
                    if( x.responseText == "yes" )
						window.location.href = "home-page.html";
            	}
        	} )
        } );

	// prendo l'id del form di login
    form = document.getElementById("frmLogin");
	// definisco la funzione da chiamare all'evento e di submit del form
    form.addEventListener("submit", (e) => {
		// impedisco l'azione di default di submit (action)
		e.preventDefault();
		// se il form è valido
	    if( form.checkValidity() )
			
			// faccio la chiamata alla servlet di login
	        makeCall("POST", 'login', form, function(x){
                if ( x.readyState == XMLHttpRequest.DONE ) {
                    switch (x.status) {
                        case 200: // ok
                            window.location.href = "home-page.html";
                            break;
                        case 400: // bad request
                            document.getElementById("errormessage").textContent = "Bad Request";
                            break;
                        case 401: // unauthorized
                            document.getElementById("errormessage").textContent = "Unauthorized";
                            break;
                        case 500: // server error
                            document.getElementById("errormessage").textContent = "Server error";
                            break;
                    }
                }
            } );
		
	    else
	    	// mostro un eventuale errore se il form non è valido
	        form.reportValidity();
    });

})();