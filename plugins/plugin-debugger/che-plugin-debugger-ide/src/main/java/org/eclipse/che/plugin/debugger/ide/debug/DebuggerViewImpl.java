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
package org.eclipse.che.plugin.debugger.ide.debug;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Expression;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.DebuggerNodeFactory;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.HasUniqueKeyProvider;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

/**
 * The class business logic which allow us to change visual representation of debugger panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
@Singleton
public class DebuggerViewImpl extends BaseView<DebuggerView.ActionDelegate>
    implements DebuggerView {

  interface DebuggerViewImplUiBinder extends UiBinder<Widget, DebuggerViewImpl> {}

  @UiField Label vmName;
  @UiField Label executionPoint;
  @UiField SimplePanel toolbarPanel;
  @UiField ScrollPanel variablesPanel;
  @UiField ScrollPanel breakpointsPanel;
  @UiField SimplePanel watchExpressionPanel;

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField(provided = true)
  Resources coreRes;

  @UiField(provided = true)
  SplitLayoutPanel splitPanel = new SplitLayoutPanel(3);

  @UiField ListBox threads;
  @UiField ScrollPanel framesPanel;

  private final SimpleList<Breakpoint> breakpoints;
  private final SimpleList<StackFrameDump> frames;
  private final DebuggerResources debuggerResources;
  private final Tree tree;
  private final DebuggerNodeFactory nodeFactory;

  @Inject
  protected DebuggerViewImpl(
      PartStackUIResources partStackUIResources,
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      Resources coreRes,
      DebuggerViewImplUiBinder uiBinder,
      DebuggerNodeFactory nodeFactory) {
    super(partStackUIResources);

    this.locale = locale;
    this.debuggerResources = resources;
    this.coreRes = coreRes;

    setContentWidget(uiBinder.createAndBindUi(this));

    this.breakpoints = createBreakpointList();
    this.breakpointsPanel.add(breakpoints);

    this.frames = createFramesList();
    this.framesPanel.add(frames);
    this.nodeFactory = nodeFactory;

    tree =
        new org.eclipse.che.ide.ui.smartTree.Tree(
            new NodeStorage(item -> ((HasUniqueKeyProvider) item).getKey()), new NodeLoader());
    tree.ensureDebugId("debugger-explorer");

    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.addExpandHandler(
        event -> {
          Node expandedNode = event.getNode();
          if (expandedNode instanceof VariableNode) {
            delegate.onExpandVariablesTree(((VariableNode) expandedNode));
          }
        });

    this.variablesPanel.add(tree);
    minimizeButton.ensureDebugId("debugger-minimizeBut");

    watchExpressionPanel.addStyleName(resources.getCss().watchExpressionsPanel());
  }

  @Override
  public void setExecutionPoint(@Nullable Location location) {
    StringBuilder labelText = new StringBuilder();
    if (location != null) {
      labelText
          .append("{")
          .append(Path.valueOf(location.getTarget()).lastSegment())
          .append(":")
          .append(location.getLineNumber())
          .append("} ");
    }
    executionPoint.getElement().setClassName(coreRes.coreCss().defaultFont());
    executionPoint.setText(labelText.toString());
  }

  @Override
  public void setVariables(@NotNull List<? extends Variable> variables) {
    tree.getNodeStorage().clear();
    for (Variable variable : variables) {
      VariableNode node = nodeFactory.createVariableNode(variable);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void updateVariableNodeValue(VariableNode variable) {
    if (variable != null) {
      tree.getNodeStorage().update(variable);
      tree.refresh(variable);

      List<? extends Variable> varChildren = variable.getData().getValue().getVariables();
      for (int i = 0; i < varChildren.size(); i++) {
        Node childNode = nodeFactory.createVariableNode(varChildren.get(i));
        tree.getNodeStorage().insert(variable, i, childNode);
      }
    }
  }

  @Override
  public void updateVariable(Variable variable) {
    Node nodeToUpdate = nodeFactory.createVariableNode(variable);

    if (nodeToUpdate != null) {
      tree.getNodeStorage().update(nodeToUpdate);
      tree.refresh(nodeToUpdate);

      for (Variable nestedVariable : variable.getValue().getVariables()) {
        updateVariable(nestedVariable);
      }
    }
  }

  @Override
  public WatchExpressionNode createWatchExpressionNode(@NotNull Expression expression) {
    WatchExpressionNode exprNode = nodeFactory.createExpressionNode(expression);
    tree.getNodeStorage().add(exprNode);
    return exprNode;
  }

  @Override
  public void updateWatchExpressionNode(WatchExpressionNode expressionNode) {
    tree.getNodeStorage().update(expressionNode);
    tree.refresh(expressionNode);
  }

  @Override
  public void setBreakpoints(@NotNull List<Breakpoint> breakpoints) {
    this.breakpoints.render(breakpoints);
  }

  @Override
  public void setThreadDump(List<? extends ThreadState> threadDump, long threadIdToSelect) {
    threads.clear();

    for (int i = 0; i < threadDump.size(); i++) {
      ThreadState ts = threadDump.get(i);

      StringBuilder title = new StringBuilder();
      title.append("\"");
      title.append(ts.getName());
      title.append("\"@");
      title.append(ts.getId());
      title.append(" in group \"");
      title.append(ts.getGroupName());
      title.append("\": ");
      title.append(ts.getStatus());

      threads.addItem(title.toString(), String.valueOf(ts.getId()));
      if (ts.getId() == threadIdToSelect) {
        threads.setSelectedIndex(i);
      }
    }
  }

  @Override
  public void setFrames(List<? extends StackFrameDump> stackFrameDumps) {
    frames.render(new ArrayList<>(stackFrameDumps));
    if (!stackFrameDumps.isEmpty()) {
      frames.getSelectionModel().setSelectedItem(0);
    }
  }

  @Override
  public void setVMName(@Nullable String name) {
    vmName.setText(name == null ? "" : name);
  }

  @Override
  public Node getSelectedTeeNode() {
    return tree.getSelectionModel().getSelectedNodes().isEmpty()
        ? null
        : tree.getSelectionModel().getSelectedNodes().get(0);
  }

  @Override
  public AcceptsOneWidget getDebuggerToolbarPanel() {
    return toolbarPanel;
  }

  @Override
  public AcceptsOneWidget getDebuggerWatchToolbarPanel() {
    return watchExpressionPanel;
  }

  @Override
  public long getSelectedThreadId() {
    String selectedValue = threads.getSelectedValue();
    return selectedValue == null ? -1 : Integer.parseInt(selectedValue);
  }

  @Override
  public int getSelectedFrameIndex() {
    return frames.getSelectionModel().getSelectedIndex();
  }

  @UiHandler({"threads"})
  void onThreadChanged(ChangeEvent event) {
    delegate.onSelectedThread(Integer.parseInt(threads.getSelectedValue()));
  }

  private SimpleList<Breakpoint> createBreakpointList() {
    TableElement breakPointsElement = Elements.createTableElement();
    breakPointsElement.setAttribute("style", "width: 100%");

    SimpleList.ListEventDelegate<Breakpoint> breakpointListEventDelegate =
        new SimpleList.ListEventDelegate<Breakpoint>() {
          public void onListItemClicked(Element itemElement, Breakpoint itemData) {
            breakpoints.getSelectionModel().setSelectedItem(itemData);
          }

          public void onListItemDoubleClicked(Element listItemBase, Breakpoint itemData) {}
        };

    return SimpleList.create(
        (SimpleList.View) breakPointsElement,
        coreRes.defaultSimpleListCss(),
        new BreakpointItemRender(debuggerResources),
        breakpointListEventDelegate);
  }

  private SimpleList<StackFrameDump> createFramesList() {
    TableElement frameElement = Elements.createTableElement();
    frameElement.setAttribute("style", "width: 100%");

    SimpleList.ListEventDelegate<StackFrameDump> frameListEventDelegate =
        new SimpleList.ListEventDelegate<StackFrameDump>() {
          public void onListItemClicked(Element itemElement, StackFrameDump itemData) {
            frames.getSelectionModel().setSelectedItem(itemData);
            delegate.onSelectedFrame(frames.getSelectionModel().getSelectedIndex());
          }

          public void onListItemDoubleClicked(Element listItemBase, StackFrameDump itemData) {}
        };

    return SimpleList.create(
        (SimpleList.View) frameElement,
        coreRes.defaultSimpleListCss(),
        new FrameItemRender(),
        frameListEventDelegate);
  }
}
