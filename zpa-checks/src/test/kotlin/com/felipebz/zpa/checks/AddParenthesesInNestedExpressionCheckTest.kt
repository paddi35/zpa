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

import com.felipebz.zpa.checks.verifier.PlSqlCheckVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AddParenthesesInNestedExpressionCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("add_parentheses_in_nested_expression.sql"), AddParenthesesInNestedExpressionCheck())
    }

    @Test
    fun quickFixes() {
        val check = AddParenthesesInNestedExpressionCheck()
        PlSqlCheckVerifier.verify(getPath("add_parentheses_in_nested_expression.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(2, 4, 6, 13, 16, 17, 17, 19)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Add parentheses around the AND condition")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("add_parentheses_in_nested_expression.sql"), AddParenthesesInNestedExpressionCheck(), getPath("add_parentheses_in_nested_expression.fixed.sql"))
    }

}
