name := "amanuensis"

version := "1.0"

scalaVersion := "2.10.3"

// "-optimise"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

javacOptions ++= Seq( "-XX:+TieredCompilation", "-XX:CICompilerCount=1" )

// use src/main/webapp
unmanagedResourceDirectories in Compile <+= (baseDirectory) { _ / "src" / "main" / "webapp" }


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
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"
)

// Libraries
libraryDependencies ++= Seq(
	"io.spray"								  %   "spray-routing"				% "1.2-RC2",
	"io.spray"								  %   "spray-can"					% "1.2-RC2",
	"io.spray"								  %   "spray-caching"				% "1.2-RC2",
    "io.spray"                                %%  "spray-json"                  % "1.2.5",
	"org.scala-lang"                          %   "scala-reflect"               % "2.10.3",
    "com.typesafe.akka"                       %%  "akka-actor"                  % "2.2.3",
    "com.typesafe.akka"                       %%  "akka-slf4j"                  % "2.2.3",
    "com.typesafe.akka"                       %%  "akka-testkit"                % "2.2.3",
    "org.parboiled"                           %%  "parboiled-scala"             % "1.1.4",
    "com.chuusai"                             %%  "shapeless"                   % "1.2.4",
    "org.scalatest"                           %%  "scalatest"                   % "1.9.2",
    "org.specs2"                              %%  "specs2"                      % "2.2.2",
    "com.googlecode.concurrentlinkedhashmap"  %   "concurrentlinkedhashmap-lru" % "1.4",
    "ch.qos.logback"                          %   "logback-classic"             % "1.0.13",
    "org.jvnet.mimepull"                      %   "mimepull"                    % "1.9.1",
    "org.pegdown"                             %   "pegdown"                     % "1.4.1",
    "org.reactivemongo"                       %%  "reactivemongo"                % "0.10.0-SNAPSHOT",
    "joda-time"                               % "joda-time"                     % "2.1"
)




