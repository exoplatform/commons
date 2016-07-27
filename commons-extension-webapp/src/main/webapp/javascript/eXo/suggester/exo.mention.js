(function($) {  
  var $input, $editable;
  
  $.widget('exo.mention', {
    options : {
      type : 0,
      source : []
    },
    _create : function() {
      $input = this.element;
      $input.hide();
      
      if (this.options.type === 1) {
        log('creating jquery.mention input');
        
        $editable = $('<div id="' + $input.attr('id') + '_editable" contenteditable="true"></div>');
        $input.after($editable);
        
        this.options.autocomplete = {
            appendTo: $input.parent()
        }
        
        var source = this.options.source;
        if (source && $.isFunction(source)) {
          this.options.source = function(request, response) {
            source.call(this, request.term, response);
          }
        }
        
        $editable.mentionsInput(this.options);

        if (this.options.renderMenuItem) {
          $editable.editablecomplete('instance')._renderItem = this.options.renderMenuItem;
        }

        if (this.options.renderItem) {
          $editable.data('mentionsInput').mentionTpl = this.options.renderItem;
        } else {
          $editable.data('mentionsInput').mentionTpl = function(mention) {
            var tpl = '<span data-mention="' + mention.uid + '">' + mention.label + 
            '<i class="uiIconClose uiIconLightGray" onclick="this.parentNode.parentNode.removeChild(this.parentNode)"></i></span>';
            return tpl;
          }
        }
        
        $editable.data('mentionsInput')._markupMention = function(mention) {
          return "@" + mention.uid;
        };
        
        $editable.on('change.mentionsInput keyup', function() {
          var val = $editable.mentionsInput('getValue');          
          val = val.replace('<br>', '');
          $input.val(val);
        });
      } else {
        log('creating selectize input');
        
        if (!this.options.valueField) {
          this.options.valueField = 'uid';
        }
        if (!this.options.labelField) {
          this.options.labelField = 'value';
        }
        if (!this.options.searchField) {
          this.options.searchField = [this.options.labelField];
        }
        if (this.options.create === null) {
          this.options.create = true;
        }
        
        if (this.options.source) {
          var source = this.options.source;
          if ($.isArray(source)) {
            this.options.options = this.options.source;            
          } else {
            this.options.options = [];
            this.options.onType = function() {
              $input[0].selectize.load(function(callback) {
                if ($.isFunction(source)) {
                  source.call(this, this.currentResults.query, callback);
                } else {
                  $.ajax({
                    url : source,
                    data: 'term=' + this.currentResults.query,
                    success: function(response) {
                      callback(repsonse);
                    }, 
                    error: function() {
                      callback([]);
                    }
                  });
                }
              });
            }
          }
        }
        
        if (this.options.renderMenuItem) {
          this.options.render = {
              option: this.options.renderMenuItem
          };
        }        
        
        if (this.options.renderItem) {
          if (!this.options.render) {
            this.options.render = {};
          }
          this.options.render.item = this.options.renderItem;
        }
        $input.selectize(this.options);
      }
    },
    getValue : function() {
      if (this.options.type === 0) {
        return $input[0].selectize.getValue();
      } else {
        return $editable.mentionsInput('getValue');
      }
    },
    setValue : function(val) {
      if (this.options.type === 0) {
        return $input[0].selectize.setValue(val);
      } else {
        return $editable.mentionsInput('setValue', val);
      }
    },
    getMentions: function() {
      if (this.options.type === 0) {
        return $input[0].selectize.getValue();
      } else {
        return $editable.mentionsInput('getMentions');
      }
    }
  });
  
  function log(msg) {
    if (window.console && window.console.log) {
      window.console.log(msg);
    }
  }
  
  return $;
})($);