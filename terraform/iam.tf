# IAM Role for Lambda execution
resource "aws_iam_role" "lambda_role" {
  name_prefix = "scare-scale-batch-lambda-"
  description = "IAM role for ScareSale batch Lambda function"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# Attach basic Lambda execution policy
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Custom policy for database and external API access
resource "aws_iam_role_policy" "lambda_custom" {
  name_prefix = "scare-scale-batch-policy-"
  role        = aws_iam_role.lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # Allow reading secrets from Secrets Manager for sensitive credentials
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = "arn:aws:secretsmanager:${var.aws_region}:*:secret:scare-scale/*"
      },
      # Allow reading from Parameter Store for configuration
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters"
        ]
        Resource = "arn:aws:ssm:${var.aws_region}:*:parameter/scare-scale/*"
      }
    ]
  })
}

