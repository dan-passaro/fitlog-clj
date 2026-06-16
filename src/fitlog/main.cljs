(ns fitlog.main
  (:require
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.router :as router]
   [fitlog.ui.app-shell :refer [app-shell]]))

(defonce root (atom nil))

(defn- ensure-root! []
  (or @root
      (reset! root (rdc/create-root (.getElementById js/document "app")))))

(defn ^:dev/after-load mount! []
  (rdc/render (ensure-root!) [app-shell]))

(defn ^:export init []
  (d/load-user-data)
  (add-watch d/data ::persist d/persist-data)
  (router/setup-router)
  (mount!))
