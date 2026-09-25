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

class InequalityUsageCheckTest : BaseCheckTest() {

    @Test
    fun test() {
        PlSqlCheckVerifier.verify(getPath("inequality_usage_check.sql"), InequalityUsageCheck())
    }

    @Test
    fun quickFixes() {
        val check = InequalityUsageCheck()
        PlSqlCheckVerifier.verify(getPath("inequality_usage_check.sql"), check)
        assertThat(check.issues().flatMap { issue -> issue.quickFixes().map { it.message() } })
            .containsExactly("Replace \"<>\" with \"!=\"", "Replace \"^=\" with \"!=\"", "Replace \"~=\" with \"!=\"", "Replace \"<>\" with \"!=\"",
                "Replace \"<>\" with \"!=\"", "Replace \"^=\" with \"!=\"", "Replace \"<>\" with \"!=\"")

        PlSqlCheckVerifier.verifyQuickFixes(getPath("inequality_usage_check.sql"), InequalityUsageCheck(), getPath("inequality_usage_check.fixed.sql"))
    }

}
