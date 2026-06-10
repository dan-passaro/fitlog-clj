(ns fitlog.ui.add-exercise-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   ["@testing-library/react" :as rtl]
   ["@testing-library/dom" :refer [waitFor]]
   ["@testing-library/user-event" :as user-event-mod]
   [fitlog.routes :as routes]
   [fitlog.data :as d]
   [fitlog.nav :as nav]
   [fitlog.test-util :refer [deftest-async get-nav make-click make-type render set-exercises! set-workouts! setup-user-events use-fitlog-fixtures wait-for with-mock-date]]
   [fitlog.ui.add-exercise :as ae :refer [add-exercise]]))

(def user-event user-event-mod/default)

(use-fitlog-fixtures
 :each [{:before #(reset! ae/adding-exercise? false)}])

(defn- shown-exercise-names [c]
  (let [exercise-names (.getAllByTestId c "exercise-name")]
    (map #(.-textContent %) exercise-names)))


(deftest shows-header
  (set-workouts! (d/make-workout))
  (let [c (render [add-exercise :id "0"])]
    (is (= "Choose an exercise"
           (.-textContent (.getByRole c "heading"))))))

(deftest-async allows-creating-new-exercise
  (set-workouts! (d/make-workout))
  (set-exercises!)
  (let [user (.setup user-event)
        c (render [add-exercise :id "0"])
        click (make-click user c)
        type (make-type user c)]
    (is (= "Choose an exercise"
           (.-textContent (.getByRole c "heading"))))
    (await (click "Create new exercise"))
    (await (type "Name" "Treadmill"))

    ;; Without this flush, sometimes this test will fail because the name gets
    ;; saved as "Treadl". I'm not sure why this flush fixes it, and there's also
    ;; a chance it doesn't and I've just gotten lucky and not had the test flake
    ;; on me since adding it.
    (r/flush)

    (await (click "Add exercise variable"))
    (await (type "Variable name" "Speed"))
    (await (type "Unit" "mph"))
    (await (click "Save variable"))
    (await (click "Save exercise"))
    (await (.findByRole c "heading" #js {:text "Choose an exercise"}))
    (await (wait-for #(let [exercise (get-in @d/data [:exercises 0])]
                        (= {:name "Treadmill"
                            :variables [{:name "Speed" :unit "mph"}]}
                           exercise))))))

(deftest-async allows-editing-variables-when-creating-exercise
  (set-exercises!)
  (set-workouts! (d/make-workout))
  (let [user (setup-user-events)
        c (render [add-exercise :id "0"])
        click (make-click user c)
        type (make-type user c)]
    (await (click "Create new exercise"))
    (await (type "Name" "Treadmill"))
    (await (click "Add exercise variable"))
    (await (type "Variable name" "Speed"))
    (await (type "Unit" "mph"))
    (await (click "Save variable"))
    (await (click "Edit Speed variable"))
    (await (type "Variable name" "y"))
    (await (click "Save variable"))
    (await (click "Save exercise"))
    (await (.findByRole c "heading" #js {:text "Choose an exercise"}))
    (await (wait-for #(let [exercise (get-in @d/data [:exercises 0])]
                        (= {:name "Treadmill"
                            :variables [{:name "Speedy" :unit "mph"}]}
                           exercise))))))

(deftest-async allows-removing-variables-when-creating-exercise
  (set-exercises!)
  (set-workouts! (d/make-workout))
  (let [user (setup-user-events)
        c (render [add-exercise :id "0"])
        click (make-click user c)
        type (make-type user c)]
    (await (click "Create new exercise"))
    (await (type "Name" "Treadmill"))
    (await (click "Add exercise variable"))
    (await (type "Variable name" "Speed"))
    (await (type "Unit" "mph"))
    (r/flush)
    (await (click "Save variable"))
    (await (.findByText c "Speed (mph)"))
    (await (click "Delete Speed variable"))
    (r/flush)
    (await (click "Save exercise"))
    (await (.findByRole c "heading" #js {:text "Choose an exercise"}))
    (await (wait-for #(let [exercise (get-in @d/data [:exercises 0])]
                        (= {:name "Treadmill"
                            :variables []}
                           exercise))))))

(deftest lists-available-exercises
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercise "Treadmill" [{:name "Speed" :unit "mph"}])
                  (d/make-exercise "Bench press" [{:name "Weight" :unit "lbs"}]))
  (let [c (render [add-exercise :id "0"])]
    (is (= ["Bench press" "Treadmill"]
           (-> c shown-exercise-names sort)))))

(deftest-async can-add-existing-exercise-to-workout
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercise "Treadmill" [{:name "Speed" :unit "mph"}]))
  (let [c (render [add-exercise :id "0"])
        user (.setup user-event)
        add-treadmill-btn (.getByRole c "button" #js {:name "Add Treadmill"})]
    (await (.click user add-treadmill-btn))
    (testing "adds the exercise set in the data model"
      (is (= (get-in @d/data [:workouts 0 :sets])
             [{:exercise {:name "Treadmill"
                          :variables [{:name "Speed" :unit "mph"}]}
               :variables [[{:name "Speed" :unit "mph"}, nil]]}])))
    (testing "navigates back to the workout screen"
      (is (= [routes/workout {:id "0"}]
             (get-nav))))))

(deftest links-back-to-workout
  (set-workouts! (d/make-workout))
  (let [c (render [add-exercise :id "0"])]
    (.click rtl/fireEvent (.getByText c "Back"))
    (is (= [routes/workout {:id "0"}] (get-nav)))))

(deftest-async allows-searching-exercises
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercise "Treadmill" [])
                  (d/make-exercise "Bench press" [])
                  (d/make-exercise "Chest press" []))
  (let [user (.setup user-event)
        c (render [add-exercise :id "0"])
        search (.getByRole c "searchbox")]
    (await (.type user search "press"))
    (await (wait-for #(= ["Bench press" "Chest press"]
                         (-> c  shown-exercise-names sort))))
    (is (= ["Bench press" "Chest press"]
           (-> c  shown-exercise-names sort))))
  (let [c (render [add-exercise :id "0"])
        search (.getByRole c "searchbox")
        user (.setup user-event)]
    (await (.type user search "tread"))
    (await (wait-for #(= ["Treadmill"]
                         (-> c shown-exercise-names sort))))))

(deftest-async search-still-shows-when-everything-filtered
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercise "Treadmill" []))
  (let [c (render [add-exercise :id "0"])
        search (.getByRole c "searchbox")
        user (.setup user-event)]
    (await (.type user search "randomstuff"))
    (is (await (.findByText c "No exercises match your search.")))
    (is (await (.findByRole c "searchbox")))))

(deftest-async allows-editing-exercises
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercisev "Treadmill" "Speed" "mph" "Time" "mins"))
  (let [user (setup-user-events)
        c (render [add-exercise :id "0"])
        type (make-type user c)
        click (make-click user c)]
    (await (click "Edit Treadmill"))
    (await (.findByText c "Edit Treadmill exercise"))
    (await (type "Name" "ing"))
    (await (click "Edit Speed variable"))
    (await (type "Variable name" "y"))
    (await (click "Save variable"))
    (await (click "Delete Time variable"))
    (await (click "Save exercise"))
    (await (wait-for #(= [{:name "Treadmilling"
                           :variables [{:name "Speedy" :unit "mph"}]}]
                         (:exercises @d/data))))))

