/**
 * Z PL/SQL Analyzer
 * Copyright (C) 2015-2026 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.felipebz.zpa.api.checks

import java.util.Collections

/**
 * An automatic correction for an issue: a short description of the change (e.g. `Replace "<>" with "!="`)
 * and the [TextEdit]s that perform it in the file of the issue.
 *
 * All edits refer to the original source, so they must be applied together (usually from the last one to the
 * first one). They must not overlap; an insertion may touch another edit, but two insertions at the same
 * position are rejected because their order would be ambiguous.
 */
class QuickFix(private val message: String, edits: List<TextEdit>) {

    private val edits: List<TextEdit> = Collections.unmodifiableList(edits.toList())

    constructor(message: String, vararg edits: TextEdit) : this(message, edits.toList())

    init {
        require(this.edits.isNotEmpty()) { "A quick fix needs at least one edit" }
        val sorted = this.edits.sortedWith(
            compareBy<TextEdit>({ it.startLine() }, { it.startLineOffset() }, { it.endLine() }, { it.endLineOffset() })
        )
        for (i in 1 until sorted.size) {
            val previous = sorted[i - 1]
            val current = sorted[i]
            val overlaps = previous.endLine() > current.startLine() ||
                (previous.endLine() == current.startLine() && previous.endLineOffset() > current.startLineOffset())
            val sameInsertionPoint = previous.isInsertion() && current.isInsertion() &&
                previous.startLine() == current.startLine() && previous.startLineOffset() == current.startLineOffset()
            require(!overlaps && !sameInsertionPoint) { "Overlapping edits in quick fix \"$message\": $previous, $current" }
        }
    }

    fun message() = message

    fun edits(): List<TextEdit> = edits

    override fun toString() = "QuickFix(message=\"$message\", edits=$edits)"
}
