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

class VariableInCountCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("variable_in_count.sql"), VariableInCountCheck())
    }

    @Test
    fun quickFixes() {
        val check = VariableInCountCheck()
        PlSqlCheckVerifier.verify(getPath("variable_in_count.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(5, 9, 10)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Replace \"foo\" with \"*\"", "Replace \"FOO\" with \"*\"")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("variable_in_count.sql"), VariableInCountCheck(), getPath("variable_in_count.fixed.sql"))
    }

}
