# MinIO File Storage Setup Guide

## Overview

This application uses MinIO as the object storage solution for file upload and download operations. MinIO is a high-performance, S3-compatible object storage system that's perfect for storing unstructured data such as photos, videos, log files, backups, and container images.

## MinIO Configuration

### Default Settings
- **MinIO Server Port**: `9002`
- **MinIO Console Port**: `9003`
- **Access Key**: `minioadmin`
- **Secret Key**: `minioadmin`
- **Default Bucket**: Auto-created based on API requests

### Application Configuration

The MinIO settings are configured in `src/main/resources/application.yml`:

```yaml
minio:
  endpoint: http://localhost:9002
  access-key: minioadmin
  secret-key: minioadmin

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

## Installation and Setup

### Option 1: Docker (Recommended)

```bash
# Start MinIO server with custom ports
docker run -d \
  --name minio-server \
  -p 9002:9000 \
  -p 9003:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v minio-data:/data \
  minio/minio server /data --console-address ":9001"
```

### Option 2: Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  minio:
    image: minio/minio
    container_name: minio-server
    ports:
      - "9002:9000"  # MinIO API
      - "9003:9001"  # MinIO Console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

volumes:
  minio-data:
```

Run with:
```bash
docker-compose up -d
```

### Option 3: Local Installation

1. Download MinIO from [https://min.io/download](https://min.io/download)
2. Make it executable and run:

```bash
# Linux/macOS
chmod +x minio
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin
./minio server /data --address ":9002" --console-address ":9003"

# Windows
set MINIO_ROOT_USER=minioadmin
set MINIO_ROOT_PASSWORD=minioadmin
minio.exe server C:\data --address ":9002" --console-address ":9003"
```

## Verification

### 1. Check MinIO Health
```bash
curl http://localhost:9002/minio/health/live
```

### 2. Access MinIO Console
Open your browser and go to: [http://localhost:9003](http://localhost:9003)
- Username: `minioadmin`
- Password: `minioadmin`

### 3. Test API Connection
```bash
# Test if MinIO is accessible
curl -I http://localhost:9002
```

## Bucket Management

### Automatic Bucket Creation
The application automatically creates buckets when files are uploaded. No manual bucket creation is required.

### Manual Bucket Creation (Optional)
You can create buckets manually through:

1. **MinIO Console**: Use the web interface at http://localhost:9003
2. **MinIO Client (mc)**:
```bash
# Install mc client
curl https://dl.min.io/client/mc/release/linux-amd64/mc -o mc
chmod +x mc

# Configure mc
./mc alias set local http://localhost:9002 minioadmin minioadmin

# Create bucket
./mc mb local/my-bucket
```

## Security Considerations

### Development Environment
- Default credentials are fine for development
- MinIO runs without TLS (HTTP only)

### Production Environment
1. **Change Default Credentials**:
```yaml
minio:
  access-key: your-production-access-key
  secret-key: your-production-secret-key-min-8-chars
```

2. **Enable TLS**:
```yaml
minio:
  endpoint: https://your-minio-server.com
```

3. **Network Security**:
- Use firewall rules to restrict access
- Consider running MinIO behind a reverse proxy
- Use VPC/private networks in cloud environments

## Troubleshooting

### Common Issues

1. **Port Already in Use**:
```bash
# Check what's using port 9002
lsof -i :9002
# Kill the process or use different ports
```

2. **Permission Denied**:
```bash
# Ensure data directory is writable
chmod 755 /data
```

3. **Connection Refused**:
- Verify MinIO is running: `docker ps` or `ps aux | grep minio`
- Check firewall settings
- Verify port configuration

### Logs and Debugging

```bash
# Docker logs
docker logs minio-server

# Check MinIO server status
curl http://localhost:9002/minio/health/ready
```

## Performance Tuning

### For High-Volume Applications

1. **Increase File Size Limits**:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 1GB
      max-request-size: 1GB
```

2. **MinIO Performance Settings**:
```bash
# Set environment variables for better performance
export MINIO_API_REQUESTS_MAX=1000
export MINIO_API_REQUESTS_DEADLINE=10s
```

3. **Hardware Recommendations**:
- SSD storage for better I/O performance
- Sufficient RAM (minimum 4GB for production)
- Multiple drives for distributed setup

## Monitoring

### Health Checks
- **Liveness**: `GET http://localhost:9002/minio/health/live`
- **Readiness**: `GET http://localhost:9002/minio/health/ready`

### Metrics
MinIO provides Prometheus-compatible metrics at:
- `GET http://localhost:9002/minio/v2/metrics/cluster`

## Backup and Recovery

### Data Backup
```bash
# Using MinIO client
./mc mirror local/my-bucket /backup/location

# Using rsync (for file system)
rsync -av /data/ /backup/minio-data/
```

### Disaster Recovery
- Regular backups of data directory
- Configuration backup (access keys, policies)
- Document your bucket structure and policies

## Integration with Application

The application provides these endpoints for MinIO operations:

- `POST /api/files/upload` - Upload files
- `GET /api/files/download/{bucket}/{filename}` - Download files
- `GET /api/files/list/{bucket}` - List files
- `GET /api/files/metadata/{bucket}/{filename}` - Get file metadata
- `DELETE /api/files/{bucket}/{filename}` - Delete files

See the main README.md for detailed API usage examples.