/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification;

public interface NotificationProperties {

  public static final String NTF_FROM             = "ntf:from";

  public static final String NTF_TYPE             = "ntf:type";

  public static final String NTF_NAME             = "ntf:name";

  public static final String NTF_PARAMS           = "ntf:params";

  public static final String NTF_MESSAGE          = "ntf:message";

  public static final String NTF_SUBJECTS         = "ntf:subjects";

  public static final String NTF_IS_ACTIVE        = "ntf:isActive";

  public static final String NTF_PROVIDER         = "ntf:provider";

  public static final String NTF_TEMPLATES        = "ntf:templates";

  public static final String NTF_SEND_TO_DAILY    = "ntf:sendToDaily";

  public static final String NTF_MESSAGE_HOME     = "ntf:messageHome";

  public static final String NTF_SEND_TO_WEEKLY   = "ntf:sendToWeekly";

  public static final String NTF_NOTIFICATION     = "ntf:notification";

  public static final String NTF_PROVIDER_HOME    = "ntf:providerHome";

  public static final String NTF_PROVIDER_TYPE    = "ntf:providerType";

  public static final String NTF_SEND_TO_MONTHLY  = "ntf:sendToMonthly";

  public static final String MIX_SUB_MESSAGE_HOME = "mix:subMessageHome";

  public static final String NTF_OWNER_PARAMETER  = "ntf:ownerParameter";

  public static final String NT_FILE              = "nt:file";

  public static final String JCR_CONTENT          = "jcr:content";

  public static final String JCR_MIME_TYPE        = "jcr:mimeType";

  public static final String JCR_LAST_MODIFIED    = "jcr:lastModified";

  public static final String JCR_DATA             = "jcr:data";

  public static final String JCR_SCORE            = "jcr:score";

  public static final String REP_EXCERPT          = "rep:excerpt()";

  public static final String REP_EXCERPT_PATTERN  = "rep:excerpt(%s)";

  public static final String NT_RESOURCE          = "nt:resource";

  public static final String ASCENDING            = " ascending";

  public static final String DESCENDING           = " descending";
}
