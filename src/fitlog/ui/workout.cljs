(ns fitlog.ui.workout
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.nav :refer [navigate-to]]
   [fitlog.ui.lib :refer [h2 human-date-str]]
   [fitlog.util :refer [get!]]))

(defn- primary-btn [& {:keys [text on-click]}]
  (let [opts (cond-> {:class "btn btn-primary"}
               on-click (assoc :on-click on-click))]
    [:button opts text]))

(defn workout [workout-id]
  (let [self (get-in @d/data [:workouts workout-id])]
    [:<>
     [h2 (human-date-str (:createdAt self))]
     [:p "No exercises. Add an exercise to get started!"]
     [primary-btn :text "Add Exercise"
      :on-click #(navigate-to :add-exercise :workout-id workout-id)]
     [primary-btn :text "Back"
      :on-click #(navigate-to :home)]]))
