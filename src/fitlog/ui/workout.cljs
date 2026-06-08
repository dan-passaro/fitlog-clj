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
                      :class "flex flex-wrap gap-4"}
               (doall
                (map-indexed (fn [idx var]
                               ^{:key idx}
                               [:fieldset {:class "fieldset w-1/6"}
                                [:label {:class "fieldset-legend"}
                                 (:name var)]
                                [:input {:class "input"
                                         :aria-label (:name var)
                                         :type "text"
                                         :inputMode "decimal"

                                         ;; I just realized how many IDs I have,
                                         ;; whoops.
                                         :on-change #(update-set-var! id i idx (-> % .-target .-value))
                                         :default-value (get-in @d/data [:workouts id :sets i :variables idx 1])
                                         :placeholder (:unit var)}]])
                             (-> workout-set :exercise :variables)))]]])
          (:sets self)))]
       [:p "No exercises. Add an exercise to get started!"])
     [primary-btn
      :href (rfe/href routes/add-exercise {:id id})
      :text "Add Exercise"]
     [primary-btn
      :text "Back"
      :href (rfe/href routes/workouts)]]))
