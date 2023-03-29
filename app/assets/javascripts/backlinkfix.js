

window.onload = function fixHistoryBackLink() {

    function fixLink(){
        if(elem.href == "javascript:history.back()"){
          history.back()
      }};

    const elem = document.getElementById("back-link");
    elem.onclick = fixLink;
};