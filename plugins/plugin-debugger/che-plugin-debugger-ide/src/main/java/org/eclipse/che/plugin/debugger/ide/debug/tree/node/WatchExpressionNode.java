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
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
;
import java.util.List;

public class WatchExpressionNode extends SyntheticNode<SimpleValue> {

    @Inject
    public WatchExpressionNode(SimpleValue data, NodeSettings nodeSettings) {
        super(data, nodeSettings);
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    // todo
    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {

    }
}
