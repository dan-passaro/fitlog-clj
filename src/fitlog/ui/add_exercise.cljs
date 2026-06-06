(ns fitlog.ui.add-exercise
  (:require
   [reagent.core :as r]
   [reagent.hooks :as rh]
   [reitit.frontend.easy :as rfe]
   ["fuse.js" :as Fuse]
   ["@heroicons/react/24/outline" :refer [MagnifyingGlassIcon]]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2]]))

(defonce adding-exercise? (r/atom false))

(defonce adding-variable? (r/atom false))

(defonce variable-name (r/atom ""))

(defonce variable-unit (r/atom ""))

(defonce variables (r/atom []))

(defonce exercise-name (r/atom ""))

(defonce filtered-exercises (r/atom []))

(defonce fuse (r/atom nil))

(defn- on-add-exercise [workout-id exercise]
  (swap! d/data update-in [:workouts (int workout-id) :sets] (fnil identity []))
  (swap! d/data update-in [:workouts (int workout-id) :sets] conj (d/make-set exercise))
  (rfe/navigate routes/workout {:path-params {:id workout-id}}))

(defn- js->clj-kw [v]
  (js->clj v :keywordize-keys true))

(defn- filter-exercises!
  ([]
   (filter-exercises! ""))
  ([query]
   (reset! filtered-exercises (->> query (.search @fuse) js->clj-kw (mapv :item)))))

(defn- add-existing-exercise [id]
  (reset! fuse (Fuse. (clj->js (:exercises @d/data))
                      #js {:keys #js ["name"]
                           :threshold 0.4}))
  (filter-exercises!)
  (fn [id]
    [:<>
     [h2 "Choose an exercise"]
     (let [exercises (:exercises @d/data)]
       (if (seq exercises)
         [:<>
          [:p
           [:label {:class "input"}
            [:> MagnifyingGlassIcon {:class "h-[1em]"}]
            [:input {:type "search" :class "grow" :placeholder "Search"
                     :on-change #(filter-exercises! (-> % .-target .-value))}]]]
          (if (seq @filtered-exercises)
            [:ul {:class "list"}
             (map-indexed
              (fn [i exercise]
                ^{:key i} [:li {:class "list-row"}
                           [:span {:class "list-col-grow"
                                   :data-testid "exercise-name"}
                            (:name exercise)]
                           [:button {:class "btn btn-primary"
                                     :aria-label (str "Add " (:name exercise))
                                     :on-click #(on-add-exercise id exercise)}
                            "+"]])
              @filtered-exercises)]
            [:p "No exercises match your search."])]
         [:p "There are no exercises available. Create an exercise, then you can
       add it to your workout."]))
     [:button {:class "btn btn-primary"
               :on-click #(reset! adding-exercise? true)}
      "Create new exercise"]
     [:a {:href (rfe/href routes/workout {:id id})
          :class "btn btn-neutral"}
      "Back"]]))

(defn- bind-textinput-to [atom-var]
  {:value (deref atom-var)
   :on-change #(reset! atom-var (-> % .-target .-value))})

(defn reset-new-exercise-form! []
  (reset! exercise-name "")
  (reset! variables [])
  (reset! adding-exercise? false))

(defn reset-exercise-variable-form! []
  (reset! variable-name "")
  (reset! variable-unit "")
  (reset! adding-variable? false))

(defn- create-new-exercise []
  [:<>
   [h2 "Create an exercise"]
   [:form {:id "new-exercise-form"
           :on-submit (fn [e]
                        (.preventDefault e)
                        (swap! d/data
                               update :exercises
                               conj (d/make-exercise @exercise-name @variables))
                        (reset-new-exercise-form!))}
    [:label {:for "exercise-name" :class "input"}
     "Name"
     [:input (merge {:id "exercise-name" :class "input"}
                    (bind-textinput-to exercise-name))]]]
   (when (seq @variables)
     [:<>
      [:p "Variables"]
      [:ul (map-indexed
            (fn [i var]
              ^{:key i} [:li (str (:name var) " (" (:unit var) ")")])
            @variables)]])
   (if @adding-variable?
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (swap! variables conj {:name @variable-name
                                                 :unit @variable-unit})
                          (reset-exercise-variable-form!))}
      [:label {:for "variable-name" :class "input"}
       "Variable name"
       [:input (merge {:id "variable-name" :class "input"}
                      (bind-textinput-to variable-name))]]
      [:label {:for "unit" :class "input"}
       "Unit"
       [:input (conj {:id "unit" :class "input"}
                     (bind-textinput-to variable-unit))]]
      [:button {:class "btn btn-primary"}
       "Save variable"]
      [:button {:class "btn btn-neutral"
                :type "button"
                :on-click reset-exercise-variable-form!}
       "Cancel variable"]]
     [:button {:class "btn btn-neutral"
               :type "button"
               :on-click #(reset! adding-variable? true)}
      "Add exercise variable"])
   [:input {:class "btn btn-primary" :type "submit" :value "Save exercise"
            :form "new-exercise-form"}]
   [:button {:class "btn btn-neutral"
             :type "button"
             :on-click reset-new-exercise-form!}
    "Cancel"]])

(defn add-exercise [& {:keys [id]}]
  (if @adding-exercise?
    [create-new-exercise]
    [add-existing-exercise id]))
