# SPDX-FileCopyrightText: 2026 Dan Passaro
# SPDX-License-Identifier: AGPL-3.0-or-later
tailwind_base := "pnpm tailwindcss -i resources/public/css/style.css"
tailwind_dev_build := tailwind_base + " -o public/css/main.css"
tailwind_watch_cmd := tailwind_dev_build + " --watch"

default:
    just --list

check: lint compile test test-e2e

lint:
    @# note: test isn't linted for now because of preexisting errors
    reuse lint
    clj-kondo --lint src/

dev:
    #!/usr/bin/env sh
    if nc -z localhost 8081
    then
      echo "Dev server already running"
    else
      goreman -f /dev/stdin start <<-'  EOF'
        css: {{tailwind_watch_cmd}}
        web: pnpm shadow-cljs -A:dev watch :app
      EOF
    fi

css:
    {{tailwind_dev_build}}

watch-css:
    {{tailwind_watch_cmd}}

test:
    pnpm shadow-cljs -A:dev compile test
    pnpm karma start --single-run

test-e2e:
    @# TODO: make this more robust (e.g. do an isolated build that's independent
    @# of the dev server)
    @if ! nc -z localhost 8081 ; then echo "Run the dev-server first"; exit 1; fi
    pnpm playwright test

install-deps:
    pnpm install --frozen-lockfile
    clj -P

compile:
    pnpm shadow-cljs compile :app
    pnpm shadow-cljs compile :sw

release: install-deps
    mkdir -p release/
    pnpm shadow-cljs release :app
    pnpm shadow-cljs release :sw
    cp resources/public/index.html release/
    {{tailwind_base}} -o release/css/main.css -m
