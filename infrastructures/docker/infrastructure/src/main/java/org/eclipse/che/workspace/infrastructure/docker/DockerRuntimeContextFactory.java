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
package org.eclipse.che.workspace.infrastructure.docker;

import java.util.List;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/** Helps to create {@link DockerRuntimeContext} instances. */
public interface DockerRuntimeContextFactory {
  DockerRuntimeContext create(
      DockerRuntimeInfrastructure infra,
      RuntimeIdentity identity,
      InternalEnvironment environment,
      DockerEnvironment dockerEnv,
      List<String> containersOrder)
      throws InfrastructureException, ValidationException;
}