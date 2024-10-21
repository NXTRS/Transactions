# TransactionService

This application offers a simple REST api to create/read transactions and store them in a postgres db.
Each transaction has an amount attribute - when this amount exceeds a limit (configurable), 
a message is sent on a Kafka topic.

Dependencies (Kafka, Postgres) are provided in a docker container.

## How to run locally
Docker Desktop must be installed on your system.

1. Run the terminal command (for example, from the Intellij temrinal)
> docker compose up -d
2. Run the spring boot application. It will start on port **8080**.
3. The application db schema is generated automatically by spring 
   via the *"spring.jpa.generate-ddl: true"* property
4. There is a Postman collection that can be imported to test the REST endpoints

## Integration tests

 Integration tests are running by making use of spring testcontainers, which spins up its own 
 docker container which holds the necessary components for integration testing (a test specific kafka & postgres)
