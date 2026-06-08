(ns fitlog.ui.workout
  (:require
   [reitit.frontend.easy :as rfe]
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

(defn workout [& {:keys [id]}]
  (let [id (parse-long id)
        self (get-in @d/data [:workouts id])]
    [:<>
     [h2 (human-date-str (:createdAt self))]
     (if (seq (:sets self))
       [:ul
        (doall
         (map-indexed
          (fn [i workout-set]
            ^{:key i}
            [:li {:class "card shadow-sm"}
             [:section {:class "card-body"}
              [:h3 {:class "card-title"}
               (-> workout-set :exercise :name)]
              [:form {:on-submit #(.preventDefault %)
                      :class "flex gap-4 items-stretch"}
               [:div {:class "flex flex-wrap gap-4 grow"}
                (doall
                 (map-indexed (fn [idx var]
                                (let [input-id (gensym)]
                                  ^{:key idx}
                                  [:fieldset {:class "fieldset w-1/6"}
                                   [:label {:class "fieldset-legend"
                                            :for input-id}
                                    (:name var)]
                                   [:input {:id input-id
                                            :class "input"
                                            :type "text"
                                            :inputMode "decimal"

                                            ;; I just realized how many IDs I
                                            ;; have, whoops.
                                            :on-change #(update-set-var! id i idx (-> % .-target .-value))
                                            :default-value (get-in @d/data [:workouts id :sets i :variables idx 1])
                                            :placeholder (:unit var)}]]))
                              (-> workout-set :exercise :variables)))]
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
                           :defaultChecked (boolean (get-in @d/data [:workouts id :sets i :completedAt]))
                           :on-change #(swap! d/data assoc-in [:workouts id :sets i :completedAt] (.toISOString (js/Date.)))}]]])]]])

          (:sets self)))]
       [:p "No exercises. Add an exercise to get started!"])
     [primary-btn
      :href (rfe/href routes/add-exercise {:id id})
      :text "Add Exercise"]
     [primary-btn
      :text "Back"
      :href (rfe/href routes/workouts)]]))
