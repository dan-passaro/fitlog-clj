(ns learn-cljs.weather
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [learn-cljs.data :as d]
   [learn-cljs.util :refer [get!]]))

(defonce app-view (r/atom {:view :home}))

(defn workout [workout-id]
  (let [self (get @d/workouts workout-id)]
    [:<>
     [:p "The workout screen wee"]
     [:p (str "This is workout " workout-id ": " self)]
     [:button {:class    "btn btn-primary"
               :on-click #(reset! app-view {:view :home})}
      "Go home"]]))

(defn h2 [text]
  [:h2 {:class "text-2xl"} text])

(defn workouts []
  [:<>
   [h2 "Workouts"]
   (map-indexed
    (fn [idx workout]
      ^{:key idx}
      [:p [:a {:class "link link-primary"
               :on-click #(reset! app-view {:view :workout
                                            :workout-id idx})}
           (str "Workout " idx ": " workout)]])
    @d/workouts)
   (when (empty? @d/workouts)
     [:p "No workouts. Create one to get started!"])
   [:p {:class "flex justify-center"}
    [:button {:class    "btn btn-primary w-64"
              :on-click (fn []
                          (swap! d/workouts conj {:ts (js/Date.now)})
                          (reset! app-view {:view :workout :workout-id (dec (count @d/workouts))}))}
     "New Workout"]]])

(defn show []
  (case (:view @app-view)
     :home [workouts]
     :workouts [workouts]
     :workout [workout (get! @app-view :workout-id)]))

(defonce root
  (rdc/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load init []
  (rdc/render root [show]))
