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
  (d/load-user-data)
  (rdc/render (ensure-root!) [app-shell]))

(defn ^:export init []
  (-> (js/navigator.serviceWorker.register "sw.js")
      (.catch (fn [err]
                (js/console.error "Service worker registration failed" err))))
  (router/setup-router)
  (mount!)
  (add-watch d/data ::persist d/persist-data))
