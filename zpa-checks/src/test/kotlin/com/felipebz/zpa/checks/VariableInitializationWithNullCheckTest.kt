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

class VariableInitializationWithNullCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("variable_initialization_with_null.sql"), VariableInitializationWithNullCheck())
    }

    @Test
    fun quickFixes() {
        val check = VariableInitializationWithNullCheck()
        PlSqlCheckVerifier.verify(getPath("variable_initialization_with_null.sql"), check)
        assertThat(check.issues().flatMap { issue -> issue.quickFixes().map { it.message() } })
            .containsExactly(*Array(8) { "Remove the initialization to NULL" })

        PlSqlCheckVerifier.verifyQuickFixes(getPath("variable_initialization_with_null.sql"), VariableInitializationWithNullCheck(), getPath("variable_initialization_with_null.fixed.sql"))
    }

}
