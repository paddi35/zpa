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
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.annotations.*
import com.felipebz.zpa.api.syntax.IfStatement
import com.felipebz.zpa.api.syntax.SyntaxViews
import com.felipebz.zpa.typeIs

@Rule(priority = Priority.BLOCKER, tags = [Tags.BUG])
@ConstantRemediation("5min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
@OptIn(ZpaExperimentalApi::class)
class DuplicateConditionIfElsifCheck : AbstractBaseCheck() {

    private val removals = mutableListOf<QuickFixUtils.Removal>()

    override fun init() {
        subscribeTo(SyntaxViews.IF_STATEMENT, ::visitIfStatement)
    }

    override fun visitFile(node: AstNode) {
        removals.clear()
    }

    override fun leaveFile(node: AstNode) {
        // a removed branch can contain another IF statement with a duplicated condition
        QuickFixUtils.addOutermostRemovals(removals)
        removals.clear()
    }

    private fun visitIfStatement(statement: IfStatement) {
        val conditions = collectConditionsFromBranches(statement)
        findSameConditions(conditions)
    }

    private fun findSameConditions(branches: List<AstNode>) {
        for (i in 1 until branches.size) {
            checkCondition(branches, i)
        }
    }

    private fun checkCondition(conditions: List<AstNode>, index: Int) {
        val condition = conditions[index]
        for (j in 0 until index) {
            val otherCondition = conditions[j]
            if (CheckUtils.equalNodes(otherCondition, condition)) {
                val issue = addIssue(condition, getLocalizedMessage(), otherCondition.token.line)
                        .secondary(otherCondition, "Original")
                addRemoval(issue, condition)
                return
            }
        }
    }

    /**
     * Removes the ELSIF branch of the duplicated condition: it can never be executed, because the earlier branch with
     * the same condition is taken whenever the condition is true. Not if the condition calls a function: it could
     * return another value the second time or have side effects.
     */
    private fun addRemoval(issue: PreciseIssue, condition: AstNode) {
        // only conditions of ELSIF branches are compared with earlier ones
        val branch = condition.parent
        if (branch.typeIs(PlSqlGrammar.ELSIF_CLAUSE) &&
            !condition.typeIs(PlSqlGrammar.METHOD_CALL) && !condition.hasDescendant(PlSqlGrammar.METHOD_CALL)) {
            removals.add(QuickFixUtils.Removal(issue, getQuickFixMessage(),
                QuickFixUtils.removeWithLeadingWhitespace(branch)))
        }
    }

    private fun collectConditionsFromBranches(statement: IfStatement) = buildList {
        add(statement.conditionAstNode)
        statement.elsifBranches.forEach { add(it.conditionAstNode) }
    }

}
