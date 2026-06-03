(ns fitlog.data
  (:require
   [reagent.core :as r]))

(def data (r/atom {:workouts [] :exercises []}))

(defn make-workout [& {:keys [sets]
                       :or {sets []}}]
  {:createdAt (.toISOString (new js/Date))
   :id (.randomUUID js/crypto)
   :sets sets})

(defn make-exercise [name variables]
  {:name name
   :variables variables})

(defn make-var
  "Make an exercise variable.

  This is a helper for make-exercise."
  [name unit]
  {:name name :unit unit})

(defn make-set [exercise]
  {:exercise exercise
   :variables (map (fn [var] [var nil]) (:variables exercise))})

(defn persist-data
  "Save data to local storage."
  [key ref old-state new-state]
  (js/localStorage.setItem "fitlog-data"
                           (js/JSON.stringify (clj->js new-state))))

(defn load-user-data
  "Update data from the browser's local storage."
  []
  (when-let [data-json (js/localStorage.getItem "fitlog-data")]
    (let [data-parsed (js->clj (js/JSON.parse data-json) :keywordize-keys true)]
      (reset! data data-parsed))))
