;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitpad.nav
  (:require
   [reagent.core :as r]))

(defonce app-view (r/atom nil))

(defn navigate-to [route & options]
  (reset! app-view (apply array-map :view route options)))
