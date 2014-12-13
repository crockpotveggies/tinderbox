name := "tinderbox"

version := "1.1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.cloudphysics"              %% "jerkson"                    % "0.6.3",
  "com.fasterxml.jackson.module"  %  "jackson-module-scala_2.10"  % "2.4.4",
  "org.fusesource.jansi"          %  "jansi"                      % "1.11",
  "org.apache.spark"              %% "spark-core"                 % "1.1.0",
  "com.typesafe.akka"             %% "akka-actor"                 % "2.2.3",
  "com.typesafe.akka"             %% "akka-slf4j"                 % "2.2.3",
  "org.apache.spark"              %% "spark-streaming-twitter"    % "1.1.0",
  "org.apache.spark"              %% "spark-sql"                  % "1.1.0",
  "org.apache.spark"              %% "spark-mllib"                % "1.1.0"
  )     

play.Project.playScalaSettings
