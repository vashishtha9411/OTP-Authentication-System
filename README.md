**WeCredit OTP Authentication API**

This project implements mobile number and OTP based authentication with JWT security using Spring Boot. It supports user registration, OTP generation and verification, JWT token issuance and validation, device fingerprint checks, and secure APIs. The project can run locally or inside a Docker container.

First, create a MySQL database named weCredit. Then update the application.properties file with your MySQL credentials and connection URL as below:

spring.datasource.url=jdbc:mysql://host.docker.internal:3306/weCredit
spring.datasource.username= Your_DatabaseName
spring.datasource.password= Your_DatabasePassword

To build the project locally, open your terminal in the project root folder and run:
mvn clean package

After successful build, run the application locally by:
java -jar target/auth-0.0.1-SNAPSHOT.jar

The application will start on port 8080, accessible at http://localhost:8080.

Alternatively, to run using Docker, build the Docker image with:
docker build -t wecredit-auth-app .

Run the container with:
docker run -d -p 8080:8080 --name wecredit-auth-container wecredit-auth-app

Check running containers by:
docker ps

To stop the container:
docker stop wecredit-auth-container

To remove the container:
docker rm wecredit-auth-container

API Endpoints and Usage:
Register a user:
POST http://localhost:8080/api/auth/register
Body (JSON):
{
"name": "John Doe",
"mobile": "98765****"
}

Request login OTP:
POST http://localhost:8080/api/auth/login/request
Body (JSON):
{
"mobile": "98765****",
"fingerprint": "unique-device-fingerprint"
}

Verify OTP and get JWT token:
POST http://localhost:8080/api/auth/login/verify
Body (JSON):
{
"mobile": "987654***",
"otp": "123456",
"fingerprint": "unique-device-fingerprint"
}

Response:
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Resend OTP:
POST http://localhost:8080/api/auth/resend-otp
Body (JSON):
{
"mobile": "987654****"
}

Get logged-in user info:
GET http://localhost:8080/api/auth/me
Add HTTP header:
Authorization: Bearer <JWT_TOKEN>

Replace <JWT_TOKEN> with the token received from the OTP verify response.

Testing with Postman:
Use the above URLs and methods.

For POST requests, set Body -> raw -> JSON and paste the request JSON.

After login verify, copy the token from response.

For /api/auth/me request, add Header Authorization with value Bearer <JWT_TOKEN>.

Use the same fingerprint value when requesting OTP and verifying OTP.

This project works both locally and in Docker. When running inside Docker, it connects to MySQL on the host using host.docker.internal.

Author: Yash Vashishtha
GitHub: https://github.com/vashishtha9411

