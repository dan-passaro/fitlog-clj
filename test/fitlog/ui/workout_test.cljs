(ns fitlog.ui.workout-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [reagent.core :as r]
   ["@testing-library/dom" :refer [within]]
   ["@testing-library/react" :as rtl]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! setup-user-events use-fitlog-fixtures with-mock-date]]
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

(deftest-async set-variables-show-saved-data-if-available
  (let [treadmill (d/make-exercise "Treadmill" [(d/make-var "Speed" "mph")])]
    (set-workouts! (d/make-workout
                    :sets [(d/make-set treadmill :vars {"Speed" "5.2"})]))
    (let [c (render [workout :id "0"])]
      (is (= "5.2"
             (.-value (await (.findByRole c "textbox" #js {:name "Speed"}))))))))

(deftest-async sets-can-be-marked-as-completed
  (let [treadmill (d/make-exercisev "Treadmill" "Speed" "mph")
        now (.toISOString (js/Date.))]
    (set-workouts! (d/make-workout :sets [(d/make-set treadmill)]))
    (let [c (render [workout :id "0"])
          user (setup-user-events)]
      (is (nil? (get-in @d/data [:workouts 0 :sets 0 :completedAt])))
      (with-mock-date now
        (await (.click user (await (.findByRole c "checkbox" #js {:name "Done"})))))
      (is (= now
             (get-in @d/data [:workouts 0 :sets 0 :completedAt]))))))

(deftest-async sets-show-if-they-have-been-completed
  (let [treadmill (d/make-exercisev "Treadmill" "Speed" "mph")]

    (testing "checked when complete"
      (set-workouts! (d/make-workout
                      :sets [(d/make-set treadmill
                                         :completed-at (.toISOString (js/Date.)))]))
      (let [c (render [workout :id "0"])
            done-checkbox (await (.findByRole c "checkbox" #js {:name "Done"}))]
        (is (.-checked done-checkbox))))

    (testing "blank when incomplete"
      (set-workouts! (d/make-workout :sets [(d/make-set treadmill)]))
      (let [c (render [workout :id "0"])
            done-checkbox (await (.findByRole c "checkbox" #js {:name "Done"}))]
        (is (not (.-checked done-checkbox)))))))

(deftest groups-consecutive-sets-of-the-same-exercise-together
  (let [treadmill (d/make-exercisev "Treadmill" "Speed" "mph")
        bench-press (d/make-exercisev "Bench press" "Weight" "lbs")]
    (set-workouts! (d/make-workout
                    :sets [(d/make-set bench-press)
                           (d/make-set bench-press)
                           (d/make-set treadmill)
                           (d/make-set bench-press)
                           (d/make-set bench-press)
                           (d/make-set bench-press)]))
    (let [c (render [workout :id "0"])
          workout-items (.getAllByRole c "listitem")]
      (is (= 3 (count workout-items)))
      (is (= 2 (count (.queryAllByRole (within (nth workout-items 0))
                                       "textbox"))))
      (is (= 1 (count (.queryAllByRole (within (nth workout-items 1))
                                       "textbox"))))
      (is (= 3 (count (.queryAllByRole (within (nth workout-items 2))
                                       "textbox")))))))
