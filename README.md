# Minimal Spring Service

A minimal Spring Boot project that can run locally with a `dev` profile and deploy to Kubernetes with Docker.

## Requirements

- Java 17
- Maven 3.9+
- Docker
- kubectl

## Start MySQL locally

Run MySQL with Docker Compose:

```bash
docker compose up -d mysql
```

Check status:

```bash
docker compose ps
docker compose logs -f mysql
```

The local MySQL instance uses:

- Host: `localhost`
- Port: `3306`
- Database: `minimal_service`
- Username: `appuser`
- Password: `appsecret`

Stop MySQL:

```bash
docker compose down
```

If you want to remove the database data as well:

```bash
docker compose down -v
```

## Run locally

Use IntelliJ IDEA to run `MinimalSpringApplication` with the active profile set to `dev`, or run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

You can also export the profile first:

```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

Open [http://localhost:8080/api/hello](http://localhost:8080/api/hello) to verify the app is running.

If MySQL is running locally, you can also verify the database connection:

```bash
curl http://localhost:8080/api/db/ping
```

The response includes the database product name, JDBC URL, and a simple validation query result.

## Build the jar

```bash
mvn clean package
```

## Build and run with Docker

```bash
docker build -t minimal-spring-service:0.0.1-SNAPSHOT .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=cloud -e APP_MESSAGE="Hello from Docker" minimal-spring-service:0.0.1-SNAPSHOT
```

## Deploy to Kubernetes

1. Build the image and push it to your image registry.
2. Replace `your-registry/minimal-spring-service:0.0.1-SNAPSHOT` in `k8s/deployment.yaml`.
3. Apply the manifests:

```bash
kubectl apply -f k8s/
```

4. Expose the service locally for testing:

```bash
kubectl port-forward service/minimal-spring-service 8080:80
```

Then open [http://localhost:8080/api/hello](http://localhost:8080/api/hello).
