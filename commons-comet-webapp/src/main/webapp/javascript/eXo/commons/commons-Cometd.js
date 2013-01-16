(function(gtnbase){
	var _module = {};
	
	
//-------------JSON ------------------//
/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
    json2.js
    2008-01-17

    Public Domain

    No warranty expressed or implied. Use at your own risk.

    See http://www.JSON.org/js.html

    This file creates a global JSON object containing two methods:

        JSON.stringify(value, whitelist)
            value       any JavaScript value, usually an object or array.

            whitelist   an optional array prameter that determines how object
                        values are stringified.

            This method produces a JSON text from a JavaScript value.
            There are three possible ways to stringify an object, depending
            on the optional whitelist parameter.

            If an object has a toJSON method, then the toJSON() method will be
            called. The value returned from the toJSON method will be
            stringified.

            Otherwise, if the optional whitelist parameter is an array, then
            the elements of the array will be used to select members of the
            object for stringification.

            Otherwise, if there is no whitelist parameter, then all of the
            members of the object will be stringified.

            Values that do not have JSON representaions, such as undefined or
            functions, will not be serialized. Such values in objects will be
            dropped; in arrays will be replaced with null.
            JSON.stringify(undefined) returns undefined. Dates will be
            stringified as quoted ISO dates.

            Example:

            var text = JSON.stringify(['e', {pluribus: 'unum'}]);
            // text is '["e",{"pluribus":"unum"}]'

        JSON.parse(text, filter)
            This method parses a JSON text to produce an object or
            array. It can throw a SyntaxError exception.

            The optional filter parameter is a function that can filter and
            transform the results. It receives each of the keys and values, and
            its return value is used instead of the original value. If it
            returns what it received, then structure is not modified. If it
            returns undefined then the member is deleted.

            Example:

            // Parse the text. If a key contains the string 'date' then
            // convert the value to a date.

            myData = JSON.parse(text, function (key, value) {
                return key.indexOf('date') >= 0 ? new Date(value) : value;
            });

    This is a reference implementation. You are free to copy, modify, or
    redistribute.

    Use your own copy. It is extremely unwise to load third party
    code into your pages.
*/

/*jslint evil: true */

/*global JSON */

/*members "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
    charCodeAt, floor, getUTCDate, getUTCFullYear, getUTCHours,
    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join, length,
    parse, propertyIsEnumerable, prototype, push, replace, stringify, test,
    toJSON, toString
*/

//if (!JSON) {

    JSON = function () {

        function f(n) {    // Format integers to have at least two digits.
            return n < 10 ? '0' + n : n;
        }

        Date.prototype.toJSON = function () {

// Eventually, this method will be based on the date.toISOString method.

            return this.getUTCFullYear()   + '-' +
                 f(this.getUTCMonth() + 1) + '-' +
                 f(this.getUTCDate())      + 'T' +
                 f(this.getUTCHours())     + ':' +
                 f(this.getUTCMinutes())   + ':' +
                 f(this.getUTCSeconds())   + 'Z';
        };


        var m = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        };

        function stringify(value, whitelist) {
            var a,          // The array holding the partial texts.
                i,          // The loop counter.
                k,          // The member key.
                l,          // Length.
                r = /["\\\x00-\x1f\x7f-\x9f]/g,
                v;          // The member value.

            switch (typeof value) {
            case 'string':

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe sequences.

                return r.test(value) ?
                    '"' + value.replace(r, function (a) {
                        var c = m[a];
                        if (c) {
                            return c;
                        }
                        c = a.charCodeAt();
                        return '\\u00' + Math.floor(c / 16).toString(16) +
                                                   (c % 16).toString(16);
                    }) + '"' :
                    '"' + value + '"';

            case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':
                return String(value);

            case 'object':

// Due to a specification blunder in ECMAScript,
// typeof null is 'object', so watch out for that case.

                if (!value) {
                    return 'null';
                }

// If the object has a toJSON method, call it, and stringify the result.

                if (typeof value.toJSON === 'function') {
                    return stringify(value.toJSON());
                }
                a = [];
                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length'))) {

// The object is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                    l = value.length;
                    for (i = 0; i < l; i += 1) {
                        a.push(stringify(value[i], whitelist) || 'null');
                    }

// Join all of the elements together and wrap them in brackets.

                    return '[' + a.join(',') + ']';
                }
                if (whitelist) {

// If a whitelist (array of keys) is provided, use it to select the components
// of the object.

                    l = whitelist.length;
                    for (i = 0; i < l; i += 1) {
                        k = whitelist[i];
                        if (typeof k === 'string') {
                            v = stringify(value[k], whitelist);
                            if (v) {
                                a.push(stringify(k) + ':' + v);
                            }
                        }
                    }
                } else {

// Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (typeof k === 'string') {
                            v = stringify(value[k], whitelist);
                            if (v) {
                                a.push(stringify(k) + ':' + v);
                            }
                        }
                    }
                }

// Join all of the member texts together and wrap them in braces.

                return '{' + a.join(',') + '}';
            }
        }

        return {
            stringify: stringify,
            parse: function (text, filter) {
                var j;

                function walk(k, v) {
                    var i, n;
                    if (v && typeof v === 'object') {
                        for (i in v) {
                            if (Object.prototype.hasOwnProperty.apply(v, [i])) {
                                n = walk(i, v[i]);
                                if (n !== undefined) {
                                    v[i] = n;
                                }
                            }
                        }
                    }
                    return filter(k, v);
                }


// Parsing happens in three stages. In the first stage, we run the text against
// regular expressions that look for non-JSON patterns. We are especially
// concerned with '()' and 'new' because they can cause invocation, and '='
// because it can cause mutation. But just to be safe, we want to reject all
// unexpected forms.

// We split the first stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace all backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

                if (/^[\],:{}\s]*$/.test(text.replace(/\\./g, '@').
replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the second stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                    j = eval('(' + text + ')');

// In the optional third stage, we recursively walk the new structure, passing
// each name/value pair to a filter function for possible transformation.

                    return typeof filter === 'function' ? walk('', j) : j;
                }

// If the text is not JSON parseable, then a SyntaxError is thrown.

                throw new SyntaxError('parseJSON');
            }
        };
    }();

