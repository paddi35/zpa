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
import com.felipebz.flr.api.AstNodeType
import com.felipebz.zpa.typeIs
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.PlSqlKeyword
import com.felipebz.zpa.api.annotations.*
import com.felipebz.zpa.api.checks.TextEdit
import com.felipebz.zpa.api.syntax.IfStatement
import com.felipebz.zpa.api.syntax.SyntaxViews

@Rule(priority = Priority.MINOR, tags = [Tags.CLUMSY])
@ConstantRemediation("2min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
@OptIn(ZpaExperimentalApi::class)
class ReturnOfBooleanExpressionCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(SyntaxViews.IF_STATEMENT, ::visitIfStatement)
    }

    private fun visitIfStatement(statement: IfStatement) {
        val elseBranch = statement.elseBranch
        if (!hasElsif(statement) && elseBranch != null) {
            val firstBoolean = booleanReturnedBy(statement.statementAstNodes)
            val secondBoolean = booleanReturnedBy(elseBranch.statementAstNodes)

            if (firstBoolean != null && secondBoolean != null
                    && firstBoolean.tokenValue != secondBoolean.tokenValue) {
                val issue = addIssue(statement, getLocalizedMessage())
                val edits = quickFixEdits(statement.astNode, statement.conditionAstNode,
                    negate = firstBoolean.tokenValue.equals("FALSE", ignoreCase = true))
                if (edits != null) {
                    issue.addQuickFix(getQuickFixMessage(), *edits.toTypedArray())
                }
            }
        }
    }

    /**
     * Turns "IF cond THEN RETURN TRUE; ELSE RETURN FALSE; END IF;" into "RETURN cond;" and, with the literals swapped,
     * into "RETURN NOT cond;". The condition stays in place: the IF keyword becomes RETURN and everything after the
     * condition becomes ";". NOT binds tighter than AND and OR, so these conditions must be put in parentheses; other
     * conditions except simple names, calls and parenthesized expressions get them too for readability ("NOT (x = 1)"
     * instead of "NOT x = 1"). The NOT of "NOT x" is removed instead. Returns null if a comment would be deleted.
     */
    private fun quickFixEdits(ifStatement: AstNode, condition: AstNode, negate: Boolean): List<TextEdit>? {
        val ifKeyword = ifStatement.getFirstChild(PlSqlKeyword.IF)
        val removedTokens = ifStatement.tokens.dropWhile { it !== condition.lastToken }.drop(1)
        if (removedTokens.any { QuickFixUtils.hasComment(it) }) {
            return null
        }

        val returnKeyword = CheckUtils.matchKeywordCase("RETURN", ifStatement)
        val edits = mutableListOf<TextEdit>()
        var end = ";"
        if (!negate) {
            edits.add(TextEdit.replace(ifKeyword.token, returnKeyword))
        } else if (condition.typeIs(PlSqlGrammar.NOT_EXPRESSION)) {
            edits.add(TextEdit.replace(ifKeyword.token, returnKeyword))
            // remove "NOT" and the whitespace after it
            val not = condition.firstChild.token
            val operand = condition.lastChild.token
            edits.add(if (QuickFixUtils.hasComment(operand)) {
                TextEdit.replace(not, "")
            } else {
                TextEdit(not.line, not.column, operand.line, operand.column, "")
            })
        } else {
            val notKeyword = CheckUtils.matchKeywordCase("NOT", ifStatement)
            edits.add(TextEdit.replace(ifKeyword.token, "$returnKeyword $notKeyword"))
            if (!condition.typeIs(WITHOUT_PARENTHESES)) {
                edits.add(TextEdit.insertBefore(condition, "("))
                end = ");"
            }
        }
        val last = ifStatement.lastToken
        edits.add(TextEdit(condition.lastToken.endLine, condition.lastToken.endColumn, last.endLine, last.endColumn, end))
        return edits
    }

    private fun hasElsif(ifStatement: IfStatement): Boolean {
        return ifStatement.elsifBranches.isNotEmpty()
    }

    private fun booleanReturnedBy(statements: List<AstNode>): AstNode? {
        return extractBooleanValueFromReturn(statements.singleOrNull())
    }

    private fun extractBooleanValueFromReturn(node: AstNode?): AstNode? {
        if (node != null) {
            val child = node.firstChild
            if (child.typeIs(PlSqlGrammar.RETURN_STATEMENT)) {
                val expression = child.getFirstChildOrNull(PlSqlGrammar.LITERAL)

                return getBooleanLiteral(expression)
            }
        }
        return null
    }

    private fun getBooleanLiteral(expression: AstNode?): AstNode? {
        return expression?.getFirstChildOrNull(PlSqlGrammar.BOOLEAN_LITERAL)
    }

    private companion object {
        /** Conditions that are not put in parentheses after an inserted NOT. */
        val WITHOUT_PARENTHESES = arrayOf<AstNodeType>(PlSqlGrammar.VARIABLE_NAME, PlSqlGrammar.MEMBER_EXPRESSION,
            PlSqlGrammar.METHOD_CALL, PlSqlGrammar.BRACKED_EXPRESSION, PlSqlGrammar.LITERAL)
    }

}
