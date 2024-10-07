package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.fail
import com.erdodif.capsulate.pass
import kotlin.test.Test

class StatementTest {
    private val comment = topLevel(pComment)
    private val assign = topLevel(sAssign)
    private val `parallel assign` = topLevel(sParallelAssign)
    private val `while` = topLevel(sWhile)
    private val `do while` = topLevel(sDoWhile)
    private val `if` = topLevel(sIf)

    @Test
    fun `comment pass`(){
        comment pass "//asda//s /**/ "
        comment pass "/**/"
        comment pass "/*asd asd*/"
        comment pass "/*//*/"
        comment pass "/*/*/"
        comment pass "/* ** /*/"
        comment pass "/*/* /*/"
        topLevel(pComment + pVariable) pass "//asdasdasdas\na"
        topLevel(pComment + pVariable) pass "/*\nasd\nd*/ a"
    }

    @Test
    fun `comment fails`(){
        comment fail "//asda//s /**/\nd"
        comment fail "/**/w"
        comment fail "/*asd*/asd*///"
        comment fail "/**/*/"
        comment fail "/  /"
        comment fail "/*  /*"
        comment fail "*/  */"
        comment fail "*/"
        comment fail "/*"
        topLevel(pComment + pVariable) fail "//asdasdasdasa"
        topLevel(pComment + pVariable) fail "/*\nasd\nd a"
    }

    @Test
    fun `assignment pass`(){
        assign pass "a := 1"
        assign pass "a := a"
    }

    @Test
    fun `assignment fail`(){
        assign fail " := 1"
        assign fail " a := "
    }

    @Test
    fun `parallel assignment pass`(){
        `parallel assign` pass "a := 1"
        `parallel assign` pass "a := a"
    }

    @Test
    fun `parallel assignment fail`(){
        `parallel assign` fail " := 1"
        `parallel assign` fail " a := "
    }

    @Test
    fun `if statement pass`(){
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
    fun `while pass`(){
        `while` pass "while 0 {}"
        `while` pass "while 0 {skip}"
    }

    @Test
    fun `do while pass`(){
        `do while` pass "do {} while true"
        `do while` pass "do {skip\nskip;skip} while (e)"
    }


    @Test
    fun `while fail`(){
        `while` pass "while {} "
        `while` pass "while 0 "
    }

    @Test
    fun `do while fail`(){
        `do while` pass "do {} while"
        `do while` pass "do while (e)"
    }

}