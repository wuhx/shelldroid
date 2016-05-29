package im.xun.shelldroid

import im.xun.shelldroid.model.{Location, Env}
import im.xun.shelldroid.utils.Log._
import org.scalatest.{Matchers, FlatSpec}

class EnvSerializeSpec extends FlatSpec with Matchers{
  it should "serialize" in {
    val location = Location(100.11,200.22)
    val env = Env("001","test1","weixin","com.tencent.mm",false,"2500",""
      ,"cn","46001","1234","bullhead","Nexus 191","ZTE","2344","Xiaomi","0010","Nubia","",Some(location)
    )
    val json = upickle.default.write(env,2)
    println(s"serialized json: \n$json")
    val newEnv = upickle.default.read[Env](json)
    println(s"deserialized env: \n$newEnv")
    newEnv shouldBe env
  }
}
