;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitpad.main
  (:require
   [reagent.dom.client :as rdc]
   [fitpad.data :as d]
   [fitpad.router :as router]
   [fitpad.ui.app-shell :refer [app-shell]]))

(defonce root (atom nil))

(defn- ensure-root! []
  (or @root
      (reset! root (rdc/create-root (.getElementById js/document "app")))))

(defn ^:dev/after-load mount! []
  (d/load-user-data)
  (rdc/render (ensure-root!) [app-shell]))

(defn ^:export init []
  (router/setup-router)
  (mount!)
  (add-watch d/data ::persist d/persist-data))
