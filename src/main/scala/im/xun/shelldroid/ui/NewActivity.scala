package im.xun.shelldroid

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{ViewParent, View}
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, ArrayAdapter}
import im.xun.shelldroid.model._
import utils.Log._
import utils.AndroidUtils._
import scala.collection.JavaConversions._


class NewActivity
  extends AppCompatActivity
  with TypedFindView {


  lazy val toolbar = findView(TR.my_toolbar2)
  lazy val btn = findView(TR.btn)
  lazy val textName = findView(TR.textName)
  lazy val textPhoneModel = findView(TR.textPhoneModel)
  lazy val textPhoneBrand = findView(TR.textPhoneBrand)
  lazy val textImei = findView(TR.textImei)

  lazy val  spinner = findView(TR.spinner)


  def save(env: Env) = {
    d("Save env: "+env)
    EnvManager.envDirBuild(env)
  }

  def quit(): Unit = {
    setResult(0)
    super.finish()
  }

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.new_layout)

    setSupportActionBar(toolbar)
    toolbar.setTitleTextColor(Color.WHITE)

    val spAdapter = new SpinnerAdapter
    spinner.setAdapter(spAdapter)

    btn.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        val appInfo = spinner.getSelectedItem.asInstanceOf[AppInfo]
        val env = Env(
          java.util.UUID.randomUUID().toString,
          textName,appInfo.appName ,
          appInfo.pkgName,
          active=false,
          textImei,
          buildModel= textPhoneModel,
          buildManufacturer = textPhoneBrand,
          buildBrand=textPhoneBrand)
        save(env)
        quit()
      }
    })

  }
}