
// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

// automatically submit document upload on file select
function onFileSelect() {
  try{
    document.getElementById("ndrc-fileupload-continue").disabled = true;
    document.getElementById("upload-form").classList.add("govuk-!-display-none");
    document.querySelector(".hidden-progress-row").classList.remove("govuk-!-display-none");

    var loadingMessage = document.getElementById("uploadingMessage").innerText;

    document.title = loadingMessage;
    document.getElementById("uploadingMessage").focus();

    var errorBlock = document.querySelector(".govuk-error-summary");
    if(errorBlock){
      errorBlock.classList.add("govuk-!-display-none");
    }

  }
  finally {
    // timeout before submitting is to give Safari time to render un-hidden element
    // animations stop in Safari once form is submitted so animation start is delayed by 1000 ms
    setTimeout(function () {  document.getElementById("upload-form").submit(); }, 1000);
  }
}