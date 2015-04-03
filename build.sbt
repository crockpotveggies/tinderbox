name := "tinderbox"

version := "1.1-SNAPSHOT"

val javacvVersion = "0.9"

val javacppVersion = "0.9"

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-all",
  "-language:postfixOps"
)

// Some dependencies like `javacpp` are packaged with maven-plugin packaging
classpathTypes += "maven-plugin"

// Determine current platform
val platform = {
  // Determine platform name using code similar to javacpp
  // com.googlecode.javacpp.Loader.java line 60-84
  val jvmName = System.getProperty("java.vm.name").toLowerCase
  var osName = System.getProperty("os.name").toLowerCase
  var osArch = System.getProperty("os.arch").toLowerCase
  if (jvmName.startsWith("dalvik") && osName.startsWith("linux")) {
    osName = "android"
  } else if (jvmName.startsWith("robovm") && osName.startsWith("darwin")) {
    osName = "ios"
    osArch = "arm"
  } else if (osName.startsWith("mac os x")) {
    osName = "macosx"
  } else {
    val spaceIndex = osName.indexOf(' ')
    if (spaceIndex > 0) {
      osName = osName.substring(0, spaceIndex)
    }
  }
  if (osArch.equals("i386") || osArch.equals("i486") || osArch.equals("i586") || osArch.equals("i686")) {
    osArch = "x86"
  } else if (osArch.equals("amd64") || osArch.equals("x86-64") || osArch.equals("x64")) {
    osArch = "x86_64"
  } else if (osArch.startsWith("arm")) {
    osArch = "arm"
  }
  val platformName = osName + "-" + osArch
  println("platform: " + platformName)
  platformName
}

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "edu.stanford.nlp"              %  "stanford-corenlp"           % "3.3.1",
  "edu.stanford.nlp"              %  "stanford-corenlp"           % "3.3.1" classifier "models",
  "org.mapdb"                     %  "mapdb"                      % "1.0.6",
  "com.cloudphysics"              %% "jerkson"                    % "0.6.3",
  "com.fasterxml.jackson.module"  %  "jackson-module-scala_2.10"  % "2.4.4",
  "org.fusesource.jansi"          %  "jansi"                      % "1.11",
  "org.apache.spark"              %% "spark-core"                 % "1.1.0",
  "com.typesafe.akka"             %% "akka-actor"                 % "2.2.3",
  "com.typesafe.akka"             %% "akka-slf4j"                 % "2.2.3",
  "org.imgscalr"                  %  "imgscalr-lib"               % "4.2",
  "net.sourceforge.parallelcolt"  %  "parallelcolt"               % "0.10.0"
  )     

initialize := {
  val required = "1.7"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

play.Project.playScalaSettings
