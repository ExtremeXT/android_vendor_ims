/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.carriersettings

import android.os.PersistableBundle
import android.service.carrier.CarrierIdentifier
import android.service.carrier.CarrierService
import android.telephony.SubscriptionManager
import android.util.Log

class CarrierConfigService : CarrierService() {
    companion object {
        const val TAG = "CarrierConfigService"
    }

    private fun addVersionString(config: PersistableBundle) {
        // TODO implement
    }

    @Deprecated("Deprecated in Java")
    override fun onLoadConfig(id: CarrierIdentifier?): PersistableBundle? = throw UnsupportedOperationException("Not supported")

    override fun onLoadConfig(
        subscriptionId: Int,
        id: CarrierIdentifier?,
    ): PersistableBundle? {
        Log.i(TAG, "onLoadConfig: $subscriptionId, $id")
        val noSim =
            id == null || id.mcc.isEmpty() || id.mnc.isEmpty() || id.spn?.isEmpty() != false || id.imsi?.isEmpty() != false ||
                id.gid1?.isEmpty() != false
        if (noSim) {
            try {
                val settings =
                    PbConfigLoader.readSettingsFromAssets("no_sim")
                        ?: PbConfigLoader.readConfigFromPb(
                            ExtendedCarrierIdentifier.DEFAULT,
                        )
                val bundle = (settings?.configs ?: CarrierConfig.getDefaultInstance()).toBundle()
                addVersionString(bundle)
                return bundle
            } catch (e: Exception) {
                Log.e(TAG, "Unable to read no sim settings!")
                return null
            }
        } else {
            val bundle = PersistableBundle()
            try {
                val settings = PbConfigLoader.readConfigFromPb(ExtendedCarrierIdentifier.DEFAULT)
                bundle.putAll((settings?.configs ?: CarrierConfig.getDefaultInstance()).toBundle())
            } catch (e: Exception) {
                Log.e(TAG, "Unable to read default settings!")
                e.printStackTrace()
            }
            try {
                val iccid =
                    applicationContext
                        .getSystemService(SubscriptionManager::class.java)
                        .getActiveSubscriptionInfo(
                            subscriptionId,
                        ).iccId
                val settings = PbConfigLoader.readConfigFromPb(ExtendedCarrierIdentifier(id, iccid))
                bundle.putAll((settings?.configs ?: CarrierConfig.getDefaultInstance()).toBundle())
                addVersionString(bundle)
                return bundle
            } catch (e: Exception) {
                Log.e(TAG, "Unable to read carrier settings!")
                e.printStackTrace()
                return bundle
            }
        }
    }

    class ExtendedCarrierIdentifier(
        mcc: String,
        mnc: String,
        spn: String?,
        imsi: String?,
        gid1: String?,
        gid2: String?,
        val iccid: String,
    ) : CarrierIdentifier(mcc, mnc, spn, imsi, gid1, gid2) {
        constructor(id: CarrierIdentifier, iccid: String) : this(
            id.mcc,
            id.mnc,
            id.spn,
            id.imsi,
            id.gid1,
            id.gid2,
            iccid,
        )

        override fun toString(): String =
            "{ExtendedCarrierIdentifier" +
                "carrierIdentifier=${super.toString()}" +
                ", iccid=$iccid"

        companion object {
            val DEFAULT = ExtendedCarrierIdentifier("000", "000", null, null, null, null, "")
            val NO_SIM = ExtendedCarrierIdentifier("", "", null, null, null, null, "")
        }
    }
}
