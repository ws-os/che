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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.add;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Expression;
import org.eclipse.che.api.debug.shared.model.impl.ExpressionImpl;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

/**
 * Presenter to apply expression in the debugger watch list.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class AddWatchExpressionPresenter implements TextAreaDialogView.ActionDelegate {

  private final TextAreaDialogView view;
  private final DebuggerPresenter debuggerPresenter;
  private final DebuggerManager debuggerManager;

  @Inject
  public AddWatchExpressionPresenter(
          DebuggerDialogFactory dialogFactory,
          DebuggerLocalizationConstant constant,
          DebuggerPresenter debuggerPresenter,
          DebuggerManager debuggerManager) {
    this.view =
        dialogFactory.createTextAreaDialogView(
            constant.addExpressionTextAreaDialogView(),
            constant.addExpressionViewAddButtonTitle(),
            constant.addExpressionViewCancelButtonTitle(),
            "debugger-add-expression");
    this.view.setDelegate(this);
    this.debuggerPresenter = debuggerPresenter;
    this.debuggerManager = debuggerManager;
  }

  @Override
  public void showDialog() {
    view.focusInValueField();
    view.selectAllText();
    view.setEnableChangeButton(false);
    view.show();
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onAgreeClicked() {
    Expression expression = new ExpressionImpl(view.getValue(), "");

    WatchExpressionNode createdNode = debuggerPresenter.addWatchExpressionNode(expression);

    //todo what about busy node with in progress calculation?!! Maybe progressor..
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      debuggerPresenter.calculateWatchExpression(
              createdNode,
              debuggerPresenter.getSelectedThreadId(),
              debuggerPresenter.getSelectedFrameIndex());
    }

    view.close();
  }

  @Override
  public void onValueChanged() {
    final String value = view.getValue();
    boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
    view.setEnableChangeButton(isExpressionFieldNotEmpty);
  }
}
