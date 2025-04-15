package com.erdodif.capsulate.lang.program.grammar

import com.erdodif.capsulate.fail
import com.erdodif.capsulate.at
import com.erdodif.capsulate.lang.program.grammar.expression.IntLit
import com.erdodif.capsulate.lang.program.grammar.expression.pComment
import com.erdodif.capsulate.lang.program.grammar.expression.pVariable
import com.erdodif.capsulate.lang.util.Pass
import com.erdodif.capsulate.pass
import com.erdodif.capsulate.withMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StatementTest {
    private val comment = topLevel(pComment)
    private val assign = topLevel(sAssign)
    private val parallelAssign = topLevel(sParallelAssign)
    private val tWhile = topLevel(sWhile)
    private val tDoWhile = topLevel(sDoWhile)
    private val tIf = topLevel(sIf)
    private val tWhen = topLevel(sWhen)

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
        parallelAssign pass "a := 1"
        parallelAssign pass "a := a"
    }

    @Test
    fun `parallel assignment fails`(){
        parallelAssign fail " := 1"
        parallelAssign fail " a := "
    }

    @Test
    fun `if statement passes`(){
        tIf pass "if true { } else { }"
        tIf pass "if true { } else { skip }"
        tIf pass "if true {skip} else { }"
        tIf pass "if true { skip} else {skip}"
        tIf pass "if true { } else {skip;skip}"
        tIf pass "if true\n{\n}\nelse\n{\nskip\nskip\n} "
    }

    @Test
    fun `if statement fails`(){
        tIf fail "if {} else {} "
        tIf fail "if true {} { skip } "
        tIf fail "if true else {} "
        tIf fail "if "
        tIf fail "if true {} else "
    }

    @Test
    fun `while passes`(){
        tWhile pass "while 0\n{\n}"
        tWhile pass "while 0 {skip}"
    }

    @Test
    fun `do while passes`(){
        tDoWhile pass "do {} while true"
        tDoWhile pass "do\n{skip\nskip;skip}\nwhile (e)"
    }


    @Test
    fun `while fails`(){
        tWhile fail "while {} "
        tWhile fail "while 0 "
    }

    @Test
    fun `do while fails`(){
        tDoWhile fail "do {} while"
        tDoWhile fail "do while (e)"
    }

    @Test
    fun `when passes regular`() {
        tWhen pass "when{}"
        tWhen pass "when{a:skip}"
        tWhen pass "when{a:{skip}}"
        tWhen pass "when{a:{skip},b:{skip}} "
        tWhen pass "when{a:skip,b:{skip}} "
        tWhen pass "when{a:{skip},b:skip} "
        tWhen pass "when\n{\na:{skip},\nb:{}} "
    }

    @Test
    fun `when passes with trailing coma`(){
        tWhen pass "when{a:{skip},b:skip,} "
        tWhen pass "when\n{\na:{skip},\nb:{skip},}\n "
        tWhen pass "when\n{\na:{skip},\n}"
    }

    @Test
    fun `when passes with else block`(){
        tWhen pass "when{else:{skip}}"
        tWhen pass "when{a:{skip}\nelse:{skip}} "
        tWhen pass "when\n{\na:\n{skip},\nelse:{skip}} "
        tWhen pass "when\n{\na:{skip}\n,\nelse:{\nskip\n}}\n "
        tWhen pass "when\n{\na:\n{skip},\nelse:\n{skip}} "
    }

    @Test
    fun `when fails`(){
        tWhen fail "when{a:{a}}else{b} "
        tWhen fail "when{a:0}else:{b}"
        tWhen fail "when{,}"
        tWhen fail "when{a\n:{a}}"
        tWhen fail "when{a:{0},,}"
    }
}
