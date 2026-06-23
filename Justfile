tailwind_base := "pnpm tailwindcss -i resources/public/css/style.css"
tailwind_watch_cmd := tailwind_base + " -o public/css/main.css --watch"

default:
    just --list

dev:
    #!/usr/bin/env sh
    if nc -z localhost 8081
    then
      echo "Dev server already running"
    else
      trap 'kill 0' SIGINT
      {{tailwind_watch_cmd}} &
      pnpm shadow-cljs -A:dev watch :app &
      wait
    fi

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

release:
    pnpm install --frozen-lockfile
    mkdir -p release/
    pnpm shadow-cljs release :app
    cp resources/public/index.html release/
    {{tailwind_base}} -o release/css/main.css -m
