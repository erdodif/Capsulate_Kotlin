package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.fail
import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pComment
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.matches
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StatementTest {
    private val comment = topLevel(pComment)
    private val assign = topLevel(sAssign)
    private val `parallel assign` = topLevel(sParallelAssign)
    private val `while` = topLevel(sWhile)
    private val `do while` = topLevel(sDoWhile)
    private val `if` = topLevel(sIf)
    private val `when` = topLevel(sWhen)

    @Test
    fun `comment passes single line`(){
        comment pass "//asda//s /**/ "
        topLevel(pComment + pVariable) pass "//asdasdasdas\na"
    }

    @Test
    fun `comment passes multi line`(){
        comment pass "/**/" at 4
        comment pass "/*asd asd*/"
        comment pass "/*//*/"
        comment pass "/*/*/"
        comment pass "/* ** /*/"
        comment pass "/*/* /*/"
        topLevel(pComment + pVariable) pass "/*\nasd\nd*/ a"
    }

    @Test
    fun `comment fails`(){
        comment fail "//asda//s /**/\nd" at 15
        comment fail "/**/w" at 4
        comment fail "/*asd*/asd*///"
        comment fail "/**/*/" at 4
        comment fail "/  /" at 2
        comment fail "/*  /*" at 6
        comment fail "*/  */" at 1
        comment fail "*/" at 1
        comment fail "/*" at 2
        topLevel(pComment + pVariable) fail "//asdasdasdasa"
        topLevel(pComment + pVariable) fail "/*\nasd\nd a"
    }

    @Test
    fun `assignment passes`(){
        with(assign pass "a := 1"  withMatch (0 to 6) at 6) {
            assertIs<Pass<*>>(this)
            assertIs<Assign>(this.value)
            assertIs<IntLit>(this.value.value)
            assertEquals(5, this.value.value.match.start)
            assertEquals(6, this.value.value.match.end)
        }
        assign pass "a := a" withMatch (0 to 6) at 6
    }

    @Test
    fun `assignment fails`(){
        assign fail " := 1" at 1
        assign fail " a := " at 6
    }

    @Test
    fun `parallel assignment passes`(){
        `parallel assign` pass "a := 1"
        `parallel assign` pass "a := a"
    }

    @Test
    fun `parallel assignment fails`(){
        `parallel assign` fail " := 1"
        `parallel assign` fail " a := "
    }

    @Test
    fun `if statement passes`(){
        `if` pass "if true { } else { }"
        `if` pass "if true { } else { skip }"
        `if` pass "if true {skip} else { }"
        `if` pass "if true { skip} else {skip}"
        `if` pass "if true { } else {skip;skip}"
        `if` pass "if true\n{\n}\nelse\n{\nskip\nskip\n} "
    }

    @Test
    fun `if statement fails`(){
        `if` fail "if {} else {} "
        `if` fail "if true {} { skip } "
        `if` fail "if true else {} "
        `if` fail "if "
        `if` fail "if true {} else "
    }

    @Test
    fun `while passes`(){
        `while` pass "while 0\n{\n}"
        `while` pass "while 0 {skip}"
    }

    @Test
    fun `do while passes`(){
        `do while` pass "do {} while true"
        `do while` pass "do\n{skip\nskip;skip}\nwhile (e)"
    }


    @Test
    fun `while fails`(){
        `while` fail "while {} "
        `while` fail "while 0 "
    }

    @Test
    fun `do while fails`(){
        `do while` fail "do {} while"
        `do while` fail "do while (e)"
    }

    @Test
    fun `when passes regular`() {
        `when` pass "when{}"
        `when` pass "when{a:a}"
        `when` pass "when{a:{a}}"
        `when` pass "when{a:{a},b:{b}} "
        `when` pass "when{a:a,b:{b}} "
        `when` pass "when{a:{a},b:b} "
        `when` pass "when\n{\na:{a},\nb:{}} "
    }

    @Test
    fun `when passes with trailing coma`(){
        `when` pass "when{a:{a},b:b,} "
        `when` pass "when\n{\na:{a},\nb:{d},}\n "
        `when` pass "when\n{\na:{a},\n}"
    }

    @Test
    fun `when passes with else block`(){
        `when` pass "when{else:{b}}"
        `when` pass "when{a:{a}\nelse:{b}} "
        `when` pass "when\n{\na:\n{a},\nelse:{d}} "
        `when` pass "when\n{\na:{a}\n,\nelse:{\nd\n}}\n "
        `when` pass "when\n{\na:\n{a},\nelse:\n{d}} "
    }

    @Test
    fun `when fails`(){
        `when` fail "when{a:{a}}else{b} "
        `when` fail "when{a:0}else:{b}"
        `when` fail "when{,}"
        `when` fail "when{a\n:{a}}"
        `when` fail "when{a:{0},,}"
    }
}