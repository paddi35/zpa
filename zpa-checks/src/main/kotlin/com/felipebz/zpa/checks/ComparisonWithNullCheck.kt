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
import com.felipebz.zpa.typeIs
import com.felipebz.zpa.api.ConditionsGrammar
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.annotations.*
import com.felipebz.zpa.api.checks.TextEdit

@Rule(priority = Priority.BLOCKER, tags = [Tags.BUG])
@ConstantRemediation("5min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class ComparisonWithNullCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(PlSqlGrammar.COMPARISON_EXPRESSION)
    }

    override fun visitNode(node: AstNode) {
        val children = node.getChildren(PlSqlGrammar.LITERAL)
        for (child in children) {
            if (CheckUtils.isNullLiteralOrEmptyString(child)) {
                val operator = node.getFirstChild(ConditionsGrammar.RELATIONAL_OPERATOR)
                val suggestion = if (operator.firstChild.typeIs(PlSqlGrammar.EQUALS_OPERATOR)) {
                    "IS NULL"
                } else {
                    "IS NOT NULL"
                }

                val issue = addIssue(node, getLocalizedMessage(), suggestion)
                addQuickFix(issue, node, operator, child)
            }
        }
    }

    /**
     * Rewrites "x = NULL" and "NULL = x" to "x IS NULL" (and the inequality operators to "x IS NOT NULL").
     * Nothing is offered for the other relational operators, for ANY/SOME/ALL, if both operands are NULL or if
     * the rewrite would delete a comment.
     */
    private fun addQuickFix(issue: PreciseIssue, node: AstNode, operator: AstNode, nullOperand: AstNode) {
        val isEquals = operator.firstChild.typeIs(PlSqlGrammar.EQUALS_OPERATOR)
        val isNotEquals = operator.firstChild.typeIs(PlSqlGrammar.NOTEQUALS_OPERATOR)
        if (node.numberOfChildren != 3 || node.children[1] !== operator || operator.numberOfChildren != 1 ||
            !(isEquals || isNotEquals)) {
            return
        }

        val otherOperand = if (nullOperand === node.firstChild) node.lastChild else node.firstChild
        if (otherOperand.typeIs(PlSqlGrammar.LITERAL) && CheckUtils.isNullLiteralOrEmptyString(otherOperand)) {
            return
        }

        val reference = if (nullOperand.hasDirectChildren(PlSqlGrammar.NULL_LITERAL)) nullOperand else node
        val replacement = CheckUtils.matchKeywordCase(if (isEquals) "IS NULL" else "IS NOT NULL", reference)
        val message = getQuickFixMessage(replacement)

        if (nullOperand === node.lastChild) {
            // x = NULL -> x IS NULL
            if (!removesComments(operator.tokens + nullOperand.tokens)) {
                issue.addQuickFix(message, TextEdit.replace(operator, nullOperand, replacement))
            }
        } else {
            // NULL = x -> x IS NULL
            if (!removesComments(nullOperand.tokens + operator.tokens + otherOperand.token)) {
                val start = nullOperand.token
                val end = otherOperand.token
                issue.addQuickFix(message,
                    TextEdit(start.line, start.column, end.line, end.column, ""),
                    TextEdit.insertAfter(otherOperand, " $replacement"))
            }
        }
    }

    /** Returns true if a comment precedes any of [tokens] but the first one, i.e. if removing them deletes a comment. */
    private fun removesComments(tokens: List<Token>) =
        tokens.drop(1).any { token -> token.trivia.any { it.isComment } }

}
