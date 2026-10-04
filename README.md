<!--
SPDX-FileCopyrightText: 2026 2026 Dan Passaro

SPDX-License-Identifier: AGPL-3.0-or-later
-->
# fitlog

(ALPHA SOFTWARE) A client-only fitness tracker progressive webapp. Uses
[remoteStorage](https://remotestorage.io/) to provide cross-device sync.

## Development

To get an interactive development environment run:

    just dev

Then visit http://localhost:8081/ to use the app.

Unfortunately, the CSS doesn't always live-update like it's supposed to. If you
notice things are unstyled, try running

    just css

### Tests

To run unit tests, use

    just test

To run end-to-end tests, use

    just test-e2e

Beforoe you run the end-to-end suite you may need to setup Playwright first
using this command:

    pnpm exec playwright install


### CIDER

A `.dir-locals.el` file is included which makes the Emacs
`M-x cider-jack-in-cljs` autofilled; one extra return keypress is needed to confirm the preselected `shadow` REPL type.

## License

Copyright (C) 2026 Dan Passaro

This program is free software, licensed under the GNU Affero General Public
License v3.0 or later (`AGPL-3.0-or-later`). See the [LICENSE](LICENSE) file
for the full text.
