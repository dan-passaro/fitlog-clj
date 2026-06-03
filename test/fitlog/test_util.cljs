(ns fitlog.test-util
  (:require-macros [fitlog.test-util])
  (:require
   [reagent.core :as r]
   ["@testing-library/react" :as rtl]
   [fitlog.data :as d]
   [fitlog.nav :as nav]
   [fitlog.router]))

;; Proxy because rtl/cleanup can't be imported directly by a .clj
(def cleanup rtl/cleanup)

(defn render [elem]
  (rtl/render (r/as-element elem)))

(defn get-nav []
  (let [{{name :name} :data params :path-params} @nav/app-view]
    (if (empty? params)
      name
      [name params])))

(defn set-workouts! [& workouts]
  (swap! d/data assoc :workouts (vec workouts)))

(defn set-exercises! [& exercises]
  (swap! d/data assoc :exercises (vec exercises)))
