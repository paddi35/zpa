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
import com.felipebz.zpa.typeIs
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.annotations.*

@Rule(priority = Priority.MAJOR, tags = [Tags.UNUSED])
@ConstantRemediation("5min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class DeadCodeCheck : AbstractBaseCheck() {

    private val removals = mutableListOf<QuickFixUtils.Removal>()

    override fun init() {
        subscribeTo(*CheckUtils.terminationStatements)
        subscribeTo(PlSqlGrammar.METHOD_CALL)
    }

    override fun visitFile(node: AstNode) {
        removals.clear()
    }

    override fun leaveFile(node: AstNode) {
        // dead code can contain dead code: only the outermost removal is offered
        QuickFixUtils.addOutermostRemovals(removals)
        removals.clear()
    }

    override fun visitNode(node: AstNode) {
        if (CheckUtils.isTerminationStatement(node)) {
            var parent = node.parent
            while (!checkNode(parent)) {
                parent = parent.parent
            }
        }
    }

    private fun checkNode(node: AstNode?): Boolean {
        if (!shouldCheckNode(node) || node == null) {
            return true
        }
        val nextSibling = node.nextSiblingOrNull
        if (nextSibling != null && nextSibling.typeIs(PlSqlGrammar.STATEMENT)) {
            val issue = addIssue(nextSibling, getLocalizedMessage())
            addRemoval(issue, nextSibling)
            return true
        }
        return false
    }

    /** Removes the unreachable statement and all statements after it in the same sequence. */
    private fun addRemoval(issue: PreciseIssue, first: AstNode) {
        val statements = generateSequence(first) { it.nextSiblingOrNull }
            .takeWhile { it.typeIs(PlSqlGrammar.STATEMENT) }
            .toList()
        // a GOTO outside the removed code could jump to a label in it
        if (statements.any { it.hasDescendant(PlSqlGrammar.LABEL) }) {
            return
        }
        removals.add(QuickFixUtils.Removal(issue, getQuickFixMessage(),
            QuickFixUtils.removeWithLeadingWhitespace(first, statements.last())))
    }

    private fun shouldCheckNode(node: AstNode?): Boolean {
        if (node == null || CheckUtils.isProgramUnit(node)) {
            return false
        }

        return if (node.typeIs(STATEMENT_OR_CALL)) {
            true
        } else {
            node.typeIs(STATEMENT_SECTION) && !node.hasDirectChildren(PlSqlGrammar.EXCEPTION_HANDLERS)
        }
    }

    companion object {
        val STATEMENT_OR_CALL = arrayOf(PlSqlGrammar.STATEMENT, PlSqlGrammar.BLOCK_STATEMENT, PlSqlGrammar.CALL_STATEMENT)
        val STATEMENT_SECTION = arrayOf(PlSqlGrammar.STATEMENTS_SECTION, PlSqlGrammar.STATEMENTS)
    }

}
