#!/usr/bin/env bash
# This script runs on the EC2 instance through AWS Systems Manager Run Command.
# Deploy-Application.ps1 replaces __CONFIGURATION_BASE64__ before sending it.
# Do not run this file directly: its deployment configuration is supplied by
# the PowerShell entry script and contains the exact image and AWS resources.
set -euo pipefail

# 1. Decode the non-secret deployment configuration created by PowerShell.
configuration="$(printf '%s' '__CONFIGURATION_BASE64__' | base64 --decode)"
image_uri="$(jq -er '.imageUri' <<< "$configuration")"
aws_region="$(jq -er '.awsRegion' <<< "$configuration")"
client_endpoint="$(jq -er '.clientEndpoint' <<< "$configuration")"
database_endpoint="$(jq -er '.databaseEndpoint' <<< "$configuration")"
database_secret_arn="$(jq -er '.databaseSecretArn' <<< "$configuration")"

# 2. Download the AWS RDS certificate bundle so the JDBC driver can verify the
# PostgreSQL server certificate instead of accepting an untrusted connection.
sudo install -d -m 0750 /opt/financial-app/certificates
curl --fail --silent --show-error --location \
  https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
  --output /tmp/aws-rds-global-bundle.pem
sudo install -m 0644 /tmp/aws-rds-global-bundle.pem /opt/financial-app/certificates/aws-rds-ca.pem
rm -f /tmp/aws-rds-global-bundle.pem

# 3. Read the RDS-generated credentials with the EC2 instance role. The
# password stays on the EC2 host and is never passed through GitHub Actions.
database_credentials="$(aws secretsmanager get-secret-value --region "$aws_region" --secret-id "$database_secret_arn" --query SecretString --output text)"
database_username="$(jq -er '.username' <<< "$database_credentials")"
database_password="$(jq -er '.password' <<< "$database_credentials")"

# 4. Write the API's runtime-only configuration. This file has owner-only
# permissions because it contains the database password.
sudo tee /opt/financial-app/api.env >/dev/null <<EOF
FINANCIAL_APP_DATABASE_DIALECT=postgresql
FINANCIAL_APP_DATABASE_JDBC_URL=jdbc:postgresql://${database_endpoint}:5432/financialapp?sslmode=verify-full&sslrootcert=/run/certificates/aws-rds-ca.pem
FINANCIAL_APP_DATABASE_USERNAME=${database_username}
FINANCIAL_APP_DATABASE_PASSWORD=${database_password}
FINANCIAL_APP_DATABASE_MAXIMUM_POOL_SIZE=5
FINANCIAL_APP_CORS_ALLOWED_ORIGIN=${client_endpoint}
FINANCIAL_APP_SESSION_COOKIE_SECURE=true
FINANCIAL_APP_CSRF_COOKIE_SECURE=true
EOF
sudo chmod 0600 /opt/financial-app/api.env

# 5. Authenticate the instance to ECR and download the immutable image named
# by the commit SHA. The instance role—not a stored Docker credential—permits
# this login.
aws ecr get-login-password --region "$aws_region" | sudo docker login --username AWS --password-stdin "${image_uri%%/*}"
sudo docker pull "$image_uri"

# 6. Replace the single API container. This intentionally causes a short API
# interruption and clears server-side sessions; a later blue/green deployment
# design is needed for zero-downtime releases.
sudo docker rm --force financial-app-api >/dev/null 2>&1 || true
sudo docker run --detach \
  --name financial-app-api \
  --restart unless-stopped \
  --publish 8080:8080 \
  --env-file /opt/financial-app/api.env \
  --volume /opt/financial-app/certificates/aws-rds-ca.pem:/run/certificates/aws-rds-ca.pem:ro \
  "$image_uri"

# This is retained as harmless cleanup for an older local reverse-proxy
# experiment. The current CloudFront-to-EC2 topology does not start a proxy
# container on the instance.
sudo docker rm --force financial-app-proxy >/dev/null 2>&1 || true

# 7. Do not report success until the new Spring Boot container answers its
# local health endpoint. On failure, print recent logs for SSM and GitHub.
for attempt in {1..45}; do
  if curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health; then
    echo "API deployment is healthy on its CloudFront-only origin."
    exit 0
  fi
  sleep 4
done

sudo docker logs financial-app-api --tail 100
exit 1
