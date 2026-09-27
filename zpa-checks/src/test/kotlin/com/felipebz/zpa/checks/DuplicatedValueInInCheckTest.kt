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

class DuplicatedValueInInCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("duplicated_value_in_in.sql"), DuplicatedValueInInCheck())
    }

    @Test
    fun quickFixes() {
        val check = DuplicatedValueInInCheck()
        PlSqlCheckVerifier.verify(getPath("duplicated_value_in_in.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(2, 8, 11, 11, 13, 13, 15, 18)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Remove the duplicated value \"1\"", "Remove the duplicated value \"x\"", "Remove the duplicated value \"2\"", "Remove the duplicated value \"(\"", "Remove the duplicated value \"'a'\"")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("duplicated_value_in_in.sql"), DuplicatedValueInInCheck(), getPath("duplicated_value_in_in.fixed.sql"))
    }

}
