package im.xun.shelldroid.utils

/**
  * Created by wuhx on 1/28/16.
  */
object Log {

  val TAG="SHELLDROID:"
  def d(msg: String) = {
    android.util.Log.d(TAG, msg)
  }

  def i(msg: String) = {
    android.util.Log.i(TAG, msg)
  }

  def e(msg: String) = {
    android.util.Log.e(TAG, msg)
  }

}
