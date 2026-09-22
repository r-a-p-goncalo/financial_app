# AWS deployment without a custom domain

This is the first AWS environment for the Financial App. It has a separately
deployed client, API, and database, but it deliberately does **not** require a
domain registration, Route 53 hosted zone, DNS record, or ACM certificate.
CloudFront supplies the public HTTPS address, for example
`https://d111example.cloudfront.net`.

```text
Browser ── HTTPS ──> CloudFront generated hostname
                         ├─ default ──> private S3 bucket (React client)
                         └─ /api/* and /actuator/* ──> EC2 :8080 (Spring Boot)
                                                               │
                                                               └─> private RDS PostgreSQL
```

The browser uses the same CloudFront origin for the client and `/api/v1`, so
sessions and CSRF cookies remain same-origin. The client bucket is private and
CloudFront reads it through Origin Access Control. The EC2 security group
allows port `8080` only from AWS's managed CloudFront origin-facing prefix
list; there is no SSH, HTTP, or HTTPS rule open to the internet. RDS has no
public endpoint and accepts PostgreSQL only from the API security group.

CloudFront supplies TLS to users. Its connection to the EC2 origin is HTTP
because an EC2-generated hostname cannot have a trusted certificate that
CloudFront can validate. The security group keeps that origin inaccessible to
the public. Treat this as the useful, low-complexity first loop—not the final
TLS topology for sensitive production data. A later custom domain plus an ALB,
or an HTTPS origin service such as App Runner, is the next security upgrade.

This is a single-instance learning environment, not highly available. It uses
one EC2 instance and a single-AZ RDS instance. It avoids NAT gateways, an ALB,
ECS, Aurora, and a custom domain until there is a concrete reason to learn or
need them.

## Before creating resources

1. Install AWS CLI v2, Docker Desktop/Engine, Git, and PowerShell 7. Configure
   the CLI with an IAM identity permitted to create the resources in these
   templates. Never use the AWS root user for daily work.
2. Enable MFA on the root user, configure a low monthly AWS Budget alert, and
   enable Cost Anomaly Detection. RDS, EC2, CloudFront, storage, and data
   transfer can consume free-tier allowances or account credits.
3. Choose a region, normally `eu-west-1` for this project. The foundation
   stack lives there; the small CloudFront stack is deployed in `us-east-1`.
   No DNS setup is needed.

## 1. Validate, then create infrastructure

From the repository root, validation is read-only by default:

```powershell
./infra/aws/Deploy-Infrastructure.ps1 -Region eu-west-1
```

After reviewing [`foundation.yaml`](foundation.yaml) and
[`frontend.yaml`](frontend.yaml), create or update the two stacks explicitly:

```powershell
./infra/aws/Deploy-Infrastructure.ps1 -Region eu-west-1 -Apply
```

The script discovers the AWS-managed CloudFront origin-facing prefix list for
the selected region, creates the regional foundation, reads its private S3 and
EC2-origin outputs, then creates the CloudFront distribution. It prints the
generated public CloudFront URL at the end. Distribution creation can take
several minutes.

CloudFormation retains the S3 bucket on stack deletion and takes an RDS
snapshot before deleting the database. Those safeguards prevent accidental
data loss, but retained buckets and snapshots can still incur cost; remove
them deliberately when you no longer need them.

## 2. Publish a version manually

Commit and test the version first. The script refuses a dirty worktree by
default and uses the full Git commit SHA as the immutable ECR image tag.

```powershell
./infra/aws/Deploy-Application.ps1 -Region eu-west-1
```

It performs this release sequence:

1. Builds the Spring Boot image and pushes it to the private ECR repository.
2. Uses Systems Manager Run Command to pull that exact image onto EC2, retrieve
   the RDS credential with the EC2 instance role, run Flyway migrations, and
   start the API on port `8080`.
3. Waits for the API health check on the host before publishing the client.
4. Builds the Vite client with `VITE_API_BASE_URL=/api/v1`, uploads it to the
   private bucket, and invalidates CloudFront.

Open the CloudFront URL printed by the script, then verify registration, login,
and a transaction. `-AllowDirtyWorktree` is only for an intentional temporary
experiment; do not use it for a release you want to reproduce.

## 3. Enable GitHub Actions deployment from `main`

The foundation stack creates a least-privilege IAM user named
`financial-app-github-deploy` by default. It can publish this API image, send a
deployment command to this API instance, upload this client bucket, and
invalidate CloudFront.

After the foundation stack exists, open **IAM → Users → the
`GitHubActionsDeploymentUserName` stack output → Security credentials**, then
create an access key for a third-party service. AWS shows the secret access key
only once: copy it directly into GitHub. Do not put it in the repository or a
local `.env` file.

In GitHub repository **Settings → Secrets and variables → Actions → Secrets**,
create these repository secrets:

```text
AWS_ACCESS_KEY_ID=<the IAM access-key ID>
AWS_SECRET_ACCESS_KEY=<the IAM secret access key>
AWS_REGION=eu-west-1
```

The workflow uses exactly those names. The RDS password remains in AWS Secrets
Manager and only the EC2 instance role can read it. Rotate the IAM access key
regularly: update both GitHub secrets, make a successful deployment, then
deactivate the old key.

`.github/workflows/deploy-production.yml` runs on a `push` to `main`. A pull
request merge into `main` creates that push event, so the normal protected
branch workflow is: open PR → CI passes → merge → deployment starts. The
workflow verifies the commit first, then serializes deployments so a later
push cannot interrupt a database migration in progress.

## Operations and next learning loop

* Use **Systems Manager → Session Manager** to inspect the host; no SSH key or
  inbound SSH rule exists. The instance can take a few minutes after creation
  to appear as a managed node.
* Read application logs with `sudo docker logs financial-app-api` in a Session
  Manager shell.
* In RDS, verify automated backups and take a snapshot before an application
  migration. Practice restoring a snapshot before storing valuable data.
* Do not make RDS public, open port `5432`, commit passwords, or place AWS
  access keys in `.env` files.

When you are ready for a public domain, you can keep DNS outside Route 53:
create a CNAME at your registrar for the CloudFront hostname and validate an
ACM certificate in `us-east-1`. Before handling sensitive real-world traffic,
also replace the HTTP CloudFront-to-EC2 origin leg with an HTTPS origin and add
alarms, structured log shipping, a WAF, safer database roles, and a rollback
strategy.
