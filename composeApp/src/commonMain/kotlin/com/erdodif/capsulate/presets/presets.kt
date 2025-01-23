package com.erdodif.capsulate.presets

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize

@KParcelize
data class Demo(val code: String, val description: String) : KParcelable

@KParcelize
data class Preset(val headerText: String, val demos: Array<Demo>) : KParcelable

val presets: Array<Preset> = arrayOf(
    Preset(
        "Common Text",
        arrayOf(
            Demo(
                "Proba text which will never be valid as code",
                "This is not a valid statement:"
            ),
        )
    ),
    Preset(
        "Basic Statements",
        arrayOf(
            Demo(
                "0",
                "One of the simplest are expressions"
            ),
            Demo(
                "skip abort",
                "There are also some reserved keywords"
            ),
            Demo(
                "if true {skip} else {abort}",
                "Any kind of if statement will require an else branch"
            ),
            Demo(
                "if true {abort}",
                "Therefore this is not considered a valid statement"
            ),
            Demo(
                "while true{skip}\ndo {skip} while true",
                "while and do whiles are working as usual"
            ),
        )
    )
)