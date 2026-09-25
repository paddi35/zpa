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
package com.felipebz.zpa.checks.verifier

import com.felipebz.flr.api.AstNode
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.checks.PlSqlCheck
import com.felipebz.zpa.api.checks.QuickFix
import com.felipebz.zpa.api.checks.TextEdit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class QuickFixApplierTest {

    @Test
    fun replaceInsertAndDeleteOnOneLine() {
        val source = "x := (a <> b);"
        val edits = listOf(
            TextEdit(1, 8, 1, 10, "!="),
            TextEdit(1, 5, 1, 5, "not "),
            TextEdit(1, 13, 1, 14, "")
        )
        assertThat(QuickFixApplier.apply(source, edits)).isEqualTo("x := not (a != b)")
    }

    @Test
    fun editsSpanningLinesWithAnyLineSeparator() {
        val lf = "a\nbc\nd"
        assertThat(QuickFixApplier.apply(lf, listOf(TextEdit(1, 1, 3, 0, "-")))).isEqualTo("a-d")
        val crlf = "a\r\nbc\r\nd"
        assertThat(QuickFixApplier.apply(crlf, listOf(TextEdit(2, 1, 2, 2, "x")))).isEqualTo("a\r\nbx\r\nd")
        assertThat(QuickFixApplier.apply(crlf, listOf(TextEdit(1, 1, 3, 0, "")))).isEqualTo("ad")
        val cr = "a\rbc\rd"
        assertThat(QuickFixApplier.apply(cr, listOf(TextEdit(3, 1, 3, 1, "e")))).isEqualTo("a\rbc\rde")
    }

    @Test
    fun applyQuickFix() {
        val fix = QuickFix("Swap", TextEdit(1, 0, 1, 1, "b"), TextEdit(1, 2, 1, 3, "a"))
        assertThat(QuickFixApplier.apply("a b", fix)).isEqualTo("b a")
    }

    @Test
    fun rejectsOverlappingEdits() {
        assertThatThrownBy { QuickFixApplier.apply("abcdef", listOf(TextEdit(1, 0, 1, 3, ""), TextEdit(1, 2, 1, 4, ""))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Overlapping edits")
        assertThatThrownBy { QuickFixApplier.apply("abc", listOf(TextEdit(1, 1, 1, 1, "x"), TextEdit(1, 1, 1, 1, "y"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun rejectsPositionsOutsideTheSource() {
        assertThatThrownBy { QuickFixApplier.apply("ab\ncd", listOf(TextEdit(3, 0, 3, 0, "x"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Line 3 does not exist")
        assertThatThrownBy { QuickFixApplier.apply("ab\ncd", listOf(TextEdit(1, 0, 1, 3, "x"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Line 1 has only 2 characters")
    }

    @Test
    fun verifyQuickFixes() {
        PlSqlCheckVerifier.verifyQuickFixes(QUICK_FIX_FILE, UppercaseNullCheck("NULL"), "src/test/resources/quick_fix.fixed.sql")
    }

    @Test
    fun verifyQuickFixesFailsWhenResultDiffers() {
        assertThatThrownBy {
            PlSqlCheckVerifier.verifyQuickFixes(QUICK_FIX_FILE, UppercaseNullCheck("NULL"), "src/test/resources/quick_fix.wrong.sql")
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageStartingWith("The quick fixes of $QUICK_FIX_FILE do not produce src/test/resources/quick_fix.wrong.sql")
    }

    @Test
    fun verifyQuickFixesFailsWhenIssueRemains() {
        assertThatThrownBy {
            PlSqlCheckVerifier.verifyQuickFixes(QUICK_FIX_FILE, UppercaseNullCheck("null"), QUICK_FIX_FILE)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessage("Issue with quick fix remains after applying the quick fixes at line 2 of $QUICK_FIX_FILE: \"Use uppercase\"")
    }

    private class UppercaseNullCheck(private val replacement: String) : PlSqlCheck() {
        override fun init() {
            subscribeTo(PlSqlGrammar.NULL_STATEMENT)
        }

        override fun visitNode(node: AstNode) {
            if (node.token.originalValue != "NULL") {
                addIssue(node.token, "Use uppercase").addQuickFix("Uppercase", TextEdit.replace(node.token, replacement))
            }
        }
    }

    companion object {
        private const val QUICK_FIX_FILE = "src/test/resources/quick_fix.sql"
    }
}
