package im.xun.shelldroid

import java.io.File

import im.xun.shelldroid.utils.Log._
import im.xun.shelldroid.model.Env

import scala.io.Source
import scala.util.{Success, Try}

object EnvManager {

  val envRepoPath =  App.context.getFilesDir.toString + "/ENV_REPO"

  val envRepo = {
    val repo = new File(envRepoPath)
    if(! repo.exists()) {
      repo.mkdirs()
    }
    envRepoPath
  }

  def readEnv(filepath: String): Try[Env] = {
    Try{
      val json = Source.fromFile(filepath,"utf8").mkString
      upickle.default.read[Env](json)
    }
  }
  def saveEnv(env: Env, filepath: String ) = {
    d(s"save Env: $env to $filepath ")
    val out = new java.io.PrintWriter(filepath, "utf8")
    try{
      val jsonString = upickle.default.write(env)
      out.print(jsonString)
    } catch {
      case exp: Throwable =>
        e(s"fail to save Env:$env with exception: $exp")
    } finally { out.close() }
  }

  /*
  * 扫描Env配置
  * */
  def scanEnvs: Seq[Env] = {

    val root = new File(envRepo)

    val appCurrentEnv = root.listFiles().map(app => app.toString + "/.RUNNING")

    val appEnvs = for{
      apps <- root.listFiles.filter(_.isDirectory)
      envs <- apps.listFiles.filter(_.isDirectory)
    } yield envs.toString + "/.ENV"

    val allEnvs =  appCurrentEnv ++ appEnvs
    for(env <- allEnvs) {
      d(s"Find Env: $env")
    }
    allEnvs map readEnv filter(_.isSuccess) map(_.get)

  }

  val ensureSelinuxPermissive = {
    d("ensureSelinuxPermissive")
    try {
      val proc = Runtime.getRuntime.exec("su -c setenforce 0")
      proc.waitFor()

    } catch  {
      case _: Throwable =>
        e("Fail to disable selinux")
    }
  }

  def doRoot(cmd: String) = {
    d("doRoot: "+cmd)
    try {
      val proc = Runtime.getRuntime.exec("su -c " + cmd)
      proc.waitFor()
    } catch  {
      case _: Throwable =>
        e("Fail to run cmd: "+cmd)
    }
  }

  def killApp(env: Env) = {
    val cmd = "am force-stop %s".format(env.pkgName)
    doRoot(cmd)
  }

  def startApp(env: Env) = {
    val cmd = "monkey -p %s -c android.intent.category.LAUNCHER 1".format(env.pkgName)
    doRoot(cmd)
  }

  def updateAppLastRunning(env: Env): Unit = {
    d("update last running: " + env)
    val filepath = getAppRepoDir(env) + "/.RUNNING"
    saveEnv(env.copy(active=true), filepath)
  }

  def appLastRunning(env: Env): Option[Env] = {
    val filepath = getAppRepoDir(env) + "/.RUNNING"
    Try{
      val lastString= scala.io.Source.fromFile(filepath,"utf8").mkString
      upickle.default.read[Env](lastString)
    } match {
      case Success(value) =>
        Some(value)
      case exp =>
        e(s"fail to get last running! $exp")
        None
    }
  }

  def getAppRepoDir(env: Env) = {
    envRepo + "/" + env.pkgName
  }

  def getEnvDir(env: Env)  = {
    getAppRepoDir(env) + "/" + env.id
  }

  def getAppDir(env: Env) = {
    "/data/data/" + env.pkgName
  }

  def envDirExist(env: Env) = {
    new File(getEnvDir(env)).exists()
  }

  def envDirBuild(env: Env) = {
    Seq(
      "mkdir -p %s".format(getEnvDir(env)),
      "cp -a %s/lib %s".format(getAppDir(env), getEnvDir(env)),
      "chmod 777 %s -R".format(getAppRepoDir(env))
    ).map(doRoot)

    val envFile = getEnvDir(env) + "/.ENV"
    saveEnv(env, envFile)
    Seq(
      "chmod 777 %s".format(envFile)
    ).map(doRoot)
  }

  def switchEnv(env: Env, lastEnv: Env) = {
    killApp(env)
    Seq(
      "mv %s %s".format(getAppDir(env), getEnvDir(lastEnv)),
      "mv %s %s".format(getEnvDir(env), getAppDir(env))
    ) map doRoot
  }

  def delete(env: Env) = {
    val cmd = "rm -fr %s".format(getEnvDir(env))
    doRoot(cmd)
  }

  def active(env: Env) = {
    d(s"active env:\n$env")
    if(appLastRunning(env).isEmpty) {
      if(!envDirExist(env)) {
        envDirBuild(env)
      }
      switchEnv(env, env.copy(id= "pre-shelldroid-data"))
    } else if(appLastRunning(env).get.id != env.id) {
      val last = appLastRunning(env).get
      d(s"last env:\n$last")
      if(!envDirExist(env)) {
        envDirBuild(env)
      }
      switchEnv(env,last)
    }
    updateAppLastRunning(env)
    startApp(env)
  }

}
