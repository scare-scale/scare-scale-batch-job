variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "lambda_name" {
  description = "Name of the Lambda function"
  type        = string
  default     = "scare-scale-batch-dispatcher"
}

variable "lambda_timeout" {
  description = "Lambda function timeout in seconds"
  type        = number
  default     = 300
}

variable "lambda_memory" {
  description = "Lambda function memory in MB"
  type        = number
  default     = 512
}

variable "lambda_ephemeral_storage" {
  description = "Lambda function ephemeral storage in MB (min: 512, max: 10240)"
  type        = number
  default     = 512
}

variable "schedule_expression" {
  description = "EventBridge schedule expression for Lambda execution"
  type        = string
  default     = "cron(0 2 * * ? *)" # Daily at 2 AM UTC
}

variable "lambda_jar_path" {
  description = "Path to the compiled Lambda JAR file"
  type        = string
  default     = "../build/libs/scare-scale-batch-job-all.jar"
}

variable "tags" {
  description = "Additional tags to apply to resources"
  type        = map(string)
  default     = {}
}

variable "tmdb_api_key" {
  description = "TMDB API Key - will read from TF_VAR_tmdb_api_key environment variable"
  type        = string
  sensitive   = true
  default     = ""
}

variable "supabase_secret_key" {
  description = "Supabase Secret Key - will read from TF_VAR_supabase_secret_key environment variable"
  type        = string
  sensitive   = true
  default     = ""
}

variable "supabase_project_id" {
  description = "Supabase Project ID - will read from TF_VAR_supabase_project_id environment variable"
  type        = string
  sensitive   = true
  default     = ""
}
