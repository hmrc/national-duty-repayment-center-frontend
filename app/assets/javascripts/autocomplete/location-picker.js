window.onload = function (e) {
  openregisterLocationPicker({
    additionalSynonyms: [
      {name: 'Albion', code: 'country:GB'}
    ],
    defaultValue: '',
    selectElement: document.getElementById('CountryCode'),
    url: 'location-autocomplete-graph.json'
  });

  const countryCode = document.getElementById('CountryCode');

  if (countryCode) {
    const countryCodeSelect = document.getElementById('CountryCode-select');

    countryCode.addEventListener('change', function(e) {
      if (countryCode.value.trim() === "") {
        countryCodeSelect.querySelector('[value=""]').selected = true;
      }
    });
  }
};
