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
  }

  if(!eXo.commons) eXo.commons={};
  eXo.commons.Utils = new Utils();
  return {
    Utils : eXo.commons.Utils
  };

})(jQuery);