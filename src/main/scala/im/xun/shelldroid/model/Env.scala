package im.xun.shelldroid.model

import android.graphics.drawable.Drawable

case class AppInfo(appName: String, pkgName: String, icon: Drawable)
case class Location(latitude: Double, longitude: Double)
case class Env(id: String,
               envName: String,
               appName: String,
               pkgName: String,
               active: Boolean = false,
               deviceId: String= "",
               phoneNumber: String= "",
               networkCountryIso: String= "",
               networkOperator: String= "",
               simSerialNumber: String= "",
               buildBoard: String= "",
               buildModel: String= "",
               buildManufacturer: String= "",
               buildId: String= "",
               buildDevice: String= "",
               buildSerial: String= "",
               buildBrand: String= "",
               androidId: String = "",
               location: Option[Location]=None
              )

