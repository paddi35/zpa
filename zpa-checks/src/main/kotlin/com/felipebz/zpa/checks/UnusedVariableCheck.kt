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
import com.felipebz.zpa.api.checks.TextEdit
import com.felipebz.zpa.api.symbols.Scope
import com.felipebz.zpa.api.symbols.Symbol

@Rule(priority = Priority.MAJOR, tags = [Tags.UNUSED])
@ConstantRemediation("2min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class UnusedVariableCheck : AbstractBaseCheck() {

    override fun leaveFile(node: AstNode) {
        val scopes = context.symbolTable.scopes
        for (scope in scopes) {
            if (scope.type !in arrayOf(PlSqlGrammar.CREATE_PACKAGE, PlSqlGrammar.FOR_STATEMENT)) {
                checkScope(scope)
            }
        }
    }

    private fun checkScope(scope: Scope) {
        val symbols = scope.getSymbols(Symbol.Kind.VARIABLE)
        for (symbol in symbols) {
            if (symbol.usages.isEmpty()) {
                val issue = addIssue(symbol.declaration, getLocalizedMessage(), symbol.name)
                val edit = removeDeclaration(symbol.declaration)
                if (edit != null) {
                    issue.addQuickFix(getQuickFixMessage(symbol.name), edit)
                }
            }
        }
    }

    /**
     * Removes the declaration, unless its initialization could call a user-defined function (it could have side
     * effects). Built-in SQL functions with their own grammar rule, like TO_NUMBER, are not calls in this sense.
     */
    private fun removeDeclaration(identifier: AstNode): TextEdit? {
        val declaration = identifier.parent
        if (declaration.type !== PlSqlGrammar.VARIABLE_DECLARATION && declaration.type !== PlSqlGrammar.EXCEPTION_DECLARATION) {
            return null
        }
        val initialization = declaration.getFirstChildOrNull(PlSqlGrammar.DEFAULT_VALUE_ASSIGNMENT)
        if (initialization != null && !hasNoCalls(initialization)) {
            return null
        }
        return QuickFixUtils.removeUnusedDeclaration(declaration, identifier)
    }

    /** A name that is not a known variable or parameter, e.g. SYSDATE or a package member, could be a function call. */
    private fun hasNoCalls(expression: AstNode): Boolean =
        !expression.hasDescendant(PlSqlGrammar.METHOD_CALL, PlSqlGrammar.MEMBER_EXPRESSION) &&
            expression.getDescendants(PlSqlGrammar.VARIABLE_NAME).all {
                semantic(it).symbol?.kind in arrayOf(Symbol.Kind.VARIABLE, Symbol.Kind.PARAMETER)
            }

}
