;;; SPDX-FileCopyrightText: 2026 2026 Dan Passaro <danpassaro.dev>
;;;
;;; SPDX-License-Identifier: AGPL-3.0-or-later

(ns fitlog.build
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- analytics-file [build-mode]
  (str
   (io/file "analytics"
            (str (name build-mode)

                 ;; This should be temporary. Unless it turns out someone else
                 ;; is actually using this, the GitHub Pages deployment will be
                 ;; shut down: it's being migrated to Codeberg Pages.
                 (if (System/getenv "GITHUB_ACTION")
                   "-github"
                   "")

                 ".html"))))

(defn- get-resource
  "Like io/resource, but error if the resource doesn't exist."
  [target]
  (let [resource (io/resource target)]
    (assert resource (str "resource " (pr-str target) " not found"))
    resource))

(defn- dest-path-for [output-dir]

  ;; assume output-dir is something like "public/js" and use that to
  ;; return "public/index.html"
  (assert (= "js" (.getName (io/file output-dir))))

  (str (io/file (.getParent (io/file output-dir))
                "index.html")))

(defn insert-analytics
  {:shadow.build/stage :compile-prepare}
  [build-state]
  (let [build-mode (:shadow.build/mode build-state)
        index-template (slurp (get-resource "index.html"))
        analytics-snippet (slurp (get-resource (analytics-file build-mode)))
        output-dir (:output-dir (:shadow.build/config build-state))
        index-html-path (dest-path-for output-dir)]

    (println (str "creating " index-html-path))
    (spit index-html-path (str/replace index-template
                                       "<!-- INSERT-ANALYTICS -->"
                                       (str/trim analytics-snippet))))
  build-state)
