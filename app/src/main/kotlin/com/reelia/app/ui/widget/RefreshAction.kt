package com.reelia.app.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * Backs the widget's own "⟳" refresh icon (see [ReeliaWidget]'s header row). Glance instantiates
 * [ActionCallback]s reflectively via a no-arg constructor, so this can't take constructor-injected
 * dependencies — it goes through [createReeliaWidget] (backed by [ReeliaWidgetEntryPoint]) instead,
 * same as the widget itself.
 */
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        createReeliaWidget(context).update(context, glanceId)
    }
}
