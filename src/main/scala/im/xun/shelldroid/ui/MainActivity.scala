package im.xun.shelldroid

import android.content.{DialogInterface, Intent}
import android.net.Uri
import android.support.design.widget.{Snackbar, NavigationView}
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.v4.view.GravityCompat
import android.support.v7.app.{AlertDialog, ActionBarDrawerToggle, AppCompatActivity}
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.{Menu, MenuItem, View}
import android.view.View.OnClickListener
import android.widget.Toast
import im.xun.shelldroid.utils.Log._

// mix in Contexts for Activity
class MainActivity
  extends AppCompatActivity
  with TypedFindView {

  lazy val toolbar = findView(TR.toolbar)
  lazy val fab = findView(TR.fab)
  lazy val drawer = findView(TR.drawer_layout)
  lazy val navigationView = findView(TR.nav_view)
  lazy val recyclerView = findView(TR.rv)

  lazy val dataAdapter = new DataAdapter

  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) = {
    recyclerView.setAdapter(new DataAdapter)
  }

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_layout)

    setSupportActionBar(toolbar)

    fab.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        val intent = new Intent(MainActivity.this, classOf[NewActivity])
        startActivityForResult(intent,0)
      }
    })

    //toolbar上的菜单图标唤起右边栏
    val toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    drawer.setDrawerListener(toggle)
    toggle.syncState()


    //设置右边栏的菜单点击
    navigationView.setNavigationItemSelectedListener( new OnNavigationItemSelectedListener {
      override def onNavigationItemSelected(menuItem: MenuItem): Boolean = {
        menuItem.setChecked(true)

        val title = menuItem.getTitle.toString.toLowerCase()
        d(s"select title: $title")

        if(title.contains("github")){
          val browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wuhx/shelldroid"))
          startActivity(browserIntent);
        }
        if(title.contains("issues")){
          val intent = new Intent(Intent.ACTION_SENDTO)
          val uriText = "mailto:i@xun.im?subject=About ShellDroid&body=Hi~"
          intent.setData(Uri.parse(uriText))
//          intent.putExtra(Intent.EXTRA_EMAIL, "i@xun.im")
//          intent.putExtra(Intent.EXTRA_SUBJECT, "About Shelldroid")
          startActivity(Intent.createChooser(intent, "Send Email"))
        }

        drawer.closeDrawer(GravityCompat.START)
//        Toast.makeText(MainActivity.this, menuItem.getTitle, Toast.LENGTH_LONG).show()
        true
      }
    })

    //设置主页面
    recyclerView.setLayoutManager(new LinearLayoutManager(this))
    recyclerView.setAdapter(dataAdapter)


  }

  //显示toolbar上的设置按钮
  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.setting, menu)
    true
  }

  //响应toolbar上的设置按钮
  override def onOptionsItemSelected(item: MenuItem) = {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.getItemId

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      d("options item selected!")
    }

    super.onOptionsItemSelected(item);
  }

}
