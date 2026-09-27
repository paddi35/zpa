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

class DeadCodeCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("dead_code.sql"), DeadCodeCheck())
    }

    @Test
    fun quickFixes() {
        val check = DeadCodeCheck()
        PlSqlCheckVerifier.verify(getPath("dead_code.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(4, 11, 18, 25, 33, 43, 54, 60, 71, 81, 89, 98)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Remove the unreachable code")
        // the code after the first RETURN contains a second RETURN and its dead code
        assertThat(fixable[8].quickFixes().single().edits()).containsExactly(TextEdit(70, 22, 73, 27, ""))

        PlSqlCheckVerifier.verifyQuickFixes(getPath("dead_code.sql"), DeadCodeCheck(), getPath("dead_code.fixed.sql"))
    }

}
