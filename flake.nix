# SPDX-FileCopyrightText: 2026 2026 Dan Passaro
#
# SPDX-License-Identifier: AGPL-3.0-or-later

{
  description = "Build shell for fitlog-clj";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        deps = [
            pkgs.jdk21_headless
            pkgs.clojure
            pkgs.pnpm_11
            pkgs.nodejs_24
            pkgs.just
            pkgs.rlwrap
            pkgs.netcat
        ];
      in {
        devShells.default = pkgs.mkShell {
          packages = deps;
        };
        apps.default = {
          type = "app";
          program = toString (pkgs.writeShellScript "release" ''
            export PATH=${pkgs.lib.makeBinPath deps}:$PATH
            exec just release "$@"
            '');
        };
      });
}
