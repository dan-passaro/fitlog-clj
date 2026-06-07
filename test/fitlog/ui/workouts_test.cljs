(ns fitlog.ui.workouts-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   ["@testing-library/dom" :refer [within]]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! use-fitlog-fixtures with-mock-date]]
   [fitlog.ui.lib :refer [human-date-str]]
   [fitlog.ui.workouts :refer [workouts]]))

(use-fitlog-fixtures)

(deftest lists-workouts-by-date
  (with-mock-date "2020-05-10"
    (set-workouts! (d/make-workout))
    (let [c (render [workouts])
          workout-items (.getAllByRole c "listitem")]
      (is (= ["Today"]
             (map #(.-textContent %) workout-items)))))

  (with-mock-date "2020-05-11"
    (let [c (render [workouts])
          workout-items (.getAllByRole c "listitem")]
      (is (= ["Sunday"]
             (map #(.-textContent %) workout-items))))))

(deftest orders-workouts-by-newest-first
  (set-workouts!
   (with-mock-date "2020-05-12" (d/make-workout))
   (with-mock-date "2020-05-13" (d/make-workout))
   (with-mock-date "2020-05-11" (d/make-workout)))
  (with-mock-date "2020-05-14"
    (let [c (render [workouts])
          workout-items (.getAllByRole c "listitem")]
      (is (= ["Wednesday" "Tuesday" "Monday"]
             (map #(.-textContent %) workout-items))))))

(deftest doesnt-show-new-workout-button-if-theres-already-a-workout-for-today
  (set-workouts! (d/make-workout))
  (let [c (render [workouts])
        workout-button (.queryByRole c "button" #js {:name "New Workout"})]
    (is (nil? workout-button)))

  (set-workouts!
   (with-mock-date "2020-05-05" (d/make-workout)))
  (with-mock-date "2020-05-06"
    (let [c (render [workouts])
          workout-button (.queryByRole c "button" #js {:name "New Workout"})]
      (is (not (nil? workout-button))))))

;; Test for bug #29
(deftest workout-links-are-correct
  (let [workout-records [(with-mock-date "2020-05-03" (d/make-workout))
                         (with-mock-date "2020-05-10" (d/make-workout))
                         (with-mock-date "2020-05-20" (d/make-workout))
                         (with-mock-date "2020-05-17" (d/make-workout))]
        workout-record-dates (mapv #(human-date-str (:createdAt %))
                                   workout-records)]
    (apply set-workouts! workout-records)
    (let [c (render [workouts])
          workout-links (map #(.getByRole (within %) "link")
                             (.queryAllByRole c "listitem"))]
      (doseq [workout-link workout-links]
        (let [workout-idx (.indexOf workout-record-dates
                                    (.-textContent workout-link))]
          (is (= (rfe/href routes/workout {:id workout-idx})
                 (.getAttribute workout-link "href"))))))))
