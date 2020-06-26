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
