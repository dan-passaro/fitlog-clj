(ns fitlog.ui.workout-test
    (:require
     [cljs.test :refer-macros [deftest is testing use-fixtures]]
     [reagent.core :as r]
     [reitit.frontend.easy :as rfe]
     ["@testing-library/react" :as rtl]
     [fitlog.router :refer [router setup-router]]
     [fitlog.routes :as routes]
     [fitlog.data :as d]
     [fitlog.nav :as nav]
     [fitlog.ui.workout :refer [workout]]))

(use-fixtures :once {:before setup-router})
(use-fixtures :each {:before #(reset! nav/app-view nil)
                     :after rtl/cleanup})

(defn set-workouts! [& workouts]
  (swap! d/data assoc :workouts workouts))

(defn get-nav []
  (let [{{name :name} :data params :path-params} @nav/app-view]
    (if (empty? params)
      name
      [name params])))

(defn render [elem]
  (rtl/render (r/as-element elem)))

(deftest test-nav
  (testing "links back to the workouts screen"
    (set-workouts! (d/make-workout))
    (let [c (render [workout :id "0"])]
      (.click rtl/fireEvent (.getByText c "Back"))
      (r/flush)
      (is (= routes/workouts (get-nav))))))

(deftest test-add-exercise
  (testing "allows adding an exercise"
    (set-workouts! (d/make-workout))
    (let [c (render [workout :id "0"])]
      (.click rtl/fireEvent (.getByText c "Add Exercise"))
      (r/flush)
      (is (= [routes/add-exercise {:id "0"}] (get-nav))))))
