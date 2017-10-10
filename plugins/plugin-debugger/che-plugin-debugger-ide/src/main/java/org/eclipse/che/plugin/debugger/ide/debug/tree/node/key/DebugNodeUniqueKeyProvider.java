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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node.key;

import static java.lang.String.valueOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.eclipse.che.api.debug.shared.model.Expression;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

@Singleton
public class DebugNodeUniqueKeyProvider implements UniqueKeyProvider<Node> {

  @Inject
  public DebugNodeUniqueKeyProvider() {}

  @Override
  public String getKey(Node item) {
    if (item instanceof VariableNode) {
      Variable variable = ((VariableNode) item).getData();
      return evaluateKey(variable);
    }
    if (item instanceof WatchExpressionNode) {
      Expression expression = ((WatchExpressionNode) item).getData();
      return evaluateKey(expression);
    }
    return evaluateKey(item);
  }

  public String evaluateKey(Variable variable) {
    int hash = Objects.hashCode(variable.getVariablePath());
    return valueOf(hash);
  }

  public String evaluateKey(Expression expression) {
    int hash = expression.hashCode();
    return valueOf(hash);
  }

  public <T> String evaluateKey(T item) {
    int hash = Objects.hashCode(item);
    return valueOf(hash);
  }
}
