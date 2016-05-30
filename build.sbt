import android.Keys._
import android.Dependencies.{LibraryDependency, aar}
import sbt.Keys._

platformTarget in Android := "android-23"

artifactPath in packageBin  := (artifactPath in (Compile,packageBin)).value

name := "shelldroid"

scalaVersion := "2.11.8"

val supportVersion="23.1.1"
//~ protify

val cleanResourceTask = TaskKey[Unit]("cleanResource", "clean .DS_Store file in res folder, which will cause error: TR.scala:164: illegal start of simple pattern")
cleanResourceTask := {
  println("Clean .DS_Store")
  "find . -name .DS_Store" #| "xargs rm -fr" !
}

run <<= run in Android

addCommandAlias("apk", ";cleanResource;run")


javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.7")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "0.3.7",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  aar("com.android.support" %  "design" % supportVersion),
  aar("com.android.support" %  "cardview-v7" % supportVersion),
  aar("com.android.support" % "support-v4" % supportVersion),
  aar("com.android.support" % "appcompat-v7" % supportVersion),
  aar("com.android.support" % "recyclerview-v7" % supportVersion)
)

proguardScala in Android :=  true

//dexMulti in Android := true

proguardOptions in Android ++= Seq(
  "-ignorewarnings",
  "-keep class scala.Dynamic",
  "-keep class im.xun.shelldroid.** { *; }",
  "-keep interface im.xun.shelldroid.** { *; }",
  "-keep class de.robv.android.xposed.** { *;  }",
  "-keep interface de.robv.android.xposed.** { *;  }"
)
//protifySettings
