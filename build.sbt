import android.Keys._
import android.Dependencies.{LibraryDependency, aar}
import sbt.Keys._
enablePlugins(AndroidApp)

name := "shelldroid"

organization := "im.xun"

scalaVersion := "2.11.8"

platformTarget in Android := "android-23"

version := "0.0.2"

val supportVersion="23.1.1"
//~ protify

val cleanResourceTask = TaskKey[Unit]("cleanResource", "clean .DS_Store file in res folder, which will cause error: TR.scala:164: illegal start of simple pattern")
cleanResourceTask := {
  println("Clean .DS_Store")
  "find . -name .DS_Store" #| "xargs rm -fr" !
}


javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.7")

resolvers += Resolver.bintrayRepo("rovo89", "de.robv.android.xposed")

libraryDependencies ++= Seq(
  "de.robv.android.xposed" % "api" % "82" % "provided" withSources(),
  "com.lihaoyi" %% "upickle" % "0.4.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
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

addCommandAlias("apk", ";cleanResource;android:run")
