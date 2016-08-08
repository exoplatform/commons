/**
 *    - This is a wrapper of selectize.js (http://selectize.github.io/selectize.js/) and jquery.mention (https://github.com/ivirabyan/jquery-mentions)
 *    - The purpose is providing a simple jquery ui widget that help to suggest user input, autocompletion with customer data provider and custom menu 
 * and tag rendering 
 * 
 *    - This plugin is aim to provide a simple, easy to use, and consistent in eXo PLF. 
 *    - It's created by jquery UI widget factory (https://jqueryui.com/widget/). So the api follow jquery UI widget api design pattern, 
 * anyone that know jquery UI widget should be familiar with this plugin
 *    
 *    - Quickstart: this sample will create an input that suggest "username" from what user type
 *                    $(input).suggester({
 *                          type: 'tag',
 *                          source: [{uid: 1, value: 'root'},
 *                                      {uid: 2, value: 'demo}]
 *                     );
 * 
 *    - Available options:
 *      
 *      type:        choose to create a mix or tag component
 *      
 *                      'tag'   - create selectize component
 *                      'mix'  -  create jquery.mention component, this is the default
 *      
 *      showAvatar   - boolean that decide if avatar is shown in the autocomplete menu. Default: true      
 *      
 *      source:     data source of the autocomplete
 *      
 *                      array        -  array of json objects {uid: '1', value: 'test', image: '/path/to/img.png'}  s
 *   
 *      sourceProviders  - Another option besize "source". Provider can be "shared" between suggester widget instance
 *                               - this is an array of provider names ['exo:chat', 'exo:social', 'exo:task']
 *                              
 *                     $(input1).suggester({
 *                        sourceProviders: ['exo:social']
 *                     });
 *                     
 *                     //add provider
 *                     //this need 2 parameters: provider name, and the loader function --> same as the loader function of "source"
 *                     $(input1).suggester('addProvider', 'exo:social', function(term, callback) {
 *                          //query for data 
 *                          var data = findInSocial(term);                                            
 *                          
 *                          //now response
 *                          //data = [{uid: 1, value: 'root', image: '/path/to/img'}, {uid: 2, value: 'demo', image: 'path/to/avatar.png'}]
 *                          callback(data);
 *                     });
 *                     
 *                     //now reuse in other input. Dont need to add provider again
 *                     $(input2).suggester({
 *                        sourceProviders: ['exo:social']
 *                     });
 *                      
 *      renderMenuItem  - provide custom render the autocomplete menu item
 *                                 - this function receive param: item --> the current data json {uid: 1, value: 'root', image: 'path/to/img.png'}
 *                                 - this function must return the html of the menu item
 *      
 *                     //let say we want to display the avatar in difference position
 *                     $(input).suggester({
 *                        source: [{uid: 1, value: 'root', image: 'path/to/img.png'}],
 *                        
 *                         renderMenuItem: function(item) {
 *                            return '<li>' + item.value + '<img src="' + item.image + '"></img></li>'
 *                         }
 *                     });
 *                      
 *      renderItem         - provide custom render for selected item
 *                                - work the same as renderMenuItem function, receive json data item, and return html
 *                                
 *   - Available function:
 *   
 *      getValue              - return current value of the input
 *                      
 *                      $(input).suggester({
 *                          //initialize the suggester        
 *                      });                      
 *                      //get the value after user fill the input
 *                      var val = $(input).suggester('getValue');
 *      
 *      setValue                - set value of the input programatically
 *                      
 *                      $(input).suggester({
 *                          //initialize the suggester        
 *                      });                      
 *                      //set the value after user fill the input
 *                      $(input).suggester('setValue', '@root');
 *                      
 *      getSuggests          - return selected items, this is for mix suggester that when the getValue method return both selected items mixed with other text
 *      
 *      addProvider           - register provider, need 2 parameters: name, and the loader function. Lets take a look at the sample of sourceProviders
 */
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
      if ($.inArray(name, _this.options.sourceProviders)) {
        if (!p[name]) {
          p[name] = provider;
        }
      }
    });

    $.each(p, function(name, provider) {      
      if ($.isFunction(provider)) {
        provider.call(_this, term,  function(results) {
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
      sourceProviders : [],
      showAvatar : true
    },
    _create : function() {
      $input = this.element;
      $input.hide();
      
      if (this.options.providers) {
        $.each(this.options.providers, function(name, provider) {
          providers[name] = provider;
        });
      }
      if (this.options.type.toLowerCase() === type.MIX) {
        $editable = $('<div id="' + $input.attr('id') + '_editable" contenteditable="true"></div>');
        $input.after($editable);
        
        this.options.autocomplete = {
            appendTo: $input.parent()
        }
        
        var source = this.options.source;
        if (!(source && source.length) && this.options.sourceProviders && this.options.sourceProviders.length) {
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
            '<i class="uiIconClose uiIconLightGray" onclick="this.parentNode.parentNode.removeChild(this.parentNode)"> </i></span>';
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
        
        var _this = this;
        if (!(this.options.source && this.options.source.length) && this.options.sourceProviders && this.options.sourceProviders.length) {
          this.options.source = function(term, response) {
            loadFromProvider.call(_this, term, response);
          };
        }
        
        if (this.options.preload) {
          this.options.load = loadFromProvider;          
        }

        if (this.options.source) {
          var source = this.options.source;
          if ($.isArray(source)) {
            this.options.options = this.options.source;            
          } else {
            this.options.options = [];
            this.options.onType = function() {
              $input[0].selectize.load(function(callback) {
                source.call(this, this.currentResults.query, function() {
                  callback.apply(this, arguments);
                  $input[0].selectize.refreshOptions(true);
                });
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
  
  function escapeRegExp(str) {
    var specials;
    specials = /[.*+?|()\[\]{}\\$^]/g;
    return str.replace(specials, "\\$&");
  };
  
  return $;
})($);