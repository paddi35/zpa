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

import com.felipebz.zpa.api.checks.QuickFix
import com.felipebz.zpa.api.checks.TextEdit

/**
 * Applies [TextEdit]s to a source text. Lines may be separated by `\n`, `\r\n` or `\r`, like in the lexer.
 */
object QuickFixApplier {

    /** Applies all edits of [quickFix] to [source] and returns the new text. */
    @JvmStatic
    fun apply(source: String, quickFix: QuickFix): String = apply(source, quickFix.edits())

    /**
     * Applies [edits] to [source] and returns the new text. All edits refer to [source]; they must not overlap
     * and may come from different quick fixes.
     */
    @JvmStatic
    fun apply(source: String, edits: List<TextEdit>): String {
        val lineStarts = lineStarts(source)
        val ranges = edits
            .map { Triple(offset(source, lineStarts, it.startLine(), it.startLineOffset(), it),
                offset(source, lineStarts, it.endLine(), it.endLineOffset(), it), it) }
            .sortedWith(compareBy({ it.first }, { it.second }))

        for (i in 1 until ranges.size) {
            val previous = ranges[i - 1]
            val current = ranges[i]
            if (previous.second > current.first || (previous.first == current.first && previous.second == current.second &&
                    previous.first == previous.second)) {
                throw IllegalArgumentException("Overlapping edits: ${previous.third} and ${current.third}")
            }
        }

        val result = StringBuilder(source)
        for ((start, end, edit) in ranges.asReversed()) {
            result.replace(start, end, edit.text())
        }
        return result.toString()
    }

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
