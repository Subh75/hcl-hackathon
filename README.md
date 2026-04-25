# Favourite Payee

Microservices architecture for the Favourite Payee banking application.

## Services

- auth-service (port 8081)
- payee-service (port 8082)
- bank-scoring-service (port 8083)
- api-gateway (port 8080)
- frontend (port 4200)

## Run Locally

### Auth Service
cd auth-service
mvn spring-boot:run

### Payee Service
cd payee-service
mvn spring-boot:run

### Bank Scoring Service
cd bank-scoring-service
mvn spring-boot:run

### API Gateway
cd api-gateway
mvn spring-boot:run

### Frontend
cd frontend
npm install
ng serve

## API Base URL
http://localhost:8080

## Docker Compose

docker-compose up --build
