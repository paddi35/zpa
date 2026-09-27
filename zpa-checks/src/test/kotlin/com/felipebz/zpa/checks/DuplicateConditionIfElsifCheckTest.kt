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

class DuplicateConditionIfElsifCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("duplicate_condition_if_elsif.sql"), DuplicateConditionIfElsifCheck())
    }

    @Test
    fun quickFixes() {
        val check = DuplicateConditionIfElsifCheck()
        PlSqlCheckVerifier.verify(getPath("duplicate_condition_if_elsif.sql"), check)
        val fixable = check.issues().filter { it.quickFixes().isNotEmpty() }
        assertThat(fixable.map { it.primaryLocation().startLine() }).containsExactly(4, 12, 14, 22, 32, 43, 47)
        assertThat(fixable.map { it.quickFixes().single().message() }).containsOnly("Remove the unreachable ELSIF branch")
        // the middle one of three ELSIF branches: from the end of the previous branch to the end of its last statement
        assertThat(fixable[3].quickFixes().single().edits()).containsExactly(TextEdit(21, 11, 24, 11, ""))

        PlSqlCheckVerifier.verifyQuickFixes(getPath("duplicate_condition_if_elsif.sql"), DuplicateConditionIfElsifCheck(), getPath("duplicate_condition_if_elsif.fixed.sql"))
    }

}
