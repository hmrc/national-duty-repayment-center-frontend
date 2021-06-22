// ==UserScript==
// @name     NDRC Authorisation
// @namespace  http://tampermonkey.net/
// @version   0.1
// @description Authenticates for NDRC
// @author    NDRC Team
// @match     http*://*/auth-login-stub/gg-sign-in?continue=*apply-for-repayment-of-import-duty-and-import-vat*
// @grant     none
// @updateURL https://raw.githubusercontent.com/hmrc/national-duty-repayment-center-frontend/master/docs/NDRC_Auth_Stub.js
// ==/UserScript==

(function () {
    'use strict';

    document.getElementById("affinityGroupSelect").selectedIndex = 1;
    
    document.getElementsByName("enrolment[0].name")[0].value = "HMRC-CTS-ORG";
    document.getElementById("input-0-0-name").value = "EORINumber";
    document.getElementById("input-0-0-value").value = "GB123456789000";

    document.getElementsByName("redirectionUrl")[0].value = getBaseUrl() + "/apply-for-repayment-of-import-duty-and-import-vat/what-do-you-want-to-do";


    document.querySelector('header').appendChild(createQuickButton())
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id = "quickSubmit";
    button.classList.add('govuk-button', 'govuk-!-display-none-print');
    button.innerHTML = 'Quick Submit';
    button.onclick = () => document.getElementById('submit').click();
    return button;
}

function getBaseUrl() {
    let host = window.location.host;
    if (window.location.hostname === 'localhost') {
        host = 'localhost:8450'
    }
    return window.location.protocol + "//" + host;
}