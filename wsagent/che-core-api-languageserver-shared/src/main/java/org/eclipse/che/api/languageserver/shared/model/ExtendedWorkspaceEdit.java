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

package org.eclipse.che.api.languageserver.shared.model;

import java.util.List;

/** */
public class ExtendedWorkspaceEdit {
  private List<ExtendedTextDocumentEdit> documentChanges;

  public ExtendedWorkspaceEdit() {}

  public ExtendedWorkspaceEdit(List<ExtendedTextDocumentEdit> documentChanges) {
    this.documentChanges = documentChanges;
  }

  public List<ExtendedTextDocumentEdit> getDocumentChanges() {
    return documentChanges;
  }

  public void setDocumentChanges(List<ExtendedTextDocumentEdit> documentChanges) {
    this.documentChanges = documentChanges;
  }
}
