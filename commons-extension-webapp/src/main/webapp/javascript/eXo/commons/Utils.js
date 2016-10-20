/**
 * Created by The eXo Platform SEA
 * Author : eXoPlatform
 * toannh@exoplatform.com
 * On 15/09/15
 * Utils commons
 *
 */
(function(gj) {

  var Utils = function() {}

  Utils.prototype.checkDevice = function(){
    var body = gj('body:first').removeClass('phoneDisplay').removeClass('phoneSmallDisplay').removeClass('tabletDisplay').removeClass('tabletLDisplay');
    var isMobile = body.find('.visible-phone:first').css('display') !== 'none';
    var isSmallMobile = body.find('.visible-phone-small:first').css('display') !== 'none';
    var isTablet = body.find('.visible-tablet:first').css('display') !== 'none';
    var isTabletL = body.find('.visible-tabletL:first').css('display') !== 'none';
    if (isMobile) {
      body.addClass('phoneDisplay');
    }
    if (isSmallMobile) {
      body.addClass('phoneSmallDisplay');
    }
    if (isTablet) {
      body.addClass('tabletDisplay');
    }
    if (isTabletL) {
      body.addClass('tabletLDisplay');
    }
    return {'isMobile' : isMobile,'isSmallMobile' : isSmallMobile, 'isTablet' : isTablet, 'isTabletL' : isTabletL};
  };
  Utils.prototype.urlify = function(text){
    return text.replace(/((((https?|ftp|file):\/\/)|www\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])/ig, function(url){
      var value = url;
      if(url.indexOf('www.') == 0) {
        url = 'http://' + url;
      }
      return '<a href="' + url + '" target="_blank">' + value + '</a>';
    })
  };
  Utils.prototype.fetchElementByConfig = function(params){
    Utils.activityId = params.activityId || null;
    Utils.targetDiv = params.targetDiv || "";
    Utils.targetElement = params.targetElement || "";
    if (Utils.activityId == null) {
      return;
    }
    Utils.wrapDiv = Utils.targetDiv + Utils.activityId;
    var descriptionValue = gj('div#'+Utils.wrapDiv).find('.'+Utils.targetElement).first('p').text();
    //--- Urlify description
    descriptionValue = this.urlify(descriptionValue);
    //--- Update description output
    gj('div#'+Utils.wrapDiv).find('.'+Utils.targetElement).first('p').html( descriptionValue );
  };
  if(!eXo.commons) eXo.commons={};
  eXo.commons.Utils = new Utils();
  return {
    Utils : eXo.commons.Utils
  };

})(jQuery);