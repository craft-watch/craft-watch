# Craft Watch

Mostly notes for myself right now.


## GitHub actions

**TODO: created PAT with workflow scope**


## CircleCI

Requires the following secret things:

- A deploy key for the GitHub Pages repo:
    1. Create [here][create-deploy-key] (tick **Allow write access**).
    2. Register the private key [here][register-key].
    3. Ensure the fingerprint is updated in `.circleci/config.yml`.

- A service account for read/write access to a GCS bucket.
    1. Run a `terraform apply` to create the key.
    2. Copy the output and create a `GCLOUD_SERVICE_KEY` environment variable for the CircleCI
       project [here][circleci-env-var].

[create-deploy-key]: https://github.com/craft-watch/craft-watch.github.io/settings/keys/new
[register-key]: https://app.circleci.com/settings/project/github/craft-watch/craft-watch/ssh
[circleci-env-var]: https://app.circleci.com/settings/project/github/craft-watch/craft-watch/environment-variables


## Backend

*TODO*


## Frontend

Stuff to install:

```
brew install yarn
```

Then in `frontend/`:

```
yarn dev
```

Then browse to http://localhost:3000.


## Infra

Stuff to install:

```
brew install terraform
brew cask install google-cloud-sdk
```

Then set up GCloud SDK:

```
gcloud init
gcloud auth application-default login
```

Ensure you have the **Storage Admin** role.


