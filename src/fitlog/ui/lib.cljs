(ns fitlog.ui.lib)

(defn h2 [text]
  [:h2 {:class "text-2xl"} text])

(defn- a-week-ago []
  (new js/Date (- (.now js/Date)

                  ;; a week in milliseconds
                  ;; (* 7 24 60 60 1000)
                  604800000)))

(defn- today? [date]
  (= (.toDateString date)
     (.toDateString (new js/Date))))

(defn human-date-str [js-date]
  (let [date (new js/Date js-date)]
    (cond
      (today? date) "Today"
      (> date (a-week-ago)) (.toLocaleString date false #js {:weekday "long"})
      :else (.toLocaleString date false #js {:month "short"
                                             :weekday "long"
                                             :day "numeric"}))))

(defn icon [icon-elt {:keys [size]
                      :or {size "1em"}}]
  [:> icon-elt {:style {:width size :height size}}])

(def ^:private icon_ icon)  ;; so icon-btn can take an :icon keyword

(defn icon-btn [& {:keys [label icon on-click size]}]
  [:button {:class "btn btn-sm btn-ghost"
            :type "button"
            :on-click on-click
            :aria-label label}
   [icon_ icon (when size {:size size})]])
