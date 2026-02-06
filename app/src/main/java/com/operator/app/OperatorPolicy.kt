package com.operator.app

data class OperatorPolicy(
    val allowedPackages: Set<String>,
    val allowedAutoPostResourceIds: Set<String>
) {
    fun isPackageAllowed(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return allowedPackages.contains(packageName)
    }

    fun isAutoPostAllowed(resourceId: String?): Boolean {
        if (resourceId.isNullOrBlank()) return false
        return allowedAutoPostResourceIds.contains(resourceId)
    }

    companion object {
        fun default(appPackage: String): OperatorPolicy {
            return OperatorPolicy(
                allowedPackages = setOf(appPackage, "com.openai.chatgpt"),
                allowedAutoPostResourceIds = emptySet()
            )
        }
    }
}
