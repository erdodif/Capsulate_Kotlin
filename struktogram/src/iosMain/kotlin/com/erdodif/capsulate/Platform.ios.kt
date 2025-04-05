@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.erdodif.capsulate

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class KIgnoredOnParcel actual constructor()
actual interface KParcelable
actual interface KParceler<T>

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
actual annotation class KTypeParceler<T, R : KParceler<in T>>