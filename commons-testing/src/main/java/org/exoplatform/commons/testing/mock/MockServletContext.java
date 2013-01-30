/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.testing.mock;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;


import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MockServletContext implements ServletContext {

  String servletContextName;

  public MockServletContext(String servletContextName) {
    this.servletContextName = servletContextName;
  }

  public Object getAttribute(String name) {
    return null;
  }

  public Enumeration getAttributeNames() {
    return null;
  }

  public ServletContext getContext(String uripath) {
    return null;
  }

  public String getInitParameter(String name) {
    return null;
  }

  public Enumeration getInitParameterNames() {
    return null;
  }

  @Override
  public boolean setInitParameter(String s, String s2) {
    return false;
  }

  public int getMajorVersion() {
    return 0;
  }

  public String getMimeType(String file) {
    return null;
  }

  public int getMinorVersion() {
    return 0;
  }

  @Override
  public int getEffectiveMajorVersion() {
    return 0;
  }

  @Override
  public int getEffectiveMinorVersion() {
    return 0;
  }

  public RequestDispatcher getNamedDispatcher(String name) {
    return null;
  }

  public String getRealPath(String path) {
    return null;
  }

  public RequestDispatcher getRequestDispatcher(String path) {
    return null;
  }

  public URL getResource(String path) throws MalformedURLException {
    return null;
  }

  public InputStream getResourceAsStream(String path) {
    return null;
  }

  public Set getResourcePaths(String path) {
    return null;
  }

  public String getServerInfo() {
    return null;
  }

  public Servlet getServlet(String name) throws ServletException {
    return null;
  }

  public String getServletContextName() {
    return servletContextName;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String s, String s2) {
    return null;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
    return null;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
    return null;
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException {
    return null;
  }

  @Override
  public ServletRegistration getServletRegistration(String s) {
    return null;
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String s, String s2) {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
    return null;
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException {
    return null;
  }

  @Override
  public FilterRegistration getFilterRegistration(String s) {
    return null;
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    return null;
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    return null;
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    return null;
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    return null;
  }

  @Override
  public void addListener(String s) {

  }

  @Override
  public <T extends EventListener> void addListener(T t) {

  }

  @Override
  public void addListener(Class<? extends EventListener> aClass) {

  }

  @Override
  public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException {
    return null;
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    return null;
  }

  public Enumeration getServletNames() {
    return null;
  }

  public Enumeration getServlets() {
    return null;
  }

  public void log(String msg) {
  }

  public void log(Exception exception, String msg) {
  }

  public void log(String message, Throwable throwable) {
  }

  public void removeAttribute(String name) {
  }

  public void setAttribute(String name, Object object) {
  }

  public String getContextPath() {
    return null;
  }

  @Override
  public void declareRoles(String... strings) {
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }

}
