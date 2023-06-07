// Immediately-Invoked Function Expression mettendo la funzione tra parentesi e passandole i parametri a seguire (no parametri qui)
(function() {

	// esplicito l'hoisting
	var form;
	
	if( localStorage.getItem("utente") != null )
        window.location.href = "home-page.html";

    document.getElementById("errormessage").style.display = "none";

	// prendo l'id del form di login
    form = document.getElementById("frmLogin");
	// definisco la funzione da chiamare all'evento e di submit del form
    form.addEventListener("submit", (e) => {
		// impedisco l'azione di default di submit (action)
		e.preventDefault();
		// se il form è valido
	    if( form.checkValidity() )
			
			// faccio la chiamata alla servlet di login
	        makeCall("POST", 'login', form, function(risposta){
                if ( risposta.readyState == XMLHttpRequest.DONE ) {
                    switch( risposta.status ){
                        case 200: // ok
                        	localStorage.setItem("utente", risposta.responseText);
                            window.location.href = "home-page.html";
                            break;
                        case 400: // bad request
                        	document.getElementById("errormessage").style.display = "block";
                            document.getElementById("errormessage").textContent = "Bad Request";
                            break;
                        case 401: // unauthorized
                        	document.getElementById("errormessage").style.display = "block";
                            document.getElementById("errormessage").textContent = "Unauthorized";
                            break;
                        case 500: // server error
                        	document.getElementById("errormessage").style.display = "block";
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