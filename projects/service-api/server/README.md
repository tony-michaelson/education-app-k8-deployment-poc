REST API Server
=========================== 

# Local K8s Initial Setup

> From the `server` folder:

`./build_docker.sh`

`./deploy_local.sh $(pwd)`

> Now you can connect to the PG Web server and browse the database:

[http://localhost:8081](http://localhost:8081)

> Next you must ssh into the REST API pod.

```bash
API_POD_NAME=$(kubectl get pods | grep 'api' | awk -F" +" '{print $1}')
kubectl exec --stdin --tty $API_POD_NAME -- bash
```

> Once inside, then compile the code to ensure schema is generated in the databes.

```bash
bash-4.4# cd /app
bash-4.4# ./scripts/sbt_run.sh compile
...
[success] Total time: 27 s, completed Aug 11, 2021, 9:15:22 PM
```

> Once completed, run the server with:

```bash
./scripts/sbt_run.sh
...
[info] play.api.Play - Application started (Dev) (no global state)
```

> Note! The first HTTP request will cause the server to auto compile the classes generated from the SQL schema once more. Then it will only recompile if changes are made to source files in the project scope.

# Development Environment

- Intellij

[Scalafmt](https://scalameta.org/scalafmt/) is used for code formatting with Intellij.

- Kubernetes

See parent README.md

- Postman

`MPIO.postman_collection.json` is available for manually testing API endpoints. You must obtain an `access_token` from the browser's local storage after a successful UI login or by using the Auth0 `resource owner password` flow. Then set the token value as a request header for all the API requests to use. 

- Flyway / codegen workflow

Flyway is used for managing the Postgresql database schema and Slick Codegen for generating initial Scala code needed for generating SQL queries.

### Project Commands

#### Scripts

| Script Name | Description |
| ----------- | ----------- |
| generateTypescriptAPI.sh | This script runs `sbt generateTypescript` and a few transformations on the generated code then writes the output to `../client/src/models/api.ts` for consumption by the client UI code. |  
| sbt_run.sh | This script starts the API backend by default. You can also run the integration tests suite with `sbt_run.sh test` or the PSQL constraits with `sbt_run.sh testPSQLConstraints` |
| start_dev.sh | This command will assign the necessary environment variables then run `docker-compose up` |
| test-psql-constraints.py | This script tests the errors given by the REST API service for any requests that can possibly violate PSQL constraints defined on the database tables |
     
#### Standing up system

To stand up the system in your local environment, run `./scripts/start_dev.sh`. On the first run, the `postgres` server might die. Hit `ctrl-c` and run it again if it does.

Once running, you can manually start the API service with `./scripts/sbt_run.sh compileRun`. This will start the play application with a watch mode and auto re-compilation of any changes. 

#### Deployment

Production deployment has not been defined.

### SBT

#### Plugins

| Name | Purpose |
| ---- | ------- |
| [flyway-sbt](https://davidmweber.github.io/flyway-sbt-docs/) | Flyway SBT Tasks |
| [sbt-scala-tsi](https://mvnrepository.com/artifact/com.scalatsi/scala-tsi) | Generate Typescript models from Scala Classes |
| [play % sbt-plugin](https://mvnrepository.com/artifact/com.typesafe.play/play) | Play Server |
| [sbt-slick-codegen](https://github.com/tototoshi/sbt-slick-codegen) | SBT plugin for Slick codegen |

#### Dependencies

| Name | Purpose |
| ---- | ------- |
| [play-slick](https://mvnrepository.com/artifact/com.typesafe.play/play-slick) | Manages DB configuration and connection pool for slick |
| [jackson-module-scala](https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala) | [Resolve Issue](https://github.com/FasterXML/jackson-module-scala/issues/513) |
| [com.amazonaws](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3/1.12.10) | Amazon AWS SDK |
| [postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql) | Database driver for slick |
| [jwt-play](https://mvnrepository.com/artifact/com.pauldijou/jwt-play) | Provides `{JwtAlgorithm, JwtBase64, JwtClaim, JwtJson}` |
| [jwks-rsa](https://mvnrepository.com/artifact/com.auth0/jwks-rsa) | Provides `com.auth0.jwk.UrlJwkProvider` for validation of public keys |
| [scala-guice](https://mvnrepository.com/artifact/net.codingwell/scala-guice) | Scala syntax for Guice |
| [ficus](https://mvnrepository.com/artifact/com.iheart/ficus) | A Scala-friendly wrapper companion for Typesafe config |
| [play-json-extensions](https://mvnrepository.com/artifact/ai.x/play-json-extensions) | Used for serialization of case classes > 22 fields |
| [enumeratum-play-json](https://mvnrepository.com/artifact/com.beachape/enumeratum-play-json) | Superior enumerations library |
| [sendgrid-java](https://mvnrepository.com/artifact/com.sendgrid/sendgrid-java) | Sending email via Sendgrid API |
| [flexmark](https://mvnrepository.com/artifact/com.vladsch.flexmark/flexmark) | Core of flexmark-java (implementation of CommonMark for parsing markdown and rendering to HTML) |
| [google-cloud-storage](https://mvnrepository.com/artifact/com.google.cloud/google-cloud-storage) | Java idiomatic client for Google Cloud Storage |
| [com.sksamuel.scrimage](https://mvnrepository.com/artifact/com.sksamuel.scrimage/scrimage-core) | JVM Image Library |

#### Sub-projects

Flyway and Slick and defined as sub-projects with folders in the `modules` folder. The definition for the slickCodegen task is in the `modules/slick/build.sbt` file.

### Todo

- Project Nomenclature
- Development Standards
- REST API
- Scaladoc
- Cloud Architecture
- DB Schema
- Repository Pattern
- Authorization
- Maps
- Flashcards
- Exercises