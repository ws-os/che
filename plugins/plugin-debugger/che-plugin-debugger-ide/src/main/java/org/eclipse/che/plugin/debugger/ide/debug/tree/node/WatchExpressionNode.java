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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node;

import static java.util.Collections.emptyList;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Expression;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

public class WatchExpressionNode extends AbstractDebuggerNode<Expression> {

  private final PromiseProvider promiseProvider;

  private Expression expression;
  private DebuggerResources debuggerResources;

  @Inject
  public WatchExpressionNode(
      @Assisted Expression expression,
      PromiseProvider promiseProvider,
      DebuggerResources debuggerResources) {
    this.promiseProvider = promiseProvider;
    this.expression = expression;
    this.debuggerResources = debuggerResources;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    // Todo: current server side returns result of evaluation expression like simple string line,
    // so we have not ability to get and render children.
    return promiseProvider.resolve(emptyList());
  }

  @Override
  public String getName() {
    return expression.getExpression();
  }

  @Override
  public boolean isLeaf() {
    // Todo: for current implementation it's an always leaf.
    return true;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    String content = expression.getExpression() + "=" + expression.getResult();
    presentation.setPresentableText(content);
    presentation.setPresentableIcon(debuggerResources.watchExpressionIcon());
  }

  @Override
  public Expression getData() {
    return expression;
  }

  @Override
  public void setData(Expression expression) {
    this.expression = expression;
  }
}
