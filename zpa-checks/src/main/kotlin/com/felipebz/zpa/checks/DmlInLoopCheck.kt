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

@Rule(priority = Priority.MAJOR, tags = [Tags.PERFORMANCE])
@ConstantRemediation("20min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class DmlInLoopCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(PlSqlGrammar.LOOP_STATEMENT, PlSqlGrammar.FOR_STATEMENT, PlSqlGrammar.WHILE_STATEMENT)
    }

    override fun visitNode(node: AstNode) {
        node.getFirstChild(PlSqlGrammar.STATEMENTS).children.forEach(::scanForDml)
    }

    private fun scanForDml(node: AstNode) {
        when (node.type) {
            PlSqlGrammar.INSERT_STATEMENT, PlSqlGrammar.UPDATE_STATEMENT,
            PlSqlGrammar.DELETE_STATEMENT, PlSqlGrammar.MERGE_STATEMENT ->
                addIssue(node, getLocalizedMessage())
            PlSqlGrammar.LOOP_STATEMENT, PlSqlGrammar.FOR_STATEMENT,
            PlSqlGrammar.WHILE_STATEMENT, PlSqlGrammar.FORALL_STATEMENT -> {
                // each of these is its own scope and handles/reports itself
            }
            else -> node.children.forEach(::scanForDml)
        }
    }

}
