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

import com.felipebz.zpa.api.checks.TextEdit
import com.felipebz.zpa.checks.verifier.PlSqlCheckVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReturnOfBooleanExpressionCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("return_of_boolean_expression.sql"), ReturnOfBooleanExpressionCheck())
    }

    @Test
    fun quickFixes() {
        val check = ReturnOfBooleanExpressionCheck()
        PlSqlCheckVerifier.verify(getPath("return_of_boolean_expression.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(3, 10, 17, 24, 31, 38, 45, 52, 55, 63, 71)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Replace with a single RETURN statement")
        // "if a = b and c = d then return false; else return true; end if;"
        assertThat(fixable[2].quickFixes().single().edits()).containsExactly(
            TextEdit(17, 2, 17, 4, "return not"), TextEdit(17, 5, 17, 5, "("), TextEdit(17, 20, 21, 9, ");"))
        // "if not a then return false; else return true; end if;"
        assertThat(fixable[5].quickFixes().single().edits()).containsExactly(
            TextEdit(38, 2, 38, 4, "return"), TextEdit(38, 5, 38, 9, ""), TextEdit(38, 10, 42, 9, ";"))

        PlSqlCheckVerifier.verifyQuickFixes(getPath("return_of_boolean_expression.sql"), ReturnOfBooleanExpressionCheck(), getPath("return_of_boolean_expression.fixed.sql"))
    }

}
