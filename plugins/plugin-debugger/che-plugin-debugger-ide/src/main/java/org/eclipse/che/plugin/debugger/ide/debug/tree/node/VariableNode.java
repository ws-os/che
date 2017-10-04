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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import java.util.ArrayList;
import java.util.List;

public class VariableNode extends SyntheticNode<Variable> {

    private Variable data;
    private PromiseProvider promiseProvider;

    @Inject
    public VariableNode(@Assisted Variable data, PromiseProvider promiseProvider) {
        super(data, null);
        this.data = data;
        this.children = new ArrayList<>();
        this.promiseProvider = promiseProvider;
    }

    //todo maybe should be here delegate and request to get children?
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return promiseProvider.resolve(children);
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
        String content = data.getName() + "=" + data.getValue().getString();
        presentation.setPresentableText(content);
    }
}
