(function () {
  var XSSUtils = {
    sanitizeString: function (keyword) {
      return keyword.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;").replace(/\//g, "&#x2F;");
    },
    escapeHtml: function(html) {
      var map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
      };

      return html.replace(/[&<>"']/g, function(m) { return map[m]; });
    }
  };
    
  return XSSUtils;
})();
