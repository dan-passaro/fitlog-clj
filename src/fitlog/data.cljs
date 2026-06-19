(ns fitlog.data
  (:require
   [clojure.set :as set]
   [reagent.core :as r]))

(def data (r/atom {:workouts [] :exercises []}))

(defn make-workout [& {:keys [sets]
                       :or {sets []}}]
  {:createdAt (.toISOString (new js/Date))
   :id (.randomUUID js/crypto)
   :sets (vec sets)})

(defn make-var
  "Make an exercise variable.

  This is a helper for make-exercise."
  ([]
   (make-var ""))
  ([name]
   (make-var name ""))
  ([name unit]
   {:name name :unit unit}))

(defn make-exercise
  "Create an exercise.
  variables - a seq of variables as created by make-var"
  ([]
   (make-exercise "" []))
  ([name]
   (make-exercise name []))
  ([name variables]
   {:name name :variables (vec variables)}))

(defn make-exercisev
  "Make an exercise with variables.

  This is a convenience function for tests."
  [name & vars]
  (make-exercise name (mapv (partial apply make-var)
                            (partition 2 vars))))

(defn make-set
  "Create a set.

  vars - a map of variable names to values. More ergonomic to write; useful in
  test code. Ignored if variables is given.

  variables - a sequence of [(d/make-var ...) value] pairs. Suitable for copying
  from an existing set."
  [exercise & {:keys [completed-at vars variables]
               :or {completed-at nil
                    vars {}
                    variables []}}]
  (merge
   {:exercise exercise
    :variables (if (seq variables)
                 (vec variables)
                 (mapv (fn [var]
                         (let [var-val (get vars (:name var))]
                           [var var-val]))
                       (:variables exercise)))}
   (when completed-at
     {:completedAt completed-at})))

(defn set-variable [set var-name]
  (some (fn [[var-def val]] (when (= var-name (:name var-def)) val)) (:variables set)))

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

(defn- rename-params [m]
  (set/rename-keys m {:parameters :variables}))

(defn- migrate-set [wset]
  (-> wset
      rename-params
      (update :exercise rename-params)))

(defn- migrate-workout [workout]
  (update workout :sets (partial mapv migrate-set)))

(defn migrate
  "Read existing possibly legacy data and ensure it matches the current schema."
  [data]
  (-> data
      (update :exercises (partial mapv rename-params))
      (update :workouts (partial mapv migrate-workout))))
