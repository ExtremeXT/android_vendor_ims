#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# CarrierSettings
PRODUCT_PACKAGES += CarrierSettings

# IMS
$(call inherit-product, packages/modules/ImsMedia/imsmedia.mk)
