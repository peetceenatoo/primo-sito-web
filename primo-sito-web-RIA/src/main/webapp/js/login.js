(function() {

    if(localStorage.getItem("utente") !== null){
        window.location.href = "home-page.html";
    }

    var form = document.getElementById("frmLogin");

    form.addEventListener('submit', (e) => {
       e.preventDefault();
        if (form.checkValidity()) {
            makeCall("POST", 'login', form,
                function(x) {
                    if (x.readyState == XMLHttpRequest.DONE) {
                        var message = x.responseText;
                        switch (x.status) {
                            case 200:
                                localStorage.setItem('utente', message);
                                window.location.href = "home-page.html";
                                break;
                            case 400: // bad request
                                document.getElementById("errormessage").textContent = message;
                                break;
                            case 401: // unauthorized
                                document.getElementById("errormessage").textContent = message;
                                break;
                            case 500: // server error
                                document.getElementById("errormessage").textContent = message;
                                break;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });

})();