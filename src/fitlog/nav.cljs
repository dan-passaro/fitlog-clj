(ns fitlog.nav
  (:require
   [reagent.core :as r]
   [reagent.dom.client :as rdc]
   [fitlog.data :as d]
   [fitlog.util :refer [get!]]))

(defonce app-view (r/atom {:view :home}))

(defn navigate-to [route & options]
  (reset! app-view (apply array-map :view route options)))
