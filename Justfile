tailwind_cmd := "pnpm tailwindcss -i resources/public/css/style.css -o public/css/main.css --watch"

default:
    just --list

dev:
    #!/usr/bin/env sh
    if nc -z localhost 8081
    then
      echo "Dev server already running"
    else
      trap 'kill 0' SIGINT
      {{tailwind_cmd}} &
      pnpm shadow-cljs -A:dev watch :app &
      wait
    fi

watch-css:
    {{tailwind_cmd}}

test:
    npx shadow-cljs -A:dev compile test
    @echo "Visit http://localhost:9001 to view results"

test-e2e:
    @# TODO: make this more robust (e.g. do an isolated build that's independent
    @# of the dev server)
    @if ! nc -z localhost 8081 ; then echo "Run the dev-server first"; exit 1; fi
    pnpm playwright test
