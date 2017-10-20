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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.WatchExpression;

/**
 * Implementation {@link WatchExpression}
 *
 * @author Oleksandr Andriienko
 */
public class WatchExpressionImpl implements WatchExpression {

  private String expression;
  private String result;

  public WatchExpressionImpl(String expression, String result) {
    this.expression = expression;
    this.result = result;
  }

  @Override
  public String getExpression() {
    return expression;
  }

  @Override
  public void setExpression(String expression) {
    this.expression = expression;
  }

  @Override
  public String getResult() {
    return result;
  }

  @Override
  public void setResult(String result) {
    this.result = result;
  }
}