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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator;

import java.util.Comparator;

import com.google.common.base.Joiner;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;

/** @author Olexander Andriienko */
public class VariableNodeComparator implements Comparator<Node> {
  @Override
  public int compare(Node o1, Node o2) {
    if (o1 instanceof VariableNode && o2 instanceof VariableNode) {
      return getPath(((VariableNode) o1)
              .getData()).compareTo(getPath(((VariableNode) o2)
              .getData()));
//      return ((VariableNode) o1)
//          .getData()
//          .getName()
//          .compareTo(((VariableNode) o2).getData().getName());
    }
    return 0;
  }

  private String getPath(Variable variable) {
    return Joiner.on("/").join(variable.getVariablePath().getPath());
  }
}
