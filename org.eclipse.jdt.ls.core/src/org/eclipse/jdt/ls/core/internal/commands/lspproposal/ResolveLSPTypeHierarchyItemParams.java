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

import org.eclipse.lsp4j.TypeHierarchyDirection;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class ResolveLSPTypeHierarchyItemParams {
  /**
   * The hierarchy item to resolve.
   */
  @NonNull
  private LSPTypeHierarchyItem item;

  /**
   * The number of hierarchy levels to resolve. {@code 0} indicates no hierarchy level.
   */
  private int resolve;

  /**
   * The direction of the type hierarchy resolution.
   */
  @NonNull
  private TypeHierarchyDirection direction;

  public ResolveLSPTypeHierarchyItemParams() {
  }

  public ResolveLSPTypeHierarchyItemParams(@NonNull final LSPTypeHierarchyItem item, final int resolve, @NonNull final TypeHierarchyDirection direction) {
    this.item = Preconditions.<LSPTypeHierarchyItem>checkNotNull(item, "item");
    this.resolve = resolve;
    this.direction = Preconditions.<TypeHierarchyDirection>checkNotNull(direction, "direction");
  }

  /**
   * The hierarchy item to resolve.
   */
  @Pure
  @NonNull
  public LSPTypeHierarchyItem getItem() {
    return this.item;
  }

  /**
   * The hierarchy item to resolve.
   */
  public void setItem(@NonNull final LSPTypeHierarchyItem item) {
    this.item = Preconditions.checkNotNull(item, "item");
  }

  /**
   * The number of hierarchy levels to resolve. {@code 0} indicates no hierarchy level.
   */
  @Pure
  public int getResolve() {
    return this.resolve;
  }

  /**
   * The number of hierarchy levels to resolve. {@code 0} indicates no hierarchy level.
   */
  public void setResolve(final int resolve) {
    this.resolve = resolve;
  }

  /**
   * The direction of the type hierarchy resolution.
   */
  @Pure
  @NonNull
  public TypeHierarchyDirection getDirection() {
    return this.direction;
  }

  /**
   * The direction of the type hierarchy resolution.
   */
  public void setDirection(@NonNull final TypeHierarchyDirection direction) {
    this.direction = Preconditions.checkNotNull(direction, "direction");
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("item", this.item);
    b.add("resolve", this.resolve);
    b.add("direction", this.direction);
    return b.toString();
  }

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResolveLSPTypeHierarchyItemParams other = (ResolveLSPTypeHierarchyItemParams) obj;
    if (this.item == null) {
      if (other.item != null)
        return false;
    } else if (!this.item.equals(other.item))
      return false;
    if (other.resolve != this.resolve)
      return false;
    if (this.direction == null) {
      if (other.direction != null)
        return false;
    } else if (!this.direction.equals(other.direction))
      return false;
    return true;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.item== null) ? 0 : this.item.hashCode());
    result = prime * result + this.resolve;
    return prime * result + ((this.direction== null) ? 0 : this.direction.hashCode());
  }
}
