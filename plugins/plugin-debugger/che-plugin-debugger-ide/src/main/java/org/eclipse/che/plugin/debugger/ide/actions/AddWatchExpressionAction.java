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
package org.eclipse.che.plugin.debugger.ide.actions;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/** @author Alexander Andrienko */
public class AddWatchExpressionAction extends AbstractPerspectiveAction {

  @Inject
  public AddWatchExpressionAction(
      DebuggerLocalizationConstant locale, DebuggerResources resources) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.editWatchExpressionDescription(),
        locale.editWatchExpressionDescription(),
        null,
        resources.addExpressionBtn());
  }

  @Override
  public void updateInPerspective(ActionEvent event) {}

  @Override
  public void actionPerformed(ActionEvent e) {}
}
