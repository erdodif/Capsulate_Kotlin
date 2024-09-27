package com.erdodif.capsulate.lang.grammar

import com.erdodif.capsulate.assertFail
import com.erdodif.capsulate.assertPass
import com.erdodif.capsulate.lang.util.ParserState
import kotlin.test.Test

class Statement {

    @Test
    fun commentPass(){
        assertPass(ParserState("//asda//s /**/ ").parse(topLevel(pComment)))
        assertPass(ParserState("/**/").parse(topLevel(pComment)))
        assertPass(ParserState("/*asd asd*/").parse(topLevel(pComment)))
        assertPass(ParserState("/*//*/").parse(topLevel(pComment)))
        assertPass(ParserState("/*/*/").parse(topLevel(pComment)))
        assertPass(ParserState("/* ** /*/").parse(topLevel(pComment)))
        assertPass(ParserState("/*/* /*/").parse(topLevel(pComment)))
        assertPass(ParserState("//asdasdasdas\na").parse(topLevel(pComment + pVariable)))
        assertPass(ParserState("/*\nasd\nd*/ a").parse(topLevel(pComment + pVariable)))
    }

    @Test
    fun commentFail(){
        assertFail(ParserState("//asda//s /**/\nd").parse(topLevel(pComment)))
        assertFail(ParserState("/**/w").parse(topLevel(pComment)))
        assertFail(ParserState("/*asd*/asd*///").parse(topLevel(pComment)))
        assertFail(ParserState("/**/*/").parse(topLevel(pComment)))
        assertFail(ParserState("/  /").parse(topLevel(pComment)))
        assertFail(ParserState("/*  /*").parse(topLevel(pComment)))
        assertFail(ParserState("*/  */").parse(topLevel(pComment)))
        assertFail(ParserState("*/").parse(topLevel(pComment)))
        assertFail(ParserState("/*").parse(topLevel(pComment)))
        assertFail(ParserState("//asdasdasdasa").parse(topLevel(pComment + pVariable)))
        assertFail(ParserState("/*\nasd\nd a").parse(topLevel(pComment + pVariable)))
    }


}