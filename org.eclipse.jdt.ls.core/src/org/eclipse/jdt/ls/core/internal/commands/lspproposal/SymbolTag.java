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
package org.eclipse.jdt.ls.core.internal.commands.lspproposal;

/**
 * Symbol tags are extra annotations that tweak the rendering of a symbol.
 *
 * Since 3.16
 */
public enum SymbolTag {

	/**
	 * Render a symbol as obsolete, usually using a strike-out.
	 */
	Deprecated(1);

	private final int value;

	SymbolTag(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SymbolTag forValue(int value) {
		SymbolTag[] allValues = SymbolTag.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}
}