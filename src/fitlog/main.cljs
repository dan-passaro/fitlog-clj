(ns fitlog.main
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.router :as router]
   [fitlog.ui.lib :refer [h2]]
   [fitlog.ui.workout :refer [workout]]
   [fitlog.nav :refer [app-view]]
   [fitlog.util :refer [get!]]))

(defn show []
  (if-let [view (:view (:data @app-view))]
    (let [params (:path-params @app-view)]
      [view params])
    [:p "Unknown route"]))

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
  (router/setup-router)
  (rdc/render root [show]))
