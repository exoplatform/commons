(function () {

  var i18n = new VueI18n({
    locale: 'en', // set locale
    fallbackLocale: 'en',
    messages: {}
  });

  function loadLanguageAsync(lang, url) {
    if (!lang) {
      lang = i18n.locale;
    }
    return fetch(url, { credentials: 'include' })
    .then(function (resp) {
      return resp.json();
    })
    .then(function (msgs) {
      i18n.mergeLocaleMessage(lang, msgs);
      i18n.locale = lang;
      return i18n;
    });
  }

  return { 'loadLanguageAsync': loadLanguageAsync };
})();