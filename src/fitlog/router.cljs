(ns fitlog.router
  (:require
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [fitlog.nav :refer [app-view]]
   [fitlog.routes :as routes]
   [fitlog.ui.add-exercise :refer [add-exercise]]
   [fitlog.ui.workout :refer [workout]]
   [fitlog.ui.workouts :refer [workouts]]))

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
