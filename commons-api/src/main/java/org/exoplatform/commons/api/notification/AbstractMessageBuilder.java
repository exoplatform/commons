package org.exoplatform.commons.api.notification;

public abstract class AbstractMessageBuilder<T> {
	/**
	 * The method what uses to build the notification to MessageInfo
	 * It also make report before and after process transform
	 * @param ctx
	 * @return
	 */
	public T build(NotificationContext ctx) {
		return make(ctx);
	}
	
	/**
	 * Makes the MessageInfo
	 * @param ctx
	 * @return
	 */
	public abstract T make(NotificationContext ctx);
}
