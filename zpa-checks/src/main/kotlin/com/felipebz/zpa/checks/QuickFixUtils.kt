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
package com.felipebz.zpa.checks

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Token
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.checks.PlSqlCheck.PreciseIssue
import com.felipebz.zpa.api.checks.TextEdit
import java.util.Locale

/** Helpers to build the [TextEdit]s of quick fixes. */
object QuickFixUtils {

    fun hasComment(token: Token): Boolean = token.trivia.any { it.isComment }

    /** Returns the token before the first token of [node], or null if [node] starts the file. */
    fun previousToken(node: AstNode): Token? {
        var current = node.previousAstNodeOrNull
        while (current != null) {
            val token = current.lastTokenOrNull
            if (token != null) {
                return token
            }
            current = current.previousAstNodeOrNull
        }
        return null
    }

    /** Returns the token after the last token of [node] (EOF at the end of the file), or null if there is none. */
    fun nextToken(node: AstNode): Token? {
        var current = node.nextAstNodeOrNUll
        while (current != null) {
            val token = current.tokenOrNull
            if (token != null) {
                return token
            }
            current = current.nextAstNodeOrNUll
        }
        return null
    }

    /**
     * Removes the nodes from [first] to [last] (siblings, e.g. statements or declarations) together with the
     * whitespace before them, so that no blank line remains where they were: the removal starts at the end of the
     * previous token. Comments directly before [first] are kept (the removal starts after them); comments inside the
     * removed nodes and a single-line comment that follows [last] on its line are removed with them. If the next token
     * or comment directly follows the removed text, only the nodes are removed, so that the text before and after them
     * is not joined.
     */
    fun removeWithLeadingWhitespace(first: AstNode, last: AstNode = first): TextEdit {
        val lastToken = last.lastToken
        val next = nextToken(last)
        val nextTrivia = next?.trivia.orEmpty().map { it.token }
        val trailingComment = next?.trivia?.firstOrNull()
            ?.takeIf { it.isComment && it.token.line == lastToken.endLine && it.token.endLine == lastToken.endLine }
            ?.token
        val end = trailingComment ?: lastToken
        val following = (if (trailingComment != null) nextTrivia.drop(1) else nextTrivia).firstOrNull() ?: next
        val followingIsAdjacent = following != null &&
            following.line == end.endLine && following.column == end.endColumn
        if (followingIsAdjacent) {
            return TextEdit.replace(first, last, "")
        }

        val lastComment = first.token.trivia.lastOrNull { it.isComment }?.token
        val start = lastComment ?: previousToken(first) ?: return TextEdit.replace(first, last, "")
        return TextEdit(start.endLine, start.endColumn, end.endLine, end.endColumn, "")
    }

    /**
     * Removes an unused [declaration] of [identifier] in a declare section (see [removeWithLeadingWhitespace]). Returns
     * null if the name appears anywhere else in the unit or block of the declaration: the symbol table does not resolve
     * every reference (e.g. `block_label.name`), so the declaration could still be used.
     */
    fun removeUnusedDeclaration(declaration: AstNode, identifier: AstNode): TextEdit? {
        val declareSection = declaration.parentOrNull
        if (declareSection?.type !== PlSqlGrammar.DECLARE_SECTION) {
            return null
        }
        val unit = declareSection.parentOrNull ?: return null
        val name = normalizeName(identifier.tokenValue)
        val declared = identifier.token
        if (unit.tokens.any { it !== declared && normalizeName(it.value) == name }) {
            return null
        }
        return removeWithLeadingWhitespace(declaration)
    }

    private fun normalizeName(name: String) = name.removeSurrounding("\"").uppercase(Locale.ROOT)

    /**
     * Adds the quick fix of each [removals] entry to its issue, unless the edit is inside the edit of another entry
     * (e.g. dead code inside dead code): the outer quick fix removes the inner code too, and both edits could not be
     * applied together. The edits must be nested or disjoint.
     */
    fun addOutermostRemovals(removals: List<Removal>) {
        val sorted = removals.sortedWith(compareBy<Removal>({ it.edit.startLine() }, { it.edit.startLineOffset() })
            .thenByDescending { it.edit.endLine() }
            .thenByDescending { it.edit.endLineOffset() })
        var outer: TextEdit? = null
        for (removal in sorted) {
            val current = outer
            if (current != null && contains(current, removal.edit)) {
                continue
            }
            outer = removal.edit
            removal.issue.addQuickFix(removal.message, removal.edit)
        }
    }

    private fun contains(outer: TextEdit, inner: TextEdit): Boolean =
        !isBefore(inner.startLine(), inner.startLineOffset(), outer.startLine(), outer.startLineOffset()) &&
            !isBefore(outer.endLine(), outer.endLineOffset(), inner.endLine(), inner.endLineOffset())

    private fun isBefore(aLine: Int, aOffset: Int, bLine: Int, bOffset: Int) =
        aLine < bLine || (aLine == bLine && aOffset < bOffset)

    class Removal(val issue: PreciseIssue, val message: String, val edit: TextEdit)
}
