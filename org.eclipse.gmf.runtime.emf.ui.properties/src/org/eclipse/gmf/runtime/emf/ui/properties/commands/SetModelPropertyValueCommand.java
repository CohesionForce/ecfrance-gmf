/******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.ui.properties.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Command to set a property value in the model in an undo interval. Delegates
 * the actual work of setting the property value to a
 * <code>SetPropertyValueCommand</code>.
 * 
 * @author ldamus
 */
public class SetModelPropertyValueCommand extends AbstractTransactionalCommand {

    /**
     * Flag to indicate that the property value had never before been set, so a
     * reset is appropriate when the command is undone.
     */
    private boolean valueResetOnUndo = false;

    /**
     * The property source that owns the property to be restored to its default
     * value.
     */
    private final IPropertySource propertySource;

    /**
     * The ID of the property whose default value is to be restored.
     */
    private final Object propertyId;

    /**
     * The property value which is used when this command execution is undone.
     */
    private Object undoValue;

    /**
     * The new property value which will be set by this command.
     */
    private final Object propertyValue;

    /**
     * Constructs a new command with the property source and the id of the
     * property to be reset.
     * 
     * @param editingDomain the editing domain in which to make this change
     * @param label
     *            The label for the command. Appears in the Edit menu items.
     * @param affectedObjects
     * 			  The model operation context for the new model command.
     * @param propertySource
     *            The property source that owns the property whose value is to
     *            be reset.
     * @param propertyId
     *            The ID of the property to be reset.
     * @param propertyValue
     * 			  The new property value which will be set by this command.
     */
    public SetModelPropertyValueCommand(TransactionalEditingDomain editingDomain, String label, Object affectedObjects,
            IPropertySource propertySource, Object propertyId,
            Object propertyValue) {

        super(
            editingDomain,
            label,
            (affectedObjects instanceof EObject) ? getWorkspaceFiles((EObject) affectedObjects)
                : null);

        this.propertySource = propertySource;
        this.propertyId = propertyId;
        this.propertyValue = propertyValue;

    }

    protected CommandResult doExecuteWithResult(
            IProgressMonitor progressMonitor, IAdaptable info)
        throws ExecutionException {

        setValueResetOnUndo(!getPropertySource().isPropertySet(getPropertyId()));

        if (!isValueResetOnUndo()) {
            setUndoValue(getPropertySource().getPropertyValue(getPropertyId()));
        } else {
            undoValue = null;
        }
        getPropertySource().setPropertyValue(getPropertyId(),
                getPropertyValue());
        return CommandResult.newOKCommandResult(getPropertyValue());
    }

    protected CommandResult doRedoWithResult(IProgressMonitor progressMonitor,
            IAdaptable info)
        throws ExecutionException {
        
        return doExecuteWithResult(new NullProgressMonitor(), info);
    }

    protected CommandResult doUndoWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {

        if (isValueResetOnUndo()) {
            getPropertySource().resetPropertyValue(getPropertyId());

        } else {
            getPropertySource().setPropertyValue(getPropertyId(),
                    getUndoValue());
        }
        return CommandResult.newOKCommandResult(getPropertySource().getPropertyValue(
                getPropertyId()));
    }

    /**
     * Gets the flag to indicate that the property value had never before been
     * set, so a reset is appropriate when the command is undone.
     * 
     * @return <code>true</code> if undoing this command should reset the
     *         property value to its default, <code>false</code> otherwise.
     */
    protected boolean isValueResetOnUndo() {
        return valueResetOnUndo;
    }

    /**
     * Sets the flag to indicate that the property value had never before been
     * set, so a reset is appropriate when the command is undone.
     * 
     * @param b
     *            <code>true</code> if undoing this command should reset the
     *            property value to its default, <code>false</code> otherwise.
     */
    protected void setValueResetOnUndo(boolean b) {
        valueResetOnUndo = b;
    }

    /**
     * Gets the property source that owns the property to be restored to its
     * default value.
     * 
     * @return the property source
     */
    protected IPropertySource getPropertySource() {
        return propertySource;
    }

    /**
     * Gets the ID of the property whose default value is to be restored.
     * 
     * @return the property ID
     */
    protected Object getPropertyId() {
        return propertyId;
    }

    /**
     * Gets the property value which is used when this command execution is
     * undone.
     * 
     * @return the undo property value
     */
    protected Object getUndoValue() {
        return undoValue;
    }

    /**
     * Sets the property value which is used when this command execution is
     * undone.
     * 
     * @param object
     *            the undo property value
     */
    protected void setUndoValue(Object object) {
        undoValue = object;
    }

    /**
     * Gets the property value that is set by this command.
     * 
     * @return the property value
     */
    protected Object getPropertyValue() {
        return propertyValue;
    }

}