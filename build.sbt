name := "AmazonFineFoodsParser"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Maven Central" at "https://repo1.maven.org/maven2/"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.1.1"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.5.2",
	"com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-http" % "10.0.7",
	"com.typesafe.akka" %% "akka-http-testkit" % "10.0.7" % Test
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-stream" % "2.5.2",
	"com.typesafe.akka" %% "akka-stream-testkit" % "2.5.2" % Test
)

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
libraryDependencies += "io.spray" % "spray-httpx_2.11" % "1.3.3"
libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.3"
libraryDependencies += "io.spray" % "spray-can_2.11" % "1.3.3"
libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.5.2"