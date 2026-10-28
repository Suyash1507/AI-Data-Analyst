# AI-Data-Analyst
Users upload CSV or Excel files and ask questions in plain English. A team of agents cleans the data, writes and executes Python, generates charts, and produces a business report.

## Backend database

The backend uses PostgreSQL. Start PostgreSQL and create a database named `ai_data_analyst`, or override `SPRING_DATASOURCE_URL` with your database's JDBC URL. The default connection is `jdbc:postgresql://localhost:5432/ai_data_analyst` with username `postgres`. Set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` to match your PostgreSQL credentials.

On Windows, set credentials in the terminal before starting the backend:

```powershell
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "<your PostgreSQL password>"
cd backend
.\mvnw.cmd spring-boot:run
```
