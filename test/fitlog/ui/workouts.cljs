(ns fitlog.ui.workouts
  (:require
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2]]))

(defn workouts []
  [:<>
   [h2 "Workouts"]
   (map-indexed
    (fn [idx workout]
      ^{:key idx}
      [:p [:a {:class "link link-primary"
               :href (rfe/href routes/workout {:id idx})}
           (str "Workout " idx ": " workout)]])
    (get @d/data :workouts))
   (when (empty? (get @d/data :workouts))
     [:p "No workouts. Create one to get started!"])
   [:p {:class "flex justify-center"}
    [:button {:class    "btn btn-primary w-64"
              :on-click (fn []
                          (swap! d/data update :workouts conj (d/make-workout))
                          (let [idx (dec (count (get @d/data :workouts)))]
                            (rfe/navigate routes/workout {:path-params {:id idx}})))}
     "New Workout"]]])
