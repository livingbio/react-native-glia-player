package com.gliaplayer

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

class GliaPlayerViewPackage : BaseReactPackage() {
  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return listOf(GliaPlayerViewManager())
  }

  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    when (name) {
      GliaPlayerViewManager.NAME -> GliaPlayerViewManager()
    }
    return null
  }

  override fun getReactModuleInfoProvider() = ReactModuleInfoProvider {
    mapOf(GliaPlayerViewManager.NAME to ReactModuleInfo(
      name = GliaPlayerViewManager.NAME,
      className = GliaPlayerViewManager.NAME,
      canOverrideExistingModule = false,
      needsEagerInit = false,
      isCxxModule = false,
      isTurboModule = true,
    )
    )
  }
}
