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
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.changevalue.ChangeValuePresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.edit.EditWatchExpressionPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

/**
 * Action which allows change value of selected variable with debugger
 *
 * @author Mykola Morhun
 */
public class ChangeDebugNodeAction extends AbstractPerspectiveAction {

  private final ChangeValuePresenter changeValuePresenter;
  private final DebuggerPresenter debuggerPresenter;
  private final EditWatchExpressionPresenter editWatchExpressionPresenter;

  private Node selectedNode;

  @Inject
  public ChangeDebugNodeAction(
      DebuggerLocalizationConstant locale,
      DebuggerResources resources,
      ChangeValuePresenter changeValuePresenter,
      EditWatchExpressionPresenter editWatchExpressionPresenter,
      DebuggerPresenter debuggerPresenter) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.changeDebugNode(),
        locale.changeDebugNodeDescription(),
        null,
        resources.changeDebugNode());
    this.changeValuePresenter = changeValuePresenter;
    this.debuggerPresenter = debuggerPresenter;
    this.editWatchExpressionPresenter = editWatchExpressionPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (selectedNode instanceof VariableNode) {
      changeValuePresenter.showDialog();
    }
    if (selectedNode instanceof WatchExpressionNode) {
      editWatchExpressionPresenter.showDialog();
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    selectedNode = debuggerPresenter.getSelectedDebugNode();
    event.getPresentation().setEnabled(selectedNode != null);
  }
}
