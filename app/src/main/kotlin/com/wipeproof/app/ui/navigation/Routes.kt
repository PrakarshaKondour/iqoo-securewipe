package com.wipeproof.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val IDENTIFY = "identify"
    const val ASSESS = "assess/{caseId}"
    const val SANITIZE = "sanitize/{caseId}"
    const val VERIFY = "verify/{caseId}"
    const val PROVE = "prove/{caseId}"
    const val HANDOVER = "handover/{caseId}"
    const val SCANNER = "scanner"
    const val HISTORY = "history"
    const val DEMO = "demo"

    fun assess(caseId: String) = "assess/$caseId"
    fun sanitize(caseId: String) = "sanitize/$caseId"
    fun verify(caseId: String) = "verify/$caseId"
    fun prove(caseId: String) = "prove/$caseId"
    fun handover(caseId: String) = "handover/$caseId"
}
