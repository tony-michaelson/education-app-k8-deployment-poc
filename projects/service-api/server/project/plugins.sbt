logLevel := Level.Debug

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.2.22"
)

addSbtPlugin("com.typesafe.play"     % "sbt-plugin"        % "2.8.8")
addSbtPlugin("com.scalatsi"          % "sbt-scala-tsi"     % "0.5.0")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt"        % "5.0.0-RC2")
addSbtPlugin("com.github.tototoshi"  % "sbt-slick-codegen" % "1.4.0")
