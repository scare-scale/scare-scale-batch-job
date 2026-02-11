# Data source to get the current AWS account ID
data "aws_caller_identity" "current" {}

# Local variable for Lambda archive
locals {
  lambda_jar_path     = "${path.module}/${var.lambda_jar_path}"
  lambda_handler      = "com.scarescale.batch.BatchDispatcher::handleRequest"
  lambda_runtime      = "java21"
  lambda_architectures = ["x86_64"]
}

# Create a CloudWatch log group for Lambda
resource "aws_cloudwatch_log_group" "lambda_logs" {
  name_prefix       = "/aws/lambda/${var.lambda_name}"
  retention_in_days = 14

  tags = merge(
    var.tags,
    {
      Name = "${var.lambda_name}-logs"
    }
  )
}

# Lambda function
resource "aws_lambda_function" "batch_dispatcher" {
  filename         = local.lambda_jar_path
  function_name    = var.lambda_name
  role             = aws_iam_role.lambda_role.arn
  handler          = local.lambda_handler
  runtime          = local.lambda_runtime
  architectures    = local.lambda_architectures
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory
  ephemeral_storage {
    size = var.lambda_ephemeral_storage
  }

  # Environment variables for the Lambda function
  environment {
    variables = {
      LOG_LEVEL                = "INFO"
      TMDB_API_KEY             = var.tmdb_api_key
      SUPABASE_SECRET_KEY      = var.supabase_secret_key
      SUPABASE_PROJECT_ID      = var.supabase_project_id
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_cloudwatch_log_group.lambda_logs
  ]

  tags = merge(
    var.tags,
    {
      Name = var.lambda_name
    }
  )
}

# CloudWatch Alarms for Lambda
resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  alarm_name          = "${var.lambda_name}-errors"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  alarm_description   = "Alert when Lambda function has errors"
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.batch_dispatcher.function_name
  }
}

resource "aws_cloudwatch_metric_alarm" "lambda_throttles" {
  alarm_name          = "${var.lambda_name}-throttles"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "Throttles"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  alarm_description   = "Alert when Lambda function is throttled"
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.batch_dispatcher.function_name
  }
}

resource "aws_cloudwatch_metric_alarm" "lambda_duration" {
  alarm_name          = "${var.lambda_name}-high-duration"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "Duration"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Average"
  threshold           = var.lambda_timeout * 800  # Alert at 80% of timeout
  alarm_description   = "Alert when Lambda execution duration is too high"
  treat_missing_data  = "notBreaching"

  dimensions = {
    FunctionName = aws_lambda_function.batch_dispatcher.function_name
  }
}

# Lambda function URL (alternative to EventBridge for manual testing)
resource "aws_lambda_function_url" "batch_dispatcher" {
  function_name          = aws_lambda_function.batch_dispatcher.function_name
  authorization_type    = "AWS_IAM"
  cors {
    allow_credentials = false
    allow_origins     = ["*"]
    allow_methods     = ["POST"]
    max_age           = 0
  }
}

