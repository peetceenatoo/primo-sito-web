{
	/************************************************************************************/
	
	// se sono sloggato, torno a login.html
	if( localStorage.getItem("utente") == null )
        logout();

	/************************************************************************************/
	
	// dichiaro il pageOrchestrator e le variabili che conterranno i componenti della pagina
    let home, search, carrello, ordini;
    let pageOrchestrator = new PageOrchestrator();
    
    /************************************************************************************/
	
	// se quando la pagina carica per la prima volta non sono loggato chiamo logout, altrimenti visualizzo la home
	window.addEventListener('load', function(){
			pageOrchestrator.start();
            if( localStorage.getItem("utente") == null )
        		logout()
    		else
        		start();
	    } );
	    
	/************************************************************************************/

	// questo metodo prova a fare il logout e in ogni caso torno a login.html
    function logout(){
        makeCall("POST", 'logout', null, function(risposta){
        	if( risposta.readyState === XMLHttpRequest.DONE ){
                localStorage.clear();
                window.location.href = "login.html";
            }
        } );
    }	

	// visualizzo la home per la prima volta
    function start(){
        pageOrchestrator.showHome();
    }
    
    /************************************************************************************/

    function PageOrchestrator(){

		// prendo il container della pagina
        this.container = document.getElementById('container');
	
		// metodo che inizializza l'oggetto
        this.start = function(){
			// salvo this in self per colpa della visibilità di js
            const self = this;
            
            // creo un oggetto-pagina Home
            home = new Home(this.container);
            // creo un oggetto-funzionalità Search
            search = new Search(this.container);
            // creo un oggetto-pagina Carrello
            carrello = new Carrello(this.container);
            // creo un oggetto-funzionalità Ordine
            ordini = new Ordini(this.container);

			// assegno al bottone carrello la funzione
            document.getElementById('aCarrello').onclick = function(){
                self.hide();
                self.showCarrello();
            };

			// assegno al bottone ordini la funzione
            document.getElementById('aOrdini').onclick = function(){
                self.hide();
                self.showOrdini();
            };

			// assegno al bottone Home la funzione
            document.getElementById('aHome').onclick = function(){
                self.hide();
                self.showHome();
            };
            
            // aggiungo la funzione al click del bottone di logout
            document.getElementById("btnLogout").addEventListener("click", function(){
	            logout();
        	});	
	
			// svuoto la pagina
            self.hide();
        }
        
        // svuoto la pagina
        this.hide = function(){
            this.container.innerHTML = "";
        }

		// mostro la home
        this.showHome = function(){
            this.hide();
            if( localStorage.getItem("utente") == null )
        		logout();
        	else
        		home.show();
        }

		// mostro il carrello
        this.showCarrello = function(){
            this.hide();
            if( localStorage.getItem("utente") == null )
        		logout();
        	else
        		carrello.show();
        }

		// mostro gli ordini
        this.showOrdini = function(){
            this.hide();
            if( localStorage.getItem("utente") == null )
        		logout();
        	else
        		ordini.show();
        }
    }
    
    /************************************************************************************/
	
	// pagina-componente home
    function Home(container){
        this.container = container;

		// metodo che mostra la home
        this.show = function(){
			// salvo this in self per colpa della visibilità di js
            let self = this;
            
			// aggiungo la barra di ricerca nella navbar, prima inserendo il div al posto giusto, qui il form, dunque la barra
            let divSearch = document.querySelector('.searchForm');
			if( !divSearch ){
				divSearch = document.createElement('div');
	            divSearch.classList.add("searchForm");
	            document.getElementById("navbar").insertBefore(divSearch, document.getElementById("btnLogout"));
	            let formSearch = document.createElement('form');
	            formSearch.action = "#";
	            formSearch.id = "formRisultati";
	            divSearch.appendChild(formSearch);
	            let inputSearch = document.createElement('input');
	            inputSearch.type = "text";
	            inputSearch.placeholder = "Cerca...";
	            inputSearch.name = "queryString";
	            inputSearch.required = true;
	            formSearch.appendChild(inputSearch);
				// in caso di invio, chiamo il metodo cerca
	            formSearch.addEventListener("submit", search.cerca);
	        }

			// recupero dal server gli ultimi visualizzati e gestisco la risposta in base allo stato
            makeCall("GET", "ultimiVisualizzati", null, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch( risposta.status ){
                        case 200: // ok
                            self.showUltimiVisualizzati(risposta);
                            break;
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.\n");
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            })
        }
        
        // metodo che mostra gli ultimi visualizzati nella home
        this.showUltimiVisualizzati = function(risposta){
            // leggo i prodotti dalla risposta contenente i prodotti
            let prodotti;
            try{
                prodotti = JSON.parse(risposta.responseText);
            } catch(e) {
                alert("Errore lato client durante il parsing di JSON dei prodotti forniti dal server: " + e);
                return;
            }
			
			// se non ce ne sono, scrivo che non ci sono prodotti da mostrare
            if( prodotti.length == 0 ){
                let noProdottih2 = document.createElement('h2');
                h2NoProd.textContent = "Non ci sono prodotti da mostrare.";
                this.container.appendChild(noProdottih2);
            }
            // 
            else{
				// aggiungo il titolo
                let titoloh2 = document.createElement('h2');
                titoloh2.textContent = "Hai visto di recente (o potrebbe interessarti): ";
                this.container.appendChild(titoloh2);

				// aggiungo il card container
                let divContainer = document.createElement('div');
                divContainer.classList.add("card-container");
                this.container.appendChild(divContainer);

				// per ogni prodotto aggiungo una card
                for( let i=0; i<prodotti.length; i++ ){
                    let card = document.createElement('div');
                    card.classList.add("card");
                    divContainer.appendChild(card);

					// ci metto dentro l'immagine
                    let img = document.createElement('img');
                    img.src = 'immagine?idProdotto=' + prodotti[i].id;
                    img.classList.add('card-img-top');
                    card.appendChild(img);

					// e riempio il body
                    let cardBody = document.createElement('div');
                    cardBody.classList.add("card-body");
                    card.appendChild(cardBody);
                    // e nome
                    let nomeParag = document.createElement('p');
                    nomeParag.textContent = "Nome Prodotto: " + prodotti[i].nome;
                    cardBody.appendChild(nomeParag);
                }
            }

        }

    }
    
    /************************************************************************************/

	// classe per la ricerca
    function Search(container){
        this.containter = container;

		// salvo this in self per colpa della visibilità di js
        const self = this;

		// questo metodo effettua la ricerca dei risultati
        this.cerca = function(e){
			// impedisco l'azione di default di submit (action)
            e.preventDefault();
            // svuoto la pagina
            self.containter.innerHTML = "";

			// prendo il form
            var form = e.target;

			// se il contenuto della barra di ricerca al momento del submit è valido
            if( form.checkValidity() )
                
				// faccio la chiamata alla servlet dei risultati
                makeCall("GET", 'risultati?' + new URLSearchParams(new FormData(form)).toString(), null, function(risposta){
                    if ( risposta.readyState === XMLHttpRequest.DONE )
                    
                        switch( risposta.status ){
                            case 200: // ok
                                self.showRisultati(risposta);
                                break;
                            case 400: // bad request
                            	alert("Parametro non valido, rifiutato dal server.\nVerrai riportato alla home.");
                                pageOrchestrator.hide();
                                pageOrchestrator.showHome();
                                break;
                            case 401: // unauthorized
                                alert("Non sei loggato.\nVerrai riportato al login.");
                                logout();
                                break;
                            case 500: // server error
                                alert("Errore nel server.\nVerrai riportato alla home.");
                                pageOrchestrator.hide();
                                pageOrchestrator.showHome();
                                break;
                            default:
                                alert("Errore sconosciuto.");
                                pageOrchestrator.hide();
                                pageOrchestrator.showHome();
                                break;
                        }
                 } );      
                  
           	else
           		// mostro un eventuale errore se il form non è valido
           		form.reportValidity();
        }

		// metodo che mostra i risultati
        this.showRisultati = function(risposta){
            let risultati;
            try {
                risultati = JSON.parse(risposta.responseText);
            } catch(e) {
                alert("Errore lato client durante il parsing di JSON dei risultati forniti dal server: " + e);
                return;
            }
			
			// svuoto la pagina
            this.containter.innerHTML = "";
            // se non ci sono risultati mostro un messaggio
            if( risultati.length === 0 ){
                let p = document.createElement('p');
                p.textContent = "Nessun risultato per le parole chiave inserite...";
                this.containter.appendChild(p);
                return;
            }
	
			// creo la lista (non la aggiungo qui)
            let ul = document.createElement('ul');
            ul.classList.add('listview');

			// aggiungo una riga alla lista per ogni risultato
            for( let i = 0; i<risultati.length; i++ ){
				// aggiungo una riga
                let li = document.createElement('li');
                li.classList.add('listview-row');

				// aggiungo id, nome del prodotto e testo come intestazione della riga
                let a = document.createElement('a');
                let prodotto = document.createTextNode(risultati[i].primo.id + " - " + risultati[i].primo.nome + ": " + (risultati[i].secondo).toFixed(2) + " €")
                a.appendChild(prodotto);
                a.classList.add("listview-row-title");
                li.appendChild(a);
                a.setAttribute("data-idprodotto", risultati[i].primo.id);

				// assegno alla riga del prodotto la funzione per visualizzare e chiamare apriDettagli
                a.onclick = function(e){
                    if( ( e.target.getAttribute("data-opened") === null ) || ( e.target.getAttribute("data-opened") === "false" ) ){
                        e.target.setAttribute("data-opened", true);
                        makeCall("GET", "visualizza?idProdotto=" + e.target.getAttribute("data-idprodotto"), null, function(risposta){
                            if( risposta.readyState === XMLHttpRequest.DONE ){
                                switch( risposta.status ){
                                    case 200: // ok
                                        self.apriDettagli(e.target.parentNode, risposta);
                                        break;
                                    case 400: // bad request
										alert("Parametro non valido, rifiutato dal server.\nVerrai riportato alla home.");
                                        pageOrchestrator.hide();
                                        pageOrchestrator.showHome();
                                        break;
                                    case 401: // unauthorized
                                        alert("Non sei loggato.\nVerrai riportato al login.")
                                        logout();
                                        break;
                                    case 500: // server error
                                        alert("Errore nel server.\nVerrai riportato alla home.");
                                        pageOrchestrator.hide();
                                        pageOrchestrator.showHome();
                                        break;
                                    default:
                                        alert("Errore sconosciuto.");
                                        pageOrchestrator.hide();
                                        pageOrchestrator.showHome();
                                        return;
                                }
                            }
                        } );
                    }
                    else{
                        e.target.setAttribute("data-opened", false);
                        self.chiudiDettagli(e.target.parentNode);
                    }

                }
				
				// aggiungo la riga alla lista
                ul.appendChild(li);
            }
            
            // aggiungo effettivamente la lista
            this.containter.appendChild(ul);
            
            /* --- aggiungo il div myModal nel container, la cui posizione a video verrà definita a posteriori --- */
			
            let divModalContent = document.createElement('div')
            divModalContent.id = "myModal";
            divModalContent.classList.add('modal-content')
            divModalContent.onmouseleave = function(){
                closeModal();
            }
            this.containter.appendChild(divModalContent);
            
            /* ------- */
        }

		// metodo che apre i dettagli di un risultato
        this.apriDettagli = function(li,risposta){
            let dettagli;
            try{
                dettagli = JSON.parse(risposta.responseText);
            } catch(e) {
                alert("Errore lato client durante il parsing di JSON dei dettagli forniti dal server: " + e);
                return;
            }

			// aggiungo il div che conterrà i dettagli
            let div = document.createElement('div');
            li.appendChild(div);

			// nel div dei dettagli aggiungo il tag per l'immagine
            let img = document.createElement('img');
            img.src = "immagine?idProdotto=" + dettagli.primo.id;
            div.appendChild(img);

			// aggiungo i paragrafi per nome, descrizione e categoria
            let p1 = document.createElement('p');
            let p2 = document.createElement('p');
            let p3 = document.createElement('p');
            p1.textContent = dettagli.primo.nome;
            p2.textContent = dettagli.primo.descrizione;
            p3.textContent = dettagli.primo.categoria;
            div.appendChild(p1);
            div.appendChild(p2);
            div.appendChild(p3);

			// aggiungo la tabella con le informazioni per ogni fornitore
            let table = document.createElement('table');
            div.appendChild(table);
            let tableHead = document.createElement('thead');
            table.appendChild(tableHead);
            let tableHeaderRow = document.createElement('tr');
            tableHead.appendChild(tableHeaderRow);
			// creo una colonna per ogni informazione necessaria
            let nomiColonne = ['Nome','Valutazione','Prezzo (Scontato)','Sconto','Fasce di Spese di Spedizione','Soglia Spedizione Gratis','Già nel carrello','']
            for( let i=0; i<nomiColonne.length; i++ ){
                let th = document.createElement('th');
                th.textContent = nomiColonne[i];
                tableHeaderRow.appendChild(th);
            }
			// aggiungo il corpo della tabella
            let tableBody = document.createElement('tbody');
            table.appendChild(tableBody);

			// per ogni fornitore aggiungo una riga alla tabella
            for( let i=0; i<dettagli.secondo.length; i++ ){
				// prendo dai dettagli l'i-esimo fornitore e aggiungo una riga
                let coppiaFornitorePrezzo = dettagli.secondo[i];
                let rigaFornitore = document.createElement('tr');
                tableBody.appendChild(rigaFornitore);

				// inserisco il nome
                let tdNome = document.createElement('td');
                rigaFornitore.appendChild(tdNome);
                tdNome.textContent = coppiaFornitorePrezzo.primo.nome;

				// inserisco la valutazione
                let tdValutazione = document.createElement('td');
                rigaFornitore.appendChild(tdValutazione);
                tdValutazione.textContent = coppiaFornitorePrezzo.primo.valutazione + " / 5.0";

				// inserisco il prezzo
                let tdPrezzo = document.createElement('td');
                rigaFornitore.appendChild(tdPrezzo);
                tdPrezzo.textContent = (coppiaFornitorePrezzo.secondo.primo).toFixed(2) + ' €';
	
				// inserisco lo sconto
                let tdSconto = document.createElement('td');
                rigaFornitore.appendChild(tdSconto);
                tdSconto.textContent = (coppiaFornitorePrezzo.secondo.secondo * 100.00).toFixed(2) + ' %';

				// inserisco le varie fasce per le spese di spedizione
                let tdFasceSpedizione = document.createElement('td');
                rigaFornitore.appendChild(tdFasceSpedizione);
                // come lista di fasce
                let ul = document.createElement('ul');
                tdFasceSpedizione.appendChild(ul);
                // considerando per ogni fascia il caso in cui sia l'ultima fascia con max non definito
                coppiaFornitorePrezzo.primo.fasceDiSpedizione.forEach( f => {
                    let li = document.createElement('li');
                    li.textContent = ( f.max == undefined ) ? "Da " + f.min + " articoli " + f.prezzo.toFixed(2) + " €" :
                        "Da " + f.min + " a " + f.max + " articoli " + f.prezzo.toFixed(2) + " €";
                    ul.appendChild(li);
                })

				// inserisco la soglia
                let tdSogliaSpedizione = document.createElement('td');
                rigaFornitore.appendChild(tdSogliaSpedizione);
                tdSogliaSpedizione.textContent = coppiaFornitorePrezzo.primo.soglia == undefined ? "Nessuna soglia di spesa per la spedizione gratuita" : coppiaFornitorePrezzo.primo.soglia.toFixed(2) + " €";

				// preparo la colonna "già nel carrello" aggiungendo idfornitore e idprodotto come attributi
                let tdNelCarrello = document.createElement('td');
                rigaFornitore.appendChild(tdNelCarrello);
                // aggiungendo idfornitore e idprodotto come attributi per questa cella
                tdNelCarrello.setAttribute("data-idfornitore", coppiaFornitorePrezzo.primo.id);
                tdNelCarrello.setAttribute("data-idprodotto", dettagli.primo.id);
                tdNelCarrello.textContent = "";
                // e voglio che si apra myModal onmouseover
                tdNelCarrello.onmouseover = function(e){
                    openModal(e);
                }

				// inserisco "niente" nell'ultima colonna
                let tdAggiungiAlCarrello = document.createElement('td');
                rigaFornitore.appendChild(tdAggiungiAlCarrello);
				// e ci metto dentro l'elemento per l'input della quantità di prodotti
                let inputQuantita = document.createElement('input');
                inputQuantita.type = 'number';
                inputQuantita.min = 0;
                inputQuantita.value = 0;
                tdAggiungiAlCarrello.appendChild(inputQuantita);
				// e il bottone per aggiungere al carrello
                let btnAggiungi = document.createElement('button');
                btnAggiungi.setAttribute('data-idfornitore', coppiaFornitorePrezzo.primo.id);
                btnAggiungi.setAttribute('data-idprodotto', dettagli.primo.id);
                btnAggiungi.textContent = "Metti nel carrello!";
				// impostando la funzione da attivare onclick
                btnAggiungi.onclick = function(e){
					// prendo la quantità come parametro
                    let quantita = e.target.parentNode.querySelector('input').value;
                    // e controllo che sia un valore valido anche lato client
                    if( isNaN(quantita) || ( quantita <= 0 ) || !Number.isInteger(parseFloat(quantita)) ){
                        alert("Valore quantità non valido.\nIl controllo è avvenuto lato client.")
                        return;
                    }
                    let idProdotto = e.target.getAttribute('data-idprodotto');
                    let idFornitore = e.target.getAttribute('data-idfornitore');
					// controllo anche che gli id siano numeri interi anche lato client
                    if( isNaN(idProdotto) || isNaN(idFornitore) || !Number.isInteger(parseFloat(idProdotto)) || !Number.isInteger(parseFloat(idFornitore)) ){
                        alert("Valore di uno dei due id non valido.\nIl controllo è avvenuto lato client.");
                        return;
                    }
					
					// aggiungo il prodotto al carrello
                    carrello.aggiungiProdotto(idProdotto, idFornitore, quantita);

					// e riaggiorno la pagina
                    pageOrchestrator.hide();
                    pageOrchestrator.showCarrello();

                }
				// ed aggiungo anche il bottone all'ultima colonna
                tdAggiungiAlCarrello.appendChild(btnAggiungi);
            }
			// e aggiorno la colonna "Già nel carrello"
            this.aggiornaRisultati();
        }

		// metodo che chiude i dettagli di un risultato
        this.chiudiDettagli = function(li){
            let divDetails = li.querySelectorAll("div");
            if (divDetails != null)
                divDetails.forEach( node => {li.removeChild(node)})
        }

		// metodo che prende il listino per aggiornare il risultato a seguito di un'aggiunta al carrello
        this.aggiornaRisultati = function(){
			// salvo this in self per colpa della visibilità di js
            const self = this;
            
            // faccio la chiamata a listinoPrezzi
            makeCall("GET", "listino", null, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch (risposta.status) {
                        case 200: // ok
                            self.riempiGiaNelCarrello(risposta);
                            break
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.");
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            });

        }

		// metodo che riempie effettivamente la colonna Già nel carrello a seguito di un'aggiunta
        this.riempiGiaNelCarrello = function(risposta){
            // faccio il parsing del listino
            let listino;
            try{
                listino = JSON.parse(risposta.responseText);
            } catch(e) {
                alert("Errore lato client durante il parsing di JSON del listino prezzi fornito dal server: " + e);
                return;
            }

			// prendo tutte le celle della tabella che hanno come attributo data-idfornitore (le celle della colonna Già nel carrello)
            let tds = document.querySelectorAll("td[data-idfornitore]");
            // per ognuna, aggiorno il testo
            tds.forEach( td => {
				// leggo idfornitore e idprodotto
                let sAttr1 = td.getAttribute("data-idfornitore");
                let sAttr2 = td.getAttribute("data-idprodotto");
                // controllo che gli id siano interi anche lato client
                if( isNaN(sAttr1) || isNaN(sAttr2) || !Number.isInteger(parseFloat(sAttr1)) || !Number.isInteger(parseFloat(sAttr2)) ){
                    alert("Valore di almeno un id non valido.\nIl controllo è avvenuto lato client.");
                    return;
                }
                // li leggo come interi effettivamente
                let idFornitore = parseInt(sAttr1);
                let idProdotto = parseInt(sAttr2);

				// prendo la parte di carrello associata al fornitore corrente
                let carrelloFornitore = carrello.getCarrelloFornitore(idFornitore);
                if( carrelloFornitore == undefined ){
                    td.textContent = "Nessun prodotto di questo fornitore nel carrello.";
                    return;
                }
                // calcolo il numero totale di prodotti
                let numeroArticoli = carrelloFornitore.prodotti.reduce(function(q, prodotto){
                    return q + prodotto.quantita;
                }, 0);
				// calcolo il valore totale in euro
                let totale = 0;
                for( let i=0; i<carrelloFornitore.prodotti.length; i++ ){
					// prendo il prodotto i-esimo
                    let prodotto = carrelloFornitore.prodotti[i];
                    // controllo che sia sul listino
                    let prodottoInListino = listino.filter((prod) => ( prod.idProdotto === idProdotto ) && ( prod.idFornitore === idFornitore ));
                    if( prodottoInListino.length == 0 ){
                        alert("Un prodotto presente nel carrello non è nel listino fornito dal server.\nIl carrello verrà cancellato e tu verrai riportato al login.");
                        localStorage.removeItem("carrello");
                        logout();
                        return;
                    }
                    totale += prodotto.quantita * prodottoInListino[0].prezzoScontato;
                }
                // aggiungo il contenuto nella cella corrente
                td.textContent = numeroArticoli + " articoli di questo fornitore nel carrello, per un valore di " + totale.toFixed(2) + " €";
            } );
        }

		/* --- definisco la funzione che visualizza myModal (viene chiamata quando il mouse passa su una cella in Già nel carrello) --- */
		
        function openModal(e){
			// prendo myModal
            let modal = document.getElementById("myModal");
            
            // "The returned value is a DOMRect object which is the smallest rectangle which contains the entire element,
            // including its padding and border-width. The left, top, right, bottom, x, y, width, and height properties
            // describe the position and size of the overall rectangle in pixels.
            // Properties other than width and height are relative to the top-left of the viewport.
            // The width and height properties include the padding and border-width."
            let rect = e.target.getBoundingClientRect();
            
            // prendo la parte di carrello associata al fornitore corrente
            let carrelloFornitore = carrello.getCarrelloFornitore(e.target.getAttribute("data-idfornitore"));
            if( carrelloFornitore == null )
                return;
                        
            // faccio richiesta a infoCarrello mandando la suddetta parte di carrello come JSON
            let string = "[" + JSON.stringify(carrelloFornitore) + "]";
            postJsonData("infoCarrello", string, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch( risposta.status ){
                        case 200: // ok
                        	// prendo le info sul carrello
                            let infoCarrello;
                            try{
                                infoCarrello = JSON.parse(risposta.responseText);
                            } catch (e) {
                                alert("Errore lato client durante il parsing di JSON delle informazioni sul carrello fornite dal server: " + e);
                                return;
                            }
                            
                            // assegno a modal.style tutte le proprietà dell'oggetto dato come secondo parametro
                            // px scelti oggettivamente a caso
                            Object.assign(modal.style, {
                                left: `${rect.left - 40}px`,
                                top: `${e.pageY - rect.height}px`,
                                display: 'block'
                            });
                            // lo svuoto
                            modal.innerHTML = "";

							// indico il nome del fornitore
                            let nome = document.createElement('h4');
                            console.log(infoCarrello[0].nome);
                            nome.textContent = infoCarrello[0].nome;
                            modal.appendChild(nome);

							// e aggiungo la lista di prodotti
                            let list = document.createElement('ul');
                            modal.appendChild(list);

							// appendo i prodotti alla lista
							// (ricorda che info è un array in generale, se il carrello ha articoli di un solo fornitore è lungo 1)
                            infoCarrello[0].prodotti.forEach( p => {
                                let li = document.createElement('li');
                                li.textContent = p.quantita + "x " + p.nome;
                                list.appendChild(li);
                            })

                            break;
                        case 400: // bad request
							alert("Carrello non valido, rifiutato dal server.\nIl carrello verrà cancellato e tu verrai riportato alla home.")
                            localStorage.removeItem("carrello");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;                	
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.")
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            }, false);

        }

		// metodo per chiudere myModal
        function closeModal(){
            document.getElementById("myModal").style.display = "none";
        }
    }
    
    /************************************************************************************/

	// pagina-componente carrello
    function Carrello(container){
        this.container = container;

		// metodo che recupera le informazioni sul carrello
        this.show = function(){
			// salvo this in self per colpa della visibilità di js
            const self = this;
            
            // se presente la divSearch la tolgo
            let divSearch = document.querySelector('.searchForm');
			if( divSearch )
  				divSearch.remove();

			// aggiungo il titolo
            let h2 = document.createElement('h2');
            h2.textContent = "Carrello";
            this.container.appendChild(h2);
            
            // prendo il carrello
            let carrello;
            try {
                carrello = JSON.parse(localStorage.getItem("carrello"));
            } catch (e) {
                alert("Errore durante il parsing di JSON del carrello.\nIl carrello verrà cancellato e tu verrai riportato alla home.\nIl controllo è avvenuto lato client.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
                return ;
            }

			// se il carrello è vuoto non mostro nulla
            if( carrello === null || ( Object.keys(carrello).length === 0 ) ){
                let p = document.createElement('p');
                p.textContent = "Nessun prodotto nel carrello";
                this.container.appendChild(p);
                return;
            }

			// faccio richiesta a infoCarrello mandando la mappa del carrello come JSON
            postJsonData("infoCarrello", carrello, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch(risposta.status) {
                        case 200: // ok
                            self.riempiPaginaCarrello(risposta);
                            break;
                        case 400: // bad request
							alert("Carrello non valido, rifiutato dal server.\nIl carrello verrà cancellato e tu verrai riportato alla home.")
                            localStorage.removeItem("carrello");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;                	
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.")
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            });

        }

		// metodo che riempie effettivamente la pagina del carrello
        this.riempiPaginaCarrello = function(risposta){
			// salvo this in self per colpa della visibilità di js
            const self = this;

			// prendo le informazioni del carrello
			let infoCarrello;
            try {
               	infoCarrello = JSON.parse(risposta.responseText);
            } catch(e) {
                alert("Errore lato client durante il parsing di JSON delle informazioni sul carrello fornite dal server: " + e);
                return;
            }

			// aggiungo la lista di fornitori
            let ul = document.createElement('ul');
            ul.classList.add('listview');
            this.container.appendChild(ul);

			// per ogni fornitore aggiungo una riga
            infoCarrello.forEach( (infoFornitore) => {
				// aggiungo la riga
                let li = document.createElement('li');
                li.classList.add('listview-row');
                ul.appendChild(li);

				// aggiungo il div in testata
                let divHeading = document.createElement('div');
                divHeading.classList.add('order-heading');
                li.appendChild(divHeading);

				// mostro il nome del fornitore
                let h3 = document.createElement('h3');
                h3.textContent = infoFornitore.nome;
                divHeading.appendChild(h3);

				// aggiungo il bottone per ordinare
                let btnOrdina = document.createElement('button');
                btnOrdina.textContent = "Ordina";
                btnOrdina.setAttribute('data-idfornitore', infoFornitore.id);
                // specificando la funzione onclick
                btnOrdina.onclick = function(e){
					// controllo che l'id del fornitore sia un intero
                    if( isNaN(e.target.getAttribute('data-idfornitore')) || !Number.isInteger(parseFloat(e.target.getAttribute("data-idfornitore"))) ){
                        alert("Valore dell'id del fornitore non valido.\nControllo avvenuto lato client.");
                        return;
                    }
                    // mando l'ordine per quel fornitore
                    self.inviaOrdine(e.target.getAttribute('data-idfornitore'))
                }
                divHeading.appendChild(btnOrdina);

				// aggiungo un a capo
                li.appendChild(document.createElement('br'));
                
                // e sulla stella riga aggiungo la tabella con la parte di carrello per quel fornitore
                
                // aggiungo la tabella con le informazioni per ogni fornitore
	            let table = document.createElement('table');
	            li.appendChild(table);
	            table.classList.add('order-table');
	            let tableHead = document.createElement('thead');
	            table.appendChild(tableHead);
	            let tableHeaderRow = document.createElement('tr');
	            tableHead.appendChild(tableHeaderRow);
				// creo una colonna per ogni informazione necessaria
	            let nomiColonne = ['Nome','Immagine','Quantita','Prezzo']
	            for( let i=0; i<nomiColonne.length; i++ ){
	                let th = document.createElement('th');
	                th.textContent = nomiColonne[i];
	                tableHeaderRow.appendChild(th);
	            }
				// aggiungo il corpo della tabella
	            let tableBody = document.createElement('tbody');
	            table.appendChild(tableBody);

				// e per ogni prodotto nella parte di carrello del fornitore corrente aggiungo una riga
                infoFornitore.prodotti.forEach( (p) => {
					// creo la riga
                    let riga = document.createElement('tr');
                    tableBody.appendChild(riga);
					// aggiungo il nome del prodotto
                    let tdNome = document.createElement('td');
                    riga.appendChild(tdNome);
                    tdNome.textContent = p.nome;
					// aggiungo l'immagine
                    let tdImg = document.createElement('td');
                    riga.appendChild(tdImg);
                    let img = document.createElement('img');
                    img.src = "immagine?idProdotto=" + p.idProdotto;
                    tdImg.appendChild(img);
					// aggiungo la quantita
                    let tdQuantita = document.createElement('td');
                    riga.appendChild(tdQuantita);
                    tdQuantita.textContent = p.quantita;
					// aggiungo il prezzo
                    let tdPrezzo = document.createElement('td');
                    riga.appendChild(tdPrezzo);
                    tdPrezzo.textContent = p.prezzo.toFixed(2) + " €";
                } );
                
            } );
        }
		
		// metodo che restituisce la parte di carrello del fornitore richiesto
        this.getCarrelloFornitore = function(id){
			// controllo se l'id è valido anche lato client
            if( isNaN(id) || !Number.isInteger(parseFloat(id)) ){
                alert("Valore dell'id del fornitore di cui è richiesto il carrello non valido.\nErrore avvenuto lato client.");
                return ;
            }
			// prendo l'id come intero
            let idFornitore = parseInt(id);
            
            // prendo il carrello
            let carrello;
            try {
                carrello = JSON.parse(localStorage.getItem("carrello"));
            } catch (e) {
                alert("Errore durante il parsing di JSON del carrello.\nIl carrello verrà cancellato e tu verrai riportato alla home.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
                return ;
            }
			// se il carrello non esiste ancora, lo creo
            if( carrello == null )
                carrello = [];
	
			// prendo la parte di carrello per il fornitore richiesto
            let carrelloFornitore = carrello.filter(o => o.idFornitore == idFornitore);
			// se non c'è, ritorno undefined
            if( carrelloFornitore.length == 0 )
                return undefined;
            // altrimenti ritorno una copia della parte del carrello del fornitore richiesto
            // (ricorda che carrelloFornitore è inizializzato come array in generale, in questo caso lungo 1)
            else
            	return carrelloFornitore.slice()[0];
        }

		// metodo che aggiunge un prodotto al carrello
        this.aggiungiProdotto = function(idP, idF, q){

			// prendo gli id e la quantita
            if( isNaN(idP) || isNaN(idF) || isNaN(q) || !Number.isInteger(parseFloat(idP)) || !Number.isInteger(parseFloat(idF)) || !Number.isInteger(parseFloat(q)) ){
                alert("Valore di almeno uno tra i due id e la quantità non valido.\nControllo avvenuto lato client.");
                return;
            }
            let idProdotto = parseInt(idP);
            let idFornitore = parseInt(idF);
            let quantita = parseInt(q);
            
            // prendo il carrello
            let carrello;
            try {
                carrello = JSON.parse(localStorage.getItem("carrello"));
            } catch (e) {
                alert("Errore durante il parsing di JSON del carrello.\nIl carrello verrà cancellato e tu verrai riportato alla home.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
                return ;
            }
			// se il carrello non esiste ancora, lo creo
            if( carrello == null )
                carrello = [];

			// prendo la parte di carrello del fornitore richiesto
            let carrelloFornitore = carrello.filter(o => o.idFornitore === idFornitore)[0];
            // se il carrello è vuoto aggiungo la prima voce al carrello
            if( carrelloFornitore == undefined ){
                let newVoice = {
                    "idFornitore" : idFornitore,
                    "prodotti" : [
                        {
                            "idProdotto" : idProdotto,
                            "quantita" : quantita
                        }
                    ]
                }
                carrello.push(newVoice);
            }
            // se non lo è, aggiungo un nuovo prodotto
            else{
				// se non c'erano prodotti, aggiungo una nuova riga
                if( carrelloFornitore.prodotti.length == 0 ){
                    let newVoice = {
                                "idProdotto" : idProdotto,
                                "quantita" : quantita
                    }
                    carrelloFornitore.prodotti.push(newVoice);
                }
                // se invece ce ne sono devo gestire due casi
                else{
					// prendo il la riga del prodotto nella parte di carrello del fornitore richiesto
                    let prodotto = carrelloFornitore.prodotti.filter(o => o.idProdotto === idProdotto)[0];
                    // se il prodotto non c'era, lo aggiungo
                    if( prodotto == undefined ){
                        let newVoice = {
                            "idProdotto" : idProdotto,
                            "quantita" : quantita
                        }
                        carrelloFornitore.prodotti.push(newVoice);
                    }
                    // altrimenti aumento la sua quantità
                    else
                        prodotto.quantita += quantita;
                }

            }
			// aggiorno il carrello
            localStorage.setItem("carrello", JSON.stringify(carrello));
        }

		// metodo che invia un nuovo ordine
        this.inviaOrdine = function(id){
			// salvo this in self per colpa della visibilità di js
            const self = this;
            
            // prendo l'id del fornitore
            if( isNaN(id) ){
                alert("Valore dell'id del fornitore non valido.\nControllo avvenuto lato client.");
                return;
            }
            let idFornitore = parseInt(id);

			// prendo il carrello
            let carrello;
            try {
                carrello = JSON.parse(localStorage.getItem("carrello"));
            } catch (e) {
                alert("Errore durante il parsing di JSON del carrello.\nIl carrello verrà cancellato e tu verrai riportato alla home.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
                return ;
            }

			// prendo la parte di carrello del fornitore richiesto
            let carrelloFornitore = carrello.filter(o => o.idFornitore === idFornitore)[0];
            if( carrelloFornitore == undefined ){
                alert("Valore dell'id del fornitore non presente nel carrello.\nControllo avvenuto lato client.");
                return;
            }
            if( ( carrelloFornitore.prodotti == null ) || ( Object.keys(carrelloFornitore.prodotti).length == 0 ) || ( carrelloFornitore.prodotti.length == 0 ) ){
                alert("Valore dell'id del fornitore non ha prodotti associati nel carrello.\nControllo avvenuto lato client.");
                carrello = carrello.filter(o => o.idFornitore != idFornitore);
                localStorage.setItem(JSON.stringify(carrello));
                return;
            }

			// creo l'ordine
            postJsonData("ordini", carrelloFornitore, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch(risposta.status){
                        case 200: // ok
                            self.rimuoviProdotti(idFornitore);
                            pageOrchestrator.hide();
                            pageOrchestrator.showOrdini();
                            break;
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.");
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            })

        }

		// metodo che rimuove i prodotti di un certo fornitore dal carrello
        this.rimuoviProdotti = function(idF){
			
			// prendo l'id del fornitore
            if( isNaN(idF) || !Number.isInteger(parseFloat(idF)) ){
                alert("Valore dell'id del fornitore non valido.\nIl carrello verrà cancellato e tu verrai riportato alla home.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
            }
            let idFornitore = parseInt(idF);

			// prendo il carrello
            let carrello;
            try {
                carrello = JSON.parse(localStorage.getItem("carrello"));
            } catch (e) {
                alert("Errore durante il parsing di JSON del carrello.\nIl carrello verrà cancellato e tu verrai riportato alla home.");
                localStorage.removeItem("carrello");
                pageOrchestrator.hide();
                pageOrchestrator.showHome();
                return ;
            }

			// tolgo dal carrello i prodotti del fornitore da rimuovere
            carrello = carrello.filter(x => x.idFornitore != idFornitore);
            localStorage.setItem("carrello", JSON.stringify(carrello));
        }
    }
    
    /************************************************************************************/

    function Ordini(container){
        this.container = container;

		// metodo che recupera gli ordini
        this.show = function(){
			// salvo this in self per colpa della visibilità di js
            const self = this;
            
            // se presente la divSearch la tolgo
            let divSearch = document.querySelector('.searchForm');
			if( divSearch )
  				divSearch.remove();

			// faccio la chiamata di get per vedere gli ordini
            makeCall("GET", "ordini", null, function(risposta){
                if( risposta.readyState === XMLHttpRequest.DONE ){
                    switch( risposta.status ){
                        case 200: // ok
                            self.riempiPaginaOrdini(risposta);
                            break;
                        case 401: // unauthorized
                            alert("Non sei loggato.\nVerrai riportato al login.");
                            logout();
                            break;
                        case 500: // server error
                            alert("Errore nel server.\nVerrai riportato alla home.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                        default:
                            alert("Errore sconosciuto.");
                            pageOrchestrator.hide();
                            pageOrchestrator.showHome();
                            break;
                    }
                }
            } );
        }

		// metodo che riempie effettivamente la pagina degli ordini
        this.riempiPaginaOrdini = function(risposta){
            // prendo gli ordini
            let ordini;
            try {
                ordini = JSON.parse(risposta.responseText);
            } catch (e) {
                alert("Errore lato client durante il parsing di JSON degli ordini forniti dal server: " + e);
                return;
            }
            
            // se non ci sono ordini non mostro nulla
            if( ordini.length == 0 ){
                let p = document.createElement('p');
                p.textContent = "Non hai ancora effettuato ordini.";
                this.container.appendChild(p);
                return;
            }

			// aggiungo il titolo
            let h2 = document.createElement('h2');
            h2.textContent = "Storico ordini";
            this.container.appendChild(h2);

			// aggiungo il div che conterrà gli ordini
            let divRisultati = document.createElement('div');
            divRisultati.classList.add("risultati");
            this.container.appendChild(divRisultati);

			// aggiungo la lista di ordini
            let listview = document.createElement('ul');
            listview.classList.add("listview");
            divRisultati.appendChild(listview);
	
			// per ogni ordine, aggiungo un elemento alla lista
            ordini.forEach( (o) => {
				// aggiungo un elemento alla lista
                let li = document.createElement('li');
                listview.appendChild(li);
                li.classList.add('listview-row');
                
				// mostro l'id dell'ordine
                let h3 = document.createElement('h3');
                li.appendChild(h3);
                h3.textContent = "Id ordine: " + o.id;
				// il nome del fornitore
                let pFornitore = document.createElement('p');
                li.appendChild(pFornitore);
                pFornitore.textContent = "Fornitore: " + o.nomeFornitore;
				// l'eventuale data di spedizione
                let pDataSpedizione = document.createElement('p');
                li.appendChild(pDataSpedizione);
                pDataSpedizione.textContent = o.dataSpedizione == undefined ? "Ordine non ancora spedito." : "Data di spedizione: " + o.dataSpedizione;
				// l'indirizzo di spedizione
                let pIndirizzo = document.createElement('p');
                li.appendChild(pIndirizzo);
                pIndirizzo.textContent = 'Indirizzo di spedizione: ' + o.indirizzo;
				// il totale in € senza spese di spedizione
                let pTotale = document.createElement('p');
                li.appendChild(pTotale);
                pTotale.textContent = 'Totale ordine (senza spese di spedizione): ' + o.totaleOrdine.toFixed(2) + ' €';
				// le spese di spedizione
                let pSpeseSpedizione = document.createElement('p');
                li.appendChild(pSpeseSpedizione);
                pSpeseSpedizione.textContent = 'Spese di spedizione: ' + o.speseSpedizione.toFixed(2) + ' €';
				
				// aggiungo il titolo "prodotti"
                let titoloProdotti = document.createElement('h3');
                titoloProdotti.textContent = "Prodotti";
                li.appendChild(titoloProdotti);
				// e aggiungo la tabella con le informazioni per ogni prodotto
	            let table = document.createElement('table');
	            li.appendChild(table);
	            let tableHead = document.createElement('thead');
	            table.appendChild(tableHead);
	            let tableHeaderRow = document.createElement('tr');
	            tableHead.appendChild(tableHeaderRow);
				// creo una colonna per ogni informazione necessaria
	            let nomiColonne = ['Nome Prodotto','Prezzo Unitario','Quantità ordinata'];
	            for( let i=0; i<nomiColonne.length; i++ ){
	                let th = document.createElement('th');
	                th.textContent = nomiColonne[i];
	                tableHeaderRow.appendChild(th);
	            }
				// aggiungo il corpo della tabella
	            let tableBody = document.createElement('tbody');
	            table.appendChild(tableBody);
				// e per ogni prodotto nell'ordine aggiungo una riga
                o.dettagli.forEach( (p) => {
					// aggiungo una riga alla tabella
                    let rigaProdotto = document.createElement('tr');
                    tableBody.appendChild(rigaProdotto);
					// aggiungo il nome alla riga
                    let tdNome = document.createElement('td');
                    tdNome.textContent = p.secondo;
                    rigaProdotto.appendChild(tdNome);
					// aggiungo il prezzo alla riga
                    let tdPrezzo = document.createElement('td');
                    tdPrezzo.textContent = p.primo.prezzoProdotto.toFixed(2) + " €";
                    rigaProdotto.appendChild(tdPrezzo);
					// aggiungo la quantita alla riga
                    let tdQuantita = document.createElement('td');
                    tdQuantita.textContent = p.primo.quantita;
                    rigaProdotto.appendChild(tdQuantita);
                })

            })

        }
    }
    
}