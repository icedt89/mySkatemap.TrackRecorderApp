package com.janhafner.myskatemap.apps.activityrecorder.core

public final class MetDefinition(public val code: String, public val value: Float, public val name: String = "${code} (name not specified)") {
    public companion object {
        private val metDefinitions = listOf(
                MetDefinition("00000", 1.0f, "Unspecified"),
                MetDefinition(KnownMetDefinitionCodes.BIKING_GENERAL, 7.5f, "Bicyling (general)"),
                MetDefinition(KnownMetDefinitionCodes.INLINE_SKATING_GENERAL, 7.5f, "Inline Skating (general)"),
                MetDefinition(KnownMetDefinitionCodes.RUNNING_JOGGING_GENERAL, 7.0f, "Running/Jogging (general)"),
                MetDefinition("17160", 3.0f, "Walking")
        )

        private lateinit var metDefinitionsMap: Map<String, MetDefinition>

        init {
            val map = mutableMapOf<String, MetDefinition>()
            for (metDefinition in metDefinitions) {
                map[metDefinition.code] = metDefinition
            }

            this.metDefinitionsMap = map
        }

        public fun getMetDefinitionByCode(code: String) : MetDefinition? {
            // 1015 -> 01015
            val fixedCode = code.padStart(5, '0')

            if (!metDefinitionsMap.containsKey(fixedCode)) {
                return null
            }

            return metDefinitionsMap[fixedCode]
        }
    }
}

