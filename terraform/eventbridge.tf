# EventBridge Rule for daily scheduled Lambda execution
resource "aws_cloudwatch_event_rule" "batch_schedule" {
  name_prefix       = "${replace(var.lambda_name, "-", "_")}_schedule_"
  description       = "Scheduled trigger for ScareSale batch Lambda job"
  schedule_expression = var.schedule_expression
  state             = "ENABLED"

  tags = merge(
    var.tags,
    {
      Name = "${var.lambda_name}-schedule"
    }
  )
}

# EventBridge Target for Lambda
resource "aws_cloudwatch_event_target" "lambda_target" {
  rule       = aws_cloudwatch_event_rule.batch_schedule.name
  target_id  = "${var.lambda_name}-target"
  arn        = aws_lambda_function.batch_dispatcher.arn
  role_arn   = aws_iam_role.eventbridge_role.arn

  # Input to pass to Lambda - specify the job name
  input = jsonencode({
    jobName = "MovieUpdateJob"
  })

  dead_letter_config {
    arn = aws_sqs_queue.lambda_dlq.arn
  }
}

# IAM Role for EventBridge to invoke Lambda
resource "aws_iam_role" "eventbridge_role" {
  name_prefix = "eventbridge-lambda-"
  description = "IAM role for EventBridge to invoke Lambda"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "events.amazonaws.com"
        }
      }
    ]
  })
}

# IAM Policy for EventBridge to invoke Lambda
resource "aws_iam_role_policy" "eventbridge_lambda_policy" {
  name_prefix = "eventbridge-lambda-policy-"
  role        = aws_iam_role.eventbridge_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "lambda:InvokeFunction"
        ]
        Resource = aws_lambda_function.batch_dispatcher.arn
      }
    ]
  })
}

# Permission for EventBridge to invoke Lambda
resource "aws_lambda_permission" "allow_eventbridge" {
  statement_id  = "AllowExecutionFromEventBridge"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.batch_dispatcher.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.batch_schedule.arn
}

# SQS Queue for Dead Letter Queue (DLQ)
resource "aws_sqs_queue" "lambda_dlq" {
  name_prefix             = "${replace(var.lambda_name, "-", "_")}_dlq_"
  message_retention_seconds = 1209600  # 14 days

  tags = merge(
    var.tags,
    {
      Name = "${var.lambda_name}-dlq"
    }
  )
}

# CloudWatch Alarm for DLQ messages
resource "aws_cloudwatch_metric_alarm" "dlq_messages" {
  alarm_name          = "${var.lambda_name}-dlq-messages"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Average"
  threshold           = 1
  alarm_description   = "Alert when messages are in the Lambda DLQ"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.lambda_dlq.name
  }
}

# Lambda already logs to CloudWatch automatically via the log group created above
# No additional EventBridge logging rule is needed

