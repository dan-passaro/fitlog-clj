(ns fitlog.ui.workout-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [reagent.core :as r]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! use-fitlog-fixtures]]
   [fitlog.ui.workout :refer [workout]]))

(use-fitlog-fixtures)

(deftest test-nav
  (set-workouts! (d/make-workout))
  (let [c (render [workout :id "0"])]
    (.click rtl/fireEvent (.getByText c "Back"))
    (r/flush)
    (is (= routes/workouts (get-nav)))))

(deftest test-add-exercise
  (set-workouts! (d/make-workout))
  (let [c (render [workout :id "0"])]
    (.click rtl/fireEvent (.getByText c "Add Exercise"))
    (r/flush)
    (is (= [routes/add-exercise {:id "0"}] (get-nav)))))

(deftest header-is-workout-day
  (set-workouts! (d/make-workout))
  (let [c (render [workout :id "0"])]
    (= "Today"
       (.-textContent (.getByRole c "heading")))))

(deftest-async lists-all-sets
  (let [treadmill (d/make-exercise "Treadmill" [(d/make-var "Speed" "mph")])
        bench-press (d/make-exercise "Bench press" [(d/make-var "Weight" "lbs")])]
    (set-exercises! treadmill bench-press)
    (set-workouts! (d/make-workout :sets [(d/make-set treadmill)
                                          (d/make-set bench-press)]))
    (let [c (render [workout :id "0"])
          sets (await (.getAllByRole c "listitem"))]
      (is (= ["Bench press" "Treadmill"]
             (->> sets (map #(.-textContent %)) sort))))))
