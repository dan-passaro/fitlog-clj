;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.ui.workouts
  (:require
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2 human-date-str]]))

(defn- no-workout-for-today? []
  (let [today (.toDateString (js/Date.))]
    (not (some #(= (-> % :createdAt js/Date. .toDateString) today)
               (:workouts @d/data)))))

(defn- start-new-workout! []
  (swap! d/data update :workouts conj (d/make-workout))
  (let [idx (dec (count (:workouts @d/data)))]
    (rfe/navigate routes/workout {:path-params {:id idx}})))

(defn workouts []
  [:<>
   [h2 "Workouts"]
   (when (no-workout-for-today?)
     [:p {:class "flex justify-center"}
      [:button {:class    "btn btn-primary w-64"
                :on-click start-new-workout!}
       "New Workout"]])
   (if (seq (:workouts @d/data))
     [:ul
      (map
       (fn [[idx workout]]
         ^{:key idx}
         [:li [:a {:class "link link-primary"
                   :href (rfe/href routes/workout {:id idx})}
               (-> workout :createdAt human-date-str)]])
       (sort-by #(-> % second :createdAt)
                >
                (map-indexed vector (:workouts @d/data))))]
     [:p "No workouts. Create one to get started!"])])
