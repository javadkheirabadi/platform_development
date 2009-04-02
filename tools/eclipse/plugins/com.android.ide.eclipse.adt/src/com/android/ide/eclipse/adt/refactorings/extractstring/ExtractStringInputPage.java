/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.eclipse.adt.refactorings.extractstring;


import com.android.ide.eclipse.adt.wizards.newstring.NewStringBaseImpl;
import com.android.ide.eclipse.adt.wizards.newstring.NewStringBaseImpl.INewStringPageCallback;
import com.android.ide.eclipse.adt.wizards.newstring.NewStringBaseImpl.ValidationStatus;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @see ExtractStringRefactoring
 */
class ExtractStringInputPage extends UserInputWizardPage
    implements IWizardPage, INewStringPageCallback {

    private NewStringBaseImpl mImpl;
    
    public ExtractStringInputPage(IProject project) {
        super("ExtractStringInputPage");  //$NON-NLS-1$
        mImpl = new NewStringBaseImpl(project, this);
    }

    /**
     * Create the UI for the refactoring wizard.
     * <p/>
     * Note that at that point the initial conditions have been checked in
     * {@link ExtractStringRefactoring}.
     */
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        content.setLayout(layout);

        mImpl.createControl(content);
        setControl(content);
    }

    /**
     * Creates the top group with the field to replace which string and by what
     * and by which options.
     * 
     * @param content A composite with a 1-column grid layout
     * @return The {@link Text} field for the new String ID name.
     */
    public Text createStringGroup(Composite content) {

        final ExtractStringRefactoring ref = getOurRefactoring();
        
        Group group = new Group(content, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("String Replacement");

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);

        // line: Textfield for string value (based on selection, if any)
        
        Label label = new Label(group, SWT.NONE);
        label.setText("String:");

        String selectedString = ref.getTokenString();
        
        final Text stringValueField = new Text(group, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        stringValueField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        stringValueField.setText(selectedString != null ? selectedString : "");  //$NON-NLS-1$
        
        ref.setNewStringValue(stringValueField.getText());

        stringValueField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (mImpl.validatePage()) {
                    ref.setNewStringValue(stringValueField.getText());
                }
            }
        });

        
        // TODO provide an option to replace all occurences of this string instead of
        // just the one.

        // line : Textfield for new ID
        
        label = new Label(group, SWT.NONE);
        label.setText("Replace by R.string.");

        final Text stringIdField = new Text(group, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        stringIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        stringIdField.setText(guessId(selectedString));

        ref.setNewStringId(stringIdField.getText().trim());

        stringIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (mImpl.validatePage()) {
                    ref.setNewStringId(stringIdField.getText().trim());
                }
            }
        });
        
        return stringIdField;
    }

    /**
     * Utility method to guess a suitable new XML ID based on the selected string.
     */
    private String guessId(String text) {
        if (text == null) {
            return "";  //$NON-NLS-1$
        }

        // make lower case
        text = text.toLowerCase();
        
        // everything not alphanumeric becomes an underscore
        text = text.replaceAll("[^a-zA-Z0-9]+", "_");  //$NON-NLS-1$ //$NON-NLS-2$

        // the id must be a proper Java identifier, so it can't start with a number
        if (text.length() > 0 && !Character.isJavaIdentifierStart(text.charAt(0))) {
            text = "_" + text;  //$NON-NLS-1$
        }
        return text;
    }

    /**
     * Returns the {@link ExtractStringRefactoring} instance used by this wizard page.
     */
    private ExtractStringRefactoring getOurRefactoring() {
        return (ExtractStringRefactoring) getRefactoring();
    }

    public void postValidatePage(ValidationStatus status) {
        ExtractStringRefactoring ref = getOurRefactoring();
        ref.setTargetFile(mImpl.getResFileProjPath());
    }
}
