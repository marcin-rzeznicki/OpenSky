import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import sbt.Keys._
import sbt._

object JavaAppSettings {
  val jvmParameters = Seq("-Xmx1G", "-Xms1G", "-server", "-XX:+UseCompressedOops", "-XX:+UseParNewGC", "-XX:+UseConcMarkSweepGC")

  def configurationMappings(resources: File): Seq[(File, String)] = {
    val logbackConf     = resources / "logback.xml"
    val applicationConf = resources / "application.conf"

    Seq(logbackConf -> "conf/logback.xml", applicationConf -> "conf/application.conf")
  }

  def properties: Seq[String] =
    Seq("-Dlogback.configurationFile=conf/logback.xml", "-Dconfig.file=conf/application.conf")

  def allJavaOptions: Seq[String] =
    jvmParameters.map(opt => s"-J$opt") ++ properties

  def apply(): Seq[Setting[_]] =
    Seq(javaOptions in Universal ++= allJavaOptions,
        mappings in Universal ++= configurationMappings(sourceDirectory.value / "main" / "resources"))
}
