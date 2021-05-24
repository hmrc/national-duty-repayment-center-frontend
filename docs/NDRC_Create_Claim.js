// ==UserScript==
// @name         NDRC AutoComplete
// @namespace    http://tampermonkey.net/
// @version      0.1

// @description  NDRC AutoComplete
// @author       NDRC Team
// @match        http*://*/national-duty-repayment-centre/*
// @grant        none
// @updateURL    https://raw.githubusercontent.com/hmrc/national-duty-repayment-center-frontend/master/docs/NDRC_Create_Claim.js
// ==/UserScript==

(function () {
    'use strict';
    document.getElementsByTagName("body")[0].appendChild(createQuickButton());
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id = "quickSubmit";
    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start', 'govuk-!-display-none-print');
    } else {
        button.classList.add('govuk-button', 'govuk-!-display-none-print');
    }
    button.style.position = "absolute"
    button.style.top = "50px"
    button.innerHTML = 'Quick Submit';
    button.onclick = () => completePage();
    return button;
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path + "$");
    return matches && matches.length > 0
}

function submit() {
    document.getElementsByClassName("govuk-button")[0].click();
}

function completePage() {

    /* START */
    if (currentPageIs("/what-do-you-want-to-do")) {
        document.getElementById("value").checked = true;
        submit();
    }


    if (currentPageIs("/importer-or-representative")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/how-many-entries-submitting")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/acceptance-date-all-entries")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/why-are-you-applying-uk")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/oldest-entry-date")) {
        document.getElementById("EPU").value = "123";
        document.getElementById("EntryNumber").value = "123456Q";
        document.getElementById("EntryDate.day").value = "12";
        document.getElementById("EntryDate.month").value = "12";
        document.getElementById("EntryDate.year").value = "2020";
        submit();
    }
    if (currentPageIs("/application-reason")) {
        document.getElementById("04").checked = true;
        submit();
    }
    if (currentPageIs("/reason-for-overpayment")) {
        document.getElementById("value").value = "I believe I have paid too much duty";
        submit();
    }
    if (currentPageIs("/reclaim")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/customs-duty-overpayment")) {
        document.getElementById("ActualPaidAmount").value = "100.00";
        document.getElementById("ShouldHavePaidAmount").value = "89.99";
        submit();
    }
    if (currentPageIs("/repayment-summary")) {
        submit();
    }
    if (currentPageIs("/evidenceSupportingDocs")) {
        submit();
    }
    if (currentPageIs("/file-uploaded")) {
        document.getElementById("uploadAnotherFile-2").checked = true;
        submit();
    }
    if (currentPageIs("/do-you-have-an-eori-number")) {
        document.getElementById("value-no").checked = true;
        submit();
    }
    if (currentPageIs("/vat-registered")) {
        document.getElementById("value-2").checked = true;
        submit();
    }
    if (currentPageIs("/enter-your-name")) {
        document.getElementById("firstName").value = "Tim";
        document.getElementById("lastName").value = "Tester";
        submit();
    }
    if (currentPageIs("/goods-owner")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/select-importer-address")) {
        document.getElementById("PostalCode").value = "AA000AA";
        submit();
    }
    if (currentPageIs("/contact")) {
        document.getElementById("value").checked = true;
        document.getElementById("email").value = "tim@example.com";
        submit();
    }
    if (currentPageIs("/declarant-reference-number")) {
        document.getElementById("value-2").checked = true;
        submit();
    }
    if (currentPageIs("/select-repayment-method")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/enter-bank-details")) {
        document.getElementById("AccountName").value = "ACME Importers Ltd";
        document.getElementById("SortCode").value = "400530";
        document.getElementById("AccountNumber").value = "71584685";
        submit();
    }

}
