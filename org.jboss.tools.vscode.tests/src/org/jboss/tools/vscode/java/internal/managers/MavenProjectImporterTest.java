/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.vscode.java.internal.managers;

import org.junit.Test;

/**
 * @author Fred Bricon
 */
public class MavenProjectImporterTest extends AbstractMavenBasedTest {

	@Test
	public void testImportSimpleJavaProject() throws Exception {
		importSimpleJavaProject();
	}


}