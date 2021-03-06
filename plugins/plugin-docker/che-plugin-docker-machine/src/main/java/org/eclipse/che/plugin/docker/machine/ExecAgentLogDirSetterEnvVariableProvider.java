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
package org.eclipse.che.plugin.docker.machine;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add env variable to docker dev-machine with an expression to set exec agent log directory.
 *
 * @author David Festal
 */
@Singleton
public class ExecAgentLogDirSetterEnvVariableProvider implements Provider<String> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExecAgentLogDirSetterEnvVariableProvider.class);

  public static final String LOGS_DIR_SETTER_VARIABLE = "EXEC_AGENT_LOGS_DIR_SETTER";
  public static final String LOGS_DIR_VARIABLE = "EXEC_AGENT_LOGS_DIR";

  private final ExecAgentLogDirProvider logDirProvider;

  @Inject
  public ExecAgentLogDirSetterEnvVariableProvider(ExecAgentLogDirProvider logDirProvider) {
    this.logDirProvider = logDirProvider;
    LOGGER.info("ExecAgentLogDirProvider = {}", logDirProvider);
  }

  @Override
  public String get() {
    String logDir = logDirProvider.get();
    String setter = "";
    if (logDir != null) {
      setter = "export " + LOGS_DIR_VARIABLE + "=\"" + logDir + "\"";
    }
    return LOGS_DIR_SETTER_VARIABLE + '=' + setter;
  }
}
