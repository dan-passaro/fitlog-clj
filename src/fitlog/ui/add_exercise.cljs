(ns fitlog.ui.add-exercise
  (:require
   [reagent.core :as r]
   [reagent.hooks :as rh]
   [reitit.frontend.easy :as rfe]
   ["fuse.js" :as Fuse]
   ["@heroicons/react/24/outline" :refer [MagnifyingGlassIcon PlusIcon PencilIcon TrashIcon XMarkIcon]]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [icon icon-btn h2]]
   [fitlog.util :refer [vec-dissoc]]))

(defonce adding-exercise? (r/atom false))

(defonce variables (r/atom []))

(defonce exercise-name (r/atom ""))

(defonce filtered-exercises (r/atom []))

(defonce fuse (r/atom nil))

(defonce variable-editors (r/atom {}))

(defonce editing-exercise-idx (r/atom nil))

(defn- most-recent-set-of [exercise]
  (let [workouts (sort-by :createdAt > (:workouts @d/data))]
    (some (fn [workout]
            (last (filter #(= (:exercise %) exercise) (:sets workout))))
          workouts)))

(defn- on-add-exercise [workout-id exercise]
  (let [workout-id (int workout-id)
        workouts (sort-by :createdAt (:workouts @d/data))
        prev-set (most-recent-set-of exercise)
        new-set (apply d/make-set exercise (if prev-set
                                             [{:variables (:variables prev-set)}]
                                             []))]
    (swap! d/data update-in [:workouts workout-id :sets] (fnil identity []))
    (swap! d/data update-in [:workouts workout-id :sets] conj new-set)
    (rfe/navigate routes/workout {:path-params {:id workout-id}})))

(defn- set-exercise-form-initial! [exercise]
  (reset! exercise-name (:name exercise))
  (reset! variables (:variables exercise))
  (reset! variable-editors {}))

(defn- cancel-exercise-form! []
  (reset! adding-exercise? false)
  (reset! editing-exercise-idx nil))

(defn- set-edit-exercise! [exercise-idx]
  (let [exercise (get-in @d/data [:exercises exercise-idx])]
    (set-exercise-form-initial! exercise))
  (reset! editing-exercise-idx exercise-idx))

(defn- delete-exercise! [exercise-idx]
  (swap! d/data update :exercises vec-dissoc exercise-idx))

(defn- set-add-exercise! []
  (set-exercise-form-initial! (d/make-exercise))
  (reset! variable-editors {})
  (reset! adding-exercise? true))

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
                ^{:key i}
                [:li {:class "list-row flex justify-between items-center"}
                 [:span {:class "list-col-grow"
                         :data-testid "exercise-name"}
                  (:name exercise)]
                 [:span
                  [icon-btn {:label (str "Delete " (:name exercise))
                             :icon TrashIcon
                             :on-click #(delete-exercise! i)}]
                  [icon-btn {:label (str "Edit " (:name exercise))
                             :icon PencilIcon
                             :on-click #(set-edit-exercise! i)}]
                  [icon-btn {:label (str "Add " (:name exercise))
                             :icon PlusIcon
                             :on-click #(on-add-exercise id exercise)}]]])
              @filtered-exercises)]
            [:p "No exercises match your search."])]
         [:p (str "There are no exercises available. Create an exercise, then "
                  "you can add it to your workout.")]))
     [:button {:class "btn btn-primary"
               :on-click set-add-exercise!}
      "Create new exercise"]
     [:a {:href (rfe/href routes/workout {:id id})
          :class "btn btn-neutral"}
      "Back"]]))

(defn- bind-textinput-to [atom-var]
  {:value (deref atom-var)
   :on-change #(reset! atom-var (-> % .-target .-value))})

;; DELETEME - inline this alias
(def reset-exercise-form! cancel-exercise-form!)

(defn- make-var-editor [var]
  {:var var :original-name (:name var)})

(defn- exercise-form [data title submit-action]
  (let [original-name (when @editing-exercise-idx (get-in @d/data [:exercises @editing-exercise-idx :name]))
        existing-exercise-names (into #{} (map :name) (:exercises @d/data))]
    (fn []
      (let [name-taken? (and (not= @exercise-name original-name)
                             (contains? existing-exercise-names @exercise-name))]
        [:<>
         [h2 title]
         [:form {:id "exercise-form"
                 :on-submit (fn [e]
                              (.preventDefault e)
                              (when (not name-taken?)
                                (submit-action (d/make-exercise @exercise-name @variables))
                                (reset-exercise-form!)))}
          [:label {:for "exercise-name" :class (str "input"
                                                    (when name-taken?
                                                      " input-error"))}
           "Name"
           [:input (merge {:id "exercise-name" :class (str "input"
                                                           (when name-taken?
                                                             " input-error"))}
                          (bind-textinput-to exercise-name))]]
          (when name-taken?
            [:p {:class "text-error text-sm"}
             (str "An exercise named '" @exercise-name  "' has already been created.")])]
         (when-let [merged-vars (not-empty (reduce-kv assoc @variables @variable-editors))]
           [:<>
            [:p "Variables"]
            [:ul
             (doall
              (map-indexed
               (fn [i var]
                 ^{:key i}
                 [:li {:class "flex justify-between items-center"}

                  ;; Note: creating a new variable is also done through a
                  ;; var-edit object, so this handles editing as well as
                  ;; creating new variables.
                  (if-let [var-edit (and (:original-name var) var)]
                    (let [curr-name (-> var-edit :var :name)
                          name-taken? (and (not= (:original-name var-edit)
                                                 curr-name)
                                           (some #(= curr-name %)
                                                 (map :name @variables)))
                          maybe-input-error (when name-taken? " input-error")
                          creating-new-variable? (not (contains? @variables i))]
                      [:form {:on-submit (fn [e]
                                           (.preventDefault e)
                                           (when (not name-taken?)
                                             (swap! variables assoc i (get-in @variable-editors [i :var]))
                                             (swap! variable-editors dissoc i)))}

                       [:label {:class (str "input" maybe-input-error)}
                        "Variable name"
                        [:input {:class (str "input" maybe-input-error)
                                 :value (get-in @variable-editors [i :var :name])
                                 :on-change #(swap! variable-editors assoc-in [i :var :name] (-> % .-target .-value))}]]
                       (when name-taken?
                         [:p {:class "text-error text-sm"}
                          (str "There is already a variable named '"
                               curr-name "'.")])

                       [:label {:class "input"}
                        "Unit"
                        [:input {:class "input"
                                 :value (get-in @variable-editors [i :var :unit])
                                 :on-change #(swap! variable-editors assoc-in [i :var :unit] (-> % .-target .-value))}]]
                       [:button {:class "btn btn-primary"}
                        "Save variable"]
                       [:button {:class "btn btn-neutral"
                                 :type "button"
                                 :on-click #(swap! variable-editors dissoc i)}
                        (str "Cancel "
                             (if creating-new-variable? "variable" "edit"))]])
                    [:<>
                     (str (:name var) (when (not (empty? (:unit var)))
                                        (str " (" (:unit var) ")")))
                     (when (empty? @variable-editors)
                       [icon-btn {:label (str "Edit " (:name var) " variable")
                                  :icon PencilIcon
                                  :on-click #(swap! variable-editors assoc i (make-var-editor var))}])
                     [icon-btn {:label (str "Delete " (:name var) " variable")
                                :icon XMarkIcon
                                :on-click #(swap! variables vec-dissoc i)}]])])
               merged-vars))]])
         (when (empty? @variable-editors)
           [:button {:class "btn btn-neutral"
                     :type "button"
                     :on-click #(swap! variable-editors assoc (count @variables) (make-var-editor (d/make-var)))}
            "Add exercise variable"])
         [:input {:class "btn btn-primary"
                  :type "submit"
                  :value "Save exercise"
                  :form "exercise-form"}]
         [:button {:class "btn btn-neutral"
                   :type "button"
                   :on-click reset-exercise-form!}
          "Cancel"]]))))

(defn- create-new-exercise []
  (exercise-form (d/make-exercise "" [])
                 "Create an exercise"
                 (partial swap! d/data update :exercises conj)))

(defn- edit-exercise [exercise-idx]
  (let [exercise (get-in @d/data [:exercises exercise-idx])]
    (exercise-form exercise
                   (str "Edit " (:name exercise) " exercise")
                   (partial swap! d/data assoc-in [:exercises exercise-idx]))))

(defn add-exercise [& {:keys [id]}]
  (cond
    @adding-exercise? [create-new-exercise]
    @editing-exercise-idx [edit-exercise @editing-exercise-idx]
    :else [add-existing-exercise id]))
