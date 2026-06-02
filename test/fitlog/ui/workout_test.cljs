(ns fitlog.ui.workout-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [reagent.core :as r]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [get-nav render set-workouts! use-fitlog-fixtures]]
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
