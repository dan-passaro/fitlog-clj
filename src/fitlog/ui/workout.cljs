(ns fitlog.ui.workout
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.nav :refer [navigate-to]]
   [fitlog.ui.lib :refer [h2 human-date-str]]
   [fitlog.util :refer [get!]]))

(defn workout [workout-id]
  (let [self (get-in @d/data [:workouts workout-id])]
    [:<>
     [h2 (human-date-str (:createdAt self))]
     [:p "No exercises. Add an exercise to get started!"]
     [:button {:class    "btn btn-primary"
               :on-click #(navigate-to :home)}
      "Back"]]))
