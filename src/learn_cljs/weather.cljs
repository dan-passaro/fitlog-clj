(ns learn-cljs.weather
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom.client :as rdc]
   [reagent.dom :as rdom]))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defonce root
  (rdc/create-root (.getElementById js/document "app")))

(defn hello-world []
  [:div
   [:h1 "Hum: " (:text @app-state)]])

(defn mount []
  (rdc/render root [hello-world]))

(defn ^:dev/after-load init []
  (mount))
