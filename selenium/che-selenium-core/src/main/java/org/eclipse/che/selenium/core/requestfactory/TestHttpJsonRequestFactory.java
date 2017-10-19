/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.requestfactory;

import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dmytro Nochevnov
 */
public abstract class TestHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {

  private static final Logger LOG = LoggerFactory.getLogger(TestHttpJsonRequestFactory.class);

  @Override
  public HttpJsonRequest fromUrl(@NotNull String url) {
    final String authToken = getAuthToken();
    LOG.info(
        "Request master with user token -----------> " + url + "   token -------> " + authToken);
    return super.fromUrl(url).setAuthorizationHeader(authToken);
  }

  @Override
  public HttpJsonRequest fromLink(@NotNull Link link) {
    final String authToken = getAuthToken();
    LOG.info(
        "Request master with user token -----------> " + link + "   token -------> " + authToken);
    return super.fromLink(link).setAuthorizationHeader(authToken);
  }

  protected abstract String getAuthToken();
}
