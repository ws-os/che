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
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.ADD_WATCH_EXPRESSION;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.CHANGE_DEBUG_TREE_NODE;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.REMOVE_WATCH_EXPRESSION;

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

/** @author Oleksander Andriienko */
public class DebuggerWatchExpressionTest {
  private static final String PROJECT =
      generate(DebuggerWatchExpressionTest.class.getSimpleName(), 2);
  private static final String PROJECT_PATH = "/projects/debug-spring-project";
  private static final String PATH_TO_CLASS =
      "/src/main/java/org/eclipse/qa/examples/AppController.java";

  private static final String START_DEBUG = "startDebug";
  private static final String LAUNCH_APP =
      "mvn clean install -f /projects/"
          + PROJECT
          + " && cp /projects/"
          + PROJECT
          + "/target/qa-spring-sample-1.0-SNAPSHOT.war "
          + "/home/user/tomcat8/webapps/ROOT.war "
          + "&& /home/user/tomcat8/bin/catalina.sh jpda run";

  private static final String NUM_GUESS_BY_USER = "numGuessByUser";
  private static final String RESULT_VARIABLE = "result";
  private static final String ARIPHMETIC_COMPLEX_EXPRESSION = "1000.0 + (60.0)/userName.length()";

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
    editor.setCursorToLine(34);
    editor.setBreakpoint(34);

    cmdPalette.openCommandPalette();
    cmdPalette.startCommandByDoubleClick(START_DEBUG);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);

    menu.runCommand(RUN_MENU, EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);
    menu.runCommand(RUN_MENU, DEBUG, DEBUG + "/" + PROJECT);

    String appUrl = "http://" + wsClient.getServerAddressByPort(ws.getId(), 8080) + "/spring/guess";

    debuggerUtils.gotoDebugAppAndSendRequest(appUrl, "11");
    debugPanel.openDebugPanel();
    debugPanel.waitDebugHighlightedText("String result = \"\";");
  }

  @Test(priority = 1)
  public void addWatchExpression() {
    editor.waitActiveBreakpoint(34);

    debugPanel.clickOnButton(ADD_WATCH_EXPRESSION);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog(NUM_GUESS_BY_USER);
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel(NUM_GUESS_BY_USER + "=" + "\"11\"");
  }

  @Test(priority = 2)
  public void editWatchExpression() {
    debugPanel.clickOnButton(CHANGE_DEBUG_TREE_NODE);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog(RESULT_VARIABLE);
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel(RESULT_VARIABLE + " = \"Sorry, you failed. Try again later!\";");
  }

  @Test(priority = 3)
  public void removeWatchExpression() {
    debugPanel.clickOnButton(REMOVE_WATCH_EXPRESSION);

    debugPanel.waitTextIsNotPresentInVariablesPanel(RESULT_VARIABLE + " = \"Sorry, you failed. Try again later!\";");
  }

  @Test
  public void watchExpressionShouldBeReEvaluatedOnNextDebugStep() {

  }

  @Test
  public void debuggerSupportComplexArithmeticExpression() {

  }

  @Test
  public void watchExpressionShouldStayAfterStopDebug() {

  }

  //  private static final
}
