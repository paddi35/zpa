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
import com.felipebz.zpa.api.PlSqlKeyword
import com.felipebz.zpa.api.annotations.*
import com.felipebz.zpa.api.checks.TextEdit

@Rule(priority = Priority.MINOR)
@ConstantRemediation("2min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class VariableInitializationWithNullCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(PlSqlGrammar.DEFAULT_VALUE_ASSIGNMENT)
    }

    override fun visitNode(node: AstNode) {
        if (node.hasParent(PlSqlGrammar.VARIABLE_DECLARATION, PlSqlGrammar.RECORD_FIELD_DECLARATION)) {
            val expression = node.lastChild
            if (CheckUtils.isNullLiteralOrEmptyString(expression)) {
                val issue = addIssue(node, getLocalizedMessage())
                if (canRemove(node)) {
                    // remove " := NULL" including the whitespace before it
                    val previous = node.previousSibling.lastToken
                    issue.addQuickFix(getQuickFixMessage(), TextEdit(previous.endLine, previous.endColumn,
                        node.lastToken.endLine, node.lastToken.endColumn, ""))
                }
            }
        }
    }

    /**
     * A constant or a NOT NULL variable must be initialized (the code does not compile anyway), and comments
     * inside the removed text would be lost.
     */
    private fun canRemove(node: AstNode): Boolean {
        val declaration = node.parent
        val nullConstraint = declaration.getFirstChildOrNull(PlSqlGrammar.DATATYPE_NULL_CONSTRAINT)
        return !declaration.hasDirectChildren(PlSqlKeyword.CONSTANT) &&
            nullConstraint?.hasDirectChildren(PlSqlKeyword.NOT) != true &&
            node.previousSiblingOrNull != null &&
            node.tokens.none { token -> token.trivia.any { it.isComment } }
    }

}
