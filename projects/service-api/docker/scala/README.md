# flashwiki_scalatest
A project for testing scala code in flash.wiki flashcards

# Build

`docker build -t flash.wiki/scalatest .`

The container is built on `hseeberger/scala-sbt` with a `./start.sh` command added on container start up.

# Operation

When the container loads it will run `./start.sh` and this bash script will overwrite two source files in the scala project, the exercise file and it's associated test file.

Then the start script will run `sbt test`. If the output contains `*"All tests passed."*` then it will print `pass` in the first line of STDOUT.