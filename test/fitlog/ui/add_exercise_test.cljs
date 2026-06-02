(ns fitlog.ui.add-exercise-test
    (:require
     [cljs.test :refer [deftest is]]
     [reagent.core :as r]
     [reitit.frontend.easy :as rfe]
     ["@testing-library/react" :as rtl]
     [fitlog.routes :as routes]
     [fitlog.data :as d]
     [fitlog.nav :as nav]
     [fitlog.test-util :refer [render set-workouts! use-fitlog-fixtures]]
     [fitlog.ui.add-exercise :refer [add-exercise]]))

(use-fitlog-fixtures)

(deftest shows-header
  (set-workouts! (d/make-workout))
  (let [c (render [add-exercise :id "0"])]
    (is (= "Choose an exercise"
           (.-textContent (.getByRole c "heading"))))))

;; (deftest test-add-exercise
;;   (testing "allows adding an exercise"
;;     (set-workouts! (d/make-workout))
;;     (let [c (render [workout :id "0"])]
;;       (.click rtl/fireEvent (.getByText c "Add Exercise"))
;;       (r/flush)
;;       (is (= [routes/add-exercise {:id "0"}] (get-nav))))))
