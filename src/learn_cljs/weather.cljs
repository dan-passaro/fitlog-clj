(ns learn-cljs.weather
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [reagent.dom :as rdom]))

(defonce click-count (r/atom 0))
(defonce timer-secs (r/atom 0))
(defonce timer-timeout nil)

(defn counter []
  [:p
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :class "btn" :value "Click Me!"
            :on-click #(swap! click-count inc)}]])

(defn timer []
  (when (not timer-timeout)
    (set! timer-timeout (r/atom (js/setInterval #(swap! timer-secs inc) 1000))))
  [:p "Seconds elapsed: " @timer-secs])

(defn home []
  [:<>
   [:h2 {:class "text-2xl"} "Workouts"]
   [:p {:class "flex justify-center"}
    [:button {:class "btn btn-primary w-64"}
     "New Workout"]]])

(defonce root
  (rdc/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load init []
  (rdc/render root [home]))