//----------------- Cometd --------------//
	/*
	The package :

	Cometd

	strongly inspired from the Dojo cometd implementation

	TODO: The transport is doing the handshake, but the server recomande a transport module
	at the handshake, so the transport should not do it, and be set after.

	*/

	/**
	 *  @author Uoc Nguyen (uoc.nguyen@exoplatform.com)
	 *  @description Re-written.
	 *
	 */
	if(!eXo.commons) eXo.commons = {};
	function Cometd() {
		this._connected = false;
		this._polling = false;
	  this._connecting = false;

		this.currentTransport=null;
		this.url = '/cometd/cometd';
		this.id = null;
		this.exoId = null;
		this.exoToken = null;

	  //var Browser = gtnbase.Browser;
	  //var JSON = _module.JSON;

	  this.clientId = gtnbase.Browser.getCookie('cometdClientID') || false;
		this.messageId = 0;
		this.batch=0;

		this._subscriptions = [];
		this._messageQ = [];
	  this._connectionReadyCallbacks = [];

		//this._maxInterval=30000;
		this._maxInterval=5*1000;
		this._backoffInterval=1000;
	  this._maxTry = 5;
	  this._tryToOpenTunnelCnt = 0;
	  this._retryInterval = 0;
	  this._multiClientsDetectCnt = 0;
	}

	Cometd.prototype.init = function(forceDisconnect) {
	  this._tryToOpenTunnelCnt = 0;
	  if ((!forceDisconnect &&
	      this._connected) ||
	      this._connecting) {
	    return;
	  }
		if(!this.currentTransport) {
			this.currentTransport = new eXo.portal.LongPollTransport();
			this.currentTransport.init(this);
		}
		
		if(this.clientId)
			this.currentTransport.initTunnel();
		else
			this.currentTransport.initHandshake();
	};

	Cometd.prototype.addOnConnectionReadyCallback = function(handle) {
	  if (handle) {
	    this._connectionReadyCallbacks.push(handle);
	  }
	};

	Cometd.prototype.removeOnConnectionReadtCallback = function(handle) {
	  for (var i=0; i<this._connectionReadyCallbacks.length; i++) {
	    if (this._connectionReadyCallbacks[i] == handle) {
	      this._connectionReadyCallbacks[i] = this._connectionReadyCallbacks[this._connectionReadyCallbacks.length - 1];
	      this._connectionReadyCallbacks.pop();
	      break;
	    }
	  }
	};

	// public API functions called by cometd or by the transport classes
	Cometd.prototype.deliver = function(messages){
		messages.each(this._deliver, this);
		return messages;
	}

	Cometd.prototype.isConnected = function(){
		return this._connected;
	}

	Cometd.prototype._deliver = function(message){
	  //console.warn('Polling: ' + this._polling + ' - connected: ' + this._connected);
		// dipatch events along the specified path

		if(!message['channel']){
			if(message['success'] !== true){
				//console.debug('cometd error: no channel for message!', message);
				return;
			}
		}
		this.lastMessage = message;

		if(message.advice){
	    this.adviceBackup = this.advice;
			this.advice = message.advice;
	    this.multiClients = message.advice['multiple-clients'];
	    if (this.multiClients) {
	      this._multiClientsDetectCnt ++;
	      //console.warn('Multiple clients detected and notify from server');
	      if (this._multiClientsDetectCnt == 1) {
	        //throw (new Error('You has multiple tab/window using Cometd!\nPlease keep only once.'));
	    	if(window.console && window.console.error)
	    		window.console.error('You has multiple tab/window using Cometd!\nPlease keep only once.');
	      }
	    } else {
	      this._multiClientsDetectCnt = 0;
	      this.resetRetryInterval();
	    }
		}

		// check to see if we got a /meta channel message that we care about
		if(	(message['channel']) &&
			(message.channel.length > 5)&&
			(message.channel.substr(0, 5) == '/meta')){
			// check for various meta topic actions that we need to respond to
			switch(message.channel){
				case '/meta/connect':
					if(message.successful && !this._connected){
						this._connected = true;
						this.endBatch();
					}                                     
					break;
				case '/meta/subscribe':
					if(!message.successful){
						throw (new Error('todo manage error subscription'));
						return;
					}
					break;
				case '/meta/unsubscribe':
					if(!message.successful){
						throw (new Error('todo manage error unsubscription'));
						return;
					}
					break;
			}
		}

		if(message.data){
			// dispatch the message to any locally subscribed listeners
			var tname = message.channel;
			var def = this._subscriptions[tname];
			if (def)
				def(message);
		}
	}

	Cometd.prototype._sendMessage = function(/* object */ message){
		if(this.currentTransport && this._connected && this.batch==0){
			return this.currentTransport.sendMessages([message]);
		}
		else{
			this._messageQ.push(message);
		}
	}

	Cometd.prototype.subscribe = function(	/*String */	channel,
					/*function */	callback){ 
						
		if(callback){
			var tname = channel;
			var subs=this._subscriptions[tname];
			
			if(!subs || subs.length==0){
				subs=[];
				var message = {
					channel: '/meta/subscribe',
					subscription: channel,
					exoId: this.exoId,
					exoToken: this.exoToken
				}
				this._sendMessage(message);	
			}
			//TODO manage mutiple callback on one channel
			this._subscriptions[tname] = callback;
		}
	}

	Cometd.prototype.unsubscribe = function(/*string*/ channel){

		var tname = channel;
		if(this._subscriptions[tname]){
			this._subscriptions[tname] = null;
		}

		this._sendMessage({
			channel: '/meta/unsubscribe',
			subscription: channel,
			exoId: this.exoId,
			exoToken: this.exoToken
		});
	}

	Cometd.prototype.startBatch = function(){
		this.batch++;
	}

	Cometd.prototype.increaseRetryInterval = function() {
	  this.advice = this.advice || {};
		if(!this.advice.interval ||
	     (this.advice.interval &&
	      this.advice.interval > this._maxInterval)) {
	    this.resetRetryInterval();
	  } else {
			this._retryInterval += this._backoffInterval;
	    this.advice.interval = this._retryInterval;
		}
	  //console.warn('Increased retry interval to: ' + this._retryInterval);
	}

	Cometd.prototype.resetRetryInterval = function() {
	  //console.warn('Reset retry interval');
		if(this.advice) 
			this.advice.interval = 1000;
	  this._retryInterval = 1000;
	}

	Cometd.prototype.endBatch = function(){
	  this._tryToOpenTunnelCnt = 0;
	  this._connecting = false;
	  // Callback to on connection ready handlers
	  for (var i=0; i<this._connectionReadyCallbacks.length; i++) {
	    var handler = this._connectionReadyCallbacks[i];
	    if (handler) {
	      handler();
	    }
	  }
		if(--this.batch <= 0 && this.currentTransport && this._connected){
			this.batch=0;

			var messages=this._messageQ;
			this._messageQ=[];
			if(messages.length>0){
				this.currentTransport.sendMessages(messages);
			}
		}
	}

	Cometd.prototype.disconnect = function(){
	  this._tryToOpenTunnelCnt = 0;
		this._subscriptions.each(this.unsubscribe, this);
		this._messageQ = [];
		if(this.currentTransport){
			this.currentTransport.disconnect();
		}
		if(!this._polling)
			this._connected=false;
	}

	Cometd.prototype._backoff = function(){
		if(!this.advice || !this.advice.interval){
			this.advice={reconnect:'retry',interval:0};
		}
	  this.increaseRetryInterval();
		/*if(this.advice.reconnect == 'handshake') {
			
		}*/
	}

	function LongPollTransport() {
		var instance = new Object() ;


		instance.init = function(cometd) {
			this._connectionType='long-polling';
			this._cometd=cometd;
		}

		instance.startup = function() {
			var request = new eXo.portal.AjaxRequest('POST', this._cometd.url);
			request.onSuccess = this._cometd.deliver;
			request.process();
		}

		instance.initHandshake = function() {
			var message = {
				channel:	'/meta/handshake',
				id:	this._cometd.messageId++,
				exoId: this._cometd.exoId,
				exoToken: this._cometd.exoToken
			};
		
			var query = 'message=' + JSON.stringify(message);

			var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
			request.onSuccess = function(request){
									this.finishInitHandshake(request.evalResponse());
								}.bind(this);
			request.onError = 	function(err) {
									throw (new Error('request Error, need to manage this error')) ;
								}.bind(this);
							
			request.process();
		
		}

		instance.finishInitHandshake = function(data){
			data = data[0];
			this._cometd.handshakeReturn = data;
		
			// pick a transport
			if(data['advice']){
				this._cometd.advice = data.advice;
			}
	   
		   	if(!data.successful){
				//console.debug('cometd init failed');
				if(this._cometd.advice && this._cometd.advice['reconnect']=='none'){
					return;
				}

				if( this._cometd.advice && this._cometd.advice['interval'] && this._cometd.advice.interval>0 ){
					setTimeout(function(){ _module.init(); }, this._cometd._retryInterval);
				}else{
					this._cometd.init(this.url,this._props);
				}

				return;
			}
			if(data.version < this.minimumVersion){
				//console.debug('cometd protocol version mismatch. We wanted', this.minimumVersion, 'but got', data.version);
				return;
			}

			this._cometd.clientId = data.clientId;
			gtnbase.Browser.setCookie('cometdClientID', this._cometd.clientId, 1);

			this.initTunnel();
		
		}

		instance.initTunnel = function() {
			var message = {
				channel:	'/meta/connect',
				clientId:	this._cometd.clientId,
				connectionType: this._connectionType,
				id:	this._cometd.messageId++
			};
			this.openTunnelWith({message: JSON.stringify(message)});
		}

		instance.openTunnelWith = function(content, url){
			this._cometd._polling = true;
			// just a hack, need to be changed, we should serialize the full object
			var query = 'message=' + content.message;
		
			var request = new eXo.portal.AjaxRequest('POST', (url||this._cometd.url), query);
			//timeout set to 3 min because of longpoll
			request.timeout = 180000;
			request.onSuccess = function(request){
									this._cometd._polling = false;
									if (request.status >=200 && request.status < 300) {
										this._cometd.deliver(request.evalResponse());
										//this._cometd.resetRetryInterval();
									}
									else
										this._cometd._backoff();
									this.tunnelReq = null;
									this.tunnelCollapse();
								}.bind(this);
			request.onError = 	function(err) {
									this.tunnelReq = null;
									this._cometd._polling = false;
									//console.debug('tunnel opening failed:', err);
	                this._cometd._tryToOpenTunnelCnt++;
									this.tunnelCollapse();
									throw (new Error('tunnel opening failed')) ;
								}.bind(this);
							
			request.process();
		}

		instance.tunnelCollapse = function(){
	    if (this._cometd._tryToOpenTunnelCnt > this._cometd._maxTry) {
	      return;
	    }
			if(!this._cometd._polling){
				// try to restart the tunnel
				this._cometd._polling = false;

				// TODO handle transport specific advice

				if(this._cometd['advice']){
					if(this._cometd.advice['reconnect']=='none'){
						return;
					}

					if(	(this._cometd.advice['interval'])&&
						(this._cometd.advice.interval>0) ){
						var transport = this;
						setTimeout(function(){ transport._connect(); },
							this._cometd._retryInterval);
							this._cometd.increaseRetryInterval();
					}else{
						this._connect();
						this._cometd.increaseRetryInterval();
					}
				}else{
					this._connect();
					this._cometd.increaseRetryInterval();
				}
			}
		}

		instance._connect = function(){
			if(	(this._cometd['advice'])&&
				(this._cometd.advice['reconnect']=='handshake')
			){
				this._cometd.clientId = null;
				this._cometd.init(this._cometd.url,this._cometd._props);
			}else if(this._cometd._connected){
				this.openTunnelWith({
					message: JSON.stringify([
						{
							channel:	'/meta/connect',
							connectionType: this._connectionType,
							clientId:	this._cometd.clientId,
							timestamp:	this.lastTimestamp,
							id:		''+this._cometd.messageId++
						}
					])
				});
			}
		}

		instance.sendMessages = function(messages){
				for(var i=0; i<messages.length; i++){
					messages[i].clientId = this._cometd.clientId;
					messages[i].id = ''+this._cometd.messageId++;
				}

				var query = 'message=' + JSON.stringify(messages);

				var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
				request.onSuccess = function(request){
										this._cometd.deliver(request.evalResponse());
									}.bind(this);
				request.onError = 	function(err) {
										throw (new Error('error sending the message')) ;
									}.bind(this);

				request.process();
		}

		instance.disconnect = function(){
			var query = 'message=' + JSON.stringify([
				{
					channel:	'/meta/disconnect',
					clientId:	this._cometd.clientId,
					id:		''+this._cometd.messageId++
				}
			]);
			var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
			request.process();	

		}
		return instance;	
	}

	Array.prototype.each = function (iterator, context) {
		iterator = iterator.bind(context);
		   for (var i = 0; i < this.length; i++) {
		iterator(this[i]) ;
		}
	};

//COMMONS-212
if (!Function.prototype.bind) {
  Function.prototype.bind = function (oThis) {
    if (typeof this !== "function") {
      // closest thing possible to the ECMAScript 5 internal IsCallable function
      throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
    }
 
    var aArgs = Array.prototype.slice.call(arguments, 1), 
        fToBind = this, 
        fNOP = function () {},
        fBound = function () {
          return fToBind.apply(this instanceof fNOP && oThis
                                 ? this
                                 : oThis,
                               aArgs.concat(Array.prototype.slice.call(arguments)));
        };
 
    fNOP.prototype = this.prototype;
    fBound.prototype = new fNOP();
 
    return fBound;
  };
}



	//eXo.commons.Cometd = new Cometd();
	eXo.portal.LongPollTransport = LongPollTransport.prototype.constructor;

	_module = new Cometd();
	
	return _module;
})(gtnbase)
