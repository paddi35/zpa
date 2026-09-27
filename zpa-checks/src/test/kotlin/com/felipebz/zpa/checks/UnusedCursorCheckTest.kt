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

class UnusedCursorCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("unused_cursor.sql"), UnusedCursorCheck())
    }

    @Test
    fun quickFixes() {
        val check = UnusedCursorCheck()
        PlSqlCheckVerifier.verify(getPath("unused_cursor.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(2, 12, 15)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Remove the declaration of \"CUR\"", "Remove the declaration of \"WITH_PARAMS\"", "Remove the declaration of \"LAST_ONE\"")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("unused_cursor.sql"), UnusedCursorCheck(), getPath("unused_cursor.fixed.sql"))
    }

}
