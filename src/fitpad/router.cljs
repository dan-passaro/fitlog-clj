;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitpad.router
  (:require
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [fitpad.nav :refer [app-view]]
   [fitpad.routes :as routes]
   [fitpad.ui.add-exercise :refer [add-exercise]]
   [fitpad.ui.workout :refer [workout]]
   [fitpad.ui.workouts :refer [workouts]]))

(def router
  (rf/router
   [["/" {:name routes/workouts
          :view workouts}]
    ["/workout/:id" {:name routes/workout
                     :view workout}]
    ["/workout/:id/add-exercise" {:name routes/add-exercise
                                  :view add-exercise}]]))

(defn setup-router []
  (rfe/start! router
              (fn [match] (reset! app-view match))
              {:use-fragment true}))
