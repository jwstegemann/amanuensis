import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

name := "amanuensis"

version := "1.0"

scalaVersion := "2.10.3"

// "-optimise"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature", "-language:implicitConversions" )

javacOptions ++= Seq( "-XX:+TieredCompilation", "-XX:CICompilerCount=1" )

// use src/main/webapp
unmanagedResourceDirectories in Compile <+= (baseDirectory) { _ / "src" / "main" / "webapp" / "dist" }

// fork when running tests
fork in test := true

// sbt-revolver
seq(Revolver.settings: _*)

// Repositories
resolvers ++= Seq(
	"spray.io nightlies" at "http://nightlies.spray.io/",
	"spray.io" at "http://repo.spray.io",
	"Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
//    "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/maven-snapshots",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/repo1/"
)

// Libraries
libraryDependencies ++= Seq(
	"io.spray"								  %   "spray-routing"				% "1.2.0",
	"io.spray"								  %   "spray-can"					% "1.2.0",
	"io.spray"								  %   "spray-caching"				% "1.2.0",
    "io.spray"                                %   "spray-client"                % "1.2.0",
    "io.spray"                                %%  "spray-json"                  % "1.2.5",
    "io.spray"                                %   "spray-testkit"               % "1.2.0"        % "test",
	"org.scala-lang"                          %   "scala-reflect"               % "2.10.3",
    "com.typesafe.akka"                       %%  "akka-actor"                  % "2.2.3",
    "com.typesafe.akka"                       %%  "akka-slf4j"                  % "2.2.3",
    "com.typesafe.akka"                       %%  "akka-testkit"                % "2.2.3"        % "test",
    "ch.qos.logback"                          %   "logback-classic"             % "1.0.13",
    "joda-time"                               %   "joda-time"                   % "2.3",
    "org.joda"                                %   "joda-convert"                % "1.2",    
    "org.specs2"                              %%  "specs2"                      % "1.14"         % "test",
    "commons-io"                              %   "commons-io"                  % "2.4"          % "optional",
    "com.amazonaws"                           %   "aws-java-sdk"                % "1.6.3"    
)


