(ns fitlog.ui.add-exercise
  (:require
   [reitit.frontend.easy :as rfe]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2]]))

(defn add-exercise [& {:keys [id]}]
  [h2 "Add Exercise"]
  [:p "...not yet implemented"])
