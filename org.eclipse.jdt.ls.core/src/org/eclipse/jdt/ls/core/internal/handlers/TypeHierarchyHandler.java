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
package org.eclipse.jdt.ls.core.internal.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.annotations.JsonAdapter;

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
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.JDTUtils.LocationType;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionAndWorkDoneProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressAndPartialResultParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

public class TypeHierarchyHandler {

	public List<TypeHierarchyItem> prepareTypeHierarchy(@NonNull TypeHierarchyPrepareParams params, IProgressMonitor monitor) {
		TypeHierarchyResponses.clear();
		TextDocumentIdentifier textDocument = params.getTextDocument();
		if (textDocument == null) {
			return null;
		}
		Position position = params.getPosition();
		String uri = textDocument.getUri();
		IType type;
		try {
			type = getType(uri, position, monitor);
			TypeHierarchyItem item = toTypeHierarchyItem(type);
			return List.of(item);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public List<TypeHierarchyItem> supertypes(@NonNull TypeHierarchySupertypesParams params, IProgressMonitor monitor) {
		TypeHierarchyItem item = params.item;
		IType type;
		ItemData data = JSONUtility.toModel(item.getData(), ItemData.class);
		IJavaElement element = JavaCore.create(data.getTypeId());
		if (element instanceof IType) {
			type = ((IType) element);
		} else if (element instanceof IOrdinaryClassFile) {
			type = ((IOrdinaryClassFile) element).getType();
		} else {
			return Collections.emptyList();
		}

		ITypeHierarchy typeHierarchy = null;
		try {
			typeHierarchy = TypeHierarchyResponses.get();
			if (typeHierarchy == null) {
				typeHierarchy = type.newTypeHierarchy(type.getJavaProject(), DefaultWorkingCopyOwner.PRIMARY, monitor);
				TypeHierarchyResponses.store(typeHierarchy);
			}
		} catch (JavaModelException e) {
			return Collections.emptyList();
		}
		IType[] supertypes = typeHierarchy.getSupertypes(type);
		List<TypeHierarchyItem> ret = Arrays.asList(supertypes).stream().map(TypeHierarchyHandler::toTypeHierarchyItem).filter(Objects::nonNull).collect(Collectors.toList());
		return ret;
	}

	public List<TypeHierarchyItem> subtypes(TypeHierarchySubtypesParams params, IProgressMonitor monitor) {
		TypeHierarchyItem item = params.item;
		IType type;
		ItemData data = JSONUtility.toModel(item.getData(), ItemData.class);
		String typeId = data.getTypeId();
		IJavaElement element = JavaCore.create(typeId);
		if (element instanceof IType) {
			type = ((IType) element);
		} else if (element instanceof IOrdinaryClassFile) {
			type = ((IOrdinaryClassFile) element).getType();
		} else {
			return Collections.emptyList();
		}

		ITypeHierarchy typeHierarchy = null;
		try {
			typeHierarchy = TypeHierarchyResponses.get();
			if (typeHierarchy == null) {
				typeHierarchy = type.newTypeHierarchy(type.getJavaProject(), DefaultWorkingCopyOwner.PRIMARY, monitor);
				TypeHierarchyResponses.store(typeHierarchy);
			}
		} catch (JavaModelException e) {
			return Collections.emptyList();
		}

		IType[] subtypes = typeHierarchy.getSubtypes(type);
		List<TypeHierarchyItem> ret = Arrays.asList(subtypes).stream().map(TypeHierarchyHandler::toTypeHierarchyItem).filter(Objects::nonNull).collect(Collectors.toList());
		return ret;
	}

	private IType getType(String uri, Position position, IProgressMonitor monitor) throws JavaModelException {
		IJavaElement typeElement = findTypeElement(JDTUtils.resolveTypeRoot(uri), position, monitor);
		if (typeElement instanceof IType) {
			return (IType) typeElement;
		} else if (typeElement instanceof IMethod) {
			return ((IMethod) typeElement).getDeclaringType();
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

	private static TypeHierarchyItem toTypeHierarchyItem(IType type) {
		if (type == null) {
			return null;
		}
		try {
			Location location = getLocation(type, LocationType.FULL_RANGE);
			Location selectLocation = getLocation(type, LocationType.NAME_RANGE);
			if (location == null || selectLocation == null) {
				return null;
			}
			TypeHierarchyItem item = new TypeHierarchyItem();
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
			List<SymbolTag> tags = new ArrayList<>();
			if (JDTUtils.isDeprecated(type)) {
				tags.add(SymbolTag.forValue(SymbolTag.Deprecated.getValue()));
			}
			item.setTags(tags);
			ItemData data = new ItemData();
			data.setTypeId(type.getHandleIdentifier());
			item.setData(data);
			return item;
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static Location getLocation(IType type, LocationType locationType) throws JavaModelException {
		Location location = locationType.toLocation(type);
		if (location == null && type.getClassFile() != null) {
			location = JDTUtils.toLocation(type.getClassFile());
		}
		return location;
	}

	public static class TypeHierarchyItem {
		private String name;
		private SymbolKind kind;
		private List<SymbolTag> tags;
		private String detail;
		private String uri;
		private Range range;
		private Range selectionRange;

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object data;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public SymbolKind getKind() {
			return kind;
		}

		public void setKind(SymbolKind kind) {
			this.kind = kind;
		}

		public List<SymbolTag> getTags() {
			return tags;
		}

		public void setTags(List<SymbolTag> tags) {
			this.tags = tags;
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public Range getRange() {
			return range;
		}

		public void setRange(Range range) {
			this.range = range;
		}

		public Range getSelectionRange() {
			return selectionRange;
		}

		public void setSelectionRange(Range selectionRange) {
			this.selectionRange = selectionRange;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
	}

	public static class ItemData {
		private String typeId;

		public String getTypeId() {
			return typeId;
		}

		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}
	}

	public static class TypeHierarchyPrepareParams extends TextDocumentPositionAndWorkDoneProgressParams {
	}

	public static class TypeHierarchySupertypesParams extends WorkDoneProgressAndPartialResultParams {
		public TypeHierarchyItem item;
	}

	public static class TypeHierarchySubtypesParams extends WorkDoneProgressAndPartialResultParams {
		public TypeHierarchyItem item;
	}
}