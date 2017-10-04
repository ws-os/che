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
package org.eclipse.che.plugin.debugger.ide.debug.tree.renderer;

import com.google.gwt.dom.client.Element;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;

public class DebuggerTreeRenderer extends DefaultPresentationRenderer<Node> {

    public DebuggerTreeRenderer(TreeStyles treeStyles) {
        super(treeStyles);
    }

    @Override
    public Element render(Node node, String domID, Tree.Joint joint, int depth) {
        final Element element = super.render(node, domID, joint, depth);

        Element root = element.getFirstChildElement();
//        SpanElement label = Elements.createSpanElement(css.variableLabel());
//        String content = node.getName() + "=" + data.getValue().getString();
//        label.setTextContent(content);

//        root.appendChild(label);

        return element;
    }
}
