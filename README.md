# TransactionService

This application offers a simple REST api to create/read transactions and store them in a postgres db.
Each transaction has an amount attribute - when this amount exceeds a limit (configurable), 
a message is sent on a Kafka topic.

Dependencies (Kafka, Postgres) are provided in a docker container.

## How to run locally
Docker Desktop must be installed on your system.

1. Run the terminal command for this project (for example, from the Intellij terminal)
> docker compose up -d
2. Before starting the app, you need to execute the docker compose.yaml file defined in this repo https://github.com/NXTRS/Notifications
    because it will start the Keycloak container. App will start without it, but you cannot authenticate before running 
> docker compose up -d 
 
   in the Notifications project as well. 
   
3. Run the spring boot application. It will start on port **8080**.
4. The application db schema is generated automatically by spring 
   via the *"spring.jpa.generate-ddl: true"* property, nothing needs to be done
5. There is a Postman collection that can be imported to test the REST endpoints

## Integration tests

 Integration tests are running by making use of spring testcontainers, which spins up its own 
 docker container which holds the necessary components for integration testing (a test specific kafka & postgres)
