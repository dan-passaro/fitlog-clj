(ns fitlog.ui.workouts
  (:require
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2 human-date-str]]))

(defn workouts []
  [:<>
   [h2 "Workouts"]
   (if (seq (:workouts @d/data))
     [:ul
      (map-indexed
       (fn [idx workout]
         ^{:key idx}
         [:li [:a {:class "link link-primary"
                   :href (rfe/href routes/workout {:id idx})}
               (-> workout :createdAt human-date-str)]])
       (:workouts @d/data))]
     [:p "No workouts. Create one to get started!"])
   [:p {:class "flex justify-center"}
    [:button {:class    "btn btn-primary w-64"
              :on-click (fn []
                          (swap! d/data update :workouts conj (d/make-workout))
                          (let [idx (dec (count (get @d/data :workouts)))]
                            (rfe/navigate routes/workout {:path-params {:id idx}})))}
     "New Workout"]]])
