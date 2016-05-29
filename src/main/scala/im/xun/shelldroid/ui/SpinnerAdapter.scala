package im.xun.shelldroid

import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ImageView, TextView, BaseAdapter}
import im.xun.shelldroid.model.AppInfo
import im.xun.shelldroid.utils.AndroidUtils._


class SpinnerAdapter extends BaseAdapter{

  lazy val data= getInstalledAppInfo

  override def getCount = data.size

  override def getItem(pos: Int) = data(pos)

  override def getItemId(pos: Int) = 0

  override def getView(pos: Int, view: View, vg: ViewGroup) = {
    val v= LayoutInflater.from(vg.getContext).inflate(R.layout.spinner_item, vg, false)
    if( v!=null){
      val iv =  v.findViewById(R.id.itemIcon).asInstanceOf[ImageView]
      val tv =  v.findViewById(R.id.itemName).asInstanceOf[TextView]
      val app = data(pos)
      iv.setImageDrawable(app.icon)
      tv.setText(app.appName)
    }
    v
  }


}
