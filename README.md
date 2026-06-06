# fitlog

An offline-only fitness tracker progressive webapp.

## Development

To get an interactive development environment run:

    just dev

Then visit http://localhost:8081/ to use the app.

You should also run

    just watch-css

### Tests

To run unit tests, use

    just test

To run end-to-end tests, use

    just test-e2e

### CIDER

A `.dir-locals.el` file is included which makes the Emacs
`M-x cider-jack-in-cljs` autofilled; one extra return keypress is needed to confirm the preselected `shadow` REPL type.
