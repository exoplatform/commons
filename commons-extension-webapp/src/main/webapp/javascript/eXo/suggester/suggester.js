(function($) {  
  var $input, $editable;
  
  var type = {
      TAG : "tag",
      MIX : "mix"
  };
  
  var providers = {};
  var items = [];
  function loadFromProvider(term, response) {
    var p = {};    
    var _this = this; 
    
    $.each(providers, function(name, provider) {
      if ($.inArray(name, _this.options.optionsProviders)) {
        if (!p[name]) {
          p[name] = provider;          
        }
      }
    });
    
    $.each(p, function(name, provider) {      
      if ($.isFunction(provider)) {
        provider.call(this, term,  function(results) {
          if (results && results.length) {
            $.each(results, function(idx, elm) {
              items[items.length] = elm;
            });
          }
        });
      }
    });
    response.call(this, items);
    items = [];
  }
  
  $.widget('exo.suggester', {
    options : {
      type : type.MIX,
      source : [],
      optionProviders : [],
      showAvatar : true
    },
    _create : function() {
      $input = this.element;
      $input.hide();
      
      if (this.options.type.toLowerCase() === type.MIX) {
        log('creating jquery.mention input');
        
        $editable = $('<div id="' + $input.attr('id') + '_editable" contenteditable="true"></div>');
        $input.after($editable);
        
        this.options.autocomplete = {
            appendTo: $input.parent()
        }
        
        var source = this.options.source;
        if (source && source.length) {
          if ($.isFunction(source)) {
            this.options.source = function(request, response) {
              source.call(this, request.term, response);
            }            
          }
        } else if (this.options.optionProviders && this.options.optionProviders.length) {
          var _this = this;
          this.options.source = function(request, response) {
            loadFromProvider.call(_this, request.term, response);
          };
        }
        
        $editable.mentionsInput(this.options);

        if (this.options.renderMenuItem) {
          $editable.editablecomplete('instance')._renderItem = function(ul, item) {
            var tpl = this.options.renderMenuItem.call(this, item);
            $(ul).append(tpl);
          }
        } else if (this.options.showAvatar) {
          $editable.editablecomplete('instance')._renderItem = function(ul, item) {
            var anchor, li, regexp, value;
            li = $('<li>');
            anchor = $('<a>').appendTo(li);
            if (item.image) {
              anchor.append("<img  width=\"20px\" height=\"20px\" src=\"" + item.image + "\" />");
            } else {
              anchor.append('<img width="20px" height="20px" src="/eXoSkin/skin/images/system/SpaceAvtDefault.png">');
            }
            regexp = new RegExp("(" + escapeRegExp(this.searchTerm) + ")", "gi");
            value = item.value.replace(regexp, "<strong>$&</strong>");
            anchor.append(value);
            return li.appendTo(ul);
          }
        }

        if (this.options.renderItem) {
          $editable.data('mentionsInput').mentionTpl = this.options.renderItem;
        } else {
          $editable.data('mentionsInput').mentionTpl = function(mention) {
            var tpl = '<span data-mention="' + mention.uid + '">' + mention.value + 
            '<i class="uiIconClose uiIconLightGray" onclick="this.parentNode.parentNode.removeChild(this.parentNode)"></i></span>';
            return tpl;
          }
        }
        
        $editable.data('mentionsInput')._markupMention = function(mention) {
          return "@" + mention.uid;
        };
        
        $editable.on('change.mentionsInput keyup', function() {
          var val = $editable.mentionsInput('getValue');          
          val = val.replace(/<br>/g, '');
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

        if (this.options.selectedItems) {
          this.options.items = this.options.selectedItems;
        }
        
        if (!this.options.source && this.options.source.length && this.options.optionProviders && this.options.optionProviders.length) {
          var _this = this;
          this.options.source = function(term, response) {
            loadFromProvider.call(_this, term, response);
          };
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
        } else if (this.options.showAvatar) {
          this.options.render = {
              option: function(data, escape) {
                var tpl = '<div data-value="' + data.uid + '" data-selectable="" class="option">';
                var img = data.image || '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
                tpl += '<img width="20px" height="20px" src="' + img + '"> ' + data.value + '</div>';
                 return tpl;
              }
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
    addProvider: function(name, provider) {
      providers[name] = provider;
    },
    getValue : function() {
      if (this.options.type === 0) {
        return $input[0].selectize.getValue();
      } else {
        return $editable.mentionsInput('getValue').replace(/<br>/g, '');
      }
    },
    setValue : function(val) {
      if (this.options.type === 0) {
        return $input[0].selectize.setValue(val);
      } else {
        return $editable.mentionsInput('setValue', val);
      }
    },
    getSuggests: function() {
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
  
  function escapeRegExp(str) {
    var specials;
    specials = /[.*+?|()\[\]{}\\$^]/g;
    return str.replace(specials, "\\$&");
  };
  
  return $;
})($);