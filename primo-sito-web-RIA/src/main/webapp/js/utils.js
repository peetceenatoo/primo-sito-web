// mando una richiesta ad AskLogged specificando la callback callback
function askLogged(callback) {
	// creo un oggetto HttpRequest
    var richiesta = new XMLHttpRequest();

	// definisco la callback da eseguire al termine della richiesta
    richiesta.onreadystatechange = function() {
        callback(richiesta)
    };
    
    // inizializzo la richiesta
    richiesta.open("GET", 'askLogged');
    
    // mando la richiesta
    richiesta.send();
    
    // e poi verrà eseguita la callback
}

// manda una richiesta http con metodo metodo, all'url url, fornendo il form form e specificando la callback callback
function makeCall(metodo, url, form, callback, reset = true) {
	// creo un oggetto HttpRequest
    var richiesta = new XMLHttpRequest();

	// definisco la callback da eseguire al termine della richiesta
    richiesta.onreadystatechange = function() {
        callback(richiesta)
    };
    
    // inizializzo la richiesta
    richiesta.open(metodo, url);
    
    // mando la richiesta
    richiesta.send(new FormData(form));

	// se il form era stato riempito, lo ripristino
    if( ( form !== null ) && ( reset === true ) )
        form.reset();
        
    // e poi verrà eseguita la callback
}

// manda una richiesta http di POST, passando obj come parametro e specificando la callback callback
function postJsonData(url, obj, callback, toBeStringified = true) {
	// creo un oggetto HttpRequest
    var richiesta = new XMLHttpRequest();
    
    // definisco la callback da eseguire al termine della richiesta
    richiesta.onreadystatechange = function() {
        callback(req)
    };
    
    // inizializzo la richiesta
    richiesta.open("POST", url);

	// imposto il content-type della richiesta
	richiesta.setRequestHeader("Content-Type", "application/json");
	
	// invio la richiesta
    if( toBeStringified )
    	richiesta.send(JSON.stringify(obj));
    richiesta.send(obj);
}

