(ns fitlog.ui.workout
  (:require
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2 human-date-str]]))

(defn- primary-btn [& {:keys [text on-click href]}]
  (let [opts (cond-> {:class "btn btn-primary"}
               on-click (assoc :on-click on-click)
               href (assoc :href href))
        elt (if href :a :button)]
    [elt opts text]))

(defn workout [& {:keys [id]}]
  (let [self (get-in @d/data [:workouts (int id)])]
    [:<>
     [h2 (human-date-str (:createdAt self))]
     [:p "No exercises. Add an exercise to get started!"]
     [primary-btn
      :href (rfe/href routes/add-exercise {:id id})
      :text "Add Exercise"]
     [primary-btn
      :text "Back"
      :href (rfe/href routes/workouts)]]))
