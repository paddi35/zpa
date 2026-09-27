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
package com.felipebz.zpa.api.checks

import com.felipebz.zpa.api.annotations.Priority
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class QuickFixesTest {

    @Test
    fun replaceInsertAndDeleteOnOneLine() {
        val source = "x := (a <> b);"
        val edits = listOf(
            TextEdit(1, 8, 1, 10, "!="),
            TextEdit(1, 5, 1, 5, "not "),
            TextEdit(1, 13, 1, 14, "")
        )
        assertThat(QuickFixes.apply(source, edits)).isEqualTo("x := not (a != b)")
    }

    @Test
    fun editsSpanningLinesWithAnyLineSeparator() {
        val lf = "a\nbc\nd"
        assertThat(QuickFixes.apply(lf, listOf(TextEdit(1, 1, 3, 0, "-")))).isEqualTo("a-d")
        val crlf = "a\r\nbc\r\nd"
        assertThat(QuickFixes.apply(crlf, listOf(TextEdit(2, 1, 2, 2, "x")))).isEqualTo("a\r\nbx\r\nd")
        assertThat(QuickFixes.apply(crlf, listOf(TextEdit(1, 1, 3, 0, "")))).isEqualTo("ad")
        val cr = "a\rbc\rd"
        assertThat(QuickFixes.apply(cr, listOf(TextEdit(3, 1, 3, 1, "e")))).isEqualTo("a\rbc\rde")
        val trailing = "a\r\n"
        assertThat(QuickFixes.apply(trailing, listOf(TextEdit(2, 0, 2, 0, "b")))).isEqualTo("a\r\nb")
    }

    @Test
    fun insertionsTouchingAnotherEdit() {
        // a deletion followed by an insertion at its end, and an insertion at the start of a replacement
        assertThat(QuickFixes.apply("NULL = b", listOf(TextEdit(1, 0, 1, 7, ""), TextEdit(1, 8, 1, 8, " IS NULL"))))
            .isEqualTo("b IS NULL")
        assertThat(QuickFixes.apply("ab", listOf(TextEdit(1, 0, 1, 1, "x"), TextEdit(1, 0, 1, 0, "y"))))
            .isEqualTo("yxb")
    }

    @Test
    fun applyQuickFix() {
        val fix = QuickFix("Swap", TextEdit(1, 0, 1, 1, "b"), TextEdit(1, 2, 1, 3, "a"))
        assertThat(QuickFixes.apply("a b", fix)).isEqualTo("b a")
    }

    @Test
    fun rejectsOverlappingEdits() {
        assertThatThrownBy { QuickFixes.apply("abcdef", listOf(TextEdit(1, 0, 1, 3, ""), TextEdit(1, 2, 1, 4, ""))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Overlapping edits")
        assertThatThrownBy { QuickFixes.apply("abc", listOf(TextEdit(1, 1, 1, 1, "x"), TextEdit(1, 1, 1, 1, "y"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun rejectsPositionsOutsideTheSource() {
        assertThatThrownBy { QuickFixes.apply("ab\ncd", listOf(TextEdit(3, 0, 3, 0, "x"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Line 3 does not exist")
        assertThatThrownBy { QuickFixes.apply("ab\ncd", listOf(TextEdit(1, 0, 1, 3, "x"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Line 1 has only 2 characters")
        assertThatThrownBy { QuickFixes.apply("ab\r\ncd", listOf(TextEdit(1, 3, 1, 3, "x"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageStartingWith("Line 1 has only 2 characters")
    }

    @Test
    fun conflictingEdits() {
        // sharing characters
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 1, 3, ""), TextEdit(1, 2, 1, 4, ""))).isTrue()
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 3, 0, ""), TextEdit(2, 0, 2, 1, ""))).isTrue()
        // touching ranges
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 1, 2, ""), TextEdit(1, 2, 1, 4, ""))).isFalse()
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 1, 2, ""), TextEdit(2, 0, 2, 4, ""))).isFalse()
        // insertions inside, at either end of, or outside a range
        assertThat(QuickFixes.conflict(TextEdit(1, 1, 1, 1, "x"), TextEdit(1, 0, 1, 2, ""))).isTrue()
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 1, 0, "x"), TextEdit(1, 0, 1, 2, ""))).isTrue()
        assertThat(QuickFixes.conflict(TextEdit(1, 0, 1, 2, ""), TextEdit(1, 2, 1, 2, "x"))).isTrue()
        assertThat(QuickFixes.conflict(TextEdit(1, 3, 1, 3, "x"), TextEdit(1, 0, 1, 2, ""))).isFalse()
        // insertions at the same or different positions
        assertThat(QuickFixes.conflict(TextEdit(1, 1, 1, 1, "x"), TextEdit(1, 1, 1, 1, "y"))).isTrue()
        assertThat(QuickFixes.conflict(TextEdit(1, 1, 1, 1, "x"), TextEdit(1, 2, 1, 2, "y"))).isFalse()

        val swap = QuickFix("Swap", TextEdit(1, 0, 1, 1, "b"), TextEdit(1, 4, 1, 5, "a"))
        assertThat(QuickFixes.conflict(swap, QuickFix("Other", TextEdit(1, 4, 1, 4, "x")))).isTrue()
        assertThat(QuickFixes.conflict(swap, QuickFix("Other", TextEdit(1, 2, 1, 3, "x")))).isFalse()
    }

    private data class Issue(val name: String, val severity: Priority, val fix: QuickFix)

    private fun select(vararg issues: Issue): List<String> =
        QuickFixes.selectNonOverlapping(issues.toList(), { it.fix }, { it.severity }).map { it.name }

    @Test
    fun selectsFixesInDocumentOrderAndSkipsConflicts() {
        val first = Issue("first", Priority.INFO, QuickFix("a", TextEdit(1, 0, 1, 4, "x")))
        val overlapping = Issue("overlapping", Priority.BLOCKER, QuickFix("b", TextEdit(1, 2, 1, 6, "y")))
        val later = Issue("later", Priority.MINOR, QuickFix("c", TextEdit(2, 0, 2, 1, "z")))
        assertThat(select(later, overlapping, first)).containsExactly("first", "later")
    }

    @Test
    fun theMostSevereFixWinsAtTheSamePosition() {
        // x <> NULL: InequalityUsage (MAJOR) "<>" -> "!=", ComparisonWithNull (BLOCKER) "<> NULL" -> "IS NOT NULL"
        val inequality = Issue("inequality", Priority.MAJOR, QuickFix("!=", TextEdit(1, 2, 1, 4, "!=")))
        val notNull = Issue("notNull", Priority.BLOCKER, QuickFix("IS NOT NULL", TextEdit(1, 2, 1, 9, "IS NOT NULL")))
        assertThat(select(inequality, notNull)).containsExactly("notNull")
        assertThat(select(notNull, inequality)).containsExactly("notNull")

        // same severity: the input order decides
        val other = Issue("other", Priority.MAJOR, QuickFix("?", TextEdit(1, 2, 1, 3, "?")))
        assertThat(select(inequality, other)).containsExactly("inequality")
        assertThat(select(other, inequality)).containsExactly("other")
    }

    @Test
    fun theEarliestEditOfAFixDecidesItsPosition() {
        // edits are not required to be sorted: the second fix starts at 1:0, before the first one
        val late = Issue("late", Priority.BLOCKER, QuickFix("late", TextEdit(1, 5, 1, 6, "")))
        val early = Issue("early", Priority.INFO, QuickFix("early", TextEdit(1, 8, 1, 9, ""), TextEdit(1, 0, 1, 5, "")))
        assertThat(select(late, early)).containsExactly("early", "late")
        val result = QuickFixes.apply("abcdefghij", listOf(late, early).flatMap { it.fix.edits() })
        assertThat(result).isEqualTo("ghj")
    }
}
