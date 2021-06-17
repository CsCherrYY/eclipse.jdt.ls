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

import org.eclipse.jdt.core.ITypeHierarchy;

public class TypeHierarchyResponses {

	private static ITypeHierarchy typeHierarchy;

	public static ITypeHierarchy get() {
		return TypeHierarchyResponses.typeHierarchy;
	}

	public static void store(ITypeHierarchy typeHierarchy) {
		TypeHierarchyResponses.typeHierarchy = typeHierarchy;
	}

	public static void clear() {
		TypeHierarchyResponses.typeHierarchy = null;
	}
}
