$mvn = "d:\H3\apache-maven-3.9.6\bin\mvn.cmd"

Write-Host "Starting auth-service on port 8081..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\auth-service'; & '$mvn' clean spring-boot:run"

Write-Host "Starting payee-service on port 8082..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\payee-service'; & '$mvn' clean spring-boot:run"

Write-Host "Starting bank-scoring-service on port 8083..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\bank-scoring-service'; & '$mvn' clean spring-boot:run"

Write-Host "Starting api-gateway on port 8080..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\api-gateway'; & '$mvn' clean spring-boot:run"

Write-Host "Starting frontend on port 4200..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\frontend'; npm start"

Write-Host "All services started in separate windows."
