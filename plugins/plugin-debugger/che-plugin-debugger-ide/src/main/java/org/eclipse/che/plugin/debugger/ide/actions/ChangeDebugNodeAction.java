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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import org.eclipse.che.api.debug.shared.model.Expression;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.changevalue.ChangeValuePresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.edit.EditWatchExpressionPresenter;

/**
 * Action which allows change value of selected variable with debugger
 *
 * @author Mykola Morhun
 * @author Oleksandr Andriienko
 */
public class ChangeDebugNodeAction extends AbstractPerspectiveAction {

  private final ChangeValuePresenter changeValuePresenter;
  private final DebuggerPresenter debuggerPresenter;
  private final EditWatchExpressionPresenter editWatchExpressionPresenter;
  private Variable selectedVariable;
  private Expression selectedExpression;

  @Inject
  public ChangeDebugNodeAction(
      DebuggerLocalizationConstant locale,
      DebuggerResources resources,
      ChangeValuePresenter changeValuePresenter,
      EditWatchExpressionPresenter editWatchExpressionPresenter,
      DebuggerPresenter debuggerPresenter) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
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
    if (selectedVariable != null) {
      changeValuePresenter.showDialog();
    }
    if (selectedExpression != null) {
      editWatchExpressionPresenter.showDialog();
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    selectedExpression = debuggerPresenter.getSelectedWatchExpression();
    selectedVariable = debuggerPresenter.getSelectedVariable();
    event.getPresentation().setEnabled(selectedExpression != null || selectedVariable != null);
  }
}
