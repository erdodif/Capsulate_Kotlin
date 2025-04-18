package com.erdodif.capsulate.lang.program.grammar

const val whiteSpaceChars = " \t"

const val lineBreak = "\n\r"
const val lineEnd = "\n\r;"

const val reservedChars = "$()[]{}|&,.:!?=+-*/\"\'¬<>≤≥∧∨"

val keywords = arrayOf(
    "true",
    "false",
    "not",
    "is",
    "if",
    "else",
    "when",
    "do",
    "for",
    "while",
    "skip",
    "abort",
    "await",
    "return",
    "function",
    "method",
    "program"
)

