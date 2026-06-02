(ns fitlog.main
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.ui.lib :refer [h2]]
   [fitlog.ui.workout :refer [workout]]
   [fitlog.nav :refer [app-view navigate-to]]
   [fitlog.util :refer [get!]]))

(defn workouts []
  [:<>
   [h2 "Workouts"]
   (map-indexed
    (fn [idx workout]
      ^{:key idx}
      [:p [:a {:class "link link-primary"
               :on-click #(navigate-to :workout :workout-id idx)}
           (str "Workout " idx ": " workout)]])
    (get @d/data :workouts))
   (when (empty? (get @d/data :workouts))
     [:p "No workouts. Create one to get started!"])
   [:p {:class "flex justify-center"}
    [:button {:class    "btn btn-primary w-64"
              :on-click (fn []
                          (js/console.log "data is:" (clj->js @d/data))
                          (swap! d/data update :workouts conj (d/make-workout))
                          (navigate-to :workout :workout-id (dec (count (get @d/data :workouts)))))}
     "New Workout"]]])

(defn show []
  (case (:view @app-view)
     :home [workouts]
     :workouts [workouts]
     :workout [workout (get! @app-view :workout-id)]))

(defonce root
  (rdc/create-root (.getElementById js/document "app")))

(defn persist-data
  "Save data to local storage."
  [key ref old-state new-state]
  (js/localStorage.setItem "fitlog-data"
                           (js/JSON.stringify (clj->js new-state))))

(defn load-user-data
  "Update d/data from the browser's local storage."
  []
  (when-let [data-json (js/localStorage.getItem "fitlog-data")]
    (let [data (js->clj (js/JSON.parse data-json) :keywordize-keys true)]
      (reset! d/data data))))

(defn ^:dev/after-load init []
  (load-user-data)
  (add-watch d/data nil persist-data)
  (rdc/render root [show]))
