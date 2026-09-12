/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.carriersettings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.CarrierConfigManager
import android.telephony.SubscriptionManager
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CarrierSettings"
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED",
            -> {
                refresh(context)
            }
        }
    }

    private fun refresh(context: Context) {
        val subscriptionManager =
            context.getSystemService(SubscriptionManager::class.java)

        val carrierConfigManager =
            context.getSystemService(CarrierConfigManager::class.java)

        val subscriptions =
            subscriptionManager.activeSubscriptionInfoList ?: return

        for (subscription in subscriptions) {
            Log.i(
                TAG,
                "Refreshing CarrierConfig: subId=${subscription.subscriptionId}",
            )

            carrierConfigManager.notifyConfigChangedForSubId(
                subscription.subscriptionId,
            )
        }
    }
}
