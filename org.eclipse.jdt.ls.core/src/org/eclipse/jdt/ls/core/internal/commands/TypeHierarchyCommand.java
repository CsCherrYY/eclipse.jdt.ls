/*******************************************************************************
 * Copyright (c) 2021 Microsoft Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.jdt.ls.core.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JSONUtility;
import org.eclipse.jdt.ls.core.internal.JDTUtils.LocationType;
import org.eclipse.jdt.ls.core.internal.commands.lspproposal.LSPTypeHierarchyItem;
import org.eclipse.jdt.ls.core.internal.commands.lspproposal.ResolveLSPTypeHierarchyItemParams;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TypeHierarchyDirection;
import org.eclipse.lsp4j.TypeHierarchyParams;

public class TypeHierarchyCommand {

	public LSPTypeHierarchyItem typeHierarchy(TypeHierarchyParams params, IProgressMonitor monitor) {
		if (params == null) {
			return null;
		}
		TextDocumentIdentifier textDocument = params.getTextDocument();
		if (textDocument == null) {
			return null;
		}
		Position position = params.getPosition();
		String uri = textDocument.getUri();
		TypeHierarchyDirection direction = params.getDirection();
		int resolve = params.getResolve();
		return getTypeHierarchy(uri, position, direction, resolve, null, monitor);
	}

	public LSPTypeHierarchyItem[] resolveTypeHierarchy(ResolveLSPTypeHierarchyItemParams params, IProgressMonitor monitor) {
		if (params == null) {
			return null;
		}
		LSPTypeHierarchyItem item = params.getItem();
		if (item == null) {
			return null;
		}
		Range range = item.getRange();
		if (range == null) {
			return null;
		}
		TypeHierarchyDirection direction = params.getDirection();
		IType type = null;
		String handleIdentifier = JSONUtility.toModel(item.getData(), String.class);
		IJavaElement element = JavaCore.create(handleIdentifier);
		if (element instanceof IType) {
			type = ((IType)element);
		} else if (element instanceof IOrdinaryClassFile) {
			type = ((IOrdinaryClassFile)element).getType();
		} else {
			return null;
		}
		try {
			ITypeHierarchy typeHierarchy = (direction == TypeHierarchyDirection.Parents) ? type.newSupertypeHierarchy(DefaultWorkingCopyOwner.PRIMARY, monitor) : type.newTypeHierarchy(type.getJavaProject(), DefaultWorkingCopyOwner.PRIMARY, monitor);
			if (direction == TypeHierarchyDirection.Children) {
				List<LSPTypeHierarchyItem> childrenItems = new ArrayList<LSPTypeHierarchyItem>();
				IType[] children = typeHierarchy.getSubtypes(type);
				for (IType childType : children) {
					LSPTypeHierarchyItem childItem = TypeHierarchyCommand.toTypeHierarchyItem(childType);
					if (childItem == null) {
						continue;
					}
					childrenItems.add(childItem);
				}
				return childrenItems.toArray(new LSPTypeHierarchyItem[childrenItems.size()]);
			}
			if (direction == TypeHierarchyDirection.Parents) {
				List<LSPTypeHierarchyItem> parentsItems = new ArrayList<LSPTypeHierarchyItem>();
				IType[] parents = typeHierarchy.getSupertypes(type);
				for (IType parentType : parents) {
					LSPTypeHierarchyItem parentItem = TypeHierarchyCommand.toTypeHierarchyItem(parentType);
					if (parentItem == null) {
						continue;
					}
					parentsItems.add(parentItem);
				}
				return parentsItems.toArray(new LSPTypeHierarchyItem[parentsItems.size()]);
			}
		} catch (Exception e) {
			int test = 1;
			// do nothing
		}

		return null;
	}

	private LSPTypeHierarchyItem getTypeHierarchy(String uri, Position position, TypeHierarchyDirection direction, int resolve, LSPTypeHierarchyItem itemInput, IProgressMonitor monitor) {
		if (uri == null || position == null || direction == null) {
			return null;
		}
		try {
			IType type = null;
			if (itemInput == null) {
				type = getType(uri, position, monitor);
			} else {
				String handleIdentifier = JSONUtility.toModel(itemInput.getData(), String.class);
				IJavaElement element = JavaCore.create(handleIdentifier);
				if (element instanceof IType) {
					type = ((IType)element);
				} else if (element instanceof IOrdinaryClassFile) {
					type = ((IOrdinaryClassFile)element).getType();
				} else {
					return null;
				}
			}
			LSPTypeHierarchyItem item = TypeHierarchyCommand.toTypeHierarchyItem(type);
			if (item == null) {
				return null;
			}
			return item;
		} catch (JavaModelException e) {
			return null;
		}
	}

	private IType getType(String uri, Position position, IProgressMonitor monitor) throws JavaModelException {
		IJavaElement typeElement = findTypeElement(JDTUtils.resolveTypeRoot(uri), position, monitor);
		if (typeElement instanceof IType) {
			return (IType)typeElement;
		} else if (typeElement instanceof IMethod) {
			return ((IMethod)typeElement).getDeclaringType();
		} else {
			return null;
		}
	}

	private static IJavaElement findTypeElement(ITypeRoot unit, Position position, IProgressMonitor monitor) throws JavaModelException {
		if (unit == null) {
			return null;
		}
		IJavaElement element = JDTUtils.findElementAtSelection(unit, position.getLine(), position.getCharacter(), JavaLanguageServerPlugin.getPreferencesManager(), monitor);
		if (element == null) {
			if (unit instanceof IOrdinaryClassFile) {
				element = ((IOrdinaryClassFile) unit).getType();
			} else if (unit instanceof ICompilationUnit) {
				element = unit.findPrimaryType();
			}
		}
		return element;
	}

	private static LSPTypeHierarchyItem toTypeHierarchyItem(IType type) throws JavaModelException {
		if (type == null) {
			return null;
		}
		Location location = getLocation(type, LocationType.FULL_RANGE);
		Location selectLocation = getLocation(type, LocationType.NAME_RANGE);
		if (location == null || selectLocation == null) {
			return null;
		}
		LSPTypeHierarchyItem item = new LSPTypeHierarchyItem();
		item.setRange(location.getRange());
		item.setUri(location.getUri());
		item.setSelectionRange(selectLocation.getRange());
		String fullyQualifiedName = type.getFullyQualifiedName();
		int index = fullyQualifiedName.lastIndexOf('.');
		if (index >= 1 && index < fullyQualifiedName.length() - 1 && !type.isAnonymous()) {
			item.setName(fullyQualifiedName.substring(index + 1));
			item.setDetail(fullyQualifiedName.substring(0, index));
		} else {
			item.setName(JDTUtils.getName(type));
			IPackageFragment packageFragment = type.getPackageFragment();
			if (packageFragment != null) {
				item.setDetail(packageFragment.getElementName());
			}
		}
		item.setKind(DocumentSymbolHandler.mapKind(type));
		item.setData(type.getHandleIdentifier());
		return item;
	}

	private static Location getLocation(IType type, LocationType locationType) throws JavaModelException {
		Location location = locationType.toLocation(type);
		if (location == null && type.getClassFile() != null) {
			location = JDTUtils.toLocation(type.getClassFile());
		}
		return location;
	}
}
