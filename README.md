# AWS Data Processing Pipeline

[![CI Pipeline](https://github.com/serhatsoysal/aws-data-pipeline/actions/workflows/ci.yml/badge.svg)](https://github.com/serhatsoysal/aws-data-pipeline/actions/workflows/ci.yml)
[![CodeQL](https://github.com/serhatsoysal/aws-data-pipeline/actions/workflows/codeql.yml/badge.svg)](https://github.com/serhatsoysal/aws-data-pipeline/actions/workflows/codeql.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=serhatsoysal_aws-data-pipeline&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=serhatsoysal_aws-data-pipeline)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=serhatsoysal_aws-data-pipeline&metric=coverage)](https://sonarcloud.io/summary/new_code?id=serhatsoysal_aws-data-pipeline)
[![codecov](https://codecov.io/gh/serhatsoysal/aws-data-pipeline/branch/master/graph/badge.svg)](https://codecov.io/gh/serhatsoysal/aws-data-pipeline)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A production-ready, enterprise-grade data processing pipeline demonstrating professional AWS architecture with Java 17, Spring Boot 3, and Terraform Infrastructure as Code.

## Architecture Overview

```
┌─────────────┐     ┌──────────────┐     ┌──────────┐
│   Client    │────▶│     ALB      │────▶│   EC2    │
└─────────────┘     └──────────────┘     │ (Spring) │
                                          └────┬─────┘
                                               │
                         ┌─────────────────────┼─────────────────────┐
                         │                     │                     │
                         ▼                     ▼                     ▼
                    ┌─────────┐          ┌─────────┐          ┌──────────┐
                    │   S3    │          │   RDS   │          │   SNS    │
                    │  (Raw)  │          │(Postgres)         │  Topic   │
                    └────┬────┘          └─────────┘          └────┬─────┘
                         │                                          │
                         │                                          ▼
                         │                                     ┌─────────┐
                         │                                     │   SQS   │
                         │                                     │  Queue  │
                         │                                     └────┬────┘
                         │                                          │
                         │                                          ▼
                         │                                     ┌──────────┐
                         │                                     │  Lambda  │
                         │                                     │Processor │
                         │                                     └────┬─────┘
                         │                                          │
                         │         ┌────────────────────────────────┤
                         │         │                                │
                         ▼         ▼                                ▼
                    ┌──────────────────┐                      ┌──────────┐
                    │  S3 (Processed)  │                      │RDS Proxy │
                    └──────────────────┘                      └──────────┘
```

## Business Use Case

This system implements an **asynchronous file processing pipeline** where:

1. Users upload files via REST API (hosted on EC2)
2. Files are stored in S3 and metadata saved to PostgreSQL
3. SNS broadcasts upload events to subscribed services
4. SQS queues processing tasks with fault tolerance (DLQ)
5. Lambda function processes files asynchronously
6. Processed files are stored separately with updated status
7. Users can query processing status and download results

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.1 |
| IaC | Terraform | ≥ 1.6.0 |
| Build Tool | Maven | 3.8+ |
| Database | PostgreSQL | 15.4 |
| AWS SDK | AWS SDK for Java | 2.21.0 |
| Runtime | Amazon Corretto | 17 |

## AWS Services Used

### EC2 (Elastic Compute Cloud)
Hosts the Spring Boot REST API with Auto Scaling Group and Application Load Balancer for high availability.

### S3 (Simple Storage Service)
- **Raw Bucket**: Stores uploaded files with versioning and lifecycle policies (Standard → IA → Glacier)
- **Processed Bucket**: Stores transformed files with encryption at rest

### Lambda
Serverless function with **SnapStart** enabled for Java 17, reducing cold start latency by 90%. Processes files asynchronously with automatic scaling.

### RDS (Relational Database Service)
PostgreSQL database with **Multi-AZ** deployment and **RDS Proxy** for connection pooling, essential for Lambda's connection management.

### SNS (Simple Notification Service)
Publishes file upload events in a **pub/sub** pattern, enabling fan-out to multiple subscribers.

### SQS (Simple Queue Service)
Queues processing tasks with **Dead Letter Queue (DLQ)** for failed messages, decoupling upload from processing.

## Project Structure

```
aws-data-pipeline/
├── terraform/                      # Infrastructure as Code
│   ├── backend.tf                  # S3 + DynamoDB state management
│   ├── provider.tf                 # AWS provider configuration
│   ├── variables.tf                # Input variables
│   ├── outputs.tf                  # Resource outputs
│   ├── main.tf                     # Root module orchestration
│   └── modules/
│       ├── networking/             # VPC, subnets, NAT, security groups
│       ├── storage/                # S3 buckets with policies
│       ├── compute/                # EC2, ASG, ALB
│       ├── database/               # RDS with Proxy, Multi-AZ
│       ├── messaging/              # SNS topics, SQS queues
│       └── serverless/             # Lambda functions
├── java-application/
│   ├── pom.xml                     # Parent POM with dependency management
│   ├── shared-config/              # Centralized AWS client configuration
│   │   └── src/main/java/com/datapipeline/config/
│   │       ├── AwsClientConfig.java       # Singleton AWS SDK clients
│   │       ├── EnvironmentConfig.java     # Environment variable loader
│   │       └── Constants.java             # Application constants
│   ├── ec2-api-service/            # Spring Boot REST API
│   │   └── src/main/java/com/datapipeline/api/
│   │       ├── Application.java
│   │       ├── controller/                # REST endpoints
│   │       ├── service/                   # Business logic
│   │       ├── repository/                # Data access layer
│   │       └── model/                     # JPA entities
│   └── lambda-processor/           # Lambda function
│       └── src/main/java/com/datapipeline/lambda/
│           ├── handler/FileProcessorHandler.java
│           ├── service/                   # S3, RDS, transformation
│           └── model/                     # Event models
├── .env.example                    # Environment variables template
├── .gitignore
└── README.md
```

## Prerequisites

- **AWS Account** with appropriate IAM permissions
- **Terraform** ≥ 1.6.0
- **Java JDK** 17 or higher
- **Maven** 3.8 or higher
- **AWS CLI** configured with credentials

## Setup Instructions

### 1. Configure Environment Variables

Copy the environment template:

```bash
cp .env.example .env
```

Edit `.env` with your AWS configuration:

```bash
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=123456789012
S3_BUCKET_RAW=data-pipeline-production-raw
S3_BUCKET_PROCESSED=data-pipeline-production-processed
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:123456789012:data-pipeline-production-file-events
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/data-pipeline-production-processing
RDS_PROXY_ENDPOINT=data-pipeline-production-rds-proxy.proxy-xxxxx.us-east-1.rds.amazonaws.com
RDS_ENDPOINT=data-pipeline-production-db.xxxxx.us-east-1.rds.amazonaws.com
DB_NAME=pipeline
DB_USERNAME=admin
DB_PASSWORD=<your-secure-password>
DB_PORT=5432
JWT_SECRET=<your-generated-secret>
```

### 1.1. Generate Secure JWT Secret

**Important**: Never use the placeholder JWT_SECRET from `.env.example` in production.

**Windows PowerShell**:
```powershell
$bytes = New-Object byte[] 64
[Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

**Linux/Mac**:
```bash
openssl rand -base64 64
```

Copy the generated key and replace `JWT_SECRET` in your `.env` file.

**Security Notes**:
- Use cryptographically secure random generators (minimum 512-bit)
- Never commit `.env` to version control
- Rotate secrets regularly
- Use different secrets for each environment (dev, staging, production)

### 2. Deploy Infrastructure with Terraform

```bash
cd terraform

# Initialize Terraform (downloads providers)
terraform init

# Preview infrastructure changes
terraform plan

# Deploy all resources
terraform apply

# Note outputs for environment variables
terraform output
```

**Important**: Update your `.env` file with Terraform outputs (bucket names, ARNs, endpoints).

### 3. Build Java Applications

```bash
cd java-application

# Build all modules (shared-config, ec2-api-service, lambda-processor)
mvn clean package
```

### 4. Deploy Lambda Function

```bash
# Upload Lambda JAR to S3
aws s3 cp lambda-processor/target/lambda-processor-1.0.0.jar \
    s3://your-lambda-deployment-bucket/

# Update Lambda function code
aws lambda update-function-code \
    --function-name data-pipeline-production-file-processor \
    --s3-bucket your-lambda-deployment-bucket \
    --s3-key lambda-processor-1.0.0.jar
```

### 5. Deploy EC2 Application

```bash
# Copy JAR to EC2 instance
scp -i your-key.pem ec2-api-service/target/ec2-api-service-1.0.0.jar \
    ec2-user@<ec2-public-ip>:/home/ec2-user/

# SSH into EC2
ssh -i your-key.pem ec2-user@<ec2-public-ip>

# Run application
java -jar ec2-api-service-1.0.0.jar
```

## API Documentation

### Base URL

```
http://<alb-dns-name>/api/files
```

### Endpoints

#### Upload File

```bash
POST /api/files/upload

# Example with curl
curl -X POST http://alb-dns-name/api/files/upload \
  -F "file=@document.txt" \
  -H "Content-Type: multipart/form-data"

# Response
{
  "id": 1,
  "fileKey": "raw/uuid-123.txt",
  "fileName": "document.txt",
  "status": "uploaded",
  "message": "File uploaded successfully"
}
```

#### Get File Metadata

```bash
GET /api/files/{id}

# Example
curl http://alb-dns-name/api/files/1

# Response
{
  "id": 1,
  "fileKey": "raw/uuid-123.txt",
  "fileName": "document.txt",
  "fileSize": 1024,
  "contentType": "text/plain",
  "status": "COMPLETED",
  "uploadedAt": "2024-01-06T10:30:00",
  "processedAt": "2024-01-06T10:30:15",
  "processedFileKey": "processed/uuid-123.txt"
}
```

#### Get All Files

```bash
GET /api/files

curl http://alb-dns-name/api/files
```

#### Download Processed File

```bash
GET /api/files/download/{id}

curl -O http://alb-dns-name/api/files/download/1
```

## Code Highlights

### AWS Client Configuration

`AwsClientConfig.java` implements the **Singleton pattern** with double-checked locking for thread-safe AWS SDK client creation. Clients are configured with retry policies and connection timeouts.

### S3 Transfer Manager

`S3Service.java` uses **S3TransferManager** for efficient multipart uploads, automatically handling large files (>5MB) by splitting them into chunks.

### IAM Authentication

`RdsProxyService.java` generates temporary authentication tokens instead of hardcoded passwords, implementing **IAM Database Authentication** for enhanced security.

### Event-Driven Architecture

SNS publishes events to SQS, which triggers Lambda. This **decoupling** ensures the upload API responds immediately while processing happens asynchronously.

## Terraform Modules Explanation

| Module | Resources Provisioned |
|--------|----------------------|
| `networking` | VPC, 2 public subnets, 2 private subnets, Internet Gateway, NAT Gateway, Route Tables |
| `storage` | S3 buckets with versioning, encryption (AES256), lifecycle policies, public access blocking |
| `compute` | Launch Template, Auto Scaling Group (1-3 instances), Application Load Balancer, Target Group |
| `database` | RDS PostgreSQL Multi-AZ, RDS Proxy, Secrets Manager for credentials, DB Subnet Group |
| `messaging` | SNS topic, SQS queue, Dead Letter Queue, SQS → Lambda event source mapping |
| `serverless` | Lambda function with SnapStart, IAM role, VPC configuration, CloudWatch Logs |

## Spring Boot Annotations Reference

| Annotation | Purpose |
|-----------|---------|
| `@SpringBootApplication` | Enables auto-configuration and component scanning |
| `@RestController` | Marks class as REST API controller returning JSON |
| `@Service` | Declares business logic component for dependency injection |
| `@Repository` | Marks data access layer interface (Spring Data JPA) |
| `@Entity` | Designates JPA entity mapped to database table |
| `@Transactional` | Manages database transactions automatically |
| `@Value` | Injects property values from application.yml |
| `@RequestMapping` | Maps HTTP requests to controller methods |
| `@PostMapping` / `@GetMapping` | Shorthand for POST/GET method mappings |

## Architecture Decisions

### Why Lambda SnapStart?
Java applications suffer from cold start latency. SnapStart creates a snapshot after initialization, reducing startup time from ~10s to ~1s.

### Why RDS Proxy?
Lambda functions scale to thousands of concurrent executions. Without RDS Proxy, each would create a database connection, exhausting the connection pool. Proxy pools and reuses connections.

### Why Multi-AZ?
RDS Multi-AZ automatically replicates data to a standby instance in a different availability zone. If the primary fails, AWS promotes the standby (failover) with ~60 seconds downtime.

### Why SNS + SQS (instead of just SQS)?
SNS enables **fan-out**: one upload event can trigger multiple processing pipelines (Lambda, analytics, archival) by adding more SQS subscribers without changing the upload API.

## Cost Estimation

Approximate monthly costs (assuming moderate usage):

| Service | Configuration | Est. Cost |
|---------|--------------|-----------|
| EC2 | 1x t3.medium (730 hrs) | $30 |
| RDS | db.t3.micro Multi-AZ | $30 |
| S3 | 100 GB storage + requests | $3 |
| Lambda | 1M requests, 1024MB, 5s avg | $20 |
| Data Transfer | 50 GB out | $5 |
| **Total** | | **~$88/month** |

*Actual costs vary by usage. Use AWS Cost Calculator for precise estimates.*

## Cleanup

To avoid ongoing charges, destroy all resources:

```bash
cd terraform
terraform destroy
```

Confirm with `yes` when prompted. This removes all provisioned infrastructure.

## Professional Patterns Applied

- **DRY (Don't Repeat Yourself)**: Shared configuration module eliminates duplication
- **SOLID Principles**: Single responsibility for each service class
- **Dependency Injection**: Spring Boot's IoC container manages object lifecycle
- **Singleton Pattern**: AWS clients instantiated once and reused
- **Event-Driven Architecture**: Asynchronous processing with SNS/SQS
- **Infrastructure as Code**: Terraform ensures reproducible environments
- **Security Best Practices**: No hardcoded credentials, IAM roles, encryption at rest/in-transit

## Scalability Features

- **Auto Scaling Group**: EC2 instances scale 1-3 based on CPU utilization
- **Lambda Concurrency**: Automatically scales to handle bursts (up to 1000 concurrent by default)
- **RDS Read Replicas**: Can be added for read-heavy workloads
- **S3 Unlimited Capacity**: No storage limits
- **SQS Throughput**: Handles unlimited messages per second

## High Availability

- **Multi-AZ RDS**: Automatic failover to standby instance
- **Multi-Subnet EC2**: Instances distributed across availability zones
- **Application Load Balancer**: Health checks and automatic traffic routing
- **S3 Durability**: 11 nines (99.999999999%) durability across multiple facilities

## Security Highlights

- **VPC Isolation**: Private subnets for Lambda and RDS
- **Security Groups**: Least-privilege network access rules
- **IAM Roles**: No access keys in code
- **Encryption at Rest**: S3 (AES256), RDS (default encryption)
- **Encryption in Transit**: TLS for all AWS API calls
- **Secrets Manager**: RDS credentials never in code or logs

## Future Enhancements

- **API Gateway**: Replace ALB with API Gateway for better Lambda integration
- **DynamoDB**: Add NoSQL for high-speed metadata queries
- **CloudFront**: CDN for processed file distribution
- **EventBridge**: Advanced event routing with custom rules
- **Step Functions**: Orchestrate complex multi-step workflows
- **X-Ray**: Distributed tracing for performance analysis

## Contributing

This is a portfolio project demonstrating professional AWS architecture patterns. Feedback and suggestions are welcome via issues.

## License

MIT License - Free to use for learning and portfolio purposes.

---

**Author**: Senior Java Engineer  
**Date**: January 2026  
**Purpose**: Enterprise-grade AWS portfolio demonstration

