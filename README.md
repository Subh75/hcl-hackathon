# Favourite Payee

Full stack app - Java Spring Boot + Angular

## Setup
### Backend
cd backend
mvn spring-boot:run

### Frontend
cd frontend
npm install
ng serve

## API Base URL
http://localhost:8080/api

## Branch Strategy
main
|- feature/auth         <- Dev A
|- feature/payee-list   <- Dev B
|- feature/payee-crud   <- Dev C
\- feature/search-smart <- Dev D

Each dev runs on their machine:
git checkout -b feature/<their-feature>

Push changes:
git add .
git commit -m "descriptive message"
git push origin feature/<their-feature>

Merge to main only when feature is complete and tested.
Always git pull origin main before starting work each session.
Never commit directly to main.
