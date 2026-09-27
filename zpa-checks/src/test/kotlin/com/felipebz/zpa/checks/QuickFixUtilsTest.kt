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

import com.felipebz.flr.api.AstNodeType
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.checks.PlSqlCheck.PreciseIssue
import com.felipebz.zpa.api.checks.QuickFixes
import com.felipebz.zpa.api.checks.TextEdit
import com.felipebz.zpa.parser.PlSqlParser
import com.felipebz.zpa.squid.PlSqlConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class QuickFixUtilsTest {

    private val parser = PlSqlParser.create(PlSqlConfiguration(StandardCharsets.UTF_8))

    private fun removeLast(source: String, type: AstNodeType): String {
        val node = parser.parse(source).getDescendants(type).last()
        return QuickFixes.apply(source, listOf(QuickFixUtils.removeWithLeadingWhitespace(node)))
    }

    @Test
    fun removesTheWhitespaceBefore() {
        assertThat(removeLast("begin\n  a := 1;\n  b := 2;\nend;", PlSqlGrammar.STATEMENT))
            .isEqualTo("begin\n  a := 1;\nend;")
    }

    @Test
    fun keepsTheCommentsBefore() {
        assertThat(removeLast("begin\n  a := 1; -- one\n  -- two\n  b := 2;\nend;", PlSqlGrammar.STATEMENT))
            .isEqualTo("begin\n  a := 1; -- one\n  -- two\nend;")
    }

    @Test
    fun removesACommentAfterOnTheSameLine() {
        assertThat(removeLast("begin\n  a := 1;\n  b := 2; -- two\nend;", PlSqlGrammar.STATEMENT))
            .isEqualTo("begin\n  a := 1;\nend;")
        assertThat(removeLast("begin\n  a := 1;\n  b := 2; /* two\n  */\nend;", PlSqlGrammar.STATEMENT))
            .isEqualTo("begin\n  a := 1; /* two\n  */\nend;")
    }

    @Test
    fun doesNotJoinTheTokensAround() {
        assertThat(removeLast("declare\n  v number;begin null; end;", PlSqlGrammar.VARIABLE_DECLARATION))
            .isEqualTo("declare\n  begin null; end;")
    }

    @Test
    fun addsOnlyTheOutermostRemovals() {
        val outer = removal(TextEdit(1, 0, 5, 0, ""))
        val inner = removal(TextEdit(2, 0, 3, 0, ""))
        val same = removal(TextEdit(1, 0, 5, 0, ""))
        val touching = removal(TextEdit(5, 0, 6, 0, ""))

        QuickFixUtils.addOutermostRemovals(listOf(inner, touching, outer, same))

        assertThat(outer.issue.quickFixes()).hasSize(1)
        assertThat(touching.issue.quickFixes()).hasSize(1)
        assertThat(inner.issue.quickFixes()).isEmpty()
        assertThat(same.issue.quickFixes()).isEmpty()
    }

    private fun removal(edit: TextEdit) =
        QuickFixUtils.Removal(PreciseIssue(IssueLocation.atFileLevel("issue")), "fix", edit)

}
