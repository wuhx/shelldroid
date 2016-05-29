package im.xun.shelldroid.utils

import android.content.pm.{ApplicationInfo, PackageManager}
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.support.design.R
import im.xun.shelldroid.App
import im.xun.shelldroid.model.AppInfo
import im.xun.shelldroid.utils.Log._

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import android.widget.TextView
import scala.collection.JavaConversions._

import scala.util.Try


object AndroidUtils {

  implicit val exec = ExecutionContext.fromExecutor(
    AsyncTask.THREAD_POOL_EXECUTOR)

  implicit def textViewToString(tv: TextView): String = {
    tv.getText.toString
  }

  lazy val pm = App.context.getApplicationContext.getPackageManager

  def getIcon(pkgName: String): Drawable = {
    Try{
     pm.getApplicationIcon(pkgName)
    } getOrElse App.context.getDrawable(R.drawable.abc_btn_check_material)
}

  def getDataDir(pkgName: String) = {
    pm.getApplicationInfo(pkgName,0).dataDir
  }

  def getInstalledAppInfo: Seq[AppInfo] = {
    for{
      pkg <- pm.getInstalledApplications(PackageManager.GET_META_DATA)
      if (pkg.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
      appName = pm.getApplicationLabel(pkg).toString
    } yield AppInfo(appName, pkg.packageName, getIcon(pkg.packageName))
  }

}
