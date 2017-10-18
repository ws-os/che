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
package org.eclipse.che.selenium.debugger;

import static java.nio.file.Paths.get;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.ADD_WATCH_EXPRESSION;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.BTN_DISCONNECT;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.CHANGE_DEBUG_TREE_NODE;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.REMOVE_WATCH_EXPRESSION;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.STEP_OUT;

import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Oleksandr Andriienko */
public class DebuggerWatchExpressionTest {
  private static final String PROJECT =
      generate(DebuggerWatchExpressionTest.class.getSimpleName(), 2);
  private static final String PROJECT_PATH = "/projects/debugWatchExpression";
  private static final String PATH_TO_CLASS = "/src/main/java/org/eclipse/qa/ShapeController.java";
  private static final String SHAPE_JSON =
      "{\"type\" : \"triangle\", \"a\": \"3\", \"b\": \"2\", \"c\": \"1\"}";

  private static final String START_DEBUG = "startDebug";
  private static final String LAUNCH_APP =
      "mvn -f /projects/"
          + PROJECT
          + " spring-boot:run -Drun.jvmArguments=\"-Xdebug "
          + "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000\"";

  private DebuggerUtils debuggerUtils = new DebuggerUtils();

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestProjectServiceClient prjServiceClient;
  @Inject private TestCommandServiceClient cmdClient;
  @Inject private TestWorkspaceServiceClient wsClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private DebugPanel debugPanel;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsPalette cmdPalette;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private JavaDebugConfig debugConfig;
  @Inject private NotificationsPopupPanel notifications;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource(PROJECT_PATH);
    prjServiceClient.importProject(ws.getId(), get(resource.toURI()), PROJECT, MAVEN_SPRING);

    cmdClient.createCommand(LAUNCH_APP, START_DEBUG, CUSTOM, ws.getId());

    ide.open(ws);

    projectExplorer.waitItem(PROJECT);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT + PATH_TO_CLASS);

    editor.waitActiveEditor();
    editor.setCursorToLine(91);
    editor.setBreakpoint(91);

    cmdPalette.openCommandPalette();
    cmdPalette.startCommandByDoubleClick(START_DEBUG);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);

    menu.runCommand(RUN_MENU, EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);
    menu.runCommand(RUN_MENU, DEBUG, DEBUG + "/" + PROJECT);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");
    editor.waitActiveBreakpoint(91);

    String appUrl = "http://" + wsClient.getServerAddressByPort(ws.getId(), 8080) + "/shape";

    debuggerUtils.gotoDebugAppAndSendRequest(appUrl, SHAPE_JSON, APPLICATION_JSON, 201);
    debugPanel.openDebugPanel();
    debugPanel.waitDebugHighlightedText("long id = shape.getId();");
  }

  @Test(priority = 1)
  public void addWatchExpression() {
    debugPanel.clickOnButton(ADD_WATCH_EXPRESSION);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("shape.a");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel("shape.a=3.0");
  }

  @Test(priority = 2)
  public void editWatchExpression() {
    debugPanel.waitTextInVariablesPanel("shape.a=3.0");
    debugPanel.selectNodeInDebuggerTree("shape.a=3.0");

    debugPanel.clickOnButton(CHANGE_DEBUG_TREE_NODE);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("shapes.size()");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextIsNotPresentInVariablesPanel("shape.a=3.0");
    debugPanel.waitTextInVariablesPanel("shapes.size()=4");
  }

  @Test(priority = 3)
  public void watchExpressionShouldBeReEvaluatedOnNextDebugStep() {
    debugPanel.waitTextInVariablesPanel("shapes.size()=4");

    editor.setCursorToLine(97);
    editor.setBreakpoint(97);

    debugPanel.clickOnButton(STEP_OUT);

    debugPanel.waitTextInVariablesPanel("shapes.size()=5");
  }

  @Test(priority = 4)
  public void debuggerSupportComplexArithmeticExpression() {
    debugPanel.clickOnButton(ADD_WATCH_EXPRESSION);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("100.0 + 1.0/2.0");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel("100.0 + 1.0/2.0=100.5");
  }

  @Test(priority = 5)
  public void watchExpressionShouldStayAfterStopDebug() {
    debugPanel.clickOnButton(BTN_DISCONNECT);

    debugPanel.waitTextInVariablesPanel("shapes.size()=");
    debugPanel.waitTextInVariablesPanel("100.0 + 1.0/2.0=");
  }

  @Test(priority = 6)
  public void removeWatchExpression() {
    debugPanel.waitTextInVariablesPanel("shapes.size()=");

    debugPanel.selectNodeInDebuggerTree("shapes.size()=");
    debugPanel.clickOnButton(REMOVE_WATCH_EXPRESSION);

    debugPanel.waitTextIsNotPresentInVariablesPanel("\"shapes.size()=\"");
  }
}
