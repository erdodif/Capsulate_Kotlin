package com.erdodif.capsulate.presets

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import kotlinx.serialization.Serializable

@KParcelize
@Serializable
data class Demo(val code: String, val description: String) : KParcelable

@KParcelize
@Serializable
data class Preset(val headerText: String, val demos: List<Demo>) : KParcelable

val presets: Array<Preset> = arrayOf(
    Preset(
        "Common Text",
        listOf(
            Demo(
                "Proba text which will never be valid as code",
                "This is not a valid statement:"
            ),
        )
    ),
    Preset(
        "Basic Statements",
        listOf(
            Demo(
                "a := 0",
                "One of the simplest statements are the assignments."
            ),
            Demo(
                "a := [1, 2, 3]\nb := a[2] // b = 2",
                "Indexers are supported on arrays."
            ),
            Demo(
                "skip abort",
                "There are also some reserved keywords."
            ),
            Demo(
                "if true {skip} else {abort}",
                "Any kind of if statement will require an else branch."
            ),
            Demo(
                "if true {abort}",
                "Therefore an if statement without it's else branch is not considered a valid statement."
            ),
            Demo(
                "when { true: skip, false: abort}",
                "Multi branched when conditions are also supported.\n" +
                        "Note that if the else branch is omitted and none of the conditions passes, " +
                        "the evaluation will abort by definition."
            ),
            Demo(
                "while true{skip}\ndo {skip} while true",
                "While and do whiles are working as expected."
            ),
            Demo(
                "[{ skip }]",
                "Atomics are useless alone, but in parallel blocks, their evaluation cannot be" +
                        "suspended, so all their statements are guaranteed to run in " +
                        "an uninterrupted sequence."
            ),
            Demo(
                "{a:=3}|{[{a := 1; b := a}]}|{a := 2}",
                "In this example, it is clear that b will always hold to the value of 1, " +
                        "because a is set to 1 and all the other blocks are either finished by now" +
                        "or can't be evaluated because they have to wait for the atomic statement " +
                        "to finish."
            ),
            Demo(
                "await a = 1 [{ abort }]",
                "The waiting is another interesting concept. An await statement will keep " +
                        "evaluating the given condition until it is true.\n" +
                        "Once the condition passes, the inner atomic statement begins to run."
            ),
            Demo(
                "a:=2\n{ a:=1 }|{ await a = 1 [{b := a}] }|\n{ a := 3 }",
                "In this example, it is clear that b will be 1, " +
                        "because the variable a must be 1 before the wait statement lets the " +
                        "assignment kick in."
            ),
        )
    ),
)
