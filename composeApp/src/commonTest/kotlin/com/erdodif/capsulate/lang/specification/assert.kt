package com.erdodif.capsulate.lang.specification

import com.erdodif.capsulate.lang.specification.coc.Assumption
import com.erdodif.capsulate.lang.specification.coc.Sort
import com.erdodif.capsulate.lang.specification.coc.Variable
import com.erdodif.capsulate.lang.specification.coc.context.Context
import com.erdodif.capsulate.lang.specification.coc.context.LocalContext
import com.erdodif.capsulate.lang.specification.coc.norm
import com.erdodif.capsulate.lang.util.Pass
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

data class AssertContext(val context :Context) {

    private val Context.print: String
        get() = if (context is LocalContext) "local context:\n$context" else "global environment:\n$context"

    infix fun <T : Sort> Pass<T>.withType(other: Sort): Pass<T> {
        assertIs<Variable>(
            this.value,
            "Given ${this.value} is not a variable in ${context.print}"
        )
        val variable = (context[(this.value as Variable).name] as Variable)
        with(context) {
            assertTrue("Type mismatch for ($variable), does not have type $other in ${context.print}") {
                variable.hasType(other)
            }
        }
        return this
    }

    infix fun <T : Sort> Pass<T>.subTypeOf(other: Sort): Pass<T> {
        assertIs<Variable>(
            this.value,
            "Given ${this.value} is not a variable in the ${context.print}"
        )
        val variable = (context[(this.value as Variable).name] as Variable)
        with(context) {
            assertTrue("Type mismatch for ($variable), not subtype of $other in ${context.print}") {
                norm(variable).hasType(norm(other))
            }
        }
        return this
    }

    infix fun <T : Sort> Pass<T>.withTypeLabel(other: String): Pass<T> {
        assertIs<Variable>(
            this.value,
            "Given ${this.value} is not a variable in ${context.print}"
        )
        assertNotNull(
            context[(this.value as Variable).name],
            "Given ${this.value} has no value in ${context.print}"
        )
        val variable = (context[(this.value as Variable).name] as Variable)
        assertEquals(
            context[other], variable.type,
            "${
                if (variable is Assumption) "Assumption" else "Definition"
            } ($variable) does not match on type with ${context[other]} in ${context.print}"
        )
        return this
    }


}

/**
 * Takes the given [context] and lets type safe assertions to be performed within the given [block]
 *
 * The result of [block] is returned in the end
 */
fun <T> assertWithContext(context: Context, block: AssertContext.() -> T) {
    AssertContext(context).block()
}
