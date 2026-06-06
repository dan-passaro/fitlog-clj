(ns fitlog.ui.workouts-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [reagent.core :as r]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! use-fitlog-fixtures with-mock-date]]
   [fitlog.ui.workouts :refer [workouts]]))

(use-fitlog-fixtures)

(deftest lists-workouts-by-date
  (with-mock-date "2020-05-10T12:00:00Z"
    (set-workouts! (d/make-workout))
    (let [c (render [workouts])
          workout-items (.getAllByRole c "listitem")]
      (is (= ["Today"]
             (map #(.-textContent %) workout-items)))))

  (with-mock-date "2020-05-11T12:00:00Z"
    (let [c (render [workouts])
          workout-items (.getAllByRole c "listitem")]
      (is (= (count (:workouts @d/data)) 1))
      (is (= ["Sunday"]
             (map #(.-textContent %) workout-items))))))
