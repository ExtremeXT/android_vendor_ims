/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.carriersettings

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class PbConfigLoader {
    companion object {
        const val TAG = "PbConfigLoader"
        private var cachedCarriers =
            mapOf<CarrierConfigService.ExtendedCarrierIdentifier, CarrierSettings>()

        private fun openPbFile(name: String): FileInputStream {
            Log.i(TAG, "Opening $name")
            return FileInputStream(
                File(
                    Environment.getProductDirectory(),
                    "etc" + File.separator + "CarrierSettings" + File.separator + name + ".pb",
                ),
            )
        }

        fun readSettingsFromAssets(name: String): CarrierSettings? {
            try {
                return CarrierSettings.parseFrom(openPbFile(name))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun readConfigFromPb(id: CarrierConfigService.ExtendedCarrierIdentifier): CarrierSettings? {
            Log.i(TAG, "readConfigFromPb")
            val carrierList = CarrierList.parseFrom(openPbFile("carrier_list"))
            val canonicalName = ProtoUtils.searchCarrier(carrierList, id)
            if (canonicalName != null) {
                try {
                    val settings = CarrierSettings.parseFrom(openPbFile(canonicalName))
                    return settings
                } catch (e: FileNotFoundException) {
                }
            }
            val multiCarrierSettings = MultiCarrierSettings.parseFrom(openPbFile("others"))
            for (settings in multiCarrierSettings.settingList) {
                if (settings.canonicalName == canonicalName) {
                    return CarrierSettings
                        .newBuilder()
                        .mergeFrom(settings)
                        .setVersion(multiCarrierSettings.version)
                        .build()
                }
            }
            return null
        }
    }
}
