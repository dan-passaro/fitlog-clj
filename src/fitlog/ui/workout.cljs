;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.ui.workout
  (:require
   [clojure.string :refer [join]]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]
   ["@heroicons/react/24/outline" :refer [PlusIcon Square3Stack3DIcon TrashIcon]]
   [tick.core :as t]
   [fitlog.data :as d]
   [fitlog.util :refer [vec-dissoc vec-insert]]
   [fitlog.routes :as routes]
   [fitlog.plate-loadout :refer [plate-loadout]]
   [fitlog.ui.rest-timer :refer [start-rest-timer!]]
   [fitlog.ui.lib :refer [h2 human-date-str icon-btn]]))

(defonce open-plate-guides (r/atom #{}))

(defn- plate-guide-open? [id set-idx]
  (contains? @open-plate-guides [id set-idx]))

(defn- toggle-plate-guide! [id set-idx]
  (swap! open-plate-guides
         (fn [state]
           (let [op (if (contains? state [id set-idx])
                      disj
                      conj)]
             (op state [id set-idx])))))

(defn- plate-loadout-str [loadout]
  (join " " (map (fn [[plate count]]
                   (str plate "×" count))
                 loadout)))

(defn- plate-guide [id set-idx]
  (let [set (get-in @d/data [:workouts id :sets set-idx])
        weight (some-> (d/set-variable set "Weight") parse-double)
        loadout (some-> weight plate-loadout)
        remaining (:remaining loadout)]
    [:div {:class "text-left text-sm"}
     [:p "Load each side of an Olympic barbell with these plates:"]
     [:p (plate-loadout-str (:pairs loadout))]
     (when (pos? remaining)
       [:p "Note: this is " (* 2 remaining) "lbs less than your target."])]))

(defn- primary-btn [& {:keys [text on-click href]}]
  (let [opts (cond-> {:class "btn btn-primary"}
               on-click (assoc :on-click on-click)
               href (assoc :href href))
        elt (if href :a :button)]
    [elt opts text]))

(defn- update-set-var! [workout-idx set-idx var-idx new-val]
  (swap! d/data assoc-in [:workouts workout-idx :sets set-idx :variables var-idx 1] new-val))

(defn- add-set! [workout-idx preceding-set-idx]
  (let [preceding-set (get-in @d/data [:workouts workout-idx :sets preceding-set-idx])
        new-set (d/make-set (:exercise preceding-set)
                            :variables (:variables preceding-set))]
    (swap! d/data update-in [:workouts workout-idx :sets]
           vec-insert (inc preceding-set-idx) new-set)))

(defn- remove-set! [workout-idx set-idx]
  (swap! d/data update-in [:workouts workout-idx :sets] vec-dissoc set-idx))

(defn- on-set-done-change! [workout-id set-idx event]
  (let [set-done? (-> event .-target .-checked)]
    (swap! d/data assoc-in [:workouts workout-id :sets set-idx :completedAt]
           (when set-done?
             (-> (js/Date.) .toISOString)))
    (when set-done?
      (start-rest-timer! (t/of-minutes 2)))))

(defn set-li [id set-idx workout-set exercise]
  [:li {:class "flex flex-col gap-2"}
   [:form {:on-submit #(.preventDefault %)
           :class "flex flex-wrap gap-4 grow items-stretch"}
    (doall
     (map-indexed
      (fn [var-idx var]
        (let [input-id (gensym)
              weight? (= (:name var) "Weight")]
          ^{:key (str set-idx "-" var-idx)}
          [:fieldset {:class "fieldset w-1/6"}
           [:label {:class "fieldset-legend"
                    :for input-id}
            (:name var)]
           [:div {:class "join items-center"}
            [:input {:id input-id
                     :class "input"
                     :type "text"
                     :on-focus (fn [event] (-> event .-target (.select)))
                     :inputMode "decimal"
                     :on-change #(update-set-var! id set-idx var-idx (-> % .-target .-value))
                     :value (get-in @d/data [:workouts id :sets set-idx :variables var-idx 1])
                     :placeholder (:unit var)}]
            (when weight?
              [icon-btn {:label "Plate loading guide"
                         :on-click #(toggle-plate-guide! id set-idx)
                         :icon Square3Stack3DIcon
                         :size "1.2em"}])]]))
      (-> workout-set :exercise :variables)))
    (let [input-id (gensym)]
      [:fieldset {:class "fieldset w-1/6 ml-auto"}
       [:label {:class "fieldset-legend"
                :for input-id}
        "Done"]

       ;; TODO: classes here aren't all needed. I was trying to get the checkbox
       ;; to be center-aligned with the textbox but I couldn't get it to work
       ;; and gave up cause I don't care that much. But someday this should be
       ;; revisited and cleaned up.
       [:div {:class "flex grow items-center justify-center"}
        [:input {:class "checkbox"
                 :type "checkbox"
                 :id input-id
                 :checked (boolean (get-in @d/data [:workouts id :sets set-idx :completedAt]))
                 :on-change (partial on-set-done-change! id set-idx)}]
        [icon-btn {:label (str "Delete " (:name exercise) " set")
                   :on-click #(remove-set! id set-idx)
                   :icon TrashIcon
                   :size "1.2em"}]]])]
   (when (plate-guide-open? id set-idx)
     [plate-guide id set-idx])])

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
                 [icon-btn {:label (str "Add " (:name exercise) " set")
                            :on-click #(add-set! id (first (last workout-set-group)))
                            :icon PlusIcon}]]
                [:ul
                 (doall
                  (map
                   (fn [[set-idx workout-set]]
                     ^{:key (str id "-" set-idx)}
                     [set-li id set-idx workout-set exercise])
                   workout-set-group))]]]))
          (partition-by (comp :name :exercise second) (map vector (range) (:sets self)))))]
       [:p "No exercises. Add an exercise to get started!"])
     [primary-btn
      :href (rfe/href routes/add-exercise {:id id})
      :text "Add Exercise"]
     [primary-btn
      :text "Back"
      :href (rfe/href routes/workouts)]]))