(deftest-async prefills-set-variables-with-most-recent-previous-values
  (let [bench-press (d/make-exercisev "Bench press" "Weight" "lbs" "Reps" "#")
        row (d/make-exercisev "Row" "Weight" "lbs" "Reps" "#")]
    (set-exercises! bench-press row)
    (set-workouts! (with-mock-date "2020-05-10" ;; later - takes precendence
                     (d/make-workout
                      :sets (d/make-set row :vars {"Weight" "90"
                                                   "Reps" "6"})))
                   (with-mock-date "2020-05-07"
                     (d/make-workout
                      :sets [(d/make-set bench-press :vars {"Weight" "100"
                                                            "Reps" "6"})
                             (d/make-set bench-press :vars {"Weight" "100"
                                                            "Reps" "5"})
                             (d/make-set row :vars {"Weight" "80"
                                                    "Reps" "6"})]))
                   (d/make-workout))
    (let [user (setup-user-events)
          c (render [add-exercise :id "2"])
          click (make-click user c)]
      (await (click "Add Bench press"))

      (is (= [(d/make-set bench-press :vars {"Weight" "100"
                                             "Reps" "5"})]
             (get-in @d/data [:workouts 2 :sets])))

      (await (click "Add Row"))
      (is (= [(d/make-set bench-press :vars {"Weight" "100"
                                             "Reps" "5"})
              (d/make-set row :vars {"Weight" "80"
                                     "Reps" "6"})]
             (get-in @d/data [:workouts 2 :sets]))))))
