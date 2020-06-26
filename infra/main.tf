terraform {
  required_version = "~> 0.12.28"

  backend "gcs" {
    bucket      = "terraform-craft-watch"
    prefix      = "terraform/state"
  }
}

provider "google" {
  version     = "~> 3.27"
  project     = "craft-watch"
  region      = "europe-west2"
}

resource "google_storage_bucket" "backend" {
  name          = "backend-craft-watch"
  location      = "europe-west2"
  force_destroy = true
}

resource "google_service_account" "circleci" {
  account_id   = "circleci"
  display_name = "CircleCI service account"
}

data "google_iam_policy" "admin" {
  binding {
    role = "roles/storage.objectAdmin"
    members = [
      "serviceAccount:${google_service_account.circleci.email}"
    ]
  }
}

# Requires you to have "Storage Admin" role
resource "google_storage_bucket_iam_policy" "policy" {
  bucket = google_storage_bucket.backend.name
  policy_data = data.google_iam_policy.admin.policy_data
}
