# fitlog

An offline-only fitness tracker progressive webapp.

## Development

To get an interactive development environment run:

    npx shadow-cljs -A:dev watch app

Then visit http://localhost:8081/ to use the app.

### Tests

To run unit tests, use

    npx shadow-cljs -A:dev compile test

Then visit http://localhost:3001 to see results.

(NOT IMPLEMENTED) To run end-to-end tests, use

    npx shadow-cljs -A:dev compile e2e-test

The tests will run automatically.

### CIDER

A `.dir-locals.el` file is included which makes the Emacs
`M-x cider-jack-in-cljs` autofilled; one extra return keypress is needed to confirm the preselected `shadow` REPL type.
