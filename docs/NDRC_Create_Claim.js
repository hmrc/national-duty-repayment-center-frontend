// ==UserScript==
// @name         NDRC Symbol("autoComplete")
// @namespace    http://tampermonkey.net/
// @version      0.7

// @description  NDRC Symbol("autoComplete")
// @author       NDRC Team
// @match        http*://*/apply-for-repayment-of-import-duty-and-import-vat/*
// @match        http*://*/lookup-address/*
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

    /* CREATE */
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
        let now = new Date();
        now.setDate(now.getDate() - 7);
        document.getElementById("EPU").value = "123";
        document.getElementById("EntryNumber").value = "123456Q";
        document.getElementById("EntryDate.day").value = now.getDate();
        document.getElementById("EntryDate.month").value = now.getMonth()+1;
        document.getElementById("EntryDate.year").value = now.getFullYear();
        submit();
    }
    if (currentPageIs("/application-reason")) {
        document.getElementById("value").checked = true;
        document.getElementById("value-6").checked = true;
        submit();
    }
    if (currentPageIs("/main-reason")) {
        document.getElementById("value").checked = true;
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
        document.getElementById("ActualPaidAmount").value = "300.00";
        document.getElementById("ShouldHavePaidAmount").value = "49.99";
        submit();
    }
    if (currentPageIs("/import-vat-overpayment")) {
        document.getElementById("ActualPaidAmount").value = "200.00";
        document.getElementById("ShouldHavePaidAmount").value = "159";
        submit();
    }
    if (currentPageIs("/other-duties-overpayment")) {
        document.getElementById("ActualPaidAmount").value = "300.00";
        document.getElementById("ShouldHavePaidAmount").value = "0";
        submit();
    }
    if (currentPageIs("/repayment-summary")) {
        submit();
    }
    if (currentPageIs("/supporting-documents")) {
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
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/does-importer-have-eori-number")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/enter-your-eori-number")) {
        document.getElementById("value").value = "GB123456789123000";
        submit();
    }
    if (currentPageIs("/enter-importer-eori-number")) {
        document.getElementById("value").value = "GB123456789123111";
        submit();
    }
    if (currentPageIs("/is-importer-vat-registered")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/vat-registered")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/your-name")) {
        document.getElementById("firstName").value = "Tim";
        document.getElementById("lastName").value = "Tester";
        submit();
    }
    if (currentPageIs("/importer-name")) {
        document.getElementById("importerName").value = "ACME Importer Ltd";
        submit();
    }
    if (currentPageIs("/your-details")) {
        document.getElementById("declarantName").value = "Tim Tester";
        document.getElementById("agentName").value = "Tester Import Agents";
        submit();
    }
    if (currentPageIs("/goods-owner")) {
        document.getElementById("value").checked = true;
        submit();
    }

    if (currentPageIs(".*/lookup-address/.*/lookup")) {
        if (window.location.host.includes("localhost")) {
            document.getElementById("postcode").value = "AA000AA";
        }
        else{
            document.getElementById("postcode").value = "WS1 2AB";
        }
        submit();
    }
    if (currentPageIs(".*/lookup-address/.*/edit")) {
        document.getElementById("line1").value = "Unit 42";
        document.getElementById("line2").value = "West Industrial Estate";
        document.getElementById("town").value = "Walsall";
        document.getElementById("postcode").value = "WS1 2AB";
        submit();
    }
    if (currentPageIs(".*/lookup-address/.*/confirm")) {
        submit();
    }

    if (currentPageIs("/enter-importer-address")) {
        document.getElementById("AddressLine1").value = "ACME Importer Ltd";
        document.getElementById("AddressLine2").value = "West Industrial Estate";
        document.getElementById("City").value = "Walsall";
        document.getElementById("PostalCode").value = "WS1 2AB";
        document.getElementById("CountryCode").getElementsByTagName("option")[1].selected = "selected";
        submit();
    }
    if (currentPageIs("/enter-your-business-address")) {
        document.getElementById("AddressLine1").value = "Unit 42";
        document.getElementById("AddressLine2").value = "East Industrial Estate";
        document.getElementById("City").value = "Walsall";
        document.getElementById("PostalCode").value = "WS1 2AB";
        document.getElementById("CountryCode").getElementsByTagName("option")[1].selected = "selected";
        submit();
    }
    if (currentPageIs("/contact")) {
        document.getElementById("value").checked = true;
        document.getElementById("email").value = "tim@example.com";
        submit();
    }
    if (currentPageIs("/declarant-reference-number")) {
        document.getElementById("value").checked = true;
        document.getElementById("declarantReferenceNumber").value = "DEC REF 1234567";
        submit();
    }
    if (currentPageIs("/select-repayment-method")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/repaid")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/indirect-representative")) {
        document.getElementById("value").checked = true;
        submit();
    }
    if (currentPageIs("/enter-bank-details")) {
        document.getElementById("AccountName").value = "Megacorp";
        document.getElementById("SortCode").value = "207106";
        document.getElementById("AccountNumber").value = "86563611";
        submit();
    }

    /* AMEND */
    if (currentPageIs("/amend/application-reference-number")) {
        document.getElementById("value").value = "NDRC000A00AB0ABCABC0AB0";
        submit();
    }
    if (currentPageIs("/amend/what-do-you-need-to-do")) {
        document.getElementById("value-2").checked = true;
        submit();
    }
    if (currentPageIs("/amend/further-information")) {
        document.getElementById("value").value = "Some further information";
        submit();
    }
    if (currentPageIs("/amend/file-uploaded")) {
        document.getElementById("uploadAnotherFile-2").checked = true;
        submit();
    }

}
