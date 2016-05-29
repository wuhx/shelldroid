package im.xun.shelldroid

import android.app.ProgressDialog
import android.content.{Intent, DialogInterface}
import android.content.DialogInterface.OnClickListener
import android.support.v4.content.{ContextCompat, IntentCompat}
import android.support.v7.app.AlertDialog
import android.support.v7.widget.{CardView, RecyclerView}
import android.view.View.OnLongClickListener
import android.view.{View, LayoutInflater, ViewGroup}
import android.widget.{ImageView, Button,  TextView}
import im.xun.shelldroid.model.Env
import im.xun.shelldroid.utils.Log._
import utils.AndroidUtils._
import scala.concurrent.Future

class ViewHolder(val card: CardView, val envs: Seq[Env] ) extends RecyclerView.ViewHolder(card) {
  val ivIcon = card.findViewById(R.id.icon).asInstanceOf[ImageView]
  val textName = card.findViewById(R.id.envName).asInstanceOf[TextView]
  val textAppName = card.findViewById(R.id.appName).asInstanceOf[TextView]
  val textImei = card.findViewById(R.id.imei).asInstanceOf[TextView]

  val btn = card.findViewById(R.id.my_button).asInstanceOf[Button]

  card.setOnLongClickListener( new OnLongClickListener {
    override def onLongClick(v: View): Boolean = {
      val env = envs(ViewHolder.this.getAdapterPosition)
      if(!env.active){
//        val red = ContextCompat.getColor(card.getContext,R.color.warn_red)
//        btn.setBackgroundColor(red)
        btn.setText("Delete")
      }
      true
    }
  })

  card.setOnClickListener(new View.OnClickListener {
    override def onClick(v: View): Unit = {
      if(btn.getText.toString.toLowerCase.contains("delete")) {
        btn.setText("Start")
      }
    }
  })

  lazy val switchDialog = new AlertDialog.Builder(card.getContext)
    .setTitle("Switching Environment")
    .setMessage("Jump to selected App with new Virtual Environment!")
    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      def onClick(interface: DialogInterface, i: Int) = {
        interface.cancel()
      }
    })
    .setPositiveButton("OK",
      new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          val processDialog = new ProgressDialog(card.getContext)
          processDialog.setTitle("Jump to App...")
          processDialog.setMessage("please wait...")
          processDialog.setIndeterminate(true)
          processDialog.setCancelable(false)
          processDialog.show()

          Future {
            val env = envs(ViewHolder.this.getAdapterPosition)
            d("env clicked!:"+env.toString)
            EnvManager.active(env)
          } onComplete {
            case _ =>
              processDialog.dismiss()
          }
        }
      })


  def refresh() = {
    val intent = new Intent(App.context, classOf[MainActivity])
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
    App.context.getApplicationContext.startActivity(intent)
  }

  lazy val deleteDialog = new AlertDialog.Builder(card.getContext)
    .setTitle("Delete")
    .setMessage("Delete Virtual Environment!")
    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      def onClick(interface: DialogInterface, i: Int) = {
        interface.cancel()
      }
    })
    .setPositiveButton("OK",
      new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          val processDialog = new ProgressDialog(card.getContext)
          processDialog.setTitle("Delete")
          processDialog.setMessage("please wait...")
          processDialog.setIndeterminate(true)
          processDialog.setCancelable(false)
          processDialog.show()

          Future {
            val env = envs(ViewHolder.this.getAdapterPosition)
            EnvManager.delete(env)
          } onComplete {
            case _ =>
              processDialog.dismiss()
              refresh()
          }
        }
      })

  btn.setOnClickListener(new View.OnClickListener {
    override def onClick(v: View): Unit = {
      if(btn.getText.toString.toLowerCase.contains("delete")) {
        deleteDialog.create().show()
      } else {
        switchDialog.create().show()
      }
    }
  })

}

class DataAdapter extends RecyclerView.Adapter[ViewHolder]{
  lazy val envs = EnvManager.scanEnvs

  override def getItemCount = {
    envs.size
  }

  override def onCreateViewHolder(vg: ViewGroup, pos: Int) = {
    //e("onCreateViewHolder pos:"+pos)
    val card = LayoutInflater.from(vg.getContext).inflate(R.layout.card, vg, false).asInstanceOf[CardView]
    new ViewHolder(card, envs)
  }

  override def onBindViewHolder(vh: ViewHolder,  pos: Int) = {
    //e(s"onBindViewHolder: $pos")
    val env = envs(pos)
    vh.ivIcon.setImageDrawable(getIcon(env.pkgName))
    vh.textName.setText(env.envName)
    vh.textAppName.setText(env.appName)
    vh.textImei.setText(env.deviceId)
  }

}
