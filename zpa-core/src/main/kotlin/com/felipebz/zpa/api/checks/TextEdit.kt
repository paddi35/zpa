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

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Token

/**
 * Replaces a range of the analyzed source with a new text.
 *
 * The range uses the same coordinates as [com.felipebz.zpa.checks.IssueLocation] and the tokens of the AST:
 * lines are 1-based, line offsets (columns) are 0-based and counted in characters, and the end position is
 * exclusive. For example, in line 3 containing `  x := (a <> b);` the operator `<>` is the range
 * `startLine = 3, startLineOffset = 10, endLine = 3, endLineOffset = 12`.
 *
 * An empty [text] deletes the range, an empty range (start equals end) inserts [text] at that position.
 */
class TextEdit(
    private val startLine: Int,
    private val startLineOffset: Int,
    private val endLine: Int,
    private val endLineOffset: Int,
    private val text: String
) {

    init {
        require(startLine >= 1 && endLine >= 1) { "Lines are 1-based: $this" }
        require(startLineOffset >= 0 && endLineOffset >= 0) { "Line offsets are 0-based: $this" }
        require(startLine < endLine || (startLine == endLine && startLineOffset <= endLineOffset)) {
            "The end of the range is before its start: $this"
        }
    }

    fun startLine() = startLine

    fun startLineOffset() = startLineOffset

    fun endLine() = endLine

    fun endLineOffset() = endLineOffset

    /** The replacement text; empty if the range is deleted. */
    fun text() = text

    /** Returns true if the range is empty, i.e. the edit only inserts [text]. */
    fun isInsertion() = startLine == endLine && startLineOffset == endLineOffset

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextEdit) return false
        return startLine == other.startLine &&
            startLineOffset == other.startLineOffset &&
            endLine == other.endLine &&
            endLineOffset == other.endLineOffset &&
            text == other.text
    }

    override fun hashCode(): Int {
        var result = startLine
        result = 31 * result + startLineOffset
        result = 31 * result + endLine
        result = 31 * result + endLineOffset
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString() =
        "TextEdit(${startLine}:${startLineOffset}-${endLine}:${endLineOffset}, text=\"$text\")"

    companion object {

        /** Replaces the text of [node], from its first to its last token (comments before the node are kept). */
        @JvmStatic
        fun replace(node: AstNode, text: String): TextEdit =
            between(node.token, node.lastToken, text)

        /** Replaces the text from the first token of [startNode] to the last token of [endNode]. */
        @JvmStatic
        fun replace(startNode: AstNode, endNode: AstNode, text: String): TextEdit =
            between(startNode.token, endNode.lastToken, text)

        @JvmStatic
        fun replace(token: Token, text: String): TextEdit =
            between(token, token, text)

        /** Deletes the text of [node], from its first to its last token. */
        @JvmStatic
        fun remove(node: AstNode): TextEdit = replace(node, "")

        /** Deletes the text from the first token of [startNode] to the last token of [endNode]. */
        @JvmStatic
        fun remove(startNode: AstNode, endNode: AstNode): TextEdit = replace(startNode, endNode, "")

        @JvmStatic
        fun remove(token: Token): TextEdit = replace(token, "")

        /** Inserts [text] directly before the first token of [node]. */
        @JvmStatic
        fun insertBefore(node: AstNode, text: String): TextEdit = insertBefore(node.token, text)

        @JvmStatic
        fun insertBefore(token: Token, text: String): TextEdit =
            TextEdit(token.line, token.column, token.line, token.column, text)

        /** Inserts [text] directly after the last token of [node]. */
        @JvmStatic
        fun insertAfter(node: AstNode, text: String): TextEdit = insertAfter(node.lastToken, text)

        @JvmStatic
        fun insertAfter(token: Token, text: String): TextEdit =
            TextEdit(token.endLine, token.endColumn, token.endLine, token.endColumn, text)

        private fun between(first: Token, last: Token, text: String) =
            TextEdit(first.line, first.column, last.endLine, last.endColumn, text)
    }
}
