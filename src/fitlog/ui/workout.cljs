(ns fitlog.ui.workout
  (:require
   [reitit.frontend.easy :as rfe]
   ["@heroicons/react/24/outline" :refer [PlusIcon TrashIcon]]
   [fitlog.data :as d]
   [fitlog.routes :as routes]
   [fitlog.ui.lib :refer [h2 human-date-str]]))

(defn- primary-btn [& {:keys [text on-click href]}]
  (let [opts (cond-> {:class "btn btn-primary"}
               on-click (assoc :on-click on-click)
               href (assoc :href href))
        elt (if href :a :button)]
    [elt opts text]))

(defn- update-set-var! [workout-idx set-idx var-idx new-val]
  (swap! d/data assoc-in [:workouts workout-idx :sets set-idx :variables var-idx 1] new-val))

(defn- add-set! [workout-idx preceding-set-idx]
  (swap! d/data update-in [:workouts workout-idx :sets]
         (fn [sets]
           (vec (concat (subvec sets 0 (inc preceding-set-idx))
                        [(d/make-set (get-in sets [preceding-set-idx :exercise]))]
                        (subvec sets (inc preceding-set-idx)))))))

(defn- remove-set! [workout-idx set-idx]
  (swap! d/data update-in [:workouts workout-idx :sets]
         (fn [sets]
           (vec (concat (subvec sets 0 set-idx)
                        (subvec sets (inc set-idx)))))))

(defn- on-set-done-change! [workout-id set-idx event]
  (swap! d/data assoc-in [:workouts workout-id :sets set-idx :completedAt]
         (when (-> event .-target .-checked)
           (-> (js/Date.) .toISOString))))

(defn workout [& {:keys [id]}]
  (let [id (parse-long id)
        self (get-in @d/data [:workouts id])]
    [:<>
     [h2 (human-date-str (:createdAt self))]
     (if (seq (:sets self))
       [:ul {:class "flex flex-col gap-2"}
        (doall
         (map-indexed
          (fn [group-idx workout-set-group]
            (let [exercise (:exercise (second (first workout-set-group)))
                  card-name-id (gensym)]
              ^{:key (str id "-" group-idx)}
              [:li {:class "card shadow-sm"
                    :role "region"
                    :aria-labelledby card-name-id}
               [:section {:class "card-body"}
                [:div {:class "flex justify-between items-center"}
                 [:h3 {:class "card-title"
                       :id card-name-id}
                  (:name exercise)]
                 [:button {:class "btn btn-sm btn-ghost"
                           :type "button"
                           :aria-label (str "Add " (:name exercise) " set")
                           :on-click #(add-set! id (first (last workout-set-group)))}
                  [:> PlusIcon {:class "h-[1em]"}]]]
                [:ul
                 (doall
                  (map
                   (fn [[set-idx workout-set]]
                     ^{:key (str id "-" set-idx)}
                     [:li {:class "flex gap-4 items-stretch"}
                      [:form {:on-submit #(.preventDefault %)
                              :class "flex flex-wrap gap-4 grow"}
                       (doall
                        (map-indexed (fn [var-idx var]
                                       (let [input-id (gensym)]
                                         ^{:key (str set-idx "-" var-idx)}
                                         [:fieldset {:class "fieldset w-1/6"}
                                          [:label {:class "fieldset-legend"
                                                   :for input-id}
                                           (:name var)]
                                          [:input {:id input-id
                                                   :class "input"
                                                   :type "text"
                                                   :inputMode "decimal"
                                                   :on-change #(update-set-var! id set-idx var-idx (-> % .-target .-value))
                                                   :default-value (get-in @d/data [:workouts id :sets set-idx :variables var-idx 1])
                                                   :placeholder (:unit var)}]]))
                                     (-> workout-set :exercise :variables)))
                       (let [input-id (gensym)]
                         [:fieldset {:class "fieldset w-1/6 ml-auto"}
                          [:label {:class "fieldset-legend"
                                   :for input-id}
                           "Done"]

                          ;; TODO: classes here aren't all needed. I was trying to get
                          ;; the checkbox to be center-aligned with the textbox but I
                          ;; couldn't get it to work and gave up cause I don't care that
                          ;; much. But someday this should be revisited and cleaned up.
                          [:div {:class "flex grow items-center justify-center"}
                           [:input {:class "checkbox"
                                    :type "checkbox"
                                    :id input-id
                                    :defaultChecked (boolean (get-in @d/data [:workouts id :sets set-idx :completedAt]))
                                    :on-change (partial on-set-done-change! id set-idx)}]
                           [:button {:class "btn btn-ghost"
                                     :type "button"
                                     :aria-label (str "Delete " (:name exercise) " set")
                                     :on-click #(remove-set! id set-idx)}
                            [:> TrashIcon {:class "h-[1.2em]"}]]]])]])
                   workout-set-group))]]]))
          (partition-by (comp :name :exercise second) (map vector (range) (:sets self)))))]
       [:p "No exercises. Add an exercise to get started!"])
     [primary-btn
      :href (rfe/href routes/add-exercise {:id id})
      :text "Add Exercise"]
     [primary-btn
      :text "Back"
      :href (rfe/href routes/workouts)]]))
