package im.xun.shelldroid

import android.location.{LocationManager, Location, LocationListener}
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed._
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import im.xun.shelldroid.model.Env
import im.xun.shelldroid.utils.Log._

import scala.io.Source


class XposedMod extends IXposedHookLoadPackage with IXposedHookZygoteInit {

  var pkgName: String = _

  def checkEnv(pkg: String): Boolean = {
    val filepath = "/data/data/"+pkg+"/.ENV"
    new java.io.File(filepath).exists()
  }

  def getEnvFromConfigFile(app: String): Option[Env] = {
    val filepath = "/data/data/"+app+"/.ENV"
    try {
      d("Load config file from: " + filepath)
      val json = Source.fromFile(filepath, "utf8").mkString
      d(json)
      val env = upickle.default.read[Env](json)
      Some(env)
    } catch {
      case exp: Throwable =>
        e(s"Fail to parse env : $exp")
        None
    }
  }

  def hookBuildProperty(env: Env) = {
    val cls = XposedHelpers.findClass("android.os.Build", ClassLoader.getSystemClassLoader)
    if(env.buildBoard.nonEmpty) {
      d(s"Build property hook: Board ${env.buildBoard}")
      XposedHelpers.setStaticObjectField(cls, "BOARD", env.buildBoard)
    }
    if(env.buildManufacturer.nonEmpty) {
      d(s"Build property hook: MANUFACTURER ${env.buildManufacturer}")
      XposedHelpers.setStaticObjectField(cls, "MANUFACTURER", env.buildManufacturer)
    }
    if(env.buildSerial.nonEmpty) {
      d(s"Build property hook: SERIAL ${env.buildSerial}")
      XposedHelpers.setStaticObjectField(cls, "SERIAL", env.buildSerial)
    }
    if(env.buildModel.nonEmpty) {
      d(s"Build property hook: MODEL ${env.buildModel}")
      XposedHelpers.setStaticObjectField(cls, "MODEL", env.buildModel)
    }
    if(env.buildBrand.nonEmpty) {
      d(s"Build property hook: BRAND ${env.buildBrand}")
      XposedHelpers.setStaticObjectField(cls, "BRAND", env.buildBrand)
    }

  }

  def locationHook(env: Env, classLoader: ClassLoader): Unit = {
    //location hook
    findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getCellLocation", new XC_MethodHook() {
      protected override def afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
        //伪造空的基站列表,强制fallback到我们的gps hook
        d(s"Fake empty cell location for $pkgName")
        d(s"real result: ${param.getResult}")
        param.setResult(null)
      }
    })

    findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
      protected override def afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
        //伪造空的基站列表
        d(s"Fake empty neighbor cell location for $pkgName")
        d(s"real result: ${param.getResult}")
        param.setResult(null)
      }
    })

    findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
      protected override def afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
        //伪造空的wifi热点扫描结果
        d(s"Fake empty wifi scan results for $pkgName")
        d(s"real result: ${param.getResult}")
        param.setResult(null)
      }
    })

    //gps hook
    //https://developer.android.com/guide/topics/location/strategies.html
    //https://www.ibm.com/developerworks/cn/opensource/os-cn-android-location/
    //TODO: gps数据获取是通过回调函数实现的,这里不能直接修改, 考虑直接hook binder, 待实现.
//    findAndHookMethod("android.location.LocationManager", classLoader, "requestLocationUpdates", new XC_MethodHook() {
//      protected override def beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
//        //处理多种重载方式https://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(long, float, android.location.Criteria, android.app.PendingIntent)
//        for(arg <- param.args if arg.isInstanceOf[LocationListener]){
//          val listener = arg.asInstanceOf[LocationListener]
//          d(s"requestLocationUpdates listener hook: $listener")
//          val fakeLoc = new Location(LocationManager.GPS_PROVIDER)
//          fakeLoc.setLatitude(env.location.get.latitude)
//          fakeLoc.setLongitude(env.location.get.longitude)
//          listener.onLocationChanged(fakeLoc)
//        }
//      }
//    })

  }
  def setupEnv(env: Env, classLoader: ClassLoader) = {
    //getDeviceId
    findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getDeviceId", new XC_MethodHook() {
      protected override def afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
        d(s"Fake deviceid ${env.deviceId} for $pkgName")
        param.setResult(env.deviceId)
      }
    })

    //location hook
    if(env.location.nonEmpty) {
      locationHook(env,classLoader)
    }

    //system properties hook
    hookBuildProperty(env)
  }


  def initZygote(startupParam: StartupParam ) = {
    d("initZygote with module path: " + startupParam.modulePath)
  }

  def handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

    pkgName =  lpparam.packageName

    if(checkEnv(pkgName)) {
      getEnvFromConfigFile(pkgName) match {
        case Some(env) =>
          d("setup env:"+env)
          setupEnv(env,lpparam.classLoader)

        case None =>
          e(".ENV file damaged! "+pkgName)
      }
    }

  }
}
