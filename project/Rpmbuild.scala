import com.twitter.sbt.GitProject._
import com.twitter.sbt.PackageDist._
import sbt.Keys._
import sbt._

import scala.util.control.Exception._

object Rpmbuild extends Plugin {
  private case class RpmbuildDirectories(base: File, sources: File, specs: File, srpms: File, rpms: File)

  private object RpmbuildDirectories {
    def create(parent: File): Option[RpmbuildDirectories] =
      createDirectory(parent, "rpmbuild").flatMap { base =>
        Seq("SOURCES", "SPECS", "SRPMS", "RPMS").map(createDirectory(base, _)) match {
          case Seq(Some(sources), Some(specs), Some(srpms), Some(rpms)) => Some(RpmbuildDirectories(base, sources, specs, srpms, rpms))
          case _ => None
        }
      }
  }

  private def createDirectory(parent: File, name: String): Option[File] = allCatch opt {
    val file = parent / name
    IO.createDirectory(file)
    file
  }

  val rpmbuildRelease = SettingKey[Int]("rpmbuildRevision", "Revision number for rpm package.")

  val rpmbuild = TaskKey[File]("rpmbuild", "Build rpm package from spec file.")

  private val rpmbuildTask = (
    target,
    packageDist,
    version,
    gitProjectSha,
    rpmbuildRelease
  ).map { (
      target: File,
      zip: File,
      version: String,
      projectSha: Option[String],
      release: Int
    ) =>
    val (versionWithoutSnapshot, commit) = if (version.endsWith("-SNAPSHOT")) {
      (version.dropRight(9), projectSha.map(_.substring(0, 8)))
    } else {
      (version, None)
    }

    // Create directories for rpmbuild
    val dirs = RpmbuildDirectories.create(target) getOrElse sys.error("Fail to create rpmbuild directorires.")

    // Create a spec file
    // Give type explicitly here so that Intelli J can handle type.
    val spec: twirl.api.Txt = txt.KestrelSpec(zip.base, versionWithoutSnapshot, commit, release)
    val specFile = dirs.specs / "kestrel.spec"
    IO.write(dirs.specs / "kestrel.spec", spec.body)

    // Copy zip package
    IO.copyFile(zip, dirs.sources / zip.name)

    // Run rpmbuild comment
    val rpmbuild = Process(Seq("rpmbuild", "-ba", "-D", "_topdir %s".format(dirs.base.absolutePath), specFile.absolutePath), dirs.base)
    rpmbuild.run().exitValue() match {
      case 0 => dirs.rpms / "kestrel.rpm" // FIXME use real rpm name, also may want to move it to dist directory.
      case _ => sys.error("Fail to rpmbuild")
    }
  }

  val newSettings = Seq(
    rpmbuildRelease := 1,
    rpmbuild <<= rpmbuildTask
  )
}