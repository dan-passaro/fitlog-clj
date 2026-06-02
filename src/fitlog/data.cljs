(ns fitlog.data
  (:require
   [reagent.core :as r]))

(def data (r/atom {:workouts [] :exercises []}))

(defn make-workout []
  {:createdAt (.toISOString (new js/Date))
   :id (.randomUUID js/crypto)})
