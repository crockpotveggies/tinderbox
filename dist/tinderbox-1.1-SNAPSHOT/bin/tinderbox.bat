@REM tinderbox launcher script
@REM
@REM Envioronment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM TINDERBOX_config.txt found in the TINDERBOX_HOME.
@setlocal enabledelayedexpansion

@echo off
if "%TINDERBOX_HOME%"=="" set "TINDERBOX_HOME=%~dp0\\.."
set ERROR_CODE=0

set "APP_LIB_DIR=%TINDERBOX_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (%cmdcmdline%) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%TINDERBOX_HOME%\TINDERBOX_config.txt"
set CFG_OPTS=
if exist %CFG_FILE% (
  FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%CFG_FILE%") DO (
    set DO_NOT_REUSE_ME=%%i
    rem ZOMG (Part #2) WE use !! here to delay the expansion of
    rem CFG_OPTS, otherwise it remains "" for this loop.
    set CFG_OPTS=!CFG_OPTS! !DO_NOT_REUSE_ME!
  )
)

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==Java set JAVAINSTALLED=1
)

rem Detect the same thing about javac
if "%_JAVACCMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\javac.exe" set "_JAVACCMD=%JAVA_HOME%\bin\javac.exe"
  )
)
if "%_JAVACCMD%"=="" set _JAVACCMD=javac
for /F %%j in ('"%_JAVACCMD%" -version 2^>^&1') do (
  if %%~j==javac set JAVACINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false
rem TODO - JAVAC is an optional requirement.
if not defined JAVACINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running tinderbox.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "%_JAVA_OPTS%"=="" set _JAVA_OPTS=%CFG_OPTS%

:run
 
set "APP_CLASSPATH=%APP_LIB_DIR%\default.tinderbox-1.1-SNAPSHOT.jar;%APP_LIB_DIR%\jviolajones-1.0.2-jar-with-dependencies.jar;%APP_LIB_DIR%\com.typesafe.play.play-jdbc_2.10-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.play_2.10-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.sbt-link-2.2.2.jar;%APP_LIB_DIR%\org.javassist.javassist-3.18.0-GA.jar;%APP_LIB_DIR%\com.typesafe.play.play-exceptions-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.templates_2.10-2.2.2.jar;%APP_LIB_DIR%\com.github.scala-incubator.io.scala-io-file_2.10-0.4.2.jar;%APP_LIB_DIR%\com.github.scala-incubator.io.scala-io-core_2.10-0.4.2.jar;%APP_LIB_DIR%\com.jsuereth.scala-arm_2.10-1.3.jar;%APP_LIB_DIR%\com.typesafe.play.play-iteratees_2.10-2.2.2.jar;%APP_LIB_DIR%\org.scala-stm.scala-stm_2.10-0.7.jar;%APP_LIB_DIR%\com.typesafe.config-1.0.2.jar;%APP_LIB_DIR%\com.typesafe.play.play-json_2.10-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.play-functional_2.10-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.play-datacommons_2.10-2.2.2.jar;%APP_LIB_DIR%\joda-time.joda-time-2.2.jar;%APP_LIB_DIR%\org.joda.joda-convert-1.3.1.jar;%APP_LIB_DIR%\io.netty.netty-3.7.0.Final.jar;%APP_LIB_DIR%\com.typesafe.netty.netty-http-pipelining-1.1.2.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.5.jar;%APP_LIB_DIR%\org.slf4j.jul-to-slf4j-1.7.5.jar;%APP_LIB_DIR%\org.slf4j.jcl-over-slf4j-1.7.5.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.0.13.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.0.13.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor_2.10-2.2.3.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-slf4j_2.10-2.2.3.jar;%APP_LIB_DIR%\com.ning.async-http-client-1.7.18.jar;%APP_LIB_DIR%\oauth.signpost.signpost-core-1.2.1.2.jar;%APP_LIB_DIR%\oauth.signpost.signpost-commonshttp4-1.2.1.2.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpcore-4.0.1.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpclient-4.0.1.jar;%APP_LIB_DIR%\commons-logging.commons-logging-1.1.1.jar;%APP_LIB_DIR%\xerces.xercesImpl-2.11.0.jar;%APP_LIB_DIR%\xml-apis.xml-apis-1.4.01.jar;%APP_LIB_DIR%\javax.transaction.jta-1.1.jar;%APP_LIB_DIR%\com.jolbox.bonecp-0.8.0.RELEASE.jar;%APP_LIB_DIR%\com.google.guava.guava-14.0.1.jar;%APP_LIB_DIR%\com.h2database.h2-1.3.172.jar;%APP_LIB_DIR%\tyrex.tyrex-1.0.1.jar;%APP_LIB_DIR%\com.typesafe.play.anorm_2.10-2.2.2.jar;%APP_LIB_DIR%\com.typesafe.play.play-cache_2.10-2.2.2.jar;%APP_LIB_DIR%\net.sf.ehcache.ehcache-core-2.6.6.jar;%APP_LIB_DIR%\edu.stanford.nlp.stanford-corenlp-3.3.1.jar;%APP_LIB_DIR%\edu.stanford.nlp.stanford-corenlp-3.3.1-models.jar;%APP_LIB_DIR%\com.io7m.xom.xom-1.2.10.jar;%APP_LIB_DIR%\xalan.xalan-2.7.0.jar;%APP_LIB_DIR%\de.jollyday.jollyday-0.4.7.jar;%APP_LIB_DIR%\javax.xml.bind.jaxb-api-2.2.7.jar;%APP_LIB_DIR%\com.googlecode.efficient-java-matrix-library.ejml-0.23.jar;%APP_LIB_DIR%\org.mapdb.mapdb-1.0.8.jar;%APP_LIB_DIR%\com.cloudphysics.jerkson_2.10-0.6.3.jar;%APP_LIB_DIR%\com.fasterxml.jackson.module.jackson-module-scala_2.10-2.5.3.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.10.4.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.10.4.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.5.3.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-annotations-2.5.3.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-databind-2.5.3.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.6.jar;%APP_LIB_DIR%\org.fusesource.jansi.jansi-1.11.jar;%APP_LIB_DIR%\org.apache.spark.spark-core_2.10-1.1.0.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-client-1.0.4.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-core-1.0.4.jar;%APP_LIB_DIR%\xmlenc.xmlenc-0.52.jar;%APP_LIB_DIR%\commons-codec.commons-codec-1.5.jar;%APP_LIB_DIR%\org.apache.commons.commons-math-2.1.jar;%APP_LIB_DIR%\commons-configuration.commons-configuration-1.6.jar;%APP_LIB_DIR%\commons-collections.commons-collections-3.2.1.jar;%APP_LIB_DIR%\commons-lang.commons-lang-2.4.jar;%APP_LIB_DIR%\commons-digester.commons-digester-1.8.jar;%APP_LIB_DIR%\commons-beanutils.commons-beanutils-1.7.0.jar;%APP_LIB_DIR%\commons-beanutils.commons-beanutils-core-1.8.0.jar;%APP_LIB_DIR%\commons-net.commons-net-2.2.jar;%APP_LIB_DIR%\commons-el.commons-el-1.0.jar;%APP_LIB_DIR%\hsqldb.hsqldb-1.8.0.10.jar;%APP_LIB_DIR%\oro.oro-2.0.8.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-mapper-asl-1.8.8.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-core-asl-1.8.8.jar;%APP_LIB_DIR%\net.java.dev.jets3t.jets3t-0.7.1.jar;%APP_LIB_DIR%\commons-httpclient.commons-httpclient-3.1.jar;%APP_LIB_DIR%\org.apache.curator.curator-recipes-2.4.0.jar;%APP_LIB_DIR%\org.apache.curator.curator-framework-2.4.0.jar;%APP_LIB_DIR%\org.apache.curator.curator-client-2.4.0.jar;%APP_LIB_DIR%\org.apache.zookeeper.zookeeper-3.4.5.jar;%APP_LIB_DIR%\org.slf4j.slf4j-log4j12-1.7.5.jar;%APP_LIB_DIR%\log4j.log4j-1.2.17.jar;%APP_LIB_DIR%\jline.jline-0.9.94.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-plus-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.orbit.javax.transaction-1.1.1.v201105210645.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-webapp-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-xml-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-util-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-servlet-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-security-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-server-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.orbit.javax.servlet-3.0.0.v201112011016.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-continuation-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-http-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-io-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.jetty-jndi-8.1.14.v20131031.jar;%APP_LIB_DIR%\org.eclipse.jetty.orbit.javax.mail.glassfish-1.4.1.v201005082020.jar;%APP_LIB_DIR%\org.eclipse.jetty.orbit.javax.activation-1.1.0.v201105071233.jar;%APP_LIB_DIR%\org.apache.commons.commons-lang3-3.3.2.jar;%APP_LIB_DIR%\com.google.code.findbugs.jsr305-1.3.9.jar;%APP_LIB_DIR%\com.ning.compress-lzf-1.0.0.jar;%APP_LIB_DIR%\org.xerial.snappy.snappy-java-1.0.5.3.jar;%APP_LIB_DIR%\net.jpountz.lz4.lz4-1.2.0.jar;%APP_LIB_DIR%\com.twitter.chill_2.10-0.3.6.jar;%APP_LIB_DIR%\com.twitter.chill-java-0.3.6.jar;%APP_LIB_DIR%\com.esotericsoftware.kryo.kryo-2.21.jar;%APP_LIB_DIR%\com.esotericsoftware.reflectasm.reflectasm-1.07-shaded.jar;%APP_LIB_DIR%\com.esotericsoftware.minlog.minlog-1.2.jar;%APP_LIB_DIR%\org.objenesis.objenesis-1.2.jar;%APP_LIB_DIR%\org.spark-project.akka.akka-remote_2.10-2.2.3-shaded-protobuf.jar;%APP_LIB_DIR%\org.spark-project.akka.akka-actor_2.10-2.2.3-shaded-protobuf.jar;%APP_LIB_DIR%\org.spark-project.protobuf.protobuf-java-2.4.1-shaded.jar;%APP_LIB_DIR%\org.uncommons.maths.uncommons-maths-1.2.2a.jar;%APP_LIB_DIR%\org.spark-project.akka.akka-slf4j_2.10-2.2.3-shaded-protobuf.jar;%APP_LIB_DIR%\org.json4s.json4s-jackson_2.10-3.2.10.jar;%APP_LIB_DIR%\org.json4s.json4s-core_2.10-3.2.10.jar;%APP_LIB_DIR%\org.json4s.json4s-ast_2.10-3.2.10.jar;%APP_LIB_DIR%\org.scala-lang.scalap-2.10.4.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.10.4.jar;%APP_LIB_DIR%\colt.colt-1.2.0.jar;%APP_LIB_DIR%\concurrent.concurrent-1.3.4.jar;%APP_LIB_DIR%\org.apache.mesos.mesos-0.18.1-shaded-protobuf.jar;%APP_LIB_DIR%\io.netty.netty-all-4.0.23.Final.jar;%APP_LIB_DIR%\com.clearspring.analytics.stream-2.7.0.jar;%APP_LIB_DIR%\com.codahale.metrics.metrics-core-3.0.0.jar;%APP_LIB_DIR%\com.codahale.metrics.metrics-jvm-3.0.0.jar;%APP_LIB_DIR%\com.codahale.metrics.metrics-json-3.0.0.jar;%APP_LIB_DIR%\com.codahale.metrics.metrics-graphite-3.0.0.jar;%APP_LIB_DIR%\org.tachyonproject.tachyon-client-0.5.0.jar;%APP_LIB_DIR%\org.tachyonproject.tachyon-0.5.0.jar;%APP_LIB_DIR%\commons-io.commons-io-2.4.jar;%APP_LIB_DIR%\org.spark-project.pyrolite-2.0.1.jar;%APP_LIB_DIR%\net.sf.py4j.py4j-0.8.2.1.jar;%APP_LIB_DIR%\org.imgscalr.imgscalr-lib-4.2.jar;%APP_LIB_DIR%\net.sourceforge.parallelcolt.parallelcolt-0.10.0.jar;%APP_LIB_DIR%\net.sourceforge.jplasma.jplasma-1.2.0.jar;%APP_LIB_DIR%\net.sourceforge.jplasma.core-lapack-0.1.jar;%APP_LIB_DIR%\net.sourceforge.f2j.arpack_combined_all-0.1.jar;%APP_LIB_DIR%\net.sourceforge.jtransforms.jtransforms-2.4.0.jar;%APP_LIB_DIR%\junit.junit-4.10.jar;%APP_LIB_DIR%\org.hamcrest.hamcrest-core-1.1.jar;%APP_LIB_DIR%\net.sourceforge.csparsej.csparsej-1.1.1.jar;%APP_LIB_DIR%\net.sourceforge.parallelcolt.optimization-1.0.jar;%APP_LIB_DIR%\com.googlecode.netlib-java.netlib-java-0.9.3.jar;%APP_LIB_DIR%\com.github.rwl.JKLU-1.0.0.jar;%APP_LIB_DIR%\com.github.rwl.BTFJ-1.0.1.jar;%APP_LIB_DIR%\com.github.rwl.AMDJ-1.0.1.jar;%APP_LIB_DIR%\com.github.rwl.COLAMDJ-1.0.1.jar"
set "APP_MAIN_CLASS=play.core.server.NettyServer"

rem TODO - figure out how to pass arguments....
"%_JAVACMD%" %_JAVA_OPTS% %TINDERBOX_OPTS% -cp "%APP_CLASSPATH%" %APP_MAIN_CLASS% %CMDS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end

@endlocal

exit /B %ERROR_CODE%
