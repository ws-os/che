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

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.loging.Log;

public class VariableNode extends AbstractDebuggerNode<Variable> {

  private final PromiseProvider promiseProvider;
  private final DebuggerNodeFactory nodeFactory;
  private Variable data;

  @Inject
  public VariableNode(@Assisted Variable data,
                      PromiseProvider promiseProvider,
                      DebuggerNodeFactory nodeFactory) {
    this.promiseProvider = promiseProvider;
    this.nodeFactory = nodeFactory;
    this.data = data;
    Log.info(getClass(), "created node: " + data);
//    updateChildren();
  }

//  private void updateChildren() {
//    children = ;
//  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return AsyncPromiseHelper.createFromAsyncRequest(callback ->
    {
      callback.onSuccess(data.getValue() != null
              ? data.getValue()
              .getVariables()
              .stream()
              .map(nodeFactory::createVariableNode)
              .collect(Collectors.toList())
              : emptyList());
  });
  }

  @Override
  public String getName() {
    return data.getName();
  }

  @Override
  public boolean isLeaf() {
    return data.isPrimitive();
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
//    String content = data.getName() + "=" + data.getValue().getString();
    presentation.setPresentableText(data.getName());
  }

  @Override
  public Variable getData() {
    return data;
  }

  @Override
  public void setData(Variable data) {
    this.data = data;
//    updateChildren();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VariableNode node = (VariableNode) o;
    return Objects.equals(data, node.data);
  }

  @Override
  public int hashCode() {
    return Joiner.on("/").join(data.getVariablePath().getPath()).hashCode();
  }
}
