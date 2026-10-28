# SPE_Miniproject

A simple calculator REST API built with Spring Boot and Maven. The main focus of this project is the CI/CD pipeline around it: code is pushed to GitHub, Jenkins builds and tests it, packages it into a Docker image, pushes the image to Docker Hub and deploys it with Docker Compose behind an Nginx reverse proxy.

This is my mini-project for CSE 816 Software Production Engineering.

Docker Hub image: https://hub.docker.com/r/lakshyasachan/calculator

## Tech stack

Java 17, Spring Boot 3.5 (Spring Web and Actuator), Maven, JUnit 5 with MockMvc, Docker, Docker Compose, Nginx, Jenkins and GitHub.

## Project structure

```
SPE_Miniproject/
├── src/main/java/com/spe/calculator/   Spring Boot application (controller, service, models)
├── src/main/resources/                 application.properties
├── src/test/java/com/spe/calculator/   JUnit unit tests and MockMvc integration tests
├── nginx/nginx.conf                    Nginx reverse proxy config
├── pom.xml                             Maven build
├── Dockerfile                          Multi-stage image build
├── .dockerignore
├── docker-compose.yml                  API + Nginx stack
├── Jenkinsfile                         Declarative CI/CD pipeline
├── .gitignore
└── README.md
```

## API

All endpoints take two numbers `a` and `b` as query parameters and return JSON.

| Operation | Endpoint | Example result |
|---|---|---|
| Add | `GET /api/v1/add?a=2&b=3` | `{"operation":"add","a":2.0,"b":3.0,"result":5.0}` |
| Subtract | `GET /api/v1/subtract?a=10&b=4` | `{"operation":"subtract","a":10.0,"b":4.0,"result":6.0}` |
| Multiply | `GET /api/v1/multiply?a=6&b=7` | `{"operation":"multiply","a":6.0,"b":7.0,"result":42.0}` |
| Divide | `GET /api/v1/divide?a=10&b=4` | `{"operation":"divide","a":10.0,"b":4.0,"result":2.5}` |
| Health | `GET /actuator/health` | `{"status":"UP"}` |

Invalid input returns HTTP 400 with an error body. This happens for division by zero, a missing parameter, a non-numeric value, or NaN/Infinity. For example `GET /api/v1/divide?a=1&b=0` returns:

```json
{"timestamp":"2026-10-28T21:36:50Z","status":400,"error":"Bad Request","message":"Division by zero is not allowed","path":"/api/v1/divide"}
```

## Build, test and run locally

You need Java 17 and Maven.

```
mvn clean package        # compile, run tests and build target/calculator.jar
mvn test                 # run tests only, reports go to target/surefire-reports/
java -jar target/calculator.jar
```

The app then runs on http://localhost:8080. Try it with `curl "http://localhost:8080/api/v1/add?a=2&b=3"`.

## Run with Docker

Build and run the image on its own:

```
docker build -t lakshyasachan/calculator .
docker run --rm -p 8080:8080 lakshyasachan/calculator
```

Or pull the published image from Docker Hub with `docker pull lakshyasachan/calculator:latest`.

The Dockerfile has two stages. The first uses a Maven and JDK 17 image to build the JAR, and the second copies only that JAR into a slim JRE 17 Alpine image. The app runs as a non-root user, port 8080 is exposed, and dependencies are downloaded in their own layer so they are cached between builds.

## Run the full stack with Docker Compose

```
docker compose up -d
docker compose ps
```

This starts two containers on a shared `calc-net` network:

- `calculator` is the API. It is not published to the host and has a health check on `/actuator/health`.
- `nginx` is a reverse proxy on port 80 that forwards requests to the API. It starts only after the API is healthy.

Both have the `unless-stopped` restart policy. The API is then available at http://localhost, for example http://localhost/api/v1/add?a=2&b=3. Stop everything with `docker compose down`.

## CI/CD pipeline

The `Jenkinsfile` defines a declarative pipeline. Jenkins polls GitHub every 2 minutes and starts a build when `main` changes.

| Stage | What it does |
|---|---|
| Checkout | Pulls the latest code from GitHub |
| Build | `mvn clean package -DskipTests` |
| Test | `mvn test`, publishes JUnit results and fails the build if a test fails |
| Docker Build | Builds the image and tags it with the Jenkins build number and `latest` |
| Push | Logs in to Docker Hub with Jenkins stored credentials and pushes both tags |
| Deploy | Runs `docker compose up -d` with the new tag, waits for the API to be healthy and calls it through Nginx |

After every build Jenkins sends an email with the result (success or failure). The Docker Hub token and Gmail app password are kept in the Jenkins credentials store and never appear in this repository. Re-running the pipeline is safe because Compose only recreates containers whose image or config changed.

## Jenkins setup

Jenkins runs natively on Ubuntu (WSL2) and uses Docker Desktop through the WSL integration. The setup in short:

1. Install `openjdk-21-jdk-headless`, `maven` and `jenkins` with apt, then run `sudo usermod -aG docker jenkins` and restart Jenkins.
2. Install the suggested plugins (Pipeline, Git, JUnit, Credentials Binding and Email Extension are needed).
3. Add two global credentials: `dockerhub-creds` (Docker Hub username and access token) and `gmail-smtp` (Gmail address and app password).
4. Configure Extended E-mail Notification with `smtp.gmail.com`, port 465, SSL and the `gmail-smtp` credential.
5. Create a Pipeline job using "Pipeline script from SCM" with this repository, branch `*/main` and script path `Jenkinsfile`.

Before working on the project or giving a demo, start Docker Desktop, open an Ubuntu terminal and keep it open (WSL stops Jenkins when Ubuntu is idle), then open Jenkins at http://localhost:8080.
