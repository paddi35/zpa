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

class UnusedVariableCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("unused_variable.sql"), UnusedVariableCheck())
    }

    @Test
    fun quickFixes() {
        val check = UnusedVariableCheck()
        PlSqlCheckVerifier.verify(getPath("unused_variable.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(2, 6, 12, 20, 32, 50, 51, 52, 52, 54)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Remove the declaration of \"VAR\"", "Remove the declaration of \"PROC_VAR\"", "Remove the declaration of \"FUNC_VAR\"", "Remove the declaration of \"VAR2\"", "Remove the declaration of \"PACKAGE_BODY_VAR\"", "Remove the declaration of \"DOCUMENTED\"", "Remove the declaration of \"WITH_DEFAULT\"", "Remove the declaration of \"A\"", "Remove the declaration of \"B\"", "Remove the declaration of \"E\"")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("unused_variable.sql"), UnusedVariableCheck(), getPath("unused_variable.fixed.sql"))
    }

}
