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

@Rule(priority = Priority.MAJOR, tags = [Tags.BUG, Tags.PITFALL])
@ConstantRemediation("10min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class EmptyOthersHandlerCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(PlSqlGrammar.EXCEPTION_HANDLER)
    }

    override fun visitNode(node: AstNode) {
        // A bare "WHEN OTHERS" is a single OTHERS designator, not combined via OR with
        // another exception name (OBJECT_REFERENCE is reduced to its inner node - e.g.
        // VARIABLE_NAME - for a plain identifier, so it is not matched against directly).
        if (!node.hasDirectChildren(PlSqlKeyword.OTHERS) || node.hasDirectChildren(PlSqlKeyword.OR)) {
            return
        }

        val statements = node.getFirstChild(PlSqlGrammar.STATEMENTS).getChildren(PlSqlGrammar.STATEMENT)
        val isSwallowed = statements.isEmpty() ||
                (statements.size == 1 && statements[0].hasDirectChildren(PlSqlGrammar.NULL_STATEMENT))
        if (isSwallowed) {
            addIssue(node.getFirstChild(PlSqlKeyword.OTHERS), getLocalizedMessage())
        }
    }

}
