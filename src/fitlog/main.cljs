(ns fitlog.main
  (:require
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.router :as router]
   [fitlog.nav :refer [app-view]]))

(defn show []
  (if-let [view (:view (:data @app-view))]
    (let [params (:path-params @app-view)]
      [view params])
    [:p "Unknown route"]))

(defonce root
  (rdc/create-root (.getElementById js/document "app")))

(defn ^:dev/after-load init []
  (d/load-user-data)
  (add-watch d/data ::persist d/persist-data)
  (router/setup-router)
  (rdc/render root [show]))
