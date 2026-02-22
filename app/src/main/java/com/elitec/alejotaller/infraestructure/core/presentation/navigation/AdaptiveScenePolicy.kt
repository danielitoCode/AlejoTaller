package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import com.elitec.alejotaller.infraestructure.core.presentation.util.AdaptiveLayoutSpec

interface AdaptiveScenePolicy {
    fun enableDualPane(route: Any, spec: AdaptiveLayoutSpec): Boolean
}

object DefaultAdaptiveScenePolicy : AdaptiveScenePolicy {
    override fun enableDualPane(route: Any, spec: AdaptiveLayoutSpec): Boolean {
        return spec.showListAndDetail
    }
}