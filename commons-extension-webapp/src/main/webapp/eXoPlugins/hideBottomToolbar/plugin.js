require(['SHARED/jquery'],function($) {
  CKEDITOR.plugins.add( 'hideBottomToolbar', {
    init : function( editor ) {

      var showToolbar = true;
      if (editor.config.showBottomToolbar === false) {
        showToolbar = false;
      }

      var toolbarHeight = 37;
      editor.on('instanceReady', function(evt) {
        var $bottom = $('#' + evt.editor.id + '_bottom');
        $bottom.removeClass('cke_bottom_visible').css('height', '0px');
      });
      editor.on('focus', function(evt) {
        if (showToolbar) {
          //Fix the editor height via autogrow plugin
          evt.editor.execCommand('autogrow');

          var $ckeBottom = $('#' + evt.editor.id + '_bottom');
          var originalHeight = $ckeBottom.css('height', 'auto').outerHeight();
          toolbarHeight = $ckeBottom.addClass('cke_bottom_visible').outerHeight();
          var heightToAddVisibleClass = toolbarHeight - originalHeight;
          $ckeBottom.removeClass('cke_bottom_visible').css('height', '0px');

          var $content = $('#' + evt.editor.id + '_contents');
          var contentHeight = $content.height();

          $ckeBottom.animate({
            height: "" + toolbarHeight
          }, {
            step: function(number, tween) {
              $content.height(contentHeight - number);
              if (number >= heightToAddVisibleClass) {
                $ckeBottom.addClass('cke_bottom_visible');
              }
            }
          });
        }
      });
      editor.on('blur', function(evt) {
        if (showToolbar) {
          $('#' + evt.editor.id + '_contents').css('height', $('#' + evt.editor.id + '_contents').height() + toolbarHeight);
          $('#' + evt.editor.id + '_bottom').css('height', '0px');
          $('#' + evt.editor.id + '_bottom').removeClass('cke_bottom_visible');
        }
      });
    }
  });
});