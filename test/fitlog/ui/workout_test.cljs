(ns fitlog.ui.workout-test
    (:require
     [cljs.test :refer-macros [deftest is testing use-fixtures]]
     [reagent.core :as r]
     ["@testing-library/react" :as rtl]
     [fitlog.data :as d]
     [fitlog.nav :as nav]
     [fitlog.ui.workout :refer [workout]]))

(use-fixtures :each {:after rtl/cleanup})

(defn set-workouts! [& workouts]
  (swap! d/data assoc :workouts workouts))

(defn get-nav []
  (let [{view :view :as all} @nav/app-view
        opts (dissoc all :view)]
    (if (empty? opts)
      view
      [view opts])))

(defn render [elem]
  (rtl/render (r/as-element elem)))

(deftest test-nav
  (testing "links back to the workouts screen"
    (set-workouts! (d/make-workout))
    (let [c (render [workout 0])]
      (.click rtl/fireEvent (.getByText c "Back"))
      (r/flush)
      (is (= :home (get-nav))))))
