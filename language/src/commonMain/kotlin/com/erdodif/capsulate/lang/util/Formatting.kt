package com.erdodif.capsulate.lang.util

const val DEFAULT_NESTING = "  "

data class Formatting(val nesting: Int) {
    internal val lines: MutableList<Pair<String, Int>> = mutableListOf()

    /**
     * creates the correct amount indentation
     */
    fun getNestingSequence(indentation: Int, nesting: String): String = buildString {
        for (i in 0..<indentation) append(nesting)
    }

    /**
     * Returns the current string representation of this [Formatting]
     *
     * All indentation is
     */
    fun finalize(nesting: String = DEFAULT_NESTING): String = buildString {
        lines.map {
            append(getNestingSequence(it.second, nesting))
            append(it.first)
            append('\n')
        }
    }

    /**
     * Appends all raw content separately to the lines
     *
     * Each wod is broken at line breaks
     *
     * @return the total lineCount added to the lines (after breaks)
     */
    fun appendAll(contents: List<Pair<String, Int>>): Int {
        val newLines = contents.flatMap { line -> line.first.split("\n").map { it to line.second } }
        lines.addAll(newLines)
        return newLines.count()
    }

    /**
     * Adds the **raw** lines of the given [content] to the lines
     *
     * Unlike [print], these are not changing the previous elements
     */
    fun append(content: Pair<String, Int>): Int {
        val newLines = content.first.split("\n").map { it to content.second }
        lines.addAll(newLines)
        return newLines.count()
    }

    /**
     * Adds the lines of the given [content] to the lines
     *
     * Unlike [print], these are not changing the previous elements
     */
    fun append(content: String): Int {
        lines.addAll(content.split("\n").map { it to nesting })
        return content.count { it == '\n' }
    }


    /**
     * Prints the [preFormat]-ed results **starting at the last line**
     * with an additional level of indentation
     */
    fun indentInline(content: Formatting.() -> Int): Int {
        var count = 0
        val newLines = copy(nesting + 1).apply { count = content() }.lines.map {
            it.copy(second = it.second + 1)
        }
        if (newLines.isEmpty()) {
            return 0
        }
        if (lines.isEmpty()) {
            lines.addAll(newLines)
            return count
        }
        val last = lines.last()
        lines[lines.count() - 1] = last.copy(first = last.first + newLines.first().first)
        lines.addAll(newLines.drop(1))
        return count - 1

    }

    /**
     * Prints the [preFormat]-ed results **starting in a new line**
     * with an additional level of indentation
     */
    fun indent(content: Formatting.() -> Int): Int {
        var count = 0
        lines.addAll(copy(nesting + 1).apply {
            count = content()
        }.lines.map { it.copy(second = it.second + 1) })
        return count
    }

    /**
     * Appends the given [content] to the last line until the first line break
     *
     * After each line break, the new line is appended to the lines
     */
    fun print(content: String): Int {
        val items = content.split("\n")
        if (items.isEmpty()) {
            return 0
        }
        if (lines.isEmpty()) {
            lines.addAll(items.map { it to nesting })
            return 1
        }
        val last = lines.last()
        lines[lines.count() - 1] = last.copy(first = last.first + items.first())
        lines.addAll(items.drop(1).map { it to nesting })
        return items.count() - 1
    }

    /**
     * Break line between each [itemContent] call and appends them to the lines
     */
    fun <T> List<T>.fencedForEach(itemContent: Formatting.(T) -> Int): Int {
        var i = 0
        var lines = 0
        while (i < count() - 1) {
            lines += itemContent(get(i)) + 1
            breakLine()
            i++
        }
        if (isNotEmpty()) {
            lines += itemContent(last())
        }
        return lines
    }

    /**
     * Opens a new empty line keeping the current indentation
     */
    fun breakLine() {
        lines.add("" to nesting)
    }

    /**
     * Prints the given [content] and breaks the line afterwards
     */
    fun printLine(content: String): Int {
        val lines = print(content) + 1
        breakLine()
        return lines
    }

    /**
     * Calls in a temporal [Formatting] instance, so modifications
     * and checks can be done before adding to the lines
     */
    fun preFormat(content: Formatting.() -> Int): List<Pair<String, Int>> {
        val tempFormat = Formatting(nesting)
        tempFormat.content()
        return tempFormat.lines
    }

    /**
     * Inserts a comment to indicate that formatting has been called on a prohibited object,
     * which isn't clearly from any source code, so that they should not be formatted with source code
     *
     * @see com.erdodif.capsulate.lang.program.evaluation.EvalSequence
     */
    fun error(content: String): Int = print("/* FORMAT ERROR: $content */")

}
