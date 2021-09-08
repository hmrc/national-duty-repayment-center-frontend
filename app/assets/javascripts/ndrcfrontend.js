
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
    var errorBlock = document.querySelector(".govuk-error-summary");
    if(errorBlock){
      errorBlock.classList.add("govuk-!-display-none");
    }
    document.getElementById("uploadingMessage").focus();
    document.title = document.getElementById("uploadInProgressTitle").innerText;
  }
  finally {
    // timeout before submitting is to give Safari time to render un-hidden element
    // animations stop in Safari once form is submitted so animation start is delayed by 500 ms
    setTimeout(function () {  document.getElementById("upload-form").submit(); }, 500);
  }
}