(ns fitlog.ui.workout-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [reagent.core :as r]
   ["@testing-library/dom" :refer [within]]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! setup-user-events use-fitlog-fixtures]]
   [fitlog.ui.workout :refer [workout]]))

(use-fitlog-fixtures)

(deftest-async test-nav
  (set-workouts! (d/make-workout))
  (let [c (render [workout :id "0"])]
    (await (.click rtl/fireEvent (.getByText c "Back")))
    (is (= routes/workouts (get-nav)))))

(deftest-async test-add-exercise
  (set-workouts! (d/make-workout))
  (let [c (render [workout :id "0"])]
    (await (.click rtl/fireEvent (.getByText c "Add Exercise")))
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
          set-items (await (.getAllByRole c "listitem"))
          set-headings (map #(.getByRole (within %) "heading")
                            set-items)]
      (is (= ["Bench press" "Treadmill"]
             (->> set-headings (map #(.-textContent %)) sort))))))

(deftest-async user-can-fill-in-set-variables
  (let [treadmill-speed (d/make-var "Speed" "mph")
        treadmill (d/make-exercise "Treadmill" [treadmill-speed])
        bench-press-weight (d/make-var "Weight" "lbs")
        bench-press (d/make-exercise "Bench press" [bench-press-weight])
        test-workout (d/make-workout :sets [(d/make-set treadmill)
                                            (d/make-set bench-press)])]
    (set-exercises! treadmill bench-press)
    (set-workouts! test-workout)
    (let [c (render [workout :id "0"])
          user (setup-user-events)
          enter (^:async fn [& {:keys [field value]}]
                 (await (.type user (await (.findByRole c "textbox" #js {:name field}))
                               value)))]
      (is (= [{:exercise treadmill :variables [[treadmill-speed nil]]}
              {:exercise bench-press :variables [[bench-press-weight nil]]}]
             (get-in @d/data [:workouts 0 :sets])))
      (await (enter :field "Speed" :value "5.2"))
      (await (enter :field "Weight" :value "100"))
      (is (= [{:exercise treadmill :variables [[treadmill-speed "5.2"]]}
                        {:exercise bench-press :variables [[bench-press-weight "100"]]}]
                       (get-in @d/data [:workouts 0 :sets]))))))

(deftest-async set-variables-saved-data-if-available
  (let [treadmill (d/make-exercise "Treadmill" [(d/make-var "Speed" "mph")])]
    (set-workouts! (d/make-workout
                    :sets [(d/make-set treadmill {"Speed" "5.2"})]))
    (let [c (render [workout :id "0"])]
      (is (= "5.2"
             (.-value (await (.findByRole c "textbox" #js {:name "Speed"}))))))))
