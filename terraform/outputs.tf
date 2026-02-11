output "lambda_function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.batch_dispatcher.function_name
}

output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.batch_dispatcher.arn
}

output "lambda_function_url" {
  description = "Lambda Function URL for manual invocation"
  value       = aws_lambda_function_url.batch_dispatcher.function_url
  sensitive   = true
}

output "eventbridge_rule_name" {
  description = "Name of the EventBridge rule"
  value       = aws_cloudwatch_event_rule.batch_schedule.name
}

output "eventbridge_schedule" {
  description = "Schedule expression for EventBridge rule"
  value       = aws_cloudwatch_event_rule.batch_schedule.schedule_expression
}

output "lambda_log_group_name" {
  description = "CloudWatch Log Group for Lambda function"
  value       = aws_cloudwatch_log_group.lambda_logs.name
}

output "dlq_queue_url" {
  description = "URL of the Dead Letter Queue"
  value       = aws_sqs_queue.lambda_dlq.url
}

output "dlq_queue_arn" {
  description = "ARN of the Dead Letter Queue"
  value       = aws_sqs_queue.lambda_dlq.arn
}

output "iam_role_arn" {
  description = "ARN of the Lambda execution IAM role"
  value       = aws_iam_role.lambda_role.arn
}