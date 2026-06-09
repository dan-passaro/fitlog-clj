(ns fitlog.test-util
  (:require-macros [fitlog.test-util])
  (:require
   [reagent.core :as r]
   ["mockdate" :as MockDate]
   ["@testing-library/dom" :refer [waitFor]]
   ["@testing-library/react" :as rtl]
   ["@testing-library/user-event" :as user-event-mod]
   [fitlog.data :as d]
   [fitlog.nav :as nav]
   [fitlog.router]))

;; Alias for macros
(def cleanup rtl/cleanup)

(defn render [elem]
  (rtl/cleanup)  ;; allow a single test to use (render) twice
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

;; Alias for macros
(def mock-date MockDate)

(defn default-to-noon [date]
  (if (not (re-find #"T[\d.:]+(Z)?$" date))
    (str date "T12:00:00Z")
    date))

(def user-event user-event-mod/default)

(defn setup-user-events [& {:keys [] :as opts}]
  (.setup user-event (clj->js (or opts {}))))

(defn wait-for [test]
  (waitFor #(when (not (test))
              (throw (js/Error. (str "waiting for " test))))))
