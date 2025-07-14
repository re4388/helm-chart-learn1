# Docker Compose Setup Guide

This guide explains how to run the Demo Spring Boot application with MongoDB and MinIO using Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB of available RAM

## Quick Start

### 1. Build and Start All Services

```bash
# Build the application first
cd application
mvn clean package
cd ..

# Start all services
docker-compose up -d
```

### 2. Verify Services

```bash
# Check all services are running
docker-compose ps

# Check logs
docker-compose logs -f demo-app
```

### 3. Access the Services

- **Spring Boot App**: http://localhost:8080
- **MinIO Console**: http://localhost:9003 (minioadmin/minioadmin)
- **MongoDB**: localhost:27017 (admin/password)

## Service Details

### MongoDB
- **Port**: 27017
- **Database**: demo
- **Admin User**: admin/password
- **App User**: demo_user/demo_password
- **Data Volume**: `mongodb_data`

### MinIO
- **API Port**: 9002 (mapped to container port 9000)
- **Console Port**: 9003 (mapped to container port 9001)
- **Credentials**: minioadmin/minioadmin
- **Data Volume**: `minio_data`

### Spring Boot Application
- **Port**: 8080
- **Profile**: docker
- **Logs Volume**: `app_logs`
- **Health Check**: http://localhost:8080/actuator/health

## Configuration

### Environment Variables

Copy `.env.example` to `.env` and modify as needed:

```bash
cp .env.example .env
```

### Application Profiles

The application uses the `docker` profile when running in containers, which:
- Connects to MongoDB using service name `mongodb`
- Connects to MinIO using service name `minio`
- Uses environment variables for configuration

## Common Commands

### Start Services
```bash
# Start all services in background
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up -d mongodb
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This deletes all data)
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f demo-app
docker-compose logs -f mongodb
docker-compose logs -f minio
```

### Rebuild Application
```bash
# After code changes, rebuild the app
cd application
mvn clean package
cd ..
docker-compose build demo-app
docker-compose up -d demo-app
```

## Testing the Application

### Basic Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Test File Upload
```bash
# Create a test file
echo "Hello World" > test.txt

# Upload to MinIO via the app
curl -X POST \
  -F "file=@test.txt" \
  -F "bucket=test-bucket" \
  -F "uploadedBy=testuser" \
  http://localhost:8080/api/files/upload

# List files in bucket
curl http://localhost:8080/api/files/list/test-bucket
```

### Test MongoDB Operations
```bash
# Get all posts
curl http://localhost:8080/api/posts

# Create a new post
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"test-post","title":"Test Post","content":"This is a test post"}'
```

## Troubleshooting

### Service Won't Start
```bash
# Check service status
docker-compose ps

# Check logs for errors
docker-compose logs [service-name]

# Restart specific service
docker-compose restart [service-name]
```

### MongoDB Connection Issues
```bash
# Check MongoDB logs
docker-compose logs mongodb

# Connect to MongoDB directly
docker-compose exec mongodb mongosh -u admin -p password
```

### MinIO Connection Issues
```bash
# Check MinIO logs
docker-compose logs minio

# Verify MinIO health
curl http://localhost:9002/minio/health/live
```

### Application Issues
```bash
# Check application logs
docker-compose logs demo-app

# Check application health
curl http://localhost:8080/actuator/health

# Restart application
docker-compose restart demo-app
```

### Clean Reset
```bash
# Stop everything and remove volumes
docker-compose down -v

# Remove images (optional)
docker-compose down --rmi all

# Start fresh
docker-compose up -d
```

## Data Persistence

- **MongoDB Data**: Stored in `mongodb_data` volume
- **MinIO Data**: Stored in `minio_data` volume  
- **Application Logs**: Stored in `app_logs` volume

To backup data:
```bash
# Backup MongoDB
docker-compose exec mongodb mongodump --out /data/backup

# Backup MinIO (access via console or mc client)
```

## Production Considerations

For production deployment, consider:

1. **Security**: Change default passwords and use secrets
2. **Networking**: Use custom networks and limit exposed ports
3. **Monitoring**: Add monitoring and logging solutions
4. **Backup**: Implement automated backup strategies
5. **Scaling**: Use Docker Swarm or Kubernetes for scaling
6. **SSL/TLS**: Add reverse proxy with SSL termination

## Next Steps

1. Test all endpoints using the provided curl commands
2. Access MinIO console to verify file storage
3. Connect to MongoDB to verify data persistence
4. Check application logs for any issues
5. Customize configuration as needed for your environment

For more detailed API documentation, see [API_DOCUMENTATION.md](application/docs/API_DOCUMENTATION.md).