(function($) {
  var cometd = $.cometd;
  
	var cCometD = $.extend({
	    eXoSecret: {exoId: null, exoToken: null},
	    eXoResubs: [],
	    eXoPublish: [],
	    eXoRemoteCalls: [],
	    autoResubscribe: true
	}, cometd);
		
	cCometD.configure = function(config) {
	  if (config.exoId) {
	    this.eXoSecret = {
	        exoId: config.exoId,
	        exoToken: config.exoToken
	    }
	  }
	  if (typeof config.autoResubscribe != 'undefined') {
	    this.autoResubscribe = config.autoResubscribe;
	  }
	  cometd.configure.apply(this, arguments);
	};
	
	cCometD.addListener('/meta/handshake', function(message) {
	  if (message.successful) {
	    //start a batch
	    cCometD.batch(cCometD, function() {
	      //resubcribe after successfull handshake
	      $.each(cCometD.eXoResubs, function(idx, elem) {
	        cometd.subscribe.apply(cCometD, elem);
	      });
	      //publish
	      $.each(cCometD.eXoPublish, function(idx, elem) {
          cometd.publish.apply(cCometD, elem);
        });
	      cCometD.eXoPublish = [];
	      //remoteCall
	      $.each(cCometD.eXoRemoteCalls, function(idx, elem) {
          cometd.remoteCall.apply(cCometD, elem);
        });
        cCometD.eXoRemoteCalls = [];
	    });
	  }
	});
	
	cCometD.subscribe = function(channel, scope, callback, subscribeProps, subscribeCallback) {
	  // Normalize arguments
    if ($.isFunction(scope)) {
        subscribeCallback = subscribeProps;
        subscribeProps = callback;
        callback = scope;
        scope = undefined;
    }
    if ($.isFunction(subscribeProps)) {
        subscribeCallback = subscribeProps;
        subscribeProps = undefined;
    }
    
    //Add eXo token
    if (!subscribeProps) {
      subscribeProps = {}
    }
    subscribeProps = $.extend({}, cCometD.eXoSecret, subscribeProps);
    
    if (this.autoResubscribe) {
      this.eXoResubs.push([channel, scope, callback, subscribeProps, subscribeCallback]);
    }
    
    if (this.isDisconnected()) {
      this.handshake(subscribeProps);
    } else if(this.getStatus() !== 'handshaking') {
      return cometd.subscribe.call(this, channel, scope, callback, subscribeProps, subscribeCallback);      
    }
	};
	
	cCometD.publish = function(channel, content, publishProps, publishCallback) {	  	  
	  if (this.isDisconnected()) {
	    if (!publishProps || _isFunction(publishProps))
	    {
	      publishProps = {};
	    }

	    //Add eXo token
	    publishProps = $.extend({}, cCometD.eXoSecret, publishProps);
      this.handshake(publishProps);
    } else if(this.getStatus() === 'handshaking') {
      this.eXoPublish.push(arguments);
    } else {
      return cometd.publish.call(this, channel, content, publishProps, publishCallback);      
    }	  
	};
	
	cCometD.remoteCall = function(target, content, timeout, callback) {    
    if (this.isDisconnected()) {
      if (!content || _isFunction(content))
      {
        content = {};
      }
      
      //Add eXo token
      content = $.extend({}, cCometD.eXoSecret, content);
      this.handshake(content);
    } else if(this.getStatus() === 'handshaking') {
      this.eXoRemoteCalls.push(arguments);
    } else {
      return cometd.remoteCall.call(this, target, content, timeout, callback);      
    }
	};
	
	cCometD.clearResubscriptions = function() {
	  this.eXoResubs = [];
	};
	
	return cCometD;
})($);