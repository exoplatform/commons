function EXOCKEDITOR() {
}


EXOCKEDITOR.prototype.loadScript = function (url, callback){
  var script = document.createElement("script")
  script.type = "text/javascript";
  if (script.readyState){ // IE
    script.onreadystatechange = function(){
      if (script.readyState == "loaded" || script.readyState == "complete"){
        script.onreadystatechange = null;
        callback();
      }
    };

  } else { // Others
    script.onload = function(){
      callback();
    };
  }
  script.src = url;
  document.getElementsByTagName("head")[0].appendChild(script);
};


EXOCKEDITOR.prototype.replaceByCKEditor = function(textAreaId, config) {
  var instances = CKEDITOR.instances[textAreaId]; if (instances) instances.destroy(true);
  if (!config) {
    CKEDITOR.replace(textAreaId).on('key', function(ev) {var me = ev.editor; me.element.setValue(me.getData()); });
  } else {
    CKEDITOR.replace(textAreaId, config).on('key', function(ev) {var me = ev.editor; me.element.setValue(me.getData()); });
  }
};

EXOCKEDITOR.prototype.makeCKEditor = function(textAreaId, config) {
  
  if (!window.CKEDITOR) {
    EXOCKEDITOR.loadScript(window.CKEDITOR_BASEPATH + 'ckeditor.js', function() {EXOCKEDITOR.replaceByCKEditor(textAreaId, config);});
  } else {
    EXOCKEDITOR.replaceByCKEditor(textAreaId, config);
  }
};
﻿EXOCKEDITOR = new EXOCKEDITOR();

// function updateck() {
// for ( instance in CKEDITOR.instances ) {
// if (document.getElementById(instance)==null) { clearInterval(intckeditor); }
// document.getElementById(instance).value =
// CKEDITOR.instances[instance].getData();
// }
// }

// This code is mandatory to update CKEditor instances as they don't sync
// natively in Ajax Popup with Chrome and Safari Browser
// var intckeditor = setInterval("updateck()", 1000);


eXo.commons.ExoEditor = ﻿EXOCKEDITOR;

_module.﻿EXOCKEDITOR = eXo.commons.ExoEditor;