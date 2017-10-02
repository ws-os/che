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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.watch.expression.add;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;

/**
 * Presenter to apply expression in the debugger watch list.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class AddWatchExpressionPresenter implements TextAreaDialogView.ActionDelegate {

    private final TextAreaDialogView view;
    private final DebuggerManager debuggerManager;
    private final DebuggerPresenter debuggerPresenter;
    private final DtoFactory dtoFactory;

    @Inject
    public AddWatchExpressionPresenter(DebuggerDialogFactory dialogFactory,
                                       DebuggerLocalizationConstant constant,
                                       DebuggerManager debuggerManager,
                                       DebuggerPresenter debuggerPresenter,
                                       DtoFactory dtoFactory) {
        this.view = dialogFactory.createTextAreaDialogView(constant.addExpressionTextAreaDialogView(),
                                                           constant.addExpressionViewAddButtonTitle(),
                                                           constant.addExpressionViewCancelButtonTitle(),
                                                           "debugger-add-expression"
                                                           );
        view.setValueTitle(constant.addExpressionViewExpressionFieldTitle());
        this.view.setDelegate(this);
        this.debuggerManager = debuggerManager;
        this.debuggerPresenter = debuggerPresenter;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void showDialog() {
        view.focusInValueField();
        view.selectAllText();
        view.setEnableChangeButton(false);
        view.show();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onAgreeClicked() {
        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            final long threadId = debuggerPresenter.getSelectedThreadId();
            final int frameIndex = debuggerPresenter.getSelectedFrameIndex();
            debugger
                    .evaluate(view.getValue(), threadId, frameIndex)
                    .then(
                            result -> {
                                Log.info(getClass(), result);
                            })
                    .then(result   -> {
                        SimpleValueDto simpleValueDto = dtoFactory.createDto(SimpleValueDto.class);
                        simpleValueDto.setString(result);
                        MutableVariable variable = new MutableVariableImpl();
                        variable.setValue(simpleValueDto);

                        debuggerPresenter.onAddWatchExpressionVariable(variable);
                    })
                    .catchError(
                            error -> {
                                Log.info(getClass(), error);
                            });
        } else {
            //todo
            //Do we add empty variable and throw event to update it?
        }
        view.close();
    }

    @Override
    public void onValueChanged() {
        final String value = view.getValue();
        boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
        view.setEnableChangeButton(isExpressionFieldNotEmpty);
    }
}
