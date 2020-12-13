import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object Publish extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = JvmPlugin

  override lazy val projectSettings = Seq(
    publishMavenStyle := true,
    publishTo := {
      if (isSnapshot.value) {
        Some(Resolver.sonatypeRepo("snapshots"))
      } else {
        Some(Resolver.sonatypeRepo("staging"))
      }
    },
    publishArtifact in Test := false,
    pomExtra :=
      <url>https://github.com/scala-ts/scala-ts</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>https://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scala-ts/scala-ts.git</url>
        <connection>scm:git:git@github.com:scala-ts/scala-ts.git</connection>
      </scm>
      <developers>
        <developer>
          <id>miloszpp</id>
          <name>Miłosz Piechocki</name>
          <url>http://codewithstyle.info</url>
        </developer>
        <developer>
          <id>cchantep</id>
          <name>cchantep</name>
          <url>https://github.com/cchantep</url>
        </developer>
      </developers>
  )
}