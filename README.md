# Online-store
Web application for an online product store based on a microservice architecture.
#### Prerequisites: [Java 21](https://jdk.java.net/21/), [Maven](https://maven.apache.org/), [PostgreSQL](https://www.postgresql.org/), [MongoDB](https://www.mongodb.com/), [Docker](https://www.docker.com/), [Keycloak](https://www.keycloak.org/), [Mockito](https://site.mockito.org/), [MockMvc](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html), [Grafana](https://grafana.com/), [VictoriaMetrics](https://victoriametrics.com/)

## Description
This project contains the following components:
* ```product-service``` - is responsible for working with the product catalog and contains a RestController for processing requests from clients.
* ```manager-service``` - is a client that processes manager requests and sends them to the product-service.
* ```customer-service``` - is a client that processes customer request and sends them to the product-service and feedback-service.
* ```feedback-service``` - is responsible for processing customer feedback.
* ```admin-server``` - is a module for administering services: viewing health and various metrics.

#### The application is covered with tests with using MockMvc and Mockito.

## Getting Started
### 1. Clone the repository
    git clone https://github.com/floMaxter/online-store-microservices.git
    cd online-store-microservices
or download zip archive

    https://github.com/floMaxter/online-store-microservices/archive/refs/heads/main.zip

### 2. Before starting application, you need to register Spring profiles:
* ```standalone``` - for starting all services.

### 3. Keycloak
OAuth 2.0/OIDC is used to authorize services and authenticate users.

Start in Docker:

    docker run --name product-delivery-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.7 start-dev --import-realm

### 4. PostgreSQL
A PostgreSQL is used to store information about products.

Start in Docker:

    docker run --name catalog-db -p 5434:5432 -e POSTGRES_USER=catalogue -e POSTGRES_PASSWORD=catalogue -e POSTGRES_DB=catalog postgres:16

### 5. MongoDB
A MongoDB is used to store feedback from customers.

Start in Docker:

    docker run --name feedback-db -p 27017:27017 mongo:7

### 6. Victoria Metrics
This component is used to collect and store metrics from services.

Start in Docker:

    docker run --name product-delivery-metrics -p 8428:8428 -v ./config/victoria-metrics/promscrape.yaml:/promscrape.yaml victoriametrics/victoria-m

* The command cannot be run on Windows, so it is recommended to use one of the Linux distributions or run via WSL.
* In the files of promscrape.yaml and application-standalone.yaml you must specify the IP address of the host on which the app is running.

### 7. Grafana
This component is used for visualization and analysis of metrics collected by Victoria Metrics

Start in Docker:

    docker run --name product-delivery-grafana -p 3000:3000 -v ./data/grafana:/var/lib/grafana -u "$(id -u)" grafana/grafana:10.2.4

 * In order to output an individual collection of metrics for each user, the command is used ```"$(id -u)"```.
 * The command cannot be run on Windows, so it is recommended to use one of the Linux distributions or run via WSL.

### 8. Grafana Loki
This component is used for centralized storage of logs.

Start in Docker:

    docker run --name product-delivery-loki -p 3100:3100 grafana/loki:2.9.4

* To enable logging at startup, you must specify in the environment variables ```LOKI=http://${host_ip}:3100```, where ```${host_ip}``` - the address of the host where the application is launched.

### 9. Grafana Tempo
This component is used for centralized collection of traces.

Start in Docker:

    docker run --name product-delivery-tracing -p 3200:3200 -p 9095:9095 -p 4317:4317 -p 4318:4318 -p 9411:9411 -p 14268:14268 -v ./config/tempo/tempo.yaml:/etc/tempo.yaml grafana/tempo:2.3.1 -config.file=/etc/tempo.yaml

* To access services via an ip address, rather than through localhost, you need to reconfigure clients in keycloak.