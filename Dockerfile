# WIP: this is just for locally testing a CI build. CI *doesn't* actually use
# this image. Nothing does at the moment. Eventually I want to make CI run off a
# Docker image but at the moment I'm just rushing to get the deployment working.
FROM node:24.14.1-trixie-slim
COPY --from=ghcr.io/casey/just:latest /just /usr/local/bin/

RUN corepack enable

RUN apt-get update && apt-get install -y --no-install-recommends openjdk-21-jdk-headless \
    curl \
    rlwrap \
    && rm -rf /var/lib/apt/lists/*

# Install clojure
RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh \
    && chmod +x linux-install.sh \
    && ./linux-install.sh \
    && rm linux-install.sh

WORKDIR /app
