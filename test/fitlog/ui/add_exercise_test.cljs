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
     [fitlog.test-util :refer [deftest-async get-nav render set-exercises! set-workouts! use-fitlog-fixtures wait-for]]
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
        click (^:async fn [btn]
               (.click user (await (.findByRole c "button" #js {:name btn}))))
        type (^:async fn [field-name value]
              (.type user (await (.findByRole c "textbox" #js {:name field-name})) value))]
    (is (= "Choose an exercise"
           (.-textContent (.getByRole c "heading"))))
    (await (click "Create new exercise"))
    (await (type "Name" "Treadmill"))
    (await (click "Add exercise variable"))
    (await (type "Variable name" "Speed"))
    (await (type "Unit" "mph"))
    (await (click "Save variable"))
    (await (click "Save exercise"))
    (await (.findByRole c "heading" #js {:text "Choose an exercise"}))
    (let [exercise (get-in @d/data [:exercises 0])]
      (is (= {:name "Treadmill"
              :variables [{:name "Speed" :unit "mph"}]}
             exercise)))))

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
    (is (= ["Treadmill"]
           (-> c shown-exercise-names sort)))))

(deftest-async search-still-shows-when-everything-filtered
  (set-workouts! (d/make-workout))
  (set-exercises! (d/make-exercise "Treadmill" []))
  (let [c (render [add-exercise :id "0"])
        search (.getByRole c "searchbox")
        user (.setup user-event)]
    (await (.type user search "randomstuff"))
    (is (await (.findByText c "No exercises match your search.")))
    (is (await (.findByRole c "searchbox")))))
