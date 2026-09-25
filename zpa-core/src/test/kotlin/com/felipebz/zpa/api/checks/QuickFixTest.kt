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

import com.felipebz.flr.api.AstNode
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.checks.IssueLocation
import com.felipebz.zpa.parser.PlSqlParser
import com.felipebz.zpa.squid.PlSqlConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class QuickFixTest {

    private val root: AstNode = PlSqlParser.create(PlSqlConfiguration(StandardCharsets.UTF_8)).parse(
        "begin\n" +
            "  x := (a <> b);\n" +
            "  y := 'multi\n" +
            "line';\n" +
            "end;\n"
    )

    private val operator = root.getFirstDescendant(PlSqlGrammar.NOTEQUALS_OPERATOR)!!
    private val literal = root.getFirstDescendant(PlSqlGrammar.CHARACTER_LITERAL)!!

    @Test
    fun replaceNodeUsesTheCoordinatesOfIssueLocation() {
        val edit = TextEdit.replace(operator, "!=")
        assertThat(edit).isEqualTo(TextEdit(2, 10, 2, 12, "!="))
        assertThat(edit.isInsertion()).isFalse()

        val location = IssueLocation.preciseLocation(operator, "")
        assertThat(listOf(edit.startLine(), edit.startLineOffset(), edit.endLine(), edit.endLineOffset()))
            .containsExactly(location.startLine(), location.startLineOffset(), location.endLine(), location.endLineOffset())
    }

    @Test
    fun replaceMultiLineToken() {
        assertThat(TextEdit.replace(literal.token, "'x'")).isEqualTo(TextEdit(3, 7, 4, 5, "'x'"))
        assertThat(TextEdit.replace(literal, "'x'")).isEqualTo(TextEdit(3, 7, 4, 5, "'x'"))
    }

    @Test
    fun replaceFromStartNodeToEndNode() {
        val comparison = operator.getFirstAncestor(PlSqlGrammar.COMPARISON_EXPRESSION)!!
        assertThat(TextEdit.replace(comparison.firstChild, comparison.lastChild, "c"))
            .isEqualTo(TextEdit(2, 8, 2, 14, "c"))
        assertThat(TextEdit.remove(comparison.firstChild, operator)).isEqualTo(TextEdit(2, 8, 2, 12, ""))
    }

    @Test
    fun remove() {
        assertThat(TextEdit.remove(operator)).isEqualTo(TextEdit(2, 10, 2, 12, ""))
        assertThat(TextEdit.remove(operator.lastToken)).isEqualTo(TextEdit(2, 11, 2, 12, ""))
    }

    @Test
    fun insertBeforeAndAfter() {
        assertThat(TextEdit.insertBefore(operator, "x")).isEqualTo(TextEdit(2, 10, 2, 10, "x"))
        assertThat(TextEdit.insertBefore(operator.lastToken, "x")).isEqualTo(TextEdit(2, 11, 2, 11, "x"))
        assertThat(TextEdit.insertAfter(operator, "x")).isEqualTo(TextEdit(2, 12, 2, 12, "x"))
        assertThat(TextEdit.insertAfter(literal.token, " || 'y'")).isEqualTo(TextEdit(4, 5, 4, 5, " || 'y'"))
        assertThat(TextEdit.insertAfter(literal, "x").isInsertion()).isTrue()
    }

    @Test
    fun rejectsInvalidRanges() {
        assertThatThrownBy { TextEdit(0, 0, 1, 0, "") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TextEdit(1, -1, 1, 0, "") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TextEdit(2, 0, 1, 5, "") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { TextEdit(1, 5, 1, 4, "") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun equalsHashCodeAndToString() {
        val edit = TextEdit(1, 2, 3, 4, "x")
        assertThat(edit).isEqualTo(TextEdit(1, 2, 3, 4, "x")).hasSameHashCodeAs(TextEdit(1, 2, 3, 4, "x"))
        assertThat(edit).isNotEqualTo(TextEdit(1, 2, 3, 4, "y"))
        assertThat(edit.toString()).isEqualTo("TextEdit(1:2-3:4, text=\"x\")")
    }

    @Test
    fun quickFixKeepsMessageAndEdits() {
        val first = TextEdit(1, 0, 1, 2, "a")
        val second = TextEdit(1, 2, 1, 2, "b")
        val fix = QuickFix("Fix it", second, first)
        assertThat(fix.message()).isEqualTo("Fix it")
        assertThat(fix.edits()).containsExactly(second, first)
        assertThat(QuickFix("Fix it", listOf(first)).edits()).containsExactly(first)
    }

    @Test
    fun quickFixRejectsInvalidEdits() {
        assertThatThrownBy { QuickFix("Nothing") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { QuickFix("Overlap", TextEdit(1, 0, 1, 3, ""), TextEdit(1, 2, 2, 0, "")) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { QuickFix("Same position", TextEdit(1, 1, 1, 1, "a"), TextEdit(1, 1, 1, 1, "b")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun issuesHaveQuickFixes() {
        val check = object : PlSqlCheck() {}
        val issue = check.addIssue(operator, "Use !=")
        assertThat(issue.quickFixes()).isEmpty()

        val replace = QuickFix("Replace with !=", TextEdit.replace(operator, "!="))
        issue.addQuickFix(replace).addQuickFix("Replace with ^=", TextEdit.replace(operator, "^="))

        assertThat(issue.quickFixes()).hasSize(2)
        assertThat(issue.quickFixes()[0]).isSameAs(replace)
        assertThat(issue.quickFixes()[1].message()).isEqualTo("Replace with ^=")
        assertThat(check.issues().single().quickFixes()).hasSize(2)
    }
}
