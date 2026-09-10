;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.nav
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.util :refer [get!]]))

(defonce app-view (r/atom nil))

(defn navigate-to [route & options]
  (reset! app-view (apply array-map :view route options)))
