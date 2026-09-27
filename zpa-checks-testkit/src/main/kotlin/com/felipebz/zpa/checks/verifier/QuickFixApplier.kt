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
import com.felipebz.zpa.api.checks.QuickFixes
import com.felipebz.zpa.api.checks.TextEdit

/**
 * Applies [TextEdit]s to a source text. Lines may be separated by `\n`, `\r\n` or `\r`, like in the lexer.
 *
 * Kept for compatibility; delegates to [QuickFixes] in zpa-core.
 */
object QuickFixApplier {

    /** Applies all edits of [quickFix] to [source] and returns the new text. */
    @JvmStatic
    fun apply(source: String, quickFix: QuickFix): String = QuickFixes.apply(source, quickFix)

    /**
     * Applies [edits] to [source] and returns the new text. All edits refer to [source]; they must not overlap
     * and may come from different quick fixes.
     */
    @JvmStatic
    fun apply(source: String, edits: List<TextEdit>): String = QuickFixes.apply(source, edits)
}
