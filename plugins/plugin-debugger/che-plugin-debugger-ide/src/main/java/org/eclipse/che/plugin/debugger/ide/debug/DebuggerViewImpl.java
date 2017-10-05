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
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.SpanElement;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.DebuggerNodeFactory;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.HasUniqueKeyProvider;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;

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
  @UiField ScrollPanel variablesPanel2;
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

  private final Tree<MutableVariable> variables;
  private final SimpleList<Breakpoint> breakpoints;
  private final SimpleList<StackFrameDump> frames;
  private final DebuggerResources debuggerResources;
  private final DtoFactory dtoFactory;
  private final org.eclipse.che.ide.ui.smartTree.Tree tree;
  private final DebuggerNodeFactory nodeFactory;

  private TreeNodeElement<MutableVariable> selectedVariable;

  @Inject
  protected DebuggerViewImpl(
      PartStackUIResources partStackUIResources,
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      Resources coreRes,
      VariableTreeNodeRenderer.Resources rendererResources,
      DebuggerViewImplUiBinder uiBinder,
      DtoFactory dtoFactory,
      DebuggerNodeFactory nodeFactory) {
    super(partStackUIResources);

    this.locale = locale;
    this.debuggerResources = resources;
    this.coreRes = coreRes;
    this.dtoFactory = dtoFactory;

    setContentWidget(uiBinder.createAndBindUi(this));

    this.breakpoints = createBreakpointList();
    this.breakpointsPanel.add(breakpoints);

    this.frames = createFramesList();
    this.framesPanel.add(frames);
    this.nodeFactory = nodeFactory;

    //create new tree
    tree = new org.eclipse.che.ide.ui.smartTree.Tree(
            new NodeStorage(item -> ((HasUniqueKeyProvider) item).getKey()),
            new NodeLoader());
    tree.ensureDebugId("debugger-explorer");

    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.addExpandHandler(
        event -> {
          Log.info(getClass(), "expand " + event.getNode().getName());
          Node expandedNode = event.getNode();
          if (expandedNode instanceof VariableNode) {
            delegate.onExpandVariablesTree(((VariableNode) expandedNode));
          }
        });

    this.variables =
        Tree.create(
            rendererResources,
            new VariableNodeDataAdapter(),
            new VariableTreeNodeRenderer(rendererResources));
    this.variables.setTreeEventHandler(
        new Tree.Listener<MutableVariable>() {
          @Override
          public void onNodeAction(@NotNull TreeNodeElement<MutableVariable> node) {}

          @Override
          public void onNodeClosed(@NotNull TreeNodeElement<MutableVariable> node) {
            selectedVariable = node;
          }

          @Override
          public void onNodeContextMenu(
              int mouseX, int mouseY, @NotNull TreeNodeElement<MutableVariable> node) {}

          @Override
          public void onNodeDragStart(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {}

          @Override
          public void onNodeDragDrop(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {}

          @Override
          public void onNodeExpanded(@NotNull final TreeNodeElement<MutableVariable> node) {
            selectedVariable = node;
            delegate.onExpandVariablesTree(node.getData());
          }

          @Override
          public void onNodeSelected(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull SignalEvent event) {
            selectedVariable = node;
          }

          @Override
          public void onRootContextMenu(int mouseX, int mouseY) {}

          @Override
          public void onRootDragDrop(@NotNull MouseEvent event) {}

          @Override
          public void onKeyboard(@NotNull KeyboardEvent event) {}
        });

    this.variablesPanel.add(variables);
    this.variablesPanel2.add(tree);
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
    MutableVariable root = this.variables.getModel().getRoot();
    if (root == null) {
      root = new MutableVariableImpl();
      this.variables.getModel().setRoot(root);
    }
    root.setValue(new SimpleValueImpl(variables, null));
    this.variables.renderTree(0);

    //render new tree
    tree.getNodeStorage().clear();
    for (Variable variable : variables) {
      VariableNode node = nodeFactory.createVariableNode(variable);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void setVariableValue(Variable variable, SimpleValue value) {
    MutableVariableImpl nodeData = new MutableVariableImpl(variable);

    TreeNodeElement<MutableVariable> node = variables.getNode(nodeData);
    if (node != null) {
      node.getData().setValue(value);

      SpanElement element = variables.getModel().getNodeRenderer().renderNodeContents(nodeData);
      node.getNodeLabel().setInnerHTML(element.getInnerHTML());

      for (Variable nestedVariable : value.getVariables()) {
        setVariableValue(nestedVariable, nestedVariable.getValue());
      }

      if (node.isOpen()) {
        if (value.getVariables().isEmpty()) {
          variables.closeNode(node);
        } else {
          variables.expandNode(node);
        }
      }
    }
  }

  @Override
  public void updateVariableNodeValue(VariableNode variable) { // todo generic it's a real here ?
    //    Node nodeToUpdate = nodeFactory.createVariableNode(variable);
    //    Node nodeToUpdate = tree.getNodeStorage().findNode(node); //todo find by key

    if (variable != null) {
      tree.getNodeStorage().update(variable);
      tree.refresh(variable);

      List<Node> nodes = new ArrayList<>();
      List<? extends Variable> varChildren = variable.getData().getValue().getVariables();
      for (int i = 0; i < varChildren.size(); i++) {
        Node childNode = nodeFactory.createVariableNode(varChildren.get(i));
        nodes.add(childNode);
        tree.getNodeStorage().insert(variable, i, childNode);
      }

      //todo refresh only if we really have children...
      //        for (Variable nestedVariable : variable.getData().getValue().getVariables()) {
      //          Node nodeToUpdate = nodeFactory.createVariableNode(nestedVariable);
      //          updateVariableNodeValue(nodeToUpdate);
      //        }
    }
  }

  @Override
  public void updateVariable(Variable variable) { // todo generic it's a real here ?
    Node nodeToUpdate = nodeFactory.createVariableNode(variable);
    //    Node nodeToUpdate = tree.getNodeStorage().findNode(node); //todo find by key

    if (nodeToUpdate != null) {
      tree.getNodeStorage().update(nodeToUpdate);
      tree.refresh(nodeToUpdate);

      //todo refresh only if we really have children...
      for (Variable nestedVariable : variable.getValue().getVariables()) {
        updateVariable(nestedVariable);
      }
    }
  }

  //todo maybe we should rename to add expression and use string like param?!!
  @Override
  public Variable createWatchExpression(String expression, String result) {
    //todo rework this code ...
    //    List<MutableVariable> vars = variables.getModel().getDataAdapter().getChildren(root);
    //    MutableVariable var = createVariable(expression, result);
    //    vars.add(var);
    //    setVariables(vars);
    //    MutableVariable root = variables.getModel().getRoot();
    //    Log.info(getClass(), root  + " root... ?");
    //    TreeNodeElement<MutableVariable> rootNode = variables.getNode(root);
    //    Log.info(getClass(), rootNode + " ?");
    //    Log.info(getClass(), "root node is not empty" + rootNode.hasChildNodes());

    //    MutableVariable newVariable = createVariable(expression, result);
    //    TreeNodeElement<MutableVariable> node = variables.createNode(newVariable);
    //    rootNode.addChild(adapter, node, variables.getResources().treeCss());

    //    Log.info(getClass(), variables.getModel().getNodeRenderer().updateNodeContents(););
    //    SpanElement element = variables.getModel().getNodeRenderer().renderNodeContents(nodeData);
    return null;
  }

  @Override
  public void updateWatchExpression(Variable oldVariable, String expression, String result) {
    //    if (oldVariable instanceof MutableVariable) {
    //
    //      SimpleValueDto simpleValueDto = dtoFactory.createDto(SimpleValueDto.class);
    //      simpleValueDto.setString();
    //      ((MutableVariable)oldVariable).setName();
    //
    //      setVariableValue((MutableVariable)oldVariable, simpleValueDto);
    //    }

  }

  private MutableVariable createVariable(String expression, String result) {
    SimpleValueDto simpleValueDto = dtoFactory.createDto(SimpleValueDto.class);

    String value = result == null ? "null" : result;
    simpleValueDto.setString(value);

    MutableVariable variable = new MutableVariableImpl();
    variable.setValue(simpleValueDto);
    variable.setName(expression);

    return variable;
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
  public MutableVariable getSelectedDebuggerVariable() {
    if (selectedVariable != null) {
      return selectedVariable.getData();
    }
    return null;
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

  @Override
  public Node getSelected() {
    return tree.getSelectionModel().getSelectedNodes().isEmpty()
        ? null
        : tree.getSelectionModel().getSelectedNodes().get(0);
  }
}
