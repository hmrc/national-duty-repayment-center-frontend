window.onload = function(e) {
     openregisterLocationPicker({
        additionalSynonyms: [
             { name: 'Albion', code: 'country:GB' }
            ],
            defaultValue: '',
            selectElement: document.getElementById('CountryCode'),
            url: 'location-autocomplete-graph.json'
     });
};