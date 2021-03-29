addSbtPlugin("org.jetbrains" % "sbt-ide-settings"    % "1.1.0")
addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % "1.5.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
addSbtPlugin("io.crashbox"   % "sbt-gpg"             % "0.2.1")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"