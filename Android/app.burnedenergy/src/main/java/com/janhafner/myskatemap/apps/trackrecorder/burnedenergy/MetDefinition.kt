package com.janhafner.myskatemap.apps.trackrecorder.burnedenergy

public final class MetDefinition(public val code: String, public val value: Float, public val name: String = "${code} (name not specified)") {
    public companion object {
        private val metDefinitions: Map<String, MetDefinition> = mapOf(
                Pair("01015", MetDefinition("01015", 7.5f, "Bicyling (general)")),
                Pair("15591", MetDefinition("15591", 7.5f, "Inline Skating (general)")),
                Pair("17160", MetDefinition("17160", 3.0f, "Walking"))
        )

        public fun getMetDefinitionByCode(code: String) : MetDefinition? {
            // 1015 -> 01015
            val fixedCode = code.padStart(5, '0')

            if (!this.metDefinitions.containsKey(fixedCode)) {
                return null
            }

            return this.metDefinitions[fixedCode]!!
        }
    }
}