(function(gj) {
  var MaskLayerControl = function() {}

  MaskLayerControl.prototype.init = function(root) {
      root = (typeof (root) == 'string') ? gj.fn.findId(root) : root;
      var jnodeList = gj(root).find('.AttachImage');
      jnodeList.on('click', function() {
        this.showPicture(this);
      });
      
    };

  MaskLayerControl.prototype.getContainerNode = function() {
      var containerNode = gj("#UIPictutreContainer");
      if (!containerNode.length > 0) {
        containerNode = gj('<div></div>');
        containerNode
            .attr('id', 'UIPictutreContainer')
            .attr('style',
                'position:absolute; top:0px; width:100%; height:100%; text-align:center')
            .attr('title', 'Click to close or press key Esc.').on('click',
                this.hidePicture);
        gj("#UIPortalApplication").append(containerNode)
      }
      return containerNode;
    };

  MaskLayerControl.prototype.showPicture = function(node) {
      if (typeof (node) === "string") {
        var imgSrcNode = new Image();
        imgSrcNode.src = node;
      } else {
        var attachmentContent = gj(node).parents('div:first');
        var imgSrcNode = attachmentContent.find('img:first')[0];
      }
      var src = imgSrcNode.src;
      if (String(src).length > 0) {

        var imgSize = this.getImageSize(imgSrcNode);
        var windowHeight = parseInt(document.documentElement.clientHeight);
        var windowWidth = parseInt(document.documentElement.clientWidth);
        
        var imgWidth = "auto";
        if(windowWidth < parseInt(imgSize.width)) {
          imgWidth = windowWidth + 'px';
        }
        
        var marginTop = 0;
        var imgHeight = "auto";
        if(windowHeight < parseInt(imgSize.height)) {
          imgHeight = windowHeight + 'px';
          imgWidth = "auto";
        } else {
          marginTop = parseInt((windowHeight - parseInt(imgSize.height)) / 2)
        }
        
        var imageNode = "<img src='" + imgSrcNode.src + "' style='height:"
            + imgHeight + ";width:" + imgWidth + ";margin-top:" + marginTop
            + "px;' alt='Click to close or press key Esc.'/>";
        var containerNode = this.getContainerNode();
        containerNode.html(imageNode);
        var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication',
            containerNode[0], 30, 'CENTER');
        gj(containerNode).find('img:first').on('click', this.hidePicture);
        this.scrollHandler();
        gj(document).on('keydown', this.hidePictureByKey);
      }
    };

  MaskLayerControl.prototype.scrollHandler = function() {
      eXo.core.UIMaskLayer.object.style.top = gj('#MaskLayer').offset().top  + "px";
      this.timer = setTimeout(this.scrollHandler, 1);
    };

  MaskLayerControl.prototype.hidePictureByKey = function(e) {
      if(e.which && e.which === 27) {
        eXo.commons.MaskLayerControl.hidePicture();
        gj(document).off('keydown', eXo.commons.MaskLayerControl.hidePictureByKey);
      }
    };

  MaskLayerControl.prototype.hidePicture = function() {
      
      var maskContent = eXo.core.UIMaskLayer.object;
      gj('#UIPictutreContainer').remove();
      gj('#MaskLayer').remove();
      clearTimeout(this.timer);
      delete this.timer;
    };

  MaskLayerControl.prototype.getImageSize = function(img) {
      var imgNode = new Image();
      imgNode.src = img.src;
      return {
        "height" : imgNode.height,
        "width" : imgNode.width
      };
    };
  if(!eXo.commons) eXo.commons={};
  eXo.commons.MaskLayerControl = new MaskLayerControl();
  return {
    MaskLayerControl : eXo.commons.MaskLayerControl
  };
})(gj);
