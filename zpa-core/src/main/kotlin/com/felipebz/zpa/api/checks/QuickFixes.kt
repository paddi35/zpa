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
import java.util.function.Function

/**
 * Applies [QuickFix]es to a source text and chooses which quick fixes of different issues can be applied together.
 *
 * The source is the text that was analyzed: lines may be separated by `\n`, `\r\n` or `\r`, like in the lexer, and a
 * leading byte order mark must already be removed (columns on the first line are counted without it).
 */
object QuickFixes {

    /** Applies all edits of [quickFix] to [source] and returns the new text. */
    @JvmStatic
    fun apply(source: String, quickFix: QuickFix): String = apply(source, quickFix.edits())

    /**
     * Applies [edits] to [source] and returns the new text. All edits refer to [source]; they must not overlap and may
     * come from different quick fixes (see [selectNonOverlapping]). An insertion may touch another edit; two insertions
     * at the same position are rejected because their order would be ambiguous.
     *
     * @throws IllegalArgumentException if two edits overlap or an edit lies outside [source]
     */
    @JvmStatic
    fun apply(source: String, edits: List<TextEdit>): String {
        val lineStarts = lineStarts(source)
        val ranges = edits
            .map { EditRange(offset(source, lineStarts, it.startLine(), it.startLineOffset(), it),
                offset(source, lineStarts, it.endLine(), it.endLineOffset(), it), it) }
            .sortedWith(compareBy({ it.start }, { it.end }))

        for (i in 1 until ranges.size) {
            val previous = ranges[i - 1]
            val current = ranges[i]
            if (previous.end > current.start || (previous.start == current.start && previous.end == current.end &&
                    previous.start == previous.end)) {
                throw IllegalArgumentException("Overlapping edits: ${previous.edit} and ${current.edit}")
            }
        }

        val result = StringBuilder(source)
        for (range in ranges.asReversed()) {
            result.replace(range.start, range.end, range.edit.text())
        }
        return result.toString()
    }

    /**
     * Returns true if two edits of different quick fixes cannot be applied together: they share at least one
     * character, or one of them is an insertion inside or at either end of the other (the order of the texts would be
     * ambiguous). Ranges that merely touch (`[a,b)` and `[b,c)`) do not conflict.
     */
    @JvmStatic
    fun conflict(a: TextEdit, b: TextEdit): Boolean {
        if (a.isInsertion() || b.isInsertion()) {
            val (point, other) = if (a.isInsertion()) a to b else b to a
            return !isBefore(point.startLine(), point.startLineOffset(), other.startLine(), other.startLineOffset()) &&
                !isBefore(other.endLine(), other.endLineOffset(), point.startLine(), point.startLineOffset())
        }
        return isBefore(a.startLine(), a.startLineOffset(), b.endLine(), b.endLineOffset()) &&
            isBefore(b.startLine(), b.startLineOffset(), a.endLine(), a.endLineOffset())
    }

    /** Returns true if any edit of [a] conflicts with any edit of [b] (see [conflict]). */
    @JvmStatic
    fun conflict(a: QuickFix, b: QuickFix): Boolean =
        a.edits().any { ea -> b.edits().any { eb -> conflict(ea, eb) } }

    /**
     * Chooses the quick fixes that can be applied together, e.g. to fix all issues of a file at once.
     *
     * [candidates] are, for example, the issues of one file; [quickFix] gives the quick fix to apply for each one
     * (usually the first, preferred quick fix of the issue) and [severity] the severity of its issue. The candidates
     * are visited in document order of the start of their quick fix (the earliest edit); a candidate is skipped if its
     * quick fix [conflicts][conflict] with one chosen before. Quick fixes that start at the same position are visited
     * by severity (most severe first), then in input order. For `x <> NULL`, for example, the "IS NOT NULL" fix of a
     * BLOCKER issue wins over the "!=" fix of a MAJOR issue on the same `<>`. A skipped issue usually gets a new quick
     * fix when the fixed source is analyzed again.
     *
     * @return the chosen candidates, in the order they were visited
     */
    @JvmStatic
    fun <T> selectNonOverlapping(
        candidates: List<T>,
        quickFix: Function<in T, QuickFix>,
        severity: Function<in T, Priority>
    ): List<T> {
        val ordered = candidates
            .mapIndexed { index, candidate -> Candidate(candidate, quickFix.apply(candidate), severity.apply(candidate), index) }
            .sortedWith(
                compareBy<Candidate<T>>({ it.firstEdit.startLine() }, { it.firstEdit.startLineOffset() })
                    .thenByDescending { it.severity.ordinal }
                    .thenBy { it.index }
            )
        val selected = mutableListOf<Candidate<T>>()
        for (candidate in ordered) {
            if (selected.none { conflict(it.quickFix, candidate.quickFix) }) {
                selected.add(candidate)
            }
        }
        return selected.map { it.value }
    }

    private class Candidate<T>(val value: T, val quickFix: QuickFix, val severity: Priority, val index: Int) {
        val firstEdit: TextEdit = quickFix.edits().minWith(compareBy({ it.startLine() }, { it.startLineOffset() }))
    }

    private class EditRange(val start: Int, val end: Int, val edit: TextEdit)

    private fun isBefore(aLine: Int, aOffset: Int, bLine: Int, bOffset: Int) =
        aLine < bLine || (aLine == bLine && aOffset < bOffset)

    private fun lineStarts(source: String): List<Int> {
        val starts = mutableListOf(0)
        var i = 0
        while (i < source.length) {
            val c = source[i]
            if (c == '\r' && i + 1 < source.length && source[i + 1] == '\n') {
                i++
            }
            if (c == '\n' || c == '\r') {
                starts.add(i + 1)
            }
            i++
        }
        return starts
    }

    private fun offset(source: String, lineStarts: List<Int>, line: Int, lineOffset: Int, edit: TextEdit): Int {
        if (line > lineStarts.size) {
            throw IllegalArgumentException("Line $line does not exist in the source: $edit")
        }
        val lineStart = lineStarts[line - 1]
        var lineEnd = if (line < lineStarts.size) lineStarts[line] else source.length
        while (lineEnd > lineStart && (source[lineEnd - 1] == '\n' || source[lineEnd - 1] == '\r')) {
            lineEnd--
        }
        if (lineStart + lineOffset > lineEnd) {
            throw IllegalArgumentException("Line $line has only ${lineEnd - lineStart} characters: $edit")
        }
        return lineStart + lineOffset
    }
}
